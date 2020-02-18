package pl.jakubraban.desist;

import pl.jakubraban.desist.cli.commands.*;
import pl.jakubraban.desist.model.User;

import java.util.HashMap;
import java.util.Map;

public class DesistSessionSpec {

    private Map<String, Class<?>> commands;
    private User loggedUser;

    public DesistSessionSpec() {
        this.commands = new HashMap<>() {{
            put("setup", SetupCommand.class);
            put("login", LoginCommand.class);
            put("logout", LogoutCommand.class);
            put("lock", LockCommand.class);
            put("exit", ExitCommand.class);
        }};
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void logout() {
        if (getLoggedUser() == null) throw new IllegalStateException("No user is logged in.");
        setLoggedUser(null);
    }

    public boolean isUserLogged() {
        return getLoggedUser() != null;
    }

    public Class<?> getCommandClass(String command) {
        return this.commands.get(command);
    }
}
