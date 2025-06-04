package parser;

import scanner.Token;
import scanner.LexicalAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;

public class Parser {
    private List<Token> tokens;
    private int currentIndex;
    private Stack<Node> stack;

    // Constructor for the Parser class
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
        this.stack = new Stack<>();
    }

    // Static method to parse a file and return the AST
    public static Node parse(String fileName) throws IOException {
        // Read the file content
        String fileContent = new String(Files.readAllBytes(Paths.get(fileName)));
        // Tokenize the file content
        List<Token> tokens = LexicalAnalyzer.tokenize(fileContent);

        // Create a Parser instance and parse the tokens
        Parser parser = new Parser(tokens);
        return parser.parseTokens();
    }


    // Internal method to parse tokens and build the AST
    private Node parseTokens() {
        parseE(); // Start with the top-level grammar rule
        if (!stack.isEmpty()) {
            return stack.pop();
        } else {
            throw new RuntimeException("Stack is empty after parsing.");
        }
    }

    // Helper method to get the current token
    private Token currentToken() {
        return tokens.get(currentIndex);
    }

    public void printTree(Node root) {
        Node.preorderTraversal(root);
    }

    // Helper method to consume the current token
    private void consume(String expectedValue) {

        // Check if the current token matches the expected value
        if (!currentToken().getValue().equals(expectedValue)) {
            throw new RuntimeException("Syntax error in line " + currentToken().getLine() +
                    ": Expected " + expectedValue + " but got " + currentToken().getValue());
        }

        // Move to the next token
        if (!currentToken().isLastToken()) {
            currentIndex++;
        } else {
            // Handle the case where the last token is reached
            if (!currentToken().getType().equals(")")) {
                currentToken().setType(")");
            }
        }
    }

    // Method to build the abstract syntax tree
    public void buildTree(String value, int numChildren) {

        // Create a new node with the given value
        Node node = new Node(value);

        // Pop children from the stack and assign them to the node
        for (int i = 0; i < numChildren; i++) {
            if (stack.isEmpty()) {
                System.out.println("Error: Stack is empty while building tree");
                throw new RuntimeException("Stack is empty");
            }
            // Add children in reverse order
            node.getChildren().add(0, stack.pop());
        }

        // Push the newly created node back onto the stack
        stack.push(node);
    }


    // Parse E -> 'let' D 'in' E | 'fn' Vb+ '.' E | Ew
    private void parseE() {

        // E -> 'let' D 'in' E
        if (currentToken().getValue().equals("let")) {
            consume("let");
            parseD();

            if (currentToken().getValue().equals("in")) {
                consume("in");
                parseE();
                buildTree("let", 2);
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": 'in' expected");
            }
        }
        // E -> 'fn' Vb+ '.' E
        else if (currentToken().getValue().equals("fn")) {
            consume("fn");;
            int n = 0;

            while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getValue().equals("(")) {
                parseVb();
                n++;
            }

            if (n == 0) {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
            }

            if (currentToken().getValue().equals(".")) {
                consume(".");
                parseE();
                buildTree("lambda", n + 1);
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": '.' expected");
            }
        }
        // E -> Ew
        else {
            parseEw();
        }
    }
    // Parse Ew -> T | T 'where' Dr
    private void parseEw() {
        parseT();
        if (currentToken().getValue().equals("where")) {
            consume("where");
            parseDr();
            buildTree("where", 2);
        }
    }

    // Parse T -> Ta (',' Ta)*
    private void parseT() {
        parseTa();
        int n = 0;
        while (currentToken().getValue().equals(",")) {
            consume(",");
            parseTa();
            n++;
        }
        if (n > 0) {
            buildTree("tau", n + 1);
        }
    }

    // Parse Ta -> Tc | Ta 'aug' Tc
    private void parseTa() {
        parseTc();
        while (currentToken().getValue().equals("aug")) {
            consume("aug");
            parseTc();
            buildTree("aug", 2);
        }
    }

    // Parse Tc -> B | B '->' Tc '|' Tc
    private void parseTc() {
        // Tc -> B
        parseB();

        // Tc -> B '->' Tc '|' Tc
        if (currentToken().getValue().equals("->")) {
            consume("->");
            parseTc();

            if (currentToken().getValue().equals("|")) {
                consume("|");
                parseTc();
                buildTree("->", 3);
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": '|' expected");
            }
        }
    }

    // Parse B -> Bt | B 'or' Bt
    private void parseB() {

        // B -> Bt
        parseBt();

        // B -> B 'or' Bt
        while (currentToken().getValue().equals("or")) {
            consume("or");
            parseBt();
            buildTree("or", 2);
        }
    }

    // Parse Bt -> Bs | Bt '&' Bs
    private void parseBt() {

        // Bt -> Bs
        parseBs();

        // Bt -> Bt '&' Bs
        while (currentToken().getValue().equals("&")) {
            consume("&");
            parseBs();
            buildTree("&", 2);
        }
    }

    // Parse Bs -> 'not' Bp | Bp
    private void parseBs() {

        // Bs -> 'not' Bp
        if (currentToken().getValue().equals("not")) {
            consume("not");
            parseBp();
            buildTree("not", 1);
        }
        // Bs -> Bp
        else {
            parseBp();
        }
    }

    private void parseBp() {

        // Bp -> A
        parseA();

        // Bp -> A ('gr' | '>') A
        if (currentToken().getValue().equals("gr") || currentToken().getValue().equals(">")) {

            consume(currentToken().getValue());
            parseA();
            buildTree("gr", 2);
        }
        // Bp -> A ('ge' | '>=') A
        else if (currentToken().getValue().equals("ge") || currentToken().getValue().equals(">=")) {
            consume(currentToken().getValue());
            parseA();
            buildTree("ge", 2);
        }
        // Bp -> A ('ls' | '<') A
        else if (currentToken().getValue().equals("ls") || currentToken().getValue().equals("<")) {
            consume(currentToken().getValue());
            parseA();
            buildTree("ls", 2);

        }
        // Bp -> A ('le' | '<=') A
        else if (currentToken().getValue().equals("le") || currentToken().getValue().equals("<=")) {
            consume(currentToken().getValue());
            parseA();
            buildTree("le", 2);
        }
        // Bp -> A 'eq' A
        else if (currentToken().getValue().equals("eq")) {
            consume("eq");
            parseA();
            buildTree("eq", 2);
        }
        // Bp -> A 'ne' A
        else if (currentToken().getValue().equals("ne")) {
            consume("ne");
            parseA();
            buildTree("ne", 2);
        }
    }

    private void parseA() {

        // A -> '+' At
        if (currentToken().getValue().equals("+")) {
            consume("+");
            parseAt();

        }
        // A -> '-' At
        else if (currentToken().getValue().equals("-")) {
            consume("-");
            parseAt();;
            buildTree("neg", 1);
        }
        // A -> At
        else {
            parseAt();
        }

        // A -> A '+' At | A '-' At
        while (currentToken().getValue().equals("+") || currentToken().getValue().equals("-")) {
            String operator = currentToken().getValue();
            consume(operator);
            parseAt();
            buildTree(operator, 2);
        }
    }

    private void parseAt() {

        // At -> Af
        parseAf();

        // At -> At '*' Af | At '/' Af
        while (currentToken().getValue().equals("*") || currentToken().getValue().equals("/")) {

            String operator = currentToken().getValue();
            consume(operator);
            parseAf();
            buildTree(operator, 2);

        }
    }

    private void parseAf() {

        // Af -> Ap
        parseAp();

        // Af -> Ap '**' Af
        if (currentToken().getValue().equals("**")) {
            consume("**");
            parseAf();
            buildTree("**", 2);
        }
    }

    private void parseAp() {

        // Ap -> R
        parseR();

        // Ap -> Ap '@' <IDENTIFIER> R
        while (currentToken().getValue().equals("@")) {
            consume("@");

            if (currentToken().getType().equals("<IDENTIFIER>")) {
                buildTree("<ID:" + currentToken().getValue() + ">", 0);
                consume(currentToken().getValue());
                parseR();
                buildTree("@", 3);
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier expected");
            }
        }
    }

    private void parseR() {

        // R -> Rn
        parseRn();

        // R -> R Rn
        while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getType().equals("<INTEGER>") || currentToken().getType().equals("<STRING>") ||
                currentToken().getValue().equals("true") || currentToken().getValue().equals("false") || currentToken().getValue().equals("nil") ||
                currentToken().getValue().equals("(") || currentToken().getValue().equals("dummy")) {
            parseRn();
            buildTree("gamma", 2);
        }
    }

    private void parseRn() {

        String value = currentToken().getValue();

        // Rn -> <IDENTIFIER>
        if (currentToken().getType().equals("<IDENTIFIER>")) {
            consume(value);
            buildTree("<ID:" + value + ">", 0);
        }
        // Rn -> <INTEGER>
        else if (currentToken().getType().equals("<INTEGER>")) {
            consume(value);
            buildTree("<INT:" + value + ">", 0);
        }
        // Rn -> <STRING>
        else if (currentToken().getType().equals("<STRING>")) {
            consume(value);
            buildTree("<STR:" + value + ">", 0);
        }
        // Rn -> 'true', 'false', 'nil', 'dummy'
        else if (value.equals("true") || value.equals("false") || value.equals("nil") || value.equals("dummy")) {
            consume(value);
            buildTree("<" + value + ">", 0);
        }
        // Rn -> '(' E ')'
        else if (value.equals("(")) {
            consume("(");
            parseE();

            if (currentToken().getValue().equals(")")) {
                consume(")");
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": ')' expected");
            }
        }
        // Syntax error
        else {
            throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier, Integer, String, 'true', 'false', 'nil', 'dummy' or '(' expected");
        }
    }

    private void parseD() {

        // D -> Da
        parseDa();
        // D -> 'within' D
        if (currentToken().getValue().equals("within")) {
            consume("within");
            parseD();
            buildTree("within", 2);
        }
    }

    private void parseDa() {

        // Da -> Dr
        parseDr();

        // Da -> Dr ('and' Dr)+
        int n = 0;
        while (currentToken().getValue().equals("and")) {
            consume("and");
            parseDr();
            n++;
        }

        if (n > 0) {
            buildTree("and", n + 1);
        }
    }

    private void parseDr() {

        // Dr -> 'rec' Db
        if (currentToken().getValue().equals("rec")) {
            consume("rec");
            parseDb();
            buildTree("rec", 1);
        }
        // Dr -> Db
        else {
            parseDb();
        }
    }

    private void parseDb() {

        String value = currentToken().getValue();

        // Db -> '(' D ')'
        if (value.equals("(")) {
            consume("(");
            parseD();

            if (currentToken().getValue().equals(")")) {
                consume(")");
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": ')' expected");
            }
        }
        // Db -> <IDENTIFIER>
        else if (currentToken().getType().equals("<IDENTIFIER>")) {
            consume(value);
            buildTree("<ID:" + value + ">", 0);

            // Db -> <IDENTIFIER> Vb+ '=' E
            if (currentToken().getValue().equals(",") || currentToken().getValue().equals("=")) {
                parseVl();
                consume("=");
                parseE();
                buildTree("=", 2);
            }
            // Db -> Vl '=' E
            else {
                int n = 0;

                while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getValue().equals("(")) {
                    parseVb();
                    n++;
                }

                if (n == 0) {
                    throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
                }

                if (currentToken().getValue().equals("=")) {
                    consume("=");
                    parseE();
                    buildTree("function_form", n + 2);
                } else {
                    throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": '=' expected");
                }
            }
        } else {
            throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
        }
    }

    private void parseVb() {

        String value1 = currentToken().getValue();

        // Vb -> <IDENTIFIER>
        if (currentToken().getType().equals("<IDENTIFIER>")) {
            consume(value1);
            buildTree("<ID:" + value1 + ">", 0);

        }
        // Vb -> '(' Vl ')'
        else if (value1.equals("(")) {
            consume("(");

            String value2 = currentToken().getValue();

            // Vb -> '(' ')'
            if (value2.equals(")")) {
                consume(")");
                buildTree("()", 0);
            }
            // Vb -> '(' Vl ')'
            else if (currentToken().getType().equals("<IDENTIFIER>")) {
                consume(value2);
                buildTree("<ID:" + value2 + ">", 0);
                parseVl();

                if (currentToken().getValue().equals(")")) {
                    consume(")");
                } else {
                    throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": ')' expected");
                }
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or ')' expected");
            }
        }
        // Syntax error
        else {
            throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
        }
    }

    private void parseVl() {

        int n = 0;

        // Vl -> <IDENTIFIER> (',' <IDENTIFIER>)*
        while (currentToken().getValue().equals(",")) {
            consume(",");

            if (currentToken().getType().equals("<IDENTIFIER>")) {
                String value = currentToken().getValue();
                consume(value);
                buildTree("<ID:" + value + ">", 0);
                n++;
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier expected");
            }
        }

        if (n > 0) {
            buildTree(",", n + 1);
        }
    }

}