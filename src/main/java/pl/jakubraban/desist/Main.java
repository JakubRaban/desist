package pl.jakubraban.desist;

import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.jakubraban.desist.dao.UserDao;
import pl.jakubraban.desist.model.Lock;
import pl.jakubraban.desist.model.User;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String ... args) {
        UserDao users = new UserDao();
        Lock lock = new Lock(new User("jakub", "jakubr"), "facebook", "Aleksander1998", Duration.ofDays(5000));
        lock.activate();
        System.out.println(lock.getDecryptedPassword());
        SessionService.closeSession();
    }

}
