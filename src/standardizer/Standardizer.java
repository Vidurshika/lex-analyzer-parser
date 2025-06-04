package standardizer;

import parser.Node;
import parser.Parser;
import java.io.IOException;

public class Standardizer {

    public static Node standardize(String fileName) throws IOException {
        Node ast = Parser.parse(fileName);
        return makeStandardizedTree(ast);
    }

    public static Node makeStandardizedTree(Node root) {
        // Recursively standardize each child first
        for (Node child : root.getChildren()) {
            makeStandardizedTree(child);
        }

        // Apply transformations with debug print statements
        if (root.getValue().equals("let") && root.getChildren().get(0).getValue().equals("=")) {

            Node child0 = root.getChildren().get(0);
            Node child1 = root.getChildren().get(1);

            root.getChildren().set(1, child0.getChildren().get(1));
            child0.getChildren().set(1, child1);
            child0.setValue("lambda");
            root.setValue("gamma");

        } else if (root.getValue().equals("where") && root.getChildren().get(1).getValue().equals("=")) {

            Node child0 = root.getChildren().get(0);
            Node child1 = root.getChildren().get(1);

            root.getChildren().set(0, child1.getChildren().get(1));
            child1.getChildren().set(1, child0);
            child1.setValue("lambda");
            // Swap the two children
            Node temp = root.getChildren().get(0);
            root.getChildren().set(0, child1); // lambda node
            root.getChildren().set(1, temp);   // E
            root.setValue("gamma");

        } else if (root.getValue().equals("function_form")) {

            int numChildren = root.getChildren().size(); // Store size before modifying the tree

            // Remove and store the last child as expression E
            Node expression = root.getChildren().remove(root.getChildren().size() - 1);

            Node currentNode = root;

            if (numChildren == 3) {

                // Remove and wrap the single variable in a lambda
                Node lambdaNode = new Node("lambda");
                Node variable = root.getChildren().remove(1); // Second child is variable (V)
                lambdaNode.addChild(variable);
                lambdaNode.addChild(expression);

                // Add lambda to root and set '='
                root.addChild(lambdaNode);
                root.setValue("=");
            } else {

                // Handle multiple variables
                while (root.getChildren().size() > 2) {
                    Node lambdaNode = new Node("lambda");

                    // Remove the second element (variable) and attach to lambda
                    Node variable = root.getChildren().remove(1);
                    lambdaNode.addChild(variable);

                    // Attach lambda to currentNode
                    currentNode.addChild(lambdaNode);
                    currentNode = lambdaNode;
                }

                // Attach the final expression to the innermost lambda
                currentNode.addChild(expression);
                root.setValue("=");
            }

        }

        else if (root.getValue().equals("gamma") && root.getChildren().size() > 2) {
            Node expression = root.getChildren().remove(root.getChildren().size() - 1);

            Node currentNode = root;
            for (int i = 1; i < root.getChildren().size(); i++) {
                Node lambdaNode = new Node("lambda");
                Node child = root.getChildren().remove(1);
                lambdaNode.addChild(child);
                currentNode.addChild(lambdaNode);
                currentNode = lambdaNode;
            }

            currentNode.addChild(expression);

        } else if (root.getValue().equals("within") &&
                root.getChildren().get(0).getValue().equals("=") &&
                root.getChildren().get(1).getValue().equals("=")) {


            Node child0 = root.getChildren().get(1).getChildren().get(0);
            Node gammaNode = new Node("gamma");

            Node lambdaNode = new Node("lambda");
            lambdaNode.addChild(root.getChildren().get(0).getChildren().get(0));
            lambdaNode.addChild(root.getChildren().get(1).getChildren().get(1));

            gammaNode.addChild(lambdaNode);
            gammaNode.addChild(root.getChildren().get(0).getChildren().get(1));

            root.getChildren().set(0, child0);
            root.getChildren().set(1, gammaNode);
            root.setValue("=");

        } else if (root.getValue().equals("@")) {
            Node expression = root.getChildren().remove(0);
            Node identifier = root.getChildren().get(0);

            Node gammaNode = new Node("gamma");
            gammaNode.addChild(identifier);
            gammaNode.addChild(expression);

            root.getChildren().set(0, gammaNode);
            root.setValue("gamma");

        } else if (root.getValue().equals("and")) {

            Node commaNode = new Node(",");
            Node tauNode = new Node("tau");

            for (Node child : root.getChildren()) {
                commaNode.addChild(child.getChildren().get(0));
                tauNode.addChild(child.getChildren().get(1));
            }

            root.getChildren().clear();
            root.addChild(commaNode);
            root.addChild(tauNode);
            root.setValue("=");

        } else if (root.getValue().equals("rec")) {

            Node temp = root.getChildren().remove(0);
            temp.setValue("lambda");

            Node gammaNode = new Node("gamma");
            gammaNode.addChild(new Node("<Y*>"));
            gammaNode.addChild(temp);

            root.addChild(temp.getChildren().get(0));
            root.addChild(gammaNode);
            root.setValue("=");

        }

        return root;
    }

    private static void printTree(Node root) {
        printTreeHelper(root, 0);
        System.out.println(); // Extra space between trees
    }

    private static void printTreeHelper(Node node, int level) {
        System.out.println(".".repeat(level) + node.getValue());
        for (Node child : node.getChildren()) {
            printTreeHelper(child, level + 1);
        }
    }
}
