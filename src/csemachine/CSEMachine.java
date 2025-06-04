package csemachine;

import parser.Node;
import structures.*;


import java.util.*;

public class CSEMachine {
    private List<List<Object>> controlStructures = new ArrayList<>();
    private Stack<Object> stack = new Stack<>();
    private List<Environment> environments = new ArrayList<>();
    private int currentEnvironment = 0;
    private int count = 0;
    private boolean printPresent = false;
    String finalResult = "";

    private static final List<String> builtInFunctions = Arrays.asList(
            "Order", "Print", "print", "Conc", "Stern", "Stem", "Isinteger", "Istruthvalue",
            "Isstring", "Istuple", "Isfunction", "ItoS"
    );

    public CSEMachine() {
        environments.add(new Environment(0, null)); // Root environment
    }

    // Main method to execute the CSE Machine
    public void execute(Node root) {

        generateControlStructure(root, 0);

        List<Object> control = new ArrayList<>();
        control.add(environments.get(0).getName());
        control.addAll(controlStructures.get(0));
        stack.push(environments.get(0).getName());


        applyRules(control);

        if (printPresent) {
            System.out.println(finalResult);
        }
    }

    // Generate control structures recursively
    private void generateControlStructure(Node root, int i) {

        while (controlStructures.size() <= i) {
            controlStructures.add(new ArrayList<>());
        }

        if (root.getValue().equals("lambda")) {
            count++;
            Node leftChild = root.getChildren().get(0);
            Lambda lambda = new Lambda(count);

            if (leftChild.getValue().equals(",")) {
                StringBuilder boundedVariables = new StringBuilder();
                for (Node child : leftChild.getChildren()) {
                    boundedVariables.append(child.getValue().substring(4, child.getValue().length() - 1)).append(",");
                }
                lambda.setBoundedVariable(boundedVariables.substring(0, boundedVariables.length() - 1));
            } else {
                lambda.setBoundedVariable(leftChild.getValue().substring(4, leftChild.getValue().length() - 1));
            }

            controlStructures.get(i).add(lambda);

            for (int j = 1; j < root.getChildren().size(); j++) {
                generateControlStructure(root.getChildren().get(j), count);
            }
        } else if (root.getValue().equals("->")) {
            count++;
            Delta delta1 = new Delta(count);
            controlStructures.get(i).add(delta1);
            generateControlStructure(root.getChildren().get(1), count);

            count++;
            Delta delta2 = new Delta(count);
            controlStructures.get(i).add(delta2);
            generateControlStructure(root.getChildren().get(2), count);

            controlStructures.get(i).add("beta");
            generateControlStructure(root.getChildren().get(0), i);
        } else if (root.getValue().equals("tau")) {
            Tau tau = new Tau(root.getChildren().size());
            controlStructures.get(i).add(tau);
            for (Node child : root.getChildren()) {
                generateControlStructure(child, i);
            }
        } else {
            controlStructures.get(i).add(root.getValue());
            for (Node child : root.getChildren()) {
                generateControlStructure(child, i);
            }
        }
    }

