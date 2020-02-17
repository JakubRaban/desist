package pl.jakubraban.desist.dao;

import org.hibernate.Session;
import pl.jakubraban.desist.hibernate.SessionService;
import pl.jakubraban.desist.model.User;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Optional;

public class UserDao extends GenericDao<User> {

    public Optional<User> findByUsername(String username) {
        Session session = SessionService.getSession();
        TypedQuery<User> findByUsernameQuery = session.createQuery(
                "from User u where u.username = :uname",
                User.class
        );
        findByUsernameQuery.setParameter("uname", username);
        try {
            return Optional.of(findByUsernameQuery.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
