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
            System.out.println("Applying LET transformation");
            Node child0 = root.getChildren().get(0);
            Node child1 = root.getChildren().get(1);

            root.getChildren().set(1, child0.getChildren().get(1));
            child0.getChildren().set(1, child1);
            child0.setValue("lambda");
            root.setValue("gamma");

            printTree(root);
        } else if (root.getValue().equals("where") && root.getChildren().get(1).getValue().equals("=")) {
            System.out.println("Applying WHERE transformation");
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

            printTree(root);
        } else if (root.getValue().equals("function_form")) {
            System.out.println("Applying FUNCTION_FORM transformation");
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
            root.setValue("=");

            printTree(root);
        } else if (root.getValue().equals("gamma") && root.getChildren().size() > 2) {
            System.out.println("Applying MULTI-GAMMA transformation");
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

            printTree(root);
        } else if (root.getValue().equals("within") &&
                root.getChildren().get(0).getValue().equals("=") &&
                root.getChildren().get(1).getValue().equals("=")) {
            System.out.println("Applying WITHIN transformation");

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

            printTree(root);
        } else if (root.getValue().equals("@")) {
            System.out.println("Applying AT transformation");

            Node expression = root.getChildren().remove(0);
            Node identifier = root.getChildren().get(0);

            Node gammaNode = new Node("gamma");
            gammaNode.addChild(identifier);
            gammaNode.addChild(expression);

            root.getChildren().set(0, gammaNode);
            root.setValue("gamma");

            printTree(root);
        } else if (root.getValue().equals("and")) {
            System.out.println("Applying AND transformation");

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

            printTree(root);
        } else if (root.getValue().equals("rec")) {
            System.out.println("Applying REC transformation");

            Node temp = root.getChildren().remove(0);
            temp.setValue("lambda");

            Node gammaNode = new Node("gamma");
            gammaNode.addChild(new Node("<Y*>"));
            gammaNode.addChild(temp);

            root.addChild(temp.getChildren().get(0));
            root.addChild(gammaNode);
            root.setValue("=");

            printTree(root);
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
