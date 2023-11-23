# Representing Code

Previously we took the raw source code as a string and transformed it into a slightly higher-level representation: a series of tokens.
In the next chapter we will take those tokens and transform them yet again, into an even richer, more complex representation.

Before we can produce that representation, we need to define it. That’s the subject of this chapter. Along the way, we’ll cover some theory around formal grammars, feel the difference between functional and object-oriented programming, go over a couple of design patterns, and do some metaprogramming.

We require a way to represent the code for the parser to be simple to understand and help produce the output.

eg: 1 + 2 \* 3 - 4
Here we know that multiplication will be done first and then plus and then minus using the old BEDMAS. We can create a binary tree using BEDMAS to represent the tree and solve it.
And to solve the equation we can apply a dfs technique which solves the leaves and moves upward returning the result from the leaves.

Note: That's not to say a tree is the only possible representation of our code. Byte Code is also another representation that isn't as human friendly but is closer to the machine.

Chomsky hierarchy: [https://en.wikipedia.org/wiki/Chomsky_hierarchy](https://en.wikipedia.org/wiki/Chomsky_hierarchy)

Why we require Context free grammar?
Here is an example of a language that is not regular (proof here) but is context-free: { a n b n ∣ n ≥ 0 } \{a^nb^n | n \geq 0\} {anbn∣n≥0}. This is the language of all strings that have an equal number of a's and b's.
In the last chapter, the formalism we used for defining the lexical grammar—the rules for how characters get grouped into tokens—was called a regular language. That was fine for our scanner, which emits a flat sequence of tokens. But regular languages aren’t powerful enough to handle expressions which can nest arbitrarily deeply.

A formal grammar takes a set of atomic pieces it calls its “alphabet”. Then it defines a (usually infinite) set of “strings” that are “in” the grammar. Each string is a sequence of “letters” in the alphabet.

| Terminology    | Lexical grammar  | Syntactic grammar |
| -------------- | ---------------- | ----------------- |
| Alphabet       | Characters       | Tokens            |
| string         | Lexeme or tokens | Expression        |
| Implemented by | Scanner          | Parser            |

A formal grammar's job is to specify which strings are valid and which are not.
Regular grammar was used to define the grammar using regular expressions.
Using CFG we can define the expressions which are valid and which are not.

## Rules for grammars

How do we write down a grammar that contains an infinite number of valid strings? We obviously can’t list them all out. Instead, we create a finite set of rules. You can think of them as a game that you can “play” in one of two directions.

If you start with the rules, you can use them to generate strings that are in the grammar. Strings created this way are called derivations because each is derived from the rules of the grammar. In each step of the game, you pick a rule and follow what it tells you to do. Most of the lingo around formal grammars comes from playing them in this direction. Rules are called productions because they produce strings in the grammar.

Each production in a context-free grammar has a head—its name—and a body, which describes what it generates. In its pure form, the body is simply a list of symbols. Symbols come in two delectable flavors:

- A terminal is a letter from the grammar’s alphabet. You can think of it like a literal value. In the syntactic grammar we’re defining, the terminals are individual lexemes—tokens coming from the scanner like if or 1234.

- A nonterminal is a named reference to another rule in the grammar. It means “play that rule and insert whatever it produces here”. In this way, the grammar composes.

There is one last refinement: you may have multiple rules with the same name. When you reach a nonterminal with that name, you are allowed to pick any of the rules for it, whichever floats your boat.

To make this concrete, we need a way to write down these production rules. People have been trying to crystallize grammar all the way back to Pāṇini’s Ashtadhyayi, which codified Sanskrit grammar a mere couple thousand years ago. Not much progress happened until John Backus and company needed a notation for specifying ALGOL 58 and came up with Backus-Naur form (BNF). Since then, nearly everyone uses some flavor of BNF, tweaked to their own tastes.

Recursion where the recursive nonterminal has productions on both sides implies that the language is not regular.

Let's define the Notation we will be using to define the CFG for our language:

```
| is used to define many productions

() is used to group productions

* is used to repeat previous group or symbol zero or more times.

+ is used to repeat previous group or symbol one or more times.

? defines that the group or symbol is optional.
```

## A Grammar for Lox expressions

In the previous chapter, we did Lox’s entire lexical grammar in one fell swoop. Every keyword and bit of punctuation is there. The syntactic grammar is larger, and it would be a real bore to grind through the entire thing before we actually get our interpreter up and running.

- Literals. Numbers, strings, Booleans, and nil.
- Unary expressions. A prefix ! to perform a logical not, and - to negate a number.
- Binary expressions. The infix arithmetic (+, -, \*, /) and logic operators (==, !=, <, <=, >, >=) we know and love.
- Parentheses. A pair of ( and ) wrapped around an expression.

```
Context Free Grammar

expression     -> literal | unary | binary | grouping;
literal        -> NUMBER | STRING | "true" | "false" | "nil";
grouping       -> "(" expression ")";
unary          -> ("-" | "!") expression;
binary         -> expression operator expression;
operator       -> "==" | "!=" | "<" | "<=" | ">" | ">=" | "+" | "-" | "*" | "/"
```

## Implementing Syntax Trees

Using python to write a meta program to generate the class for the abstract tree, we can reduce the load of writing all the classes by hand.

- Separation of concerns: https://en.wikipedia.org/wiki/Separation_of_concerns
- Interpreter pattern: https://en.wikipedia.org/wiki/Interpreter_pattern

We can create a evaluate function which could check the type of the expression and then run the assigned operation but this will have multiple check of instanceOf which could be slow for the expression which are at last in if else.
We could also add a abstract interpret function to it and defines methods to it but since we require this ast in both the parser and interpreter we would have to also add other methods which violates separation of concerns.

## The expression problem

We have a handful of types and handful of operations on it.
Eg: Binary, Grouping, Literal, Unary types and interpret(), resolve(), analyze functions.

An object-oriented language like Java assumes that all of the code in one row naturally hangs together. It figures all the things you do with a type are likely related to each other, and the language makes it easy to define them together as methods inside the same class.

This makes it easy to extend the table by adding new rows. Simply define a new class. No existing code has to be touched. But imagine if you want to add a new operation—a new column. In Java, that means cracking open each of those existing classes and adding a method to it.

ML(Meta Languages) separates types and operations, and for each operations it uses a powerful switch case for types to match each type with a function. In such languages adding a operation is easy but adding a new type would have to open and change all the functions.

Both the oop and functional faces problem when adding column and row respectively. And this difficulty is called "expression problem" because it's hard to figure out the best way to model expression syntax tree nodes in a compiler.

People have thrown all sorts of language features, design patterns, and programming tricks to try to knock that problem down but no perfect language has finished it off yet. In the meantime, the best we can do is try to pick a language whose orientation matches the natural architectural seams in the program we’re writing.

Object-orientation works fine for many parts of our interpreter, but these tree classes rub against the grain of Java. Fortunately, there’s a design pattern we can bring to bear on it.

## The Visitor pattern

Using this pattern we can define different types of operations on the class and also handle the separation of concern.

## Pretty Printer

We need a way to inspect the syntax tree we create, so to do it we convert the syntax tree to a string, it is opposite of a parser, which is called pretty printing in which we create the valid source code from the syntax tree.

But our goal is to get a useful string which can be used to get the precedence of the operators and check if it is handled correctly or not.

So we will produce a pre-order representation of the ast.

We have used visitor pattern to create a printer for our ast.
