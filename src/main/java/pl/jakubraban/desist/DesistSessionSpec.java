package pl.jakubraban.desist;

import pl.jakubraban.desist.cli.commands.ExitCommand;
import pl.jakubraban.desist.cli.commands.LoginCommand;
import pl.jakubraban.desist.cli.commands.SetupCommand;
import pl.jakubraban.desist.model.User;

import java.util.HashMap;
import java.util.Map;

public class DesistSessionSpec {

    private Map<String, Class<?>> commands;
    private User loggedUser;

    public DesistSessionSpec() {
        this.commands = new HashMap<>() {{
            put("login", LoginCommand.class);
            put("setup", SetupCommand.class);
            put("exit", ExitCommand.class);
        }};
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public void logout() {
        setLoggedUser(null);
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public Class<?> getCommandClass(String command) {
        return this.commands.get(command);
    }
}