    private void applyRules(List<Object> control) {
        List<String> op = Arrays.asList("+", "-", "", "/", "*", "gr", "ge", "ls", "le", "eq", "ne", "or", "&", "aug");
        List<String> uop = Arrays.asList("neg", "not");

        while (!control.isEmpty()) {
            Object symbol = control.remove(control.size() - 1);

            if (symbol instanceof String && ((String) symbol).startsWith("<") && ((String) symbol).endsWith(">")) {
                stack.push(lookup((String) symbol));
            } else if (symbol instanceof Lambda) {
                Lambda lambda = (Lambda) symbol;
                Lambda temp = new Lambda(lambda.getNumber());
                temp.setBoundedVariable(lambda.getBoundedVariable());
                temp.setEnvironment(currentEnvironment);
                stack.push(temp);
            } else if ("gamma".equals(symbol)) {
                Object stackSymbol1 = stack.pop();
                Object stackSymbol2 = stack.pop();

                if (stackSymbol1 instanceof Lambda) {
                    Lambda lambda = (Lambda) stackSymbol1;
                    currentEnvironment = environments.size();

                    Environment parent = environments.get(lambda.getEnvironment());
                    Environment child = new Environment(currentEnvironment, parent);
                    parent.addChild(child);
                    environments.add(child);

                    String[] variableList = lambda.getBoundedVariable().split(",");
                    if (variableList.length > 1) {
                        for (int i = 0; i < variableList.length; i++) {
                            child.addVariable(variableList[i], ((List<?>) stackSymbol2).get(i));
                        }
                    } else {
                        child.addVariable(lambda.getBoundedVariable(), stackSymbol2);
                    }

                    stack.push(child.getName());
                    control.add(child.getName());
                    control.addAll(controlStructures.get(lambda.getNumber()));
                } else if (stackSymbol1 instanceof List) {
                    stack.push(((List<?>) stackSymbol1).get((int) stackSymbol2 - 1));
                } else if ("Y*".equals(stackSymbol1)) {
                    Eta temp = new Eta(((Lambda) stackSymbol2).getNumber());
                    temp.setBoundedVariable(((Lambda) stackSymbol2).getBoundedVariable());
                    temp.setEnvironment(((Lambda) stackSymbol2).getEnvironment());
                    stack.push(temp);
                } else if (stackSymbol1 instanceof Eta) {
                    Lambda temp = new Lambda(((Eta) stackSymbol1).getNumber());
                    temp.setBoundedVariable(((Eta) stackSymbol1).getBoundedVariable());
                    temp.setEnvironment(((Eta) stackSymbol1).getEnvironment());

                    control.add("gamma");
                    control.add("gamma");
                    stack.push(stackSymbol2);
                    stack.push(stackSymbol1);
                    stack.push(temp);
                } else if (stackSymbol1 instanceof String && builtInFunctions.contains(stackSymbol1)) {
                    builtIn((String) stackSymbol1, stackSymbol2,  control);
                }
            } else if (symbol instanceof String && ((String) symbol).startsWith("e_")) {
                Object stackSymbol = stack.pop();
                stack.pop();

                if (currentEnvironment != 0) {
                    for (int i = stack.size() - 1; i >= 0; i--) {
                        Object element = stack.get(i);
                        if (element instanceof String && ((String) element).startsWith("e_")) {
                            currentEnvironment = Integer.parseInt(((String) element).substring(2));
                            break;
                        }
                    }
                }
                stack.push(stackSymbol);
            } else if (op.contains(symbol)) {

                Object rand1 = stack.pop();
                Object rand2 = stack.pop();

                switch ((String) symbol) {
                    case "+":
                        stack.push((int) rand1 + (int) rand2);
                        break;
                    case "-":
                        stack.push((int) rand1 - (int) rand2);
                        break;
                    case "*":
                        stack.push((int) rand1 * (int) rand2);
                        break;
                    case "/":
                        stack.push((int) rand1 / (int) rand2);
                        break;
                    case "":
                        stack.push((int) Math.pow((int) rand1, (int) rand2));
                        break;
                    case "gr":
                        stack.push((int) rand1 > (int) rand2);
                        break;
                    case "ge":
                        stack.push((int) rand1 >= (int) rand2);
                        break;
                    case "ls":
                        stack.push((int) rand1 < (int) rand2);
                        break;
                    case "le":
                        stack.push((int) rand1 <= (int) rand2);
                        break;
                    case "eq":
                        stack.push(rand1.equals(rand2));
                        break;
                    case "ne":
                        stack.push(!rand1.equals(rand2));
                        break;
                    case "or":
                        stack.push((boolean) rand1 || (boolean) rand2);
                        break;
                    case "&":
                        stack.push((boolean) rand1 && (boolean) rand2);
                        break;
                    case "aug":

                        List<Object> newTuple = new ArrayList<>();

                        if (rand1 instanceof List) {
                            // rand1 is already a tuple → copy it
                            newTuple.addAll((List<?>) rand1);
                        } else {
                        }

                        // always add rand2 at the end
                        newTuple.add(rand2);

                        stack.push(newTuple);
                        break;


                    default:
                        throw new IllegalStateException("Unexpected value: " + (String) symbol);
                }
            } else if (uop.contains(symbol)) {
                Object rand = stack.pop();

                switch ((String) symbol) {
                    case "not":
                        stack.push(!(boolean) rand);
                        break;
                    case "neg":
                        stack.push(-(int) rand);
                        break;
                }
            } else if ("beta".equals(symbol)) {
                boolean condition = (boolean) stack.pop();
                Object elsePart = control.remove(control.size() - 1);
                Object thenPart = control.remove(control.size() - 1);

                if (condition) {
                    control.addAll(controlStructures.get(((Delta) thenPart).getNumber()));
                } else {
                    control.addAll(controlStructures.get(((Delta) elsePart).getNumber()));
                }
            } else if (symbol instanceof Tau) {
                Tau tau = (Tau) symbol;
                int n = tau.getNumber();
                List<Object> tauList = new ArrayList<>();

                for (int i = 0; i < n; i++) {
                    Object value = stack.pop();
                    tauList.add(value);
                }


                List<Object> tauTuple = new ArrayList<>(tauList); // Representing tuple as a list in Java
                stack.push(tauTuple);

            } else if ("Y*".equals(symbol)) {
                stack.push(symbol);
            }
        }

        if (stack.peek() instanceof Lambda) {
            Lambda lambda = (Lambda) stack.pop();
            stack.push("[lambda closure: " + lambda.getBoundedVariable() + ": " + lambda.getNumber() + "]");
        }

        if (stack.peek() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> tuple = (List<Object>) stack.pop();
            for (int i = 0; i < tuple.size(); i++) {
                if (tuple.get(i) instanceof Boolean) {
                    tuple.set(i, tuple.get(i).toString().toLowerCase());
                }
            }
            if (tuple.size() == 1) {
                stack.push("(" + tuple.get(0) + ")");
            } else {
                stack.push(tuple.toString());
            }
        }

        if (stack.peek() instanceof Boolean) {
            stack.push(stack.pop().toString().toLowerCase());
        }
    }

