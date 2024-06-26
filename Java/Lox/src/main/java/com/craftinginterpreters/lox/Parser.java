package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Parser {
    private static final Set<String> invalidIdentifiers = new HashSet<>();

    private final List<Token> tokens;
    private int current = 0;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment(){
        Expr expr = equality();

        if(match(TokenType.EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            invalidIdentifiers.clear();
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration(){
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name");

        // Create a set of invalid names
        invalidIdentifiers.add(name.lexeme);

        Expr initializer = null;
        if(match(TokenType.EQUAL)){
            initializer = expression();
        }

        invalidIdentifiers.clear();

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Expr equality() {
        Expr expr = comparison();

        // Notice that if not matching token is found then we know that it is not an evaluation
        // now we go till we find == or != expressions
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token token = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token token = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token token = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token token = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, token, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token token = previous();
            Expr expr = primary();
            return new Expr.Unary(token, expr);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(TokenType.IDENTIFIER)){
            if(invalidIdentifiers.contains(previous().lexeme)){
                throw error(previous(), "Variable is shadowed");
            }
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            // consume the token if it is correct else we handle the error
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Except an Expression");
    }

    // utils
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advanceWithNoReturn();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private void advanceWithNoReturn() {
        if (!isAtEnd()) current++;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advanceWithNoReturn();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            // If discard everything until we find a statement boundary
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advanceWithNoReturn();
        }
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private static class ParseError extends RuntimeException {
    }
}
