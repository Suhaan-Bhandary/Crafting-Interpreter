# Evaluating Expression

In this chapter our interpreter will be alive, will be producing output from the expressions!!

## Main problems

1. What kinds of values do we produce?
2. How do we organize those chunks of code?

### Representing Values

Since the language is dynamically typed we store the values in an java.lang.Object, which is parent of all objects.

| Lox type      | Java representation |
| ------------- | ------------------- |
| Any Lox value | Object              |
| nil           | null                |
| Boolean       | Boolean             |
| number        | Double              |
| string        | String              |

Given a value of static type Object, we can determine if the runtime value is a number or a string or whatever using Java’s built-in instanceof operator. In other words, the JVM’s own object representation conveniently gives us everything we need to implement Lox’s built-in types. We’ll have to do a little more work later when we add Lox’s notions of functions, classes, and instances, but Object and the boxed primitive classes are sufficient for the types we need right now.

### Evaluating Expressions

Next, we need blobs of code to implement the evaluation logic for each kind of expression we can parse. We could stuff that code into the syntax tree classes in something like an interpret() method. In effect, we could tell each syntax tree node, “Interpret thyself”. This is the Gang of Four’s Interpreter design pattern. It’s a neat pattern, but like I mentioned earlier, it gets messy if we jam all sorts of logic into the tree classes.

Instead, we’re going to reuse our groovy Visitor pattern. In the previous chapter, we created an AstPrinter class. It took in a syntax tree and recursively traversed it, building up a string which it ultimately returned. That’s almost exactly what a real interpreter does, except instead of concatenating strings, it computes values.

#### What is Truthy?

In our language false and nil are falsey and every other thing is truthy.

#### How equality works

In our language we use the inbuild .equals method on the object to check for equality.

### Run-time Errors

In our interpreter we are using casts to make the Object a double type.
Since user can pass any value in the expression there may be run-time errors because of casting the types.

So when a runtime error occurs deep in some expression, we need to escape all the way out. But not in the application.

So to handle it gracefully we create a runtime error which stores the operator and the error message and displays to the user. We also use the interpreter as a static object in lox file so that the repl Interpreter remains the same in a session(Will be helpful when storing global variables).

The type errors are discovered at the last during the binary operator are applied to them.
So,

```cpp
say("hi") - say("hello")
```

Here, hi and hello both will be printed and then the error will be discovered.

Tip:
When creating a static typed language make sure that you don't push the error checking to run time as it decreases the confidence.
