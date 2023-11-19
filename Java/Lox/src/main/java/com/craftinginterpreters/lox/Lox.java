package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    // This boolean is used to confirm if error occured while running the program
    static boolean hadError = false;
    
    public static void main(String[] args) throws IOException {
        if (args.length > 1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if(args.length == 1) {
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
    }
    
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        
        for(;;){
            System.out.print("> ");
            String line = reader.readLine();
            
            if(line == null) break;
            run(line);
            
            // we reset the error in this mode for each line
            hadError = false;
        }
    }
    
    private static void run(String source){
        // Sending the file to scanner and then reading tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        
        // Printing the tokens from the scanner
        for(Token token: tokens){
            System.out.println(token);
        }   
    }
    
    // Function to call when error occurs
    static void error(int line, String message){
        report(line, "", message);
    }
    
    private static void report(int line, String where, String message){
        System.err.println("[Line " + line + "] Error" + where+ ": " + message);
        
        hadError = true;
    }
}