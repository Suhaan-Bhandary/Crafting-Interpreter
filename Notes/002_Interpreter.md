# Interpreter

## First Interpreter(JLox)

We will be creating our Interpreter in Java and then in C.

A compiler reads files in one language, translates them, and outputs files in another language. You can implement a compiler in any language, including the same language it compiles, a process called **self-hosting**.

- [Lox Implementations](https://github.com/munificent/craftinginterpreters/wiki/Lox-implementations)

You can’t compile your compiler using itself yet, but if you have another compiler for your language written in some other language, you use that one to compile your compiler once. Now you can use the compiled version of your own compiler to compile future versions of itself, and you can discard the original one compiled from the other compiler. This is called bootstrapping, from the image of pulling yourself up by your own **bootstraps**.

The compiler will be slow as it will be using JVM to compile the language.

## Second Interpreter(CLox)

A big reason that we’re using C is so I can show you things C is particularly good at, but that does mean you’ll need to be pretty comfortable with it. You don’t have to be the reincarnation of Dennis Ritchie, but you shouldn’t be spooked by pointers either.

By the end we will have a robust, accurate, fast Interpreter for our language, able to keep up with other professional caliber implementations out there.

## Design Note: What's in a Name?

One of the hardest challenges in writing this book was coming up with a name for the language it implements. I went through pages of candidates before I found one that worked. As you’ll discover on the first day you start building your own language, naming is deviously hard.

### A good name satisfies a few criteria:

1. It isn’t in use. You can run into all sorts of trouble, legal and social, if you inadvertently step on someone else’s name.
2. It’s easy to pronounce. If things go well, hordes of people will be saying and writing your language’s name. Anything longer than a couple of syllables or a handful of letters will annoy them to no end.
3. It’s distinct enough to search for. People will Google your language’s name to learn about it, so you want a word that’s rare enough that most results point to your docs. Though, with the amount of AI search engines are packing today, that’s less of an issue. Still, you won’t be doing your users any favors if you name your language “for”.
4. It doesn’t have negative connotations across a number of cultures. This is hard to be on guard for, but it’s worth considering. The designer of Nimrod ended up renaming his language to “Nim” because too many people remember that Bugs Bunny used “Nimrod” as an insult. (Bugs was using it ironically.)

If your potential name makes it through that gauntlet, keep it. Don’t get hung up on trying to find an appellation that captures the quintessence of your language. If the names of the world’s other successful languages teach us anything, it’s that the name doesn’t matter much. All you need is a reasonably unique token.
