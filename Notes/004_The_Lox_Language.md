# The Lox Language

Sometimes somethings starts somewhere for a reason - Suhaan Bhandary

## Hello, Lox

```cpp
// First Lox program
print "Hello, world!";
```

Lox is a member of C family, since we see syntax similar to it such as // and ;, and the print is a built in statement and not a function as we don't see any parentheses.

## High Level Language

While this book ended up bigger than I was hoping, it’s still not big enough to fit a huge language like Java in it. In order to fit two complete implementations of Lox in these pages, Lox itself has to be pretty compact.

When I think of languages that are small but useful, what comes to mind are high-level “scripting” languages like JavaScript, Scheme, and Lua. Of those three, Lox looks most like JavaScript, mainly because most C-syntax languages do. As we’ll learn later, Lox’s approach to scoping hews closely to Scheme. The C flavor of Lox we’ll build in Part III is heavily indebted to Lua’s clean, efficient implementation.

## Features

### Dynamic typing

- It becomes easy to write interpreter if we defer the type checking during run time.

### Automatic memory management

- reference counting and tracing garbage collection.
- reference counting stores the count of references of the object and when garbage collector runs it checks the count is zero, if it is then it can free the memory. Reference counting cannot detect cycle in the memory. Many languages such as php, python started with reference count but moved to tracing GC.

### Data types

- Booleans: You can't code without logic and you can't logic without Boolean values. "true" and "false", the yin and yang of software. Unlike some ancient languages that re-purpose an existing type to represent truth and falsehood, Lox has a dedicated Boolean type. We may be roughing it on this expedition, but we aren't savages.

```
Boolean variables are the only data type in Lox named after a person, George Boole, which is why “Boolean” is capitalized. He died in 1864, nearly a century before digital computers turned his algebra into electricity. I wonder what he’d think to see his name all over billions of lines of Java code.
```

- Numbers: Lox has only one kind of number: double-precision floating point. Since floating-point numbers can also represent a wide range of integers, that covers a lot of territory, while keeping things simple.

Full-featured languages have lots of syntax for numbers—hexadecimal, scientific notation, octal, all sorts of fun stuff. We’ll settle for basic integer and decimal literals.

```
1234; // An integer
12.34; // A decimal number
```

- Strings: "hi this is a string literal"

- Nil: There’s one last built-in value who’s never invited to the party but always seems to show up. It represents “no value”. It’s called “null” in many other languages. In Lox we spell it nil. (When we get to implementing it, that will help distinguish when we’re talking about Lox’s nil versus Java or C’s null.)

There are good arguments for not having a null value in a language since null pointer errors are the scourge of our industry. If we were doing a statically typed language, it would be worth trying to ban it. In a dynamically typed one, though, eliminating it is often more annoying than having it.

TODO Create a primitive array data-structure

### Expressions

#### Arithmetic

- Binary: +,-,\*,/ (These are infix as the operator is between the operands)
- ternary: condition ? thenArm: elseArm;
- Unary: -negateMe (It is both an infix and a prefix one)

All of these operators work on numbers, and it’s an error to pass any other types to them. The exception is the + operator—you can also pass it two strings to concatenate them.

#### Comparison and equality

Moving along, we have a few more operators that always return a Boolean result. We can compare numbers (and only numbers), using Ye Olde Comparison Operators.

```cpp
less < than;
lessThan <= orEqual;
greater > than;
greaterThan >= orEqual;

1 == 2 // false
"cat" != "dog" // true
3.14 == "pi" // false, values of different types are never equivalent, no implicit conversion to check equality.
```

#### Logical operators

```
prefix !
!true; // false
!false; // true

and, or
true and true; // true
true and false; // false

true or false; // true
false or false; // false
```

The reason _and_ and _or_ are like control flow structures is that they short-circuit. Not only does and return the left operand if it is false, it doesn’t even evaluate the right one in that case. Conversely (contrapositively?), if the left operand of an or is true, the right is skipped.

#### Bitwise Operators

|, << , >> , &, |, ~ are not present in the language.

TODO Create Bitwise Operator

#### Precedence and grouping

