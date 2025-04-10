package parser;

import lexical.Token;
import lexical.LexicalAnalyzer;

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
        System.out.println("File Content:");
        System.out.println(fileContent);
        // Tokenize the file content
        List<Token> tokens = LexicalAnalyzer.tokenize(fileContent);
        // Print all tokens
        System.out.println("Token list:");
        for (Token token : tokens) {
                System.out.println(token);
        }
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
        System.out.println("Consuming token: " + expectedValue + ", Current token: " + currentToken());

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
//                currentToken().setType(")");
            }
        }
    }

    // Method to build the abstract syntax tree
    public void buildTree(String value, int numChildren) {
        System.out.println("Building tree with value: " + value + ", number of children: " + numChildren);

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
        System.out.println("Tree built: " + node);
    }


    // Parse E -> 'let' D 'in' E | 'fn' Vb+ '.' E | Ew
    private void parseE() {
        System.out.println("E");
        System.out.println(currentToken());
        // E -> 'let' D 'in' E
        if (currentToken().getValue().equals("let")) {
            System.out.println("E->if(let)");
            System.out.println(currentToken());
            consume("let");
            System.out.println("E->if(let)_after_consuming_let");
            System.out.println(currentToken());
            parseD();

            if (currentToken().getValue().equals("in")) {
                System.out.println("E->if(let)->if(in)");
                System.out.println(currentToken());
                consume("in");
                System.out.println("E->if(let)->if(in)_after_consuming_in");
                System.out.println(currentToken());
                parseE();
                buildTree("let", 2);
            } else {
                System.out.println("E->if(let)->else");
                System.out.println(currentToken());
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": 'in' expected");
            }
        }
        // E -> 'fn' Vb+ '.' E
        else if (currentToken().getValue().equals("fn")) {
            System.out.println("E->else_if(fn)");
            System.out.println(currentToken());
            consume("fn");
            System.out.println("E->else_if(fn)_after_consuming_fn");
            System.out.println(currentToken());
            int n = 0;

            while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getValue().equals("(")) {
                System.out.println("E->else_if(fn)_while");
                System.out.println(currentToken());
                parseVb();
                n++;
            }

            if (n == 0) {
                System.out.println("E->else_if(fn)_if(n==0)");
                System.out.println(currentToken());
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
            }

            if (currentToken().getValue().equals(".")) {
                System.out.println("E->else_if(fn)_if_current_token='.'");
                System.out.println(currentToken());
                consume(".");
                parseE();
                buildTree("lambda", n + 1);
            } else {
                System.out.println("E->else_if(fn)_else");
                System.out.println(currentToken());
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
        System.out.println("Ew");
        System.out.println(currentToken());
        parseT();
        System.out.println("Ew_after_parseT");
        System.out.println(currentToken());
        if (currentToken().getValue().equals("where")) {
            System.out.println("Ew_after_parseT_if");
            System.out.println(currentToken());
            consume("where");
            System.out.println("Ew_after_parseT_if_after_consuming_where");
            System.out.println(currentToken());
            parseDr();
            buildTree("where", 2);
        }
    }

    // Parse T -> Ta (',' Ta)*
    private void parseT() {
        System.out.println("T");
        System.out.println(currentToken());
        parseTa();
        int n = 0;
        while (currentToken().getValue().equals(",")) {
            System.out.println("T_while");
            System.out.println(currentToken());
            consume(",");
            System.out.println("T_while_after_consuming_','");
            System.out.println(currentToken());
            parseTa();
            n++;
        }
        if (n > 0) {
            System.out.println("T_if");
            System.out.println(currentToken());
            buildTree("tau", n + 1);
        }
    }

    // Parse Ta -> Tc | Ta 'aug' Tc
    private void parseTa() {
        System.out.println("Ta");
        System.out.println(currentToken());
        parseTc();
        System.out.println("Ta_after_parseTc");
        System.out.println(currentToken());
        while (currentToken().getValue().equals("aug")) {
            System.out.println("Ta_after_parseTc_while");
            System.out.println(currentToken());
            consume("aug");
            System.out.println("Ta_after_parseTc_while_after_consuming_aug");
            System.out.println(currentToken());
            parseTc();
            System.out.println("Ta_after_parseTc_while_after_consuming_aug_After_ParseTc");
            System.out.println(currentToken());
            buildTree("aug", 2);
        }
    }

    // Parse Tc -> B | B '->' Tc '|' Tc
    private void parseTc() {
        System.out.println("Tc");
        System.out.println(currentToken());

        // Tc -> B
        parseB();
        System.out.println("Tc_after_parseB");
        System.out.println(currentToken());

        // Tc -> B '->' Tc '|' Tc
        if (currentToken().getValue().equals("->")) {
            System.out.println("Tc_after_parseB_if");
            System.out.println(currentToken());
            consume("->");
            System.out.println("Tc_after_parseB_if_after_consuming_->");
            System.out.println(currentToken());

            parseTc();
            System.out.println("Tc_after_parseB_if_after_consuming_->_after_parseTc");
            System.out.println(currentToken());

            if (currentToken().getValue().equals("|")) {
                System.out.println("Tc_after_parseB_if_after_consuming_->_after_parseTc_if");
                System.out.println(currentToken());
                consume("|");
                System.out.println("Tc_after_parseB_if_after_consuming_->_after_parseTc_if_after_consuming_|");
                System.out.println(currentToken());

                parseTc();
                System.out.println("Tc_after_parseB_if_after_consuming_->_after_parseTc_if_after_consuming_|_after_parseTc");
                System.out.println(currentToken());

                buildTree("->", 3);
                System.out.println("Tc_after_parseB_if_after_consuming_->_after_parseTc_if_after_consuming_|_after_parseTc_after_buildTree");
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": '|' expected");
            }
        }
    }

    // Parse B -> Bt | B 'or' Bt
    private void parseB() {
        System.out.println("B");
        System.out.println(currentToken());

        // B -> Bt
        parseBt();
        System.out.println("B_after_parseBt");
        System.out.println(currentToken());

        // B -> B 'or' Bt
        while (currentToken().getValue().equals("or")) {
            System.out.println("B_while");
            System.out.println(currentToken());
            consume("or");
            System.out.println("B_while_after_consuming_or");
            System.out.println(currentToken());
            parseBt();
            System.out.println("B_while_after_parseBt");
            System.out.println(currentToken());
            buildTree("or", 2);
            System.out.println("B_while_after_buildTree");
        }
    }

    // Parse Bt -> Bs | Bt '&' Bs
    private void parseBt() {
        System.out.println("Bt");
        System.out.println(currentToken());

        // Bt -> Bs
        parseBs();
        System.out.println("Bt_after_parseBs");
        System.out.println(currentToken());

        // Bt -> Bt '&' Bs
        while (currentToken().getValue().equals("&")) {
            System.out.println("Bt_while");
            System.out.println(currentToken());
            consume("&");
            System.out.println("Bt_while_after_consuming_&");
            System.out.println(currentToken());
            parseBs();
            System.out.println("Bt_while_after_parseBs");
            System.out.println(currentToken());
            buildTree("&", 2);
            System.out.println("Bt_while_after_buildTree");
        }
    }

    // Parse Bs -> 'not' Bp | Bp
    private void parseBs() {
        System.out.println("Bs");
        System.out.println(currentToken());

        // Bs -> 'not' Bp
        if (currentToken().getValue().equals("not")) {
            System.out.println("Bs_if");
            System.out.println(currentToken());
            consume("not");
            System.out.println("Bs_after_consuming_not");
            System.out.println(currentToken());
            parseBp();
            System.out.println("Bs_after_parseBp");
            System.out.println(currentToken());
            buildTree("not", 1);
            System.out.println("Bs_after_buildTree");
        }
        // Bs -> Bp
        else {
            System.out.println("Bs_else");
            System.out.println(currentToken());
            parseBp();
            System.out.println("Bs_after_parseBp_in_else");
            System.out.println(currentToken());
        }
    }

    private void parseBp() {
        System.out.println("Bp");
        System.out.println(currentToken());

        // Bp -> A
        parseA();
        System.out.println("Bp_after_parseA");
        System.out.println(currentToken());

        // Bp -> A ('gr' | '>') A
        if (currentToken().getValue().equals("gr") || currentToken().getValue().equals(">")) {
            System.out.println("Bp_if_gr_or_>");
            System.out.println(currentToken());
            consume(currentToken().getValue());
            System.out.println("Bp_after_consuming_gr_or_>");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_gr_or_>");
            System.out.println(currentToken());
            buildTree("gr", 2);
            System.out.println("Bp_after_buildTree_gr");
        }
        // Bp -> A ('ge' | '>=') A
        else if (currentToken().getValue().equals("ge") || currentToken().getValue().equals(">=")) {
            System.out.println("Bp_if_ge_or_>=");
            System.out.println(currentToken());
            consume(currentToken().getValue());
            System.out.println("Bp_after_consuming_ge_or_>=");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_ge_or_>=");
            System.out.println(currentToken());
            buildTree("ge", 2);
            System.out.println("Bp_after_buildTree_ge");
        }
        // Bp -> A ('ls' | '<') A
        else if (currentToken().getValue().equals("ls") || currentToken().getValue().equals("<")) {
            System.out.println("Bp_if_ls_or_<");
            System.out.println(currentToken());
            consume(currentToken().getValue());
            System.out.println("Bp_after_consuming_ls_or_<");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_ls_or_<");
            System.out.println(currentToken());
            buildTree("ls", 2);
            System.out.println("Bp_after_buildTree_ls");
        }
        // Bp -> A ('le' | '<=') A
        else if (currentToken().getValue().equals("le") || currentToken().getValue().equals("<=")) {
            System.out.println("Bp_if_le_or_<=");
            System.out.println(currentToken());
            consume(currentToken().getValue());
            System.out.println("Bp_after_consuming_le_or_<=");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_le_or_<=");
            System.out.println(currentToken());
            buildTree("le", 2);
            System.out.println("Bp_after_buildTree_le");
        }
        // Bp -> A 'eq' A
        else if (currentToken().getValue().equals("eq")) {
            System.out.println("Bp_if_eq");
            System.out.println(currentToken());
            consume("eq");
            System.out.println("Bp_after_consuming_eq");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_eq");
            System.out.println(currentToken());
            buildTree("eq", 2);
            System.out.println("Bp_after_buildTree_eq");
        }
        // Bp -> A 'ne' A
        else if (currentToken().getValue().equals("ne")) {
            System.out.println("Bp_if_ne");
            System.out.println(currentToken());
            consume("ne");
            System.out.println("Bp_after_consuming_ne");
            System.out.println(currentToken());
            parseA();
            System.out.println("Bp_after_parseA_in_ne");
            System.out.println(currentToken());
            buildTree("ne", 2);
            System.out.println("Bp_after_buildTree_ne");
        }
    }

    private void parseA() {
        System.out.println("A");
        System.out.println(currentToken());

        // A -> '+' At
        if (currentToken().getValue().equals("+")) {
            System.out.println("A_if_+");
            System.out.println(currentToken());
            consume("+");
            System.out.println("A_after_consuming_+");
            System.out.println(currentToken());
            parseAt();
            System.out.println("A_after_parseAt");
            System.out.println(currentToken());
        }
        // A -> '-' At
        else if (currentToken().getValue().equals("-")) {
            System.out.println("A_if_-");
            System.out.println(currentToken());
            consume("-");
            System.out.println("A_after_consuming_-");
            System.out.println(currentToken());
            parseAt();
            System.out.println("A_after_parseAt");
            System.out.println(currentToken());
            buildTree("neg", 1);
            System.out.println("A_after_buildTree_neg");
        }
        // A -> At
        else {
            System.out.println("A_else");
            System.out.println(currentToken());
            parseAt();
            System.out.println("A_after_parseAt_in_else");
            System.out.println(currentToken());
        }

        // A -> A '+' At | A '-' At
        while (currentToken().getValue().equals("+") || currentToken().getValue().equals("-")) {
            System.out.println("A_while");
            System.out.println(currentToken());
            String operator = currentToken().getValue();
            consume(operator);
            System.out.println("A_while_after_consuming_" + operator);
            System.out.println(currentToken());
            parseAt();
            System.out.println("A_while_after_parseAt");
            System.out.println(currentToken());
            buildTree(operator, 2);
            System.out.println("A_while_after_buildTree_" + operator);
        }
    }

    private void parseAt() {
        System.out.println("At");
        System.out.println(currentToken());

        // At -> Af
        parseAf();
        System.out.println("At_after_parseAf");
        System.out.println(currentToken());

        // At -> At '*' Af | At '/' Af
        while (currentToken().getValue().equals("*") || currentToken().getValue().equals("/")) {
            System.out.println("At_while");
            System.out.println(currentToken());
            String operator = currentToken().getValue();
            consume(operator);
            System.out.println("At_while_after_consuming_" + operator);
            System.out.println(currentToken());
            parseAf();
            System.out.println("At_while_after_parseAf");
            System.out.println(currentToken());
            buildTree(operator, 2);
            System.out.println("At_while_after_buildTree_" + operator);
        }
    }

    private void parseAf() {
        System.out.println("Af");
        System.out.println(currentToken());

        // Af -> Ap
        parseAp();
        System.out.println("Af_after_parseAp");
        System.out.println(currentToken());

        // Af -> Ap '**' Af
        if (currentToken().getValue().equals("**")) {
            System.out.println("Af_if_**");
            System.out.println(currentToken());
            consume("**");
            System.out.println("Af_after_consuming_**");
            System.out.println(currentToken());
            parseAf();
            System.out.println("Af_after_parseAf");
            System.out.println(currentToken());
            buildTree("**", 2);
            System.out.println("Af_after_buildTree_**");
        }
    }

    private void parseAp() {
        System.out.println("Ap");
        System.out.println(currentToken());

        // Ap -> R
        parseR();
        System.out.println("Ap_after_parseR");
        System.out.println(currentToken());

        // Ap -> Ap '@' <IDENTIFIER> R
        while (currentToken().getValue().equals("@")) {
            System.out.println("Ap_while");
            System.out.println(currentToken());
            consume("@");
            System.out.println("Ap_after_consuming_@");
            System.out.println(currentToken());

            if (currentToken().getType().equals("<IDENTIFIER>")) {
                System.out.println("Ap_while_if_identifier");
                System.out.println(currentToken());
                buildTree("<ID:" + currentToken().getValue() + ">", 0);
                consume(currentToken().getValue());
                System.out.println("Ap_after_consuming_identifier");
                System.out.println(currentToken());
                parseR();
                System.out.println("Ap_after_parseR");
                System.out.println(currentToken());
                buildTree("@", 3);
                System.out.println("Ap_after_buildTree_@");
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier expected");
            }
        }
    }

    private void parseR() {
        System.out.println("R");
        System.out.println(currentToken());

        // R -> Rn
        parseRn();
        System.out.println("R_after_parseRn");
        System.out.println(currentToken());

        // R -> R Rn
        while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getType().equals("<INTEGER>") || currentToken().getType().equals("<STRING>") ||
                currentToken().getValue().equals("true") || currentToken().getValue().equals("false") || currentToken().getValue().equals("nil") ||
                currentToken().getValue().equals("(") || currentToken().getValue().equals("dummy")) {
            System.out.println("R_while");
            System.out.println(currentToken());
            parseRn();
            System.out.println("R_after_parseRn_in_while");
            System.out.println(currentToken());
            buildTree("gamma", 2);
            System.out.println("R_after_buildTree_gamma");
        }
    }

    private void parseRn() {
        System.out.println("Rn");
        System.out.println(currentToken());

        String value = currentToken().getValue();

        // Rn -> <IDENTIFIER>
        if (currentToken().getType().equals("<IDENTIFIER>")) {
            System.out.println("Rn_if_identifier");
            System.out.println(currentToken());
            consume(value);
            System.out.println("Rn_after_consuming_identifier");
            System.out.println(currentToken());
            buildTree("<ID:" + value + ">", 0);
            System.out.println("Rn_after_buildTree_identifier");
        }
        // Rn -> <INTEGER>
        else if (currentToken().getType().equals("<INTEGER>")) {
            System.out.println("Rn_if_integer");
            System.out.println(currentToken());
            consume(value);
            System.out.println("Rn_after_consuming_integer");
            System.out.println(currentToken());
            buildTree("<INT:" + value + ">", 0);
            System.out.println("Rn_after_buildTree_integer");
        }
        // Rn -> <STRING>
        else if (currentToken().getType().equals("<STRING>")) {
            System.out.println("Rn_if_string");
            System.out.println(currentToken());
            consume(value);
            System.out.println("Rn_after_consuming_string");
            System.out.println(currentToken());
            buildTree("<STR:" + value + ">", 0);
            System.out.println("Rn_after_buildTree_string");
        }
        // Rn -> 'true', 'false', 'nil', 'dummy'
        else if (value.equals("true") || value.equals("false") || value.equals("nil") || value.equals("dummy")) {
            System.out.println("Rn_if_literal");
            System.out.println(currentToken());
            consume(value);
            System.out.println("Rn_after_consuming_literal");
            System.out.println(currentToken());
            buildTree("<" + value + ">", 0);
            System.out.println("Rn_after_buildTree_literal");
        }
        // Rn -> '(' E ')'
        else if (value.equals("(")) {
            System.out.println("Rn_if_parenthesis");
            System.out.println(currentToken());
            consume("(");
            System.out.println("Rn_after_consuming_open_parenthesis");
            System.out.println(currentToken());
            parseE();
            System.out.println("Rn_after_parseE");
            System.out.println(currentToken());

            if (currentToken().getValue().equals(")")) {
                System.out.println("Rn_if_closing_parenthesis");
                System.out.println(currentToken());
                consume(")");
                System.out.println("Rn_after_consuming_closing_parenthesis");
                System.out.println(currentToken());
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
        System.out.println("D");
        System.out.println(currentToken());

        // D -> Da
        parseDa();
        System.out.println("D_after_parseDa");
        System.out.println(currentToken());

        // D -> 'within' D
        if (currentToken().getValue().equals("within")) {
            System.out.println("D_if_within");
            System.out.println(currentToken());
            consume("within");
            System.out.println("D_after_consuming_within");
            System.out.println(currentToken());
            parseD();
            System.out.println("D_after_parseD");
            System.out.println(currentToken());
            buildTree("within", 2);
            System.out.println("D_after_buildTree_within");
        }
    }

    private void parseDa() {
        System.out.println("Da");
        System.out.println(currentToken());

        // Da -> Dr
        parseDr();
        System.out.println("Da_after_parseDr");
        System.out.println(currentToken());

        // Da -> Dr ('and' Dr)+
        int n = 0;
        while (currentToken().getValue().equals("and")) {
            System.out.println("Da_while");
            System.out.println(currentToken());
            consume("and");
            System.out.println("Da_after_consuming_and");
            System.out.println(currentToken());
            parseDr();
            System.out.println("Da_after_parseDr_in_while");
            System.out.println(currentToken());
            n++;
        }

        if (n > 0) {
            System.out.println("Da_if_n_greater_than_0");
            System.out.println(currentToken());
            buildTree("and", n + 1);
            System.out.println("Da_after_buildTree_and");
        }
    }

    private void parseDr() {
        System.out.println("Dr");
        System.out.println(currentToken());

        // Dr -> 'rec' Db
        if (currentToken().getValue().equals("rec")) {
            System.out.println("Dr_if_rec");
            System.out.println(currentToken());
            consume("rec");
            System.out.println("Dr_after_consuming_rec");
            System.out.println(currentToken());
            parseDb();
            System.out.println("Dr_after_parseDb");
            System.out.println(currentToken());
            buildTree("rec", 1);
            System.out.println("Dr_after_buildTree_rec");
        }
        // Dr -> Db
        else {
            System.out.println("Dr_else");
            System.out.println(currentToken());
            parseDb();
            System.out.println("Dr_after_parseDb_in_else");
            System.out.println(currentToken());
        }
    }

    private void parseDb() {
        System.out.println("Db");
        System.out.println(currentToken());

        String value = currentToken().getValue();

        // Db -> '(' D ')'
        if (value.equals("(")) {
            System.out.println("Db_if_open_parenthesis");
            System.out.println(currentToken());
            consume("(");
            System.out.println("Db_after_consuming_open_parenthesis");
            System.out.println(currentToken());
            parseD();
            System.out.println("Db_after_parseD");
            System.out.println(currentToken());

            if (currentToken().getValue().equals(")")) {
                System.out.println("Db_if_closing_parenthesis");
                System.out.println(currentToken());
                consume(")");
                System.out.println("Db_after_consuming_closing_parenthesis");
                System.out.println(currentToken());
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": ')' expected");
            }
        }
        // Db -> <IDENTIFIER>
        else if (currentToken().getType().equals("<IDENTIFIER>")) {
            System.out.println("Db_if_identifier");
            System.out.println(currentToken());
            consume(value);
            System.out.println("Db_after_consuming_identifier");
            System.out.println(currentToken());
            buildTree("<ID:" + value + ">", 0);
            System.out.println("Db_after_buildTree_identifier");

            // Db -> <IDENTIFIER> Vb+ '=' E
            if (currentToken().getValue().equals(",") || currentToken().getValue().equals("=")) {
                System.out.println("Db_if_comma_or_equals");
                System.out.println(currentToken());
                parseVl();
                System.out.println("Db_after_parseVl");
                System.out.println(currentToken());
                consume("=");
                System.out.println("Db_after_consuming_equals");
                System.out.println(currentToken());
                parseE();
                System.out.println("Db_after_parseE");
                System.out.println(currentToken());
                buildTree("=", 2);
                System.out.println("Db_after_buildTree_equals");
            }
            // Db -> Vl '=' E
            else {
                System.out.println("Db_else");
                System.out.println(currentToken());
                int n = 0;

                while (currentToken().getType().equals("<IDENTIFIER>") || currentToken().getValue().equals("(")) {
                    System.out.println("Db_while");
                    System.out.println(currentToken());
                    parseVb();
                    System.out.println("Db_after_parseVb_in_while");
                    System.out.println(currentToken());
                    n++;
                }

                if (n == 0) {
                    throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
                }

                if (currentToken().getValue().equals("=")) {
                    System.out.println("Db_if_equals");
                    System.out.println(currentToken());
                    consume("=");
                    System.out.println("Db_after_consuming_equals");
                    System.out.println(currentToken());
                    parseE();
                    System.out.println("Db_after_parseE");
                    System.out.println(currentToken());
                    buildTree("function_form", n + 2);
                    System.out.println("Db_after_buildTree_function_form");
                } else {
                    throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": '=' expected");
                }
            }
        } else {
            throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier or '(' expected");
        }
    }

    private void parseVb() {
        System.out.println("Vb");
        System.out.println(currentToken());

        String value1 = currentToken().getValue();

        // Vb -> <IDENTIFIER>
        if (currentToken().getType().equals("<IDENTIFIER>")) {
            System.out.println("Vb_if_identifier");
            System.out.println(currentToken());
            consume(value1);
            System.out.println("Vb_after_consuming_identifier");
            System.out.println(currentToken());
            buildTree("<ID:" + value1 + ">", 0);
            System.out.println("Vb_after_buildTree_identifier");
        }
        // Vb -> '(' Vl ')'
        else if (value1.equals("(")) {
            System.out.println("Vb_if_open_parenthesis");
            System.out.println(currentToken());
            consume("(");
            System.out.println("Vb_after_consuming_open_parenthesis");
            System.out.println(currentToken());

            String value2 = currentToken().getValue();

            // Vb -> '(' ')'
            if (value2.equals(")")) {
                System.out.println("Vb_if_closing_parenthesis");
                System.out.println(currentToken());
                consume(")");
                System.out.println("Vb_after_consuming_closing_parenthesis");
                System.out.println(currentToken());
                buildTree("()", 0);
                System.out.println("Vb_after_buildTree_empty_parenthesis");
            }
            // Vb -> '(' Vl ')'
            else if (currentToken().getType().equals("<IDENTIFIER>")) {
                System.out.println("Vb_if_identifier_in_parenthesis");
                System.out.println(currentToken());
                consume(value2);
                System.out.println("Vb_after_consuming_identifier_in_parenthesis");
                System.out.println(currentToken());
                buildTree("<ID:" + value2 + ">", 0);
                System.out.println("Vb_after_buildTree_identifier_in_parenthesis");
                parseVl();
                System.out.println("Vb_after_parseVl");
                System.out.println(currentToken());

                if (currentToken().getValue().equals(")")) {
                    System.out.println("Vb_if_closing_parenthesis_after_Vl");
                    System.out.println(currentToken());
                    consume(")");
                    System.out.println("Vb_after_consuming_closing_parenthesis_after_Vl");
                    System.out.println(currentToken());
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
        System.out.println("Vl");
        System.out.println(currentToken());

        int n = 0;

        // Vl -> <IDENTIFIER> (',' <IDENTIFIER>)*
        while (currentToken().getValue().equals(",")) {
            System.out.println("Vl_while");
            System.out.println(currentToken());
            consume(",");
            System.out.println("Vl_after_consuming_comma");
            System.out.println(currentToken());

            if (currentToken().getType().equals("<IDENTIFIER>")) {
                System.out.println("Vl_if_identifier");
                System.out.println(currentToken());
                String value = currentToken().getValue();
                consume(value);
                System.out.println("Vl_after_consuming_identifier");
                System.out.println(currentToken());
                buildTree("<ID:" + value + ">", 0);
                System.out.println("Vl_after_buildTree_identifier");
                n++;
            } else {
                throw new RuntimeException("Syntax error in line " + currentToken().getLine() + ": Identifier expected");
            }
        }

        if (n > 0) {
            System.out.println("Vl_if_n_greater_than_0");
            System.out.println(currentToken());
            buildTree(",", n + 1);
            System.out.println("Vl_after_buildTree_comma");
        }
    }

}