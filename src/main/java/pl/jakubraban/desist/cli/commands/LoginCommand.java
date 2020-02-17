package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.*;
import pl.jakubraban.desist.DesistSessionSpec;
import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.model.User;

import java.io.Console;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "login")
public class LoginCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Your username")
    private String username;

    @Option(names = {"-p", "--password"}, interactive = true, description = "Your password", arity = "0..1")
    private char[] password;

    private DesistSessionSpec sessionSpec;
    private UserDao users;
    private Console console;

    public LoginCommand(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
        this.users = new UserDao();
        this.console = System.console();
    }

    @Override
    public Integer call() {
        Optional<User> optionalUser = users.findByUsername(username);
        if (optionalUser.isEmpty()) {
            System.out.println("The specified username was not found. Create an account using setup command first.");
            return 1;
        }
        if (password == null) {
            System.out.print("Password (hidden): ");
            password = console.readPassword();
        }
        User loggingUser = optionalUser.get();
        if (loggingUser.validatePassword(new String(password))) {
            sessionSpec.setLoggedUser(loggingUser);
            System.out.println("You're now logged in.");
            return 0;
        } else {
            System.out.println("Specified password is not correct. Please try again.");
            return 1;
        }
    }

}
