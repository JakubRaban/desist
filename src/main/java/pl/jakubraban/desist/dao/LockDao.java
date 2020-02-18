package pl.jakubraban.desist.dao;

import org.hibernate.Session;
import pl.jakubraban.desist.hibernate.SessionService;
import pl.jakubraban.desist.model.Lock;
import pl.jakubraban.desist.model.User;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LockDao extends GenericDao<Lock> {

    public List<Lock> getLocksByUser(User user) {
        Session session = SessionService.getSession();
        TypedQuery<Lock> getLocksByUserQuery = session.createQuery(
                "from Lock l where l.lockOwner = :user and l.isRemoved = false", Lock.class
        );
        getLocksByUserQuery.setParameter("user", user);
        return getLocksByUserQuery.getResultList();
    }

    public Optional<Lock> getLockByIdentifierAndUser(String lockIdentifier, User user) {
        List<Lock> userLocks = getLocksByUser(user);
        List<Lock> foundLocks = userLocks.stream()
                .filter(lock -> lock.getLockIdentifier().equalsIgnoreCase(lockIdentifier))
                .collect(Collectors.toList());
        if (foundLocks.size() == 0) return Optional.empty();
        else return Optional.of(foundLocks.get(0));
    }

}
