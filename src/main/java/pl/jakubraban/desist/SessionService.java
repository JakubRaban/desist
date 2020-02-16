package pl.jakubraban.desist;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionService {
    private static final SessionFactory sessionFactory = new Configuration()
            .addAnnotatedClass(pl.jakubraban.desist.model.User.class)
            .configure()
            .buildSessionFactory();

    private static Session session;

    public static void openSession() {
        session = sessionFactory.openSession();
    }

    public static Session getSession() {
        if (session == null) openSession();
        return session;
    }

    public static void closeSession() {
        session.close();
    }
}