package pl.jakubraban.desist;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.model.User;

import java.util.Optional;

public class Main {

    public static void main(String ... args) {
        UserDao users = new UserDao();
//        var user = new User("szmaragdowooki", "Szmaragdowooki1998!");
//        users.save(user);
        Optional<User> user = users.findByUsername("szmaragdowooki");
        user.ifPresent(value -> System.out.println(value.validatePassword("Szmaragdowooki1998")));
        SessionService.closeSession();
    }

}