All of these operators have the same precedence and associativity that you’d expect coming from C. (When we get to parsing, we’ll get way more precise about that.) In cases where the precedence isn’t what you want, you can use () to group stuff.

### Statements

Now we’re at statements. Where an expression’s main job is to produce a value, a statement’s job is to produce an effect. Since, by definition, statements don’t evaluate to a value, to be useful they have to otherwise change the world in some way—usually modifying some state, reading input, or producing output.

A print statement evaluates a single expression and displays the result to the user.

Note: Baking print into the language instead of just making it a core library function is a hack. But it’s a useful hack for us: it means our in-progress interpreter can start producing output before we’ve implemented all of the machinery required to define functions, look them up by name, and call them.

### Variables

You declare variables using var statements. If you omit the initializer, the variable's value defaults to nil.

_This is one of those cases where not having nil and forcing every variable to be initialized to some value would be more annoying than dealing with nil itself._

var name = "suhaan";
var iAmNil;

### Control Flow

It’s hard to write useful programs if you can’t skip some code or execute some more than once. That means control flow. In addition to the logical operators we already covered, Lox lifts three statements straight from C.

_Scheme, on the other hand, has no built-in looping constructs. It does rely on recursion for repetition. Smalltalk has no built-in branching constructs, and relies on dynamic dispatch for selectively executing code._

```cpp
if (condition){
  print "yes";
} else {
  print "no";
}

var a = 1;
while(a < 10){
  print a;
  a = a + 1;
}

for (var a = 1; a < 10; a = a + 1){
  print a;
}
```

### Functions

- An argument is an actual value you pass to a function when you call it. So a function call has an argument list. Sometimes you hear actual parameter used for these.

- A parameter is a variable that holds the value of the argument inside the body of the function. Thus, a function declaration has a parameter list. Others call these formal parameters or simply formals.

- **return** statement is used to return value from the function, implicitly returns nil

### Closures

```
Functions are first class in Lox, which just means they are real values that you can get a reference to, store in variables, pass around, etc. This works:

fun addPair(a, b) {
  return a + b;
}

fun identity(a) {
  return a;
}

print identity(addPair)(1, 2); // Prints "3".
```

**_Peter J. Landin coined the term “closure”. Yes, he invented damn near half the terms in programming languages. Most of them came out of one incredible paper, “The Next 700 Programming Languages”. In order to implement these kind of functions, you need to create a data structure that bundles together the function’s code and the surrounding variables it needs. He called this a “closure” because it closes over and holds on to the variables it needs._**

### Classes or prototypes

When it comes to objects, there are actually two approaches to them, classes and prototypes. Classes came first, and are more common thanks to C++, Java, C#, and friends. Prototypes were a virtually forgotten offshoot until JavaScript accidentally took over the world.

_In a statically typed language like C++, method lookup typically happens at compile time based on the static type of the instance, giving you static dispatch. In contrast, dynamic dispatch looks up the class of the actual instance object at runtime. This is how virtual methods in statically typed languages and all methods in a dynamically typed language like Lox work._

Prototype-based languages merge these two concepts. There are only objects—no classes—and each individual object may contain state and methods. Objects can directly inherit from each other (or “delegate to” in prototypal lingo):

Prototype language push down the complexity to the user.
Larry Wall, Perl’s inventor/prophet calls this the “waterbed theory”. Some complexity is essential and cannot be eliminated. If you push it down in one place, it swells up in another.
Prototypal languages don’t so much eliminate the complexity of classes as they do make the user take that complexity by building their own class-like metaprogramming libraries.

#### Classes in Lox

The body of a class contains its methods. They look like function declarations but without the fun keyword. When the class declaration is executed, Lox creates a class object and stores that in a variable named after the class. Just like functions, classes are first class in Lox.

By first class, we can store a class in a variable and also pass classes to functions.

In Lox to create a instance of the class, the class is taken itself as a factory function for instances. Call a class like a function, and it produces a new instance of itself.

In lox, we can dynamically add properties onto objects.
If you want to access a field or method on the current object from within a method, you use good old this.

For constructor we can use init() function which runs when we create an instance.

```
class Brunch < Breakfast {
  init(meat, bread, drink) {
    super.init(meat, bread);
    this.drink = drink;
  }
}
```
