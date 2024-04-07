# Statements and State

To support bindings, we need a internal state in our interpreter.
We will give our interpreter the brain to store such things.

Statements doesnot evaluate anything so they need to do something to be useful, that something is called a side effect. It may be producing output or storing data.

Note: Pascal is an outlier. It distinguishes between procedures and functions. Functions return values, but procedures cannot. There is a statement form for calling a procedure, but functions can only be called where an expression is expected. There are no expression statements in Pascal.

## Statements

1. Expression Statement: An expression statement lets you place an expression where a statement is expected. They exist to evaluate expressions that have side effects. You may not notice them, but you use them all the time in C, Java, and other languages. Any time you see a function or method call followed by a ;, you’re looking at an expression statement.
   eg: a = 10, it is an expression where a and 10 are operand and = is an operator.

1. Print statement: A print statement evaluates an expression and displays the result to the user. I admit it’s weird to bake printing right into the language instead of making it a library function. Doing so is a concession to the fact that we’re building this interpreter one chapter at a time and want to be able to play with it before it’s all done. To make print a library function, we’d have to wait until we had all of the machinery for defining and calling functions before we could witness any side effects.

## New Syntax means new grammar rules!!

```bash
program -> statement* EOF ;

statement -> exprStmt | printStmt ;

exprStmt -> expression ";" ;
printStmt -> "print" expression ";" ;
```

The EOF at the last helps the interpreter to check if all the tokens are consumed and no erroneous expressions are remaining unconsumed.

## Statement Syntax Tree

There is no place in the grammar where both an expression and a statement are allowed. The operands of, say, + are always expressions, never statements. The body of a while loop is always a statement.

Since the two syntaxes are disjoint, we don’t need a single base class that they all inherit from. Splitting expressions and statements into separate class hierarchies enables the Java compiler to help us find dumb mistakes like passing a statement to a Java method that expects an expression.

Create a interfact Stmt with classes as Expression and Print.

## Parsing Statements

Now our parser returns a list of statements.
We then interpret the statements one by one in our interpreter.

And then create functions to parse the print and expression.

## Executing Statements

In our interpreter we extend it with the Visitor of Stmt.
Unlike expressions, statements produce no values, so the return type of the visit methods is Void, not Object. We have two statement types, and we need a visit method for each. The easiest is expression statements.

Now we can write programs!!
Ohh we can only print statements.

## Global Variables

Now we have statements so we can start working on the state.
We start with the easiest one, which is storing a global state.

**Global state gets a bad rap. Sure, lots of global state—especially mutable state—makes it hard to maintain large programs. It’s good software engineering to minimize how much you use. But when you’re slapping together a simple programming language or, heck, even learning your first language, the flat simplicity of global variables helps. My first language was BASIC and, though I outgrew it eventually, it was nice that I didn’t have to wrap my head around scoping rules before I could make a computer do fun stuff.**

### Variable syntax

First we will decide the syntax of the variable.
Variable declarations are statements, but they are different from other statements, and we'er going to split the statement grammar in two to handle them. That's because the grammar restricts where some kinds of statements are allowed.

Eg:
if(isMonday) var a = 10;
The above syntax is not allowed in many languages.

Code like this is weird, so C, Java, and friends all disallow it. It’s as if there are two levels of “precedence” for statements. Some places where a statement is allowed—like inside a block or at the top level—allow any kind of statement, including declarations. Others allow only the “higher” precedence statements that don’t declare names.

### Statement grammar

```bash
program -> declaration* EOF;

declaration -> varDecl | statement;

varDecl -> "var" IDENTIFIER ("=" expression)? ";";

statement -> exprStmt | printStmt;
```

### Expression grammar changes

To access the variable we have to make change in the expression primary.

```bash
primary -> "true" | "false" | "nil"
            | NUMBER | STRING
            | "("  expression ")"
            | IDENTIFIER;
```

The IDENTIFIER clause matches a single identifier token, which is understood to be the name of the variable being accessed.

We add a new class in the statement as Var which stores the Token and the expression and the initializer.

We use our synchronize function in the declaration as it is the top most level with lowest precedence in the recursive dependent parser.

## Environment

The bindings that associate variables to values need to be stored somewhere. Ever since the Lisp folks invented parentheses, this data structure has been called an environment.

Environment works like a key, value pair where key is the name of the variable and value is the value of the variable.
We can directly use a hash map but we will encapsulate it in it's own class.

In our Environment class we have to support:

1. Variable definition: In variable definition should we return a error or not if the variable is already defined, it's on the us.
1. Get method

We create the environment in the interpreter, so that it remains till the interpreter is running.

### Assignment of variables

#### Assignment syntax

```bash
expression     → assignment ;
assignment     → IDENTIFIER "=" assignment
               | equality ;
```

Most of C-derived languages, assignment is an express and not a statement. In C, it is the lowest precedence expression form. That means the rule slots between expression and equality.

### Scope

A scope defines a region where a name maps to a certain entity. Multiple scopes enable the same name to refer to different things in different contexts.

Lexical scope (or the less commonly heard static scope) is a specific style of scoping where the text of the program itself shows where a scope begins and ends. In Lox, as in most modern languages, variables are lexically scoped. When you see an expression that uses some variable, you can figure out which variable declaration it refers to just by statically reading the code.

Example of scope:

```javascript
let a = 20;
{
   let a = 10;
   print a;
}

{
   let a = 30;
   print a;
}
```

In the above example we have 1 global variable and 2 local variable.

Scope and environments are close cousins. The former is the theoretical concept, and the latter is the machinery that implements it. As our interpreter works its way through code, syntax tree nodes that affect scope will change the environment. In a C-ish syntax like Lox’s, scope is controlled by curly-braced blocks. (That’s why we call it block scope.)

#### Nesting and shadowning

First cut at implementing block scope might work like this:

1. As we visit each statement inside the block, keep track of any variables declared.

1. After the last statement is executed, tell the environment to delete all of those variables.

But deleting the variables inside a scope can also delete a global variable with the same name which can alter the global scope and will not encapsulate them.

When a local variable has the same name as a variable in an enclosing scope, it shadows the outer one. Code inside the block can’t see it any more—it is hidden in the “shadow” cast by the inner one—but it’s still there.

When we enter a new block scope, we need to preserve variables defined in outer scopes so they are still around when we exit the inner block. We do that by defining a fresh environment for each block containing only the variables defined in that scope. When we exit the block, we discard its environment and restore the previous one.

When we have to find value of a variable we not only have to search the current environment but all the environment enclosing it till we get an environment with the variable in it.

Note: parent-pointer tree

For each environment we create a member variable named enclosing to point to the enclosing environment.

So we recursively call the enclosing environment in assign and get so that it traverses the whole tree.

#### Block syntax and semantics

```bash
statement      → exprStmt
               | printStmt
               | block ;

block          → "{" declaration* "}" ;
```

In the interpreter, when we find a block we first create a environment and store the current environment in the previous, execute the statements in the block and then restore the environment.

In place of doing this, we could have also simply passed the environments to the functions and as it came back up it automatically would have removed it.
