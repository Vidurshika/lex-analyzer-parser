package parser;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String value;
    private List<Node> children;
    private int level;

    // Constructor
    public Node(String value) {
        this.value = value;
        this.children = new ArrayList<>();
        this.level = 0;
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // Add a child to the node
    public void addChild(Node child) {
        this.children.add(child);
    }

    // Preorder traversal of the tree
    public static void preorderTraversal(Node root) {
        if (root == null) {
            return;
        }

        // Print the current node with indentation based on its level
        System.out.println(".".repeat(root.getLevel()) + root.getValue());

        // Traverse each child node
        for (Node child : root.getChildren()) {
            child.setLevel(root.getLevel() + 1);
            preorderTraversal(child);
        }
    }

    @Override
    public String toString() {
        return "Node(value=" + value + ")";
    }
}