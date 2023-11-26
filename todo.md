# Things to do after it's complete

- Lists/arrays. You can build your own linked lists, but there's no way to create a data structure that stores a contiguous series of values that can be accessed in constant time in user code. That has to be baked into the language or core library.

- Some mechanism for handling runtime errors along the lines of exception handling.

- No break or continue for loops.

- Pretty Error messages with instructions.

- Learn LALR parser generator and syntax directed translation. There is a whole pack of parsing techniques whose names are mostly combinations of “L” and “R”—LL(k), LR(1), LALR—along with more exotic beasts like parser combinators, Earley parsers, the shunting yard algorithm, and packrat parsing. For our first interpreter, one technique is more than sufficient: recursive descent.

- Add comma and ternary operator, refer to the chapter 6 challenges
