package pl.jakubraban.desist;

import picocli.CommandLine;
import pl.jakubraban.desist.cli.CommandLineUtils;
import pl.jakubraban.desist.hibernate.SessionService;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static String newLine = System.lineSeparator();
    private static Scanner scanner = new Scanner(System.in);
    private static DesistSessionSpec sessionSpec = new DesistSessionSpec();

    public static void main(String ... args) {

        System.out.println("\nStarting Desist ...\n");
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        SessionService.openSession();
        CommandLineUtils.cls();
        printGreeting();

        while(true) {
            printPrompt();
            String requestedCommand = scanner.nextLine();
            processCommand(requestedCommand);
            System.out.println();
        }

    }

    private static void printGreeting() {
        System.out.println(newLine);
        System.out.println("Desist -- keep your passwords safe from yourself");
        System.out.println("(C) Jakub Raban 2020. All rights reserved");
        System.out.println("Type help to get more info.");
        System.out.println(newLine);
    }

    private static void printPrompt() {
        if(sessionSpec.getLoggedUser() != null) {
            System.out.print("desist(" + sessionSpec.getLoggedUser().getUsername() + ")> ");
        } else {
            System.out.print("desist> ");
        }
    }

    private static void processCommand(String command) {
        if (command.isBlank()) return;
        String[] splitCommand = command.strip().split(" ");
        String commandBase = splitCommand[0];
        String[] commandArgs = new String[0];
        if (splitCommand.length > 1) {
            commandArgs = Arrays.stream(splitCommand)
                    .skip(1)
                    .filter(str -> !str.isBlank())
                    .toArray(String[]::new);
        }
        Class<?> invokedClass = sessionSpec.getCommandClass(commandBase);
        try {
            if (invokedClass != null) {
                new CommandLine(invokedClass.getConstructor(DesistSessionSpec.class).newInstance(sessionSpec)).execute(commandArgs);
            } else {
                System.out.println("Unknown command. Type help for commands usage.");
            }
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
