package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import pl.jakubraban.desist.AccountManager;
import pl.jakubraban.desist.DesistSessionSpec;
import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.exceptions.UserAlreadyExistsException;

import java.io.Console;

@Command(name = "setup", mixinStandardHelpOptions = true, description = "Sets up a new Desist local account")
public class SetupCommand implements Runnable {

    @Parameters(index = "0", description = "Username of your choice")
    private String username;

    @Option(names = {"-p", "--password"}, interactive = true, description = "Password for your new account", arity = "0..1")
    private char[] password;

    private DesistSessionSpec sessionSpec;
    private UserDao users;
    private Console console;

    public SetupCommand(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
        this.users = new UserDao();
        this.console = System.console();
    }

    @Override
    public void run() {
        if (password == null) {
            System.out.print("Password (hidden): ");
            password = console.readPassword();
        }
        try {
            AccountManager.createAccount(username, new String(password));
            System.out.println("Your account is set up. Use login command to start using Desist");
            System.out.println("Username: " + username);
            System.out.println("Password: ***");
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            System.out.println("Account was not created");
            System.out.println(e.getMessage());
        }
    }
}
