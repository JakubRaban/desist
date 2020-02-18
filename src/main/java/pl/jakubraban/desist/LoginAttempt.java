package pl.jakubraban.desist;

import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.exceptions.UserNotFoundException;
import pl.jakubraban.desist.model.User;

import javax.security.auth.login.LoginException;
import java.util.Optional;

public class LoginAttempt {

    private User loggingUser;
    private DesistSessionSpec sessionSpec;

    public LoginAttempt(DesistSessionSpec sessionSpec, String username) {
        UserDao users = new UserDao();
        this.sessionSpec = sessionSpec;
        Optional<User> optionalUser = users.findByUsername(username);
        optionalUser.ifPresentOrElse(user -> this.loggingUser = user,
                () -> {
                    throw new UserNotFoundException();
                });
    }

    public void providePassword(String plainTextPassword) throws LoginException {
        if (loggingUser.validatePassword(plainTextPassword)) {
            sessionSpec.setLoggedUser(loggingUser);
        } else
            throw new LoginException("Specified password is not correct");
    }

}