    public Object lookup(String name) {

        // Remove the enclosing angle brackets
        name = name.substring(1, name.length() - 1);

        // Split the name into type and value
        String[] info = name.split(":");
        String value;

        if (info.length == 1) {
            // If no type is specified, treat the entire name as the value
            value = info[0];
        } else {
            // Extract the data type and value
            String dataType = info[0];
            value = info[1];

            // Handle different data types
            switch (dataType) {
                case "INT":
                    return Integer.parseInt(value);

                case "STR":
                    return value.strip().replace("'", "");

                case "ID":
                    // Check if the value is a built-in function
                    if (builtInFunctions.contains(value)) {
                        return value;
                    } else {
                        // Look up the value in the current environment
                        try {
                            Object variableValue = environments.get(currentEnvironment).getVariable(value);
                            return variableValue;
                        } catch (Exception e) {
                            System.exit(1);
                        }
                    }
                    break;

                default:
                    System.exit(1);
            }
        }

        // Handle special cases for specific values
        switch (value) {
            case "Y*":
                return "Y*";

            case "nil":
                return "[]"; // Representing an empty tuple

            case "true":
                return true;

            case "false":
                return false;

            default:
                return value;
        }
    }

    // Built-in functions
    private void builtIn(String function, Object argument, List<Object> control) {
        switch (function) {
            case "Order":

                if (argument instanceof List<?>) {
                    int size = ((List<?>) argument).size();
                    stack.push(size);
                } else if (argument instanceof String) {
                    int length = ((String) argument).length();
                    int reSizedLength = length - 2;
                    stack.push(reSizedLength);
                } else {
                    throw new IllegalArgumentException("Order function expects a List or String argument, got: " + argument.getClass());
                }
                break;


            case "Print":
            case "print":
                // Handle printing functionality
                printPresent = true;
                if (argument instanceof String) {
                    String argStr = (String) argument;

                    // Replace escape sequences
                    if (argStr.contains("\\n")) {
                        argStr = argStr.replace("\\n", "\n");
                    }
                    if (argStr.contains("\\t")) {
                        argStr = argStr.replace("\\t", "\t");
                    }

                    // Push the processed string onto the stack
                    stack.push(argStr);
                    finalResult = finalResult + argStr;
                } else {
                    // Push the argument as-is onto the stack
                    stack.push(argument);
                    finalResult = finalResult + argument;
                }
                break;
            case "Conc":
                String str1 = (String) stack.pop();
                stack.push(argument+str1);
                Object elsePart = control.remove(control.size() - 1);
                break;
            case "Stern":
                stack.push(((String) argument).substring(1));
                break;
            case "Stem":
                stack.push(((String) argument).substring(0, 1));
                break;
            case "Isinteger":
                stack.push(argument instanceof Integer);
                break;
            case "Istruthvalue":
                stack.push(argument instanceof Boolean);
                break;
            case "Isstring":
                stack.push(argument instanceof String);
                break;
            case "Istuple":

                boolean isTuple = false;

                if (argument instanceof List) {
                    // If it's a Java List, treat it as a tuple
                    isTuple = true;
                } else if (argument instanceof String) {
                    // If it's a string that looks like a bracketed list
                    String str = ((String) argument).trim();
                    if (str.matches("^\\[.*\\]$")) {
                        isTuple = true;
                    }
                }

                stack.push(isTuple);
                break;

            case "Isfunction":
                stack.push(builtInFunctions.contains(argument));
                break;
            case "ItoS":
                if (argument instanceof Integer) {
                    stack.push(argument.toString());
                } else {
                    throw new RuntimeException("ItoS function can only accept integers.");
                }
                break;
            default:
                throw new RuntimeException("Unknown built-in function: " + function);
        }
    }
}