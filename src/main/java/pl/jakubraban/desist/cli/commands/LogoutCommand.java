package pl.jakubraban.desist.cli.commands;

import picocli.CommandLine.Command;
import pl.jakubraban.desist.DesistSessionSpec;

@Command(name = "logout", description = "Logout from Desist")
public class LogoutCommand implements Runnable {

    private DesistSessionSpec sessionSpec;

    public LogoutCommand(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
    }

    @Override
    public void run() {
        try {
            sessionSpec.logout();
            System.out.println("You're now logged out.");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
