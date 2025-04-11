package main;

import parser.Parser;
import parser.Node;
import standardizer.Standardizer;
import csemachine.CSEMachine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MyRPAL {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Wrong command. Make sure the command is in the following format: \njava MyRpal [-l] [-ast] [-st] [-exec] filename");
            System.exit(1);
        }

        String fileName = args[args.length - 1];
        List<String> switches = List.of(args).subList(0, args.length - 1);

        try {
            if (args.length == 1) {
                System.out.println("No switches provided. Please specify -l, -ast, -st, or -exec.");
                System.exit(1);
            } else {
                // Handle switches
                if (switches.contains("-l") || switches.contains("-ast") || switches.contains("-st") || switches.contains("-exec")) {
                    // If '-l' is in the switches, print the file as it is
                    if (switches.contains("-l")) {
                        printFileContent(fileName);
                        System.out.println();
                    }

                    // If '-ast' is in the switches, print the abstract syntax tree
                    if (switches.contains("-ast")) {
                        Node ast = Parser.parse(fileName);
                        System.out.println("Abstract Syntax Tree:");
                        Node.preorderTraversal(ast);
                        System.out.println();

                        // If '-st' is also in the switches, print the standardized tree
                        if (switches.contains("-st")) {
                            Node standardizedTree = Standardizer.standardize(fileName);
                            System.out.println("Standardized Tree:");
                            Node.preorderTraversal(standardizedTree);
                            System.out.println();
                        }

                        // If '-exec' is also in the switches, execute the program
                        if (switches.contains("-exec")) {
                            Node standardizedTree = Standardizer.standardize(fileName);
                            CSEMachine cseMachine = new CSEMachine();
                            System.out.println("Executing Program...");
                            cseMachine.execute(standardizedTree);
                            System.out.println("Execution Complete.");
                        }

                        System.exit(0);
                    }

                    // If '-st' is in the switches but not '-ast', print the standardized tree
                    if (switches.contains("-st") && !switches.contains("-ast")) {
                        Node standardizedTree = Standardizer.standardize(fileName);
                        System.out.println("Standardized Tree:");
                        Node.preorderTraversal(standardizedTree);
                        System.out.println();

                        // If '-exec' is also in the switches, execute the program
                        if (switches.contains("-exec")) {
                            CSEMachine cseMachine = new CSEMachine();
                            System.out.println("Executing Program...");
                            cseMachine.execute(standardizedTree);
                            System.out.println("Execution Complete.");
                        }

                        System.exit(0);
                    }

                    // If '-exec' is in the switches but not '-ast' or '-st', execute the program
                    if (switches.contains("-exec") && !switches.contains("-ast") && !switches.contains("-st")) {
                        Node standardizedTree = Standardizer.standardize(fileName);
                        CSEMachine cseMachine = new CSEMachine();
                        System.out.println("Executing Program...");
                        cseMachine.execute(standardizedTree);
                        System.out.println("Execution Complete.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("Wrong command. Make sure the command is in the following format: \njava MyRpal [-l] [-ast] [-st] [-exec] filename");
                    System.exit(1);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            System.exit(1);
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printFileContent(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        for (String line : lines) {
            System.out.println(line);
        }
    }
}