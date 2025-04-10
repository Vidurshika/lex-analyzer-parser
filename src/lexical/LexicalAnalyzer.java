package lexical;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LexicalAnalyzer {
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String UNDERSCORE = "_";
    private static final String OPERATORS = "+-*<>&.@/:=~|$!#%^_[]{}\"?";
    private static final String PUNCTUATION = "();,";
    private static final Set<String> KEYWORDS = Set.of(
            "let", "in", "where", "rec", "fn", "aug", "or", "not", "gr", "ge", "ls", "le",
            "eq", "ne", "true", "false", "nil", "dummy", "within", "and"
    );


    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int lineNumber = 1;
        int i = 0;

        while (i < input.length()) {
            char currentChar = input.charAt(i);

            // Separating identifiers
            if (isLetter(currentChar)) {
                StringBuilder currentToken = new StringBuilder();
                currentToken.append(currentChar);
                i++;

                while (i < input.length() && (isLetter(input.charAt(i)) || isDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    currentToken.append(input.charAt(i));
                    i++;
                }

                String tokenValue = currentToken.toString();
                if (isKeyword(tokenValue)) {
                    tokens.add(new Token(tokenValue, "<KEYWORD>", lineNumber));
                } else {
                    tokens.add(new Token(tokenValue, "<IDENTIFIER>", lineNumber));
                }
            }
            // Separating integers
            else if (isDigit(currentChar)) {
                StringBuilder currentToken = new StringBuilder();
                currentToken.append(currentChar);
                i++;

                while (i < input.length() && isDigit(input.charAt(i))) {
                    currentToken.append(input.charAt(i));
                    i++;
                }

                tokens.add(new Token(currentToken.toString(), "<INTEGER>", lineNumber));
            }
            // Separating comments (//)
            else if (currentChar == '/' && i + 1 < input.length() && input.charAt(i + 1) == '/') {
                i += 2; // Skip the "//"
                while (i < input.length() && input.charAt(i) != '\n') {
                    i++;
                }
            }
            // Separating strings (enclosed in single quotes)
            else if (currentChar == '\'') {
                StringBuilder currentToken = new StringBuilder();
                currentToken.append(currentChar);
                i++;

                while (i < input.length()) {
                    char nextChar = input.charAt(i);
                    if (nextChar == '\n') {
                        lineNumber++;
                    }

                    if (nextChar == '\'') {
                        currentToken.append(nextChar);
                        i++;
                        break;
                    } else {
                        currentToken.append(nextChar);
                        i++;
                    }
                }

                if (currentToken.length() == 1 || currentToken.charAt(currentToken.length() - 1) != '\'') {
                    throw new RuntimeException("String is not closed properly at line " + lineNumber);
                }

                tokens.add(new Token(currentToken.toString(), "<STRING>", lineNumber));
            }
            // Separating punctuation
            else if (PUNCTUATION.indexOf(currentChar) != -1) {
                tokens.add(new Token(String.valueOf(currentChar), "<PUNCTUATION>", lineNumber));
                i++;
            }
            // Separating spaces
            else if (Character.isWhitespace(currentChar)) {
                if (currentChar == '\n') {
                    lineNumber++;
                }
                i++;
            }

            // Separating newlines
            else if (currentChar == '\n') {
                lineNumber++;
                i++;
            }
            // Separating operators
            else if (OPERATORS.indexOf(currentChar) != -1) {
                // Handle specific case for '->'
                if (currentChar == '-' && i + 1 < input.length() && input.charAt(i + 1) == '>') {
                    tokens.add(new Token("->", "<OPERATOR>", lineNumber));
                    i += 2;
                } else {
                    StringBuilder currentToken = new StringBuilder();
                    while (i < input.length() && OPERATORS.indexOf(input.charAt(i)) != -1) {
                        currentToken.append(input.charAt(i));
                        i++;
                    }
                    tokens.add(new Token(currentToken.toString(), "<OPERATOR>", lineNumber));
                }
            }
            // Invalid characters
            else {
                throw new RuntimeException("Invalid character: " + currentChar + " at line " + lineNumber);
            }
        }

        // Mark the first and last tokens
        if (!tokens.isEmpty()) {
            tokens.get(0).makeFirstToken();
            tokens.get(tokens.size() - 1).makeLastToken();
        }

        return tokens;
    }

    // Helper method to check if a character is a letter
    private static boolean isLetter(char c) {
        return LETTERS.indexOf(c) != -1;
    }

    // Helper method to check if a character is a digit
    private static boolean isDigit(char c) {
        return DIGITS.indexOf(c) != -1;
    }

    // Helper method to check if a string is a keyword
    private static boolean isKeyword(String s) {
        return KEYWORDS.contains(s);
    }
}