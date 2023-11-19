# Scanning

String of Lox source code -> tokens for the parser.

## The Interpreter Framework

For exit codes, I’m using the conventions defined in the UNIX [“sysexits.h” ](https://man.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html) header. It’s the closest thing to a standard I could find.

## Layout

In out **main** function we check how many arguments are present, if the number of argument is 1 then we interpret the file, if no argument is present then we run a interactive interpreter using a simple loop which runs till the we click ctrl+d.

        In the **run** function we take source as an input and convert it to tokens and then display it on the screen.

## Error handling

While we’re setting things up, another key piece of infrastructure is error handling. Textbooks sometimes gloss over this because it’s more a practical matter than a formal computer science-y problem. But if you care about making a language that’s actually usable, then handling errors gracefully is vital.

In our implementation the Error is bare bone and it only displays the line and the message of the error, but in real world you should display a proper message and also a fix for it.

The error handling is done in the main function itself as we store a variable named hadError which shows that if the program had an error while running. Another reason for it is that it is a good engineering practice to separate the code that generates the errors from the code that reports them.

Various phases of the front end will detect errors, but it’s not really their job to know how to present that to a user. In a full-featured language implementation, you will likely have multiple ways errors get displayed: on stderr, in an IDE’s error window, logged to a file, etc. You don’t want that code smeared all over your scanner and parser.

Ideally, we would have an actual abstraction, some kind of “ErrorReporter” interface that gets passed to the scanner and parser so that we can swap out different reporting strategies. For our simple interpreter here, I didn’t do that, but I did at least move the code for error reporting into a different class.

## Lexemes and Tokens

var langauge = "lox";

Here, var is the keyword for declaring a variable. That three-character sequence “v-a-r” means something. But if we yank three letters out of the middle of language, like “g-u-a”, those don’t mean anything on their own.

That’s what lexical analysis is about. Our job is to scan through the list of characters and group them together into the smallest sequences that still represent something. Each of these blobs of characters is called a lexeme. In that example line of code, the lexemes are:

The lexemes are only the raw substrings of the source code. However, in the process of grouping character sequences into lexemes, we also stumble upon some other useful information. When we take the lexeme and bundle it together with that other data, the result is a token. It includes useful stuff like:

### Token type

Keywords are part of the shape of the language’s grammar, so the parser often has code like, “If the next token is while then do . . . ” That means the parser wants to know not just that it has a lexeme for some identifier, but that it has a reserved word, and which keyword it is.

The parser could categorize tokens from the raw lexeme by comparing the strings, but that’s slow and kind of ugly. Instead, at the point that we recognize a lexeme, we also remember which kind of lexeme it represents. We have a different type for each keyword, operator, bit of punctuation, and literal type.

Notice that we are reducing the work for the next task by doing it initially.

Below are all the various token types:

```java
enum TokenType {
  // Single-character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  // One or two character tokens.
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // Literals.
  IDENTIFIER, STRING, NUMBER,

  // Keywords.
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  EOF
}
```

### Literal value

There are lexemes for literal values—numbers and strings and the like. Since the scanner has to walk each character in the literal to correctly identify it, it can also convert that textual representation of a value to the living runtime object that will be used by the interpreter later.

### Location information

For our interpreter we only store line.

## Regular Languages and Expressions

The rules that determine how a particular language groups characters into lexemes are called its lexical grammar. In Lox, as in most programming languages, the rules of that grammar are simple enough for the language to be classified a regular language. That’s the same “regular” as in regular expressions.

We can recognize all the lexemes using regular expressions for the language, we will be doing it by hand.
Some tools which generate the scanner for you: Lex or Flex which can generate a scanner if you provide regex to them.

## Scanning

In the scanner if we find error we display it and keep scanning the file for more error. And remember that since we set the hadError field we never execute error code.

_The code reports each invalid character separately, so this shotguns the user with a blast of errors if they accidentally paste a big blob of weird text. Coalescing a run of invalid characters into a single error would give a nicer user experience._

### String

For string once we get " we move till we get the next ".

### Number

-123 is not a number but a expression with - to the number literal.

No leading or trailing . is allowed in a number, number can have point only int the middle.

### Reserved words and Identifiers

One may think we should follow the same strategy to match the identifiers, but what happens when we have true and trueVal, trueVal will be taken as true and Val if we directly match the first reserved word.

This gets us to an important principle called **maximal munch**. When two lexical grammar rules can both match a chunk of code that the scanner is looking at, whichever one matches the most characters wins.

Maximal munch means we can’t easily detect a reserved word until we’ve reached the end of what might instead be an identifier. After all, a reserved word is an identifier, it’s just one that has been claimed by the language for its own use. That’s where the term reserved word comes from.
