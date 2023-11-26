# Parsing Expressions

Grammar, which knows how to control even kings. - Moliere

In this we will be learning to create a real parser, one with decent error handling, a coherent internal structure, and the ability to robustly chew through a sophisticated syntax - is considered a rare, impressive skill.

In this chapter we will be transforming the given tokens into a syntax tree.

Classic compiler books read like fawning hagiographies of these heroes and their tools. The cover of Compilers: Principles, Techniques, and Tools literally has a dragon labeled “complexity of compiler design” being slain by a knight bearing a sword and shield branded “LALR parser generator” and “syntax directed translation”. They laid it on thick.

A little self-congratulation is well-deserved, but the truth is you don’t need to know most of that stuff to bang out a high quality parser for a modern machine. As always, I encourage you to broaden your education and take it in later, but this book omits the trophy case.

## Ambiguity and the Parsing Game

Parser tries to fit the given tokens to map to terminals in CFG. Ambiguity means the parser may misunderstand the user’s code. As we parse, we aren’t just determining if the string is valid Lox code, we’re also tracking which rules match which parts of it so that we know what part of the language each token belongs to.

The way mathematicians have addressed this ambiguity since blackboards were first invented is by defining rules for precedence and associativity.

- Precedence determines which operator is evaluated first in an expression containing a mixture of different operators. Precedence rules tell us that we evaluate the / before the - in the above example. Operators with higher precedence are evaluated before operators with lower precedence. Equivalently, higher precedence operators are said to “bind tighter”.

- Associativity determines which operator is evaluated first in a series of the same operator. When an operator is left-associative (think “left-to-right”), operators on the left evaluate before those on the right. Since - is left-associative, this expression:

| Name       | Operators | Associates |
| ---------- | --------- | ---------- |
| Equality   | == !=     | Left       |
| Comparison | > >= < <= | Left       |
| Term       | - +       | Left       |
| Factor     | / \*      | Left       |
| Unary      | ! -       | Right      |

Right now, the grammar stuffs all expression types into a single expression rule. That same rule is used as the non-terminal for operands, which lets the grammar accept any kind of expression as a subexpression, regardless of whether the precedence rules allow it.

We fix that by stratifying the grammar. We define a separate rule for each precedence level.

Note: Each rule here only matches expressions at its precedence level or higher

So from +/- we move to factors.
We go from lowest precedence to the highest.

Initially expression contained all the expression to be matched, but now we will equate it to the lowest precedence one which is quality and from it all other can be generated.

```bash
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
```

Note: The grouping operator is placed in primary as it has the most precedence compared to other.

## Creating the Parser

A recursive descent parser is a literal translation of the grammar’s rules straight into imperative code. Each rule becomes a function. The body of the rule translates to code roughly like:

| Grammar     | Notation                          |
| ----------- | --------------------------------- |
| Terminal    | Code to match and consume a token |
| Nonterminal | Call to that rule’s function      |
| \|          | if or switch statement            |
| \* or +     | while or for loop                 |
| ?           | if statement                      |

Like the scanner class we store the tokens and the current position for the tokens.

The fact that the parser looks ahead at upcoming tokens to decide how to parse puts recursive descent into the category of predictive parsers.

Using the above rule of table we have to simply create the functions.

## Syntax Errors

A parser really has two jobs:

1. Given a valid sequence of tokens produce a corresponding syntax tree.
2. Given an invalid sequence of tokens, detect any errors and tell the user about their mistakes.

A parser must:

- Detect and report the error. If it doesn’t detect the error and passes the resulting malformed syntax tree on to the interpreter, all manner of horrors may be summoned.

- Avoid crashing or hanging. Syntax errors are a fact of life, and language tools have to be robust in the face of them. Segfaulting or getting stuck in an infinite loop isn’t allowed. While the source may not be valid code, it’s still a valid input to the parser because users use the parser to learn what syntax is allowed.

