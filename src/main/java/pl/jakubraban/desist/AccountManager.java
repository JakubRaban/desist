package pl.jakubraban.desist;

import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.exceptions.UserAlreadyExistsException;
import pl.jakubraban.desist.exceptions.UserNotFoundException;
import pl.jakubraban.desist.model.User;

import java.util.Optional;

public class AccountManager {

    private static UserDao users = new UserDao();

    public static void createAccount(String username, String plainTextPassword) {
        if (users.findByUsername(username).isEmpty()) {
            User newUser = new User(username, plainTextPassword);
            users.create(newUser);
        } else
            throw new UserAlreadyExistsException();
    }

}
