package csemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Environment {
    private String name; // Name of the environment (e.g., "e_0")
    private Map<String, Object> variables; // Variables in the current environment
    private List<Environment> children; // List of child environments
    private Environment parent; // Reference to the parent environment

    // Constructor
    public Environment(int number, Environment parent) {
        this.name = "e_" + number;
        this.variables = new HashMap<>();
        this.children = new ArrayList<>();
        this.parent = parent;
    }

    // Add a child environment to the current environment
    public void addChild(Environment child) {
        this.children.add(child);
        // Inherit variables from the parent environment
        child.variables.putAll(this.variables);
    }

    // Add a variable to the current environment
    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    // Get the value of a variable, searching recursively in parent environments
    public Object getVariable(String key) {
        if (this.variables.containsKey(key)) {
            return this.variables.get(key);
        } else if (this.parent != null) {
            return this.parent.getVariable(key);
        } else {
            throw new RuntimeException("Undeclared Identifier: " + key);
        }
    }

    // Get the name of the environment
    public String getName() {
        return this.name;
    }

    // Get the variables in the current environment
    public Map<String, Object> getVariables() {
        return this.variables;
    }

    // Get the parent environment
    public Environment getParent() {
        return this.parent;
    }

    // Get the list of child environments
    public List<Environment> getChildren() {
        return this.children;
    }

    @Override
    public String toString() {
        return "Environment{name='" + name + "', variables=" + variables + "}";
    }
}