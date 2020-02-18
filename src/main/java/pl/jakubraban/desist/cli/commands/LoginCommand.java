package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import pl.jakubraban.desist.DesistSessionSpec;
import pl.jakubraban.desist.LoginAttempt;
import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.exceptions.UserNotFoundException;

import javax.security.auth.login.LoginException;
import java.io.Console;

@Command(name = "login")
public class LoginCommand implements Runnable {

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
    public void run() {

        try {
            if (sessionSpec.isUserLogged()) throw new LoginException("Log out first to log in as another user");
            LoginAttempt loginAttempt = new LoginAttempt(sessionSpec, username);
            if (password == null) {
                System.out.print("Password (hidden): ");
                password = console.readPassword();
            }
            loginAttempt.providePassword(new String(password));
            System.out.println("You're now logged in.");
        } catch (LoginException | UserNotFoundException e) {
            System.out.println("Login failed");
            System.out.println(e.getMessage());
        }

    }

}