- Be fast. Computers are thousands of times faster than they were when parser technology was first invented. The days of needing to optimize your parser so that it could get through an entire source file during a coffee break are over. But programmer expectations have risen as quickly, if not faster. They expect their editors to reparse files in milliseconds after every keystroke.

- Report as many distinct errors as there are. Aborting after the first error is easy to implement, but it’s annoying for users if every time they fix what they think is the one error in a file, a new one appears. They want to see them all.

- Minimize cascaded errors. Once a single error is found, the parser no longer really knows what’s going on. It tries to get itself back on track and keep going, but if it gets confused, it may report a slew of ghost errors that don’t indicate other real problems in the code. When the first error is fixed, those phantoms disappear, because they reflect only the parser’s own confusion. Cascaded errors are annoying because they can scare the user into thinking their code is in a worse state than it is.

_The way a parser responds to an error and keeps going to look for later errors is called error recovery. This was a hot research topic in the ’60s. Back then, you’d hand a stack of punch cards to the secretary and come back the next day to see if the compiler succeeded. With an iteration loop that slow, you really wanted to find every single error in your code in one pass._

## Panic mode error recovery

Of all the recovery techniques devised in yesteryear, the one that best stood the test of time is called—somewhat alarmingly—panic mode. As soon as the parser detects an error, it enters panic mode. It knows at least one token doesn’t make sense given its current state in the middle of some stack of grammar productions.

Before it can get back to parsing, it needs to get its state and the sequence of forthcoming tokens aligned such that the next token does match the rule being parsed. This process is called synchronization.

To do that, we select some rule in the grammar that will mark the synchronization point. The parser fixes its parsing state by jumping out of any nested productions until it gets back to that rule. Then it synchronizes the token stream by discarding tokens until it reaches one that can appear at that point in the rule.

Any additional real syntax errors hiding in those discarded tokens aren’t reported, but it also means that any mistaken cascaded errors that are side effects of the initial error aren’t falsely reported either, which is a decent trade-off.

The traditional place in the grammar to synchronize is between statements. We don’t have those yet, so we won’t actually synchronize in this chapter, but we’ll get the machinery in place for later.

### Entering panic mode

Another way to handle common syntax errors is with error productions. You augment the grammar with a rule that successfully matches the erroneous syntax. The parser safely parses it but then reports it as an error instead of producing a syntax tree.

We throw a error once we get an incorrect token.

### Synchronizing a recursive descent parser

With recursive descent, the parser’s state—which rules it is in the middle of recognizing—is not stored explicitly in fields. Instead, we use Java’s own call stack to track what the parser is doing. Each rule in the middle of being parsed is a call frame on the stack. In order to reset that state, we need to clear out those call frames.

The natural way to do that in Java is exceptions. When we want to synchronize, we throw that ParseError object. Higher up in the method for the grammar rule we are synchronizing to, we’ll catch it. Since we synchronize on statement boundaries, we’ll catch the exception there. After the exception is caught, the parser is in the right state. All that’s left is to synchronize the tokens.

We detect the next statement by finding semicolon, this is not perfect as for loop has semicolon inside it but is our "best effort" as we have reported our first error precisely.

To synchronize we skip all tokens till be find semicolon or we see that current character is a statement.

Once we find the Error we trap it in the parser it self and doesnot allow to travel it into the interpreter, and also return null as a ast.

And with this we have tackled the parser!!
We will add new things to it, but none of that will be any complex than the binary operators we tackled here.

### Error Productions

```bash
expression → equality ;
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "-" | "+" ) factor )* ;
factor     → unary ( ( "/" | "*" ) unary )* ;
unary      → ( "!" | "-" | "--" | "++" ) unary | postfix ;
postfix    → primary ( "--" | ++" )* ;
primary    → NUMBER | STRING | "true" | "false" | "nil"
           | "(" expression ")"
           // Error productions...
           | ( "!=" | "==" ) equality
           | ( ">" | ">=" | "<" | "<=" ) comparison
           | ( "+" ) term
           | ( "/" | "*" ) factor ;
```

Notice that in the above we are handling the Error of Binary operator not having the left operand.
