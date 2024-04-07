package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    // Object to store the interpreter and run the commands
    // We make it static so that when used in REPL, it uses same instance of the interpreter
    // This will help to store global variables, those variables should persist  the session
    private static final Interpreter interpreter = new Interpreter();

    // This boolean is used to confirm if error occurred while running the program
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();

            if (line == null) break;
            run(line);

            // we reset the error in this mode for each line
            hadError = false;
        }
    }

    private static void run(String source) {
        // Sending the file to scanner and then reading tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Create a AST for Expression
        Parser p = new Parser(tokens);
        List<Stmt> statements = p.parse();

        // Stop if there was a syntax error.
        if(hadError) return;

        interpreter.interpret(statements);
    }

    // Function to call when error occurs
    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line " + line + "] Error" + where + ": " + message);

        hadError = true;
    }
}
