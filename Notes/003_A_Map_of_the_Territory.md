# A Map of the Territory

```text
You must have a map, no matter how rough. Otherwise you wander all over the place. In The Lord of the Rings I never made anyone go farther than he could on a given day.
    - J. R. R. Tolkien
```

## The Parts of a language

General Phases of Compiler:

- Lexical Analysis
- Syntax Analysis
- Semantic Analysis
- Intermediate Code Generation
- Code Optimization
- Code Generation

![Compiler Mountain](../assets/mountain.png)

Let's trace through each of those trails and points of interest. Our journey begins on the left with the bare text of the user's source code.

1. Scanning/Lexical Analysis

Scanning or lexing all mean pretty much same.
A scanner or lexer takes in the linear stream of characters and chunks them together into a series of somethings more akin to "words". In programming languages, each of these words is called a token. Some tokens are single characters, like ( and ,. Others may be several characters long, like numbers, strings and identifiers.

Some characters in a source file don’t actually mean anything. Whitespace is often insignificant, and comments, by definition, are ignored by the language. The scanner usually discards these, leaving a clean sequence of meaningful tokens.

Program:

```cpp
int a = min(2, 3);
```

After lexical Analysis:

```
int
a
=
min
(
3
,
)
;
```

2. Parsing

This is where our syntax gets a **grammar** - the ability to compose larger expressions and statements out of smaller parts.

A parser takes the flat sequence of tokens and builds a tree structure that mirrors the nested nature of the grammar. These trees have a couple of different names—parse tree or abstract syntax tree—depending on how close to the bare syntactic structure of the source language they are. In practice, language hackers usually call them syntax trees, ASTs, or often just trees.

[Abstract Syntax Tree](../assets/ast.png)

Parsing has a long, rich history in computer science that is closely tied to the artificial intelligence community. Many of the techniques used today to parse programming languages were originally conceived to parse human languages by AI researchers who were trying to get computers to talk to us.

It turns out human languages were too messy for the rigid grammars those parsers could handle, but they were a perfect fit for the simpler artificial grammars of programming languages. Alas, we flawed humans still manage to use those simple grammars incorrectly, so the parser’s job also includes letting us know when we do by reporting syntax errors.

3. Static Analysis

In previous we checked if the program is correctly written or not, now we have to check if the program is correct logically. Variable scopes and references are to be taken into consideration, in static typed languages in this step we check the expressions and their typing to make sure that the actions can be performed.

All this semantic insight that is visible to us from analysis needs to be stored somewhere. There are a few places we can squirrel it away:

    - Storing it in the Syntax Tree Nodes
    - LookUp Table, in this the keys are the identifiers - names of variables and declarations. In that case, it is known as symbol table.
    - The most powerful bookkeeping tool is to transform the tree into an entirely new data structure that more directly expresses the semantics of the code. That’s the next section.

**Everything up to this point is considered the front end of the implementation. You might guess everything after this is the back end, but no. Back in the days of yore when “front end” and “back end” were coined, compilers were much simpler. Later researchers invented new phases to stuff between the two halves. Rather than discard the old terms, William Wulf and company lumped those new phases into the charming but spatially paradoxical name middle end.**

4. Intermediate Representations

Intermediate Representations are used as a interface between the backend and frontend of the compiler. This lets you support multiple source languages and target platforms with less effort.

This lets you support multiple source languages and target platforms with less effort. Say you want to implement Pascal, C, and Fortran compilers, and you want to target x86, ARM, and, I dunno, SPARC. Normally, that means you’re signing up to write nine full compilers: Pascal→x86, C→ARM, and every other combination.

A shared intermediate representation reduces that dramatically. You write one front end for each source language that produces the IR. Then one back end for each target architecture. Now you can mix and match those to get every combination.

**Note this is the Reason why GCC can support so much languages**

5. Optimization

Once we understand what the user’s program means, we are free to swap it out with a different program that has the same semantics but implements them more efficiently—we can optimize it.

Once we understand what the user’s program means, we are free to swap it out with a different program that has the same semantics but implements them more efficiently—we can optimize it.

**constant folding** is an simple example of optimization.
10 + 20 can be replaced with 30, as we know that the expression evaluates to the exact same value.

_Note: If you can’t resist poking your foot into that hole, some keywords to get you started are “constant propagation”, “common subexpression elimination”, “loop invariant code motion”, “global value numbering”, “strength reduction”, “scalar replacement of aggregates”, “dead code elimination”, and “loop unrolling”._

6. Code generation

Now we generate the end code which the machine can read.
Native Code is lighting fast, but generating it is a lot of work. The Native Code can only be used on the OS and the architecture.

To get around that, hackers like Martin Richards and Niklaus Wirth, of BCPL and Pascal fame, respectively, made their compilers produce virtual machine code. Instead of instructions for some real chip, they produced code for a hypothetical, idealized machine. Wirth called this p-code for portable, but today, we generally call it bytecode because each instruction is often a single byte long.

### Virtual machine

If your compiler produces bytecode, your work isn’t over once that’s done. Since there is no chip that speaks that bytecode, it’s your job to translate. Again, you have two options. You can write a little mini-compiler for each target architecture that converts the bytecode to native code for that machine. You still have to do work for each chip you support, but this last stage is pretty simple and you get to reuse the rest of the compiler pipeline across all of the machines you support. You’re basically using your bytecode as an intermediate representation.

_The basic principle here is that the farther down the pipeline you push the architecture-specific work, the more of the earlier phases you can share across architectures. There is a tension, though. Many optimizations, like register allocation and instruction selection, work best when they know the strengths and capabilities of a specific chip. Figuring out which parts of your compiler can be shared and which should be target-specific is an art._

In the second part of the book we will be implementing our own VM for C to run it on any platform with C Compiler.

### Runtime

Everything which happens when the program is running, such as garbage collection and type checking for dynamically typed language.

## Shortcuts and Alternate Routes

### Single pass compiler

Some simple compilers interleave parsing, analysis, and code generation so that they produce output code directly in the parser, without ever allocating any syntax trees or other IRs. These single-pass compilers restrict the design of the language. You have no intermediate data structures to store global information about the program, and you don’t revisit any previously parsed part of the code. That means as soon as you see some expression, you need to know enough to correctly compile it.

Pascal and C were designed around this limitation. At the time, memory was so precious that a compiler might not even be able to hold an entire source file in memory, much less the whole program. This is why Pascal’s grammar requires type declarations to appear first in a block. It’s why in C you can’t call a function above the code that defines it unless you have an explicit forward declaration that tells the compiler what it needs to know to generate code for a call to the later function.

### Tree walk interpreter

Some programming languages begin executing code right after parsing it to an AST (with maybe a bit of static analysis applied). To run the program, the interpreter traverses the syntax tree one branch and leaf at a time, evaluating each node as it goes.

This implementation style is common for student projects and little languages, but is not widely used for general-purpose languages since it tends to be slow. Some people use “interpreter” to mean only these kinds of implementations, but others define that word more generally, so I’ll use the inarguably explicit tree-walk interpreter to refer to these. Our first interpreter rolls this way.

### Transpilers

In this we write the frontend and omit backend by converting our language to another language and taking the output as a intermediate code and using the languages backend to get the output.

The most notable use is the typescript to javascript transpiler.

### Just in time compilation

This last one is less a shortcut and more a dangerous alpine scramble best reserved for experts. The fastest way to execute code is by compiling it to machine code, but you might not know what architecture your end user’s machine supports. What to do?

You can do the same thing that the HotSpot Java Virtual Machine (JVM), Microsoft’s Common Language Runtime (CLR), and most JavaScript interpreters do. On the end user’s machine, when the program is loaded—either from source in the case of JS, or platform-independent bytecode for the JVM and CLR—you compile it to native code for the architecture their computer supports. Naturally enough, this is called just-in-time compilation. Most hackers just say “JIT”, pronounced like it rhymes with “fit”.

The most sophisticated JITs insert profiling hooks into the generated code to see which regions are most performance critical and what kind of data is flowing through them. Then, over time, they will automatically recompile those hot spots with more advanced optimizations.

## Compilers and Interpreters

What’s the difference between a compiler and an interpreter?

- Compiling is an implementation technique that involves translating a source language to some other—usually lower-level—form. When you generate bytecode or machine code, you are compiling. When you transpile to another high-level language, you are compiling too.

- When we say a language implementation “is a compiler”, we mean it translates source code to some other form but doesn’t execute it. The user has to take the resulting output and run it themselves.

- Conversely, when we say an implementation “is an interpreter”, we mean it takes in source code and executes it immediately. It runs programs “from source”.

## JavaScript Working

- V8 is one of the JavaScript Engine, it is used in NodeJs and Chrome.

The JavaScript engine is simply a computer program that interprets JavaScript code. The engine is responsible for executing the code.

Other major browser engines include:

- SpiderMonkey developed by Mozilla for Firefox
- JavaScriptCore which powers the Safari browser
- Chakra which powers Internet Explorer

## Challenges

1. Pick an open source implementation of a language you like. Download the source code and poke around in it. Try to find the code that implements the scanner and parser. Are they handwritten, or generated using tools like Lex and Yacc? (.l or .y files usually imply the latter.)

Took EJS and read the source code and saw how it was implemented, it is a little language or more formally a template engine.

2. Just-in-time compilation tends to be the fastest way to implement dynamically typed languages, but not all of them use it. What reasons are there to not JIT?

   - Complexity and Development time
   - Resource Constraints: It requires more memory than both interpreted and compiled languages.
   - Predictability
   - Startup Time: Has overhead during startup
   - JIT can make it hard to debug and profile as it introduces one more layer of abstraction.

3. Most Lisp implementations that compile to C also contain an interpreter that lets them execute Lisp code on the fly as well. Why?

   - Interactive Development
   - Incremental Compilation
   - Ease of Prototyping
   - Debugging
