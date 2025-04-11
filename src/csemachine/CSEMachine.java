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

    private static final List<String> builtInFunctions = Arrays.asList(
            "Order", "Print", "print", "Conc", "Stern", "Stem", "Isinteger", "Istruthvalue",
            "Isstring", "Istuple", "Isfunction", "ItoS"
    );

    public CSEMachine() {
        environments.add(new Environment(0, null)); // Root environment
    }

    // Main method to execute the CSE Machine
    public void execute(Node root) {
        System.out.println("Starting CSE Machine Execution...");
        generateControlStructure(root, 0);

        List<Object> control = new ArrayList<>();
        control.add(environments.get(0).getName());
        control.addAll(controlStructures.get(0));
        stack.push(environments.get(0).getName());

        System.out.println("Initial Control Stack: " + control);
        System.out.println("Initial Execution Stack: " + stack);

        applyRules(control);

        if (printPresent) {
            System.out.println("Final Result: " + stack.peek());
        }
    }

    // Generate control structures recursively
    private void generateControlStructure(Node root, int i) {
        System.out.println("Generating control structure for root: " + root.getValue() + ", index: " + i);

        while (controlStructures.size() <= i) {
            System.out.println("Extending controlStructures to index " + i);
            controlStructures.add(new ArrayList<>());
        }

        if (root.getValue().equals("lambda")) {
            System.out.println("Processing lambda node: " + root.getValue());
            count++;
            Node leftChild = root.getChildren().get(0);
            Lambda lambda = new Lambda(count);

            if (leftChild.getValue().equals(",")) {
                StringBuilder boundedVariables = new StringBuilder();
                for (Node child : leftChild.getChildren()) {
                    boundedVariables.append(child.getValue().substring(4, child.getValue().length() - 1)).append(",");
                }
                lambda.setBoundedVariable(boundedVariables.substring(0, boundedVariables.length() - 1));
                System.out.println("Lambda bounded variables: " + lambda.getBoundedVariable());
            } else {
                lambda.setBoundedVariable(leftChild.getValue().substring(4, leftChild.getValue().length() - 1));
                System.out.println("Lambda bounded variable: " + lambda.getBoundedVariable());
            }

            controlStructures.get(i).add(lambda);

            for (int j = 1; j < root.getChildren().size(); j++) {
                generateControlStructure(root.getChildren().get(j), count);
            }
        } else if (root.getValue().equals("->")) {
            System.out.println("Processing conditional node: " + root.getValue());
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
            System.out.println("Processing tau node: " + root.getValue());
            Tau tau = new Tau(root.getChildren().size());
            controlStructures.get(i).add(tau);
            for (Node child : root.getChildren()) {
                generateControlStructure(child, i);
            }
        } else {
            System.out.println("Processing default node: " + root.getValue());
            controlStructures.get(i).add(root.getValue());
            for (Node child : root.getChildren()) {
                generateControlStructure(child, i);
            }
        }
    }

    private void applyRules(List<Object> control) {
        List<String> op = Arrays.asList("+", "-", "*", "/", "**", "gr", "ge", "ls", "le", "eq", "ne", "or", "&", "aug");
        List<String> uop = Arrays.asList("neg", "not");

        while (!control.isEmpty()) {
            System.out.println("Control Stack: " + control);
            System.out.println("Execution Stack: " + stack);

            Object symbol = control.remove(control.size() - 1);
            System.out.println("Processing symbol: " + symbol);

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
                System.out.println("Gamma operation with: " + stackSymbol1 + ", " + stackSymbol2);

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
                System.out.println("huttooooooooooooooooooooooooooooo");
                System.out.println(stack);
                Object rand1 = stack.pop();
                System.out.println(rand1);
                Object rand2 = stack.pop();
                System.out.println("Applying operator " + symbol + " on " + rand1 + " and " + rand2);

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
                    case "**":
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
                        if (rand2 instanceof List) {
                            List<Object> augmented = new ArrayList<>((List<?>) rand2);
                            augmented.add(rand1);
                            stack.push(augmented);
                        } else {
                            stack.push(Arrays.asList(rand2, rand1));
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + (String) symbol);
                }
            } else if (uop.contains(symbol)) {
                Object rand = stack.pop();
                System.out.println("Applying unary operator " + symbol + " on " + rand);

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
                System.out.println("Beta operation with condition: " + condition);

                if (condition) {
                    control.addAll(controlStructures.get(((Delta) thenPart).getNumber()));
                } else {
                    control.addAll(controlStructures.get(((Delta) elsePart).getNumber()));
                }
            } else if (symbol instanceof Tau) {
                Tau tau = (Tau) symbol;
                int n = tau.getNumber();
                List<Object> tauList = new ArrayList<>();
                System.out.println("Processing Tau with number: " + n);

                for (int i = 0; i < n; i++) {
                    Object value = stack.pop();
                    System.out.println("Popped value from stack: " + value);
                    tauList.add(value);
                }

                //Collections.reverse(tauList); // Reverse the list to maintain the correct order
                //System.out.println("Reversed list for tuple: " + tauList);

                List<Object> tauTuple = new ArrayList<>(tauList); // Representing tuple as a list in Java
                System.out.println("Created tuple: " + tauTuple);

                stack.push(tauTuple);
                System.out.println("Pushed tuple onto stack: " + tauTuple);

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

    // Lookup function for tokens
    private Object lookup(String name) {
        System.out.println("Looking up name: " + name);
        name = name.substring(1, name.length() - 1);
        String[] info = name.split(":");

        if (info.length == 1) {
            return info[0];
        } else {
            String dataType = info[0];
            String value = info[1];

            switch (dataType) {
                case "INT":
                    return Integer.parseInt(value);
                case "STR":
                    return value.replace("'", "");
                case "ID":
                    System.out.println("Kariyoooooooooooooooooooooooooo");
                    System.out.println(value);
                    if (builtInFunctions.contains(value)) {
                        return value;
                    } else {
                        Environment env = environments.get(currentEnvironment);
                        if (env.getVariables().containsKey(value)) {
                            return env.getVariables().get(value);
                        } else {
                            throw new RuntimeException("Undeclared Identifier: " + value);
                        }
                    }
                default:
                    throw new RuntimeException("Unknown data type: " + dataType);
            }
        }
    }

    // Built-in functions
    private void builtIn(String function, Object argument, List<Object> control) {
        System.out.println("Executing built-in function: " + function + " with argument: " + argument);
        switch (function) {
            case "Order":
                stack.push(((List<?>) argument).size());
                break;
            case "Print":
            case "print":
                printPresent = true;
                stack.push(argument);
                break;
            case "Conc":
                System.out.println("Concccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc");
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
                stack.push(argument instanceof List);
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