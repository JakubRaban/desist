package pl.jakubraban.desist;

import pl.jakubraban.desist.dao.LockDao;
import pl.jakubraban.desist.exceptions.LockException;
import pl.jakubraban.desist.model.Lock;

import javax.security.auth.login.LoginException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class LockManager {

    private DesistSessionSpec sessionSpec;
    private LockDao locks;

    public LockManager(DesistSessionSpec sessionSpec) {
        this.sessionSpec = sessionSpec;
        this.locks = new LockDao();
    }

    public void createLock(String identifier, String plainTextPassword) throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Log in first to create a lock");
        Optional<Lock> existingLock = getRequestedLock(identifier);
        if (existingLock.isEmpty()) {
            Lock newLock = new Lock(sessionSpec.getLoggedUser(), identifier, plainTextPassword);
            locks.create(newLock);
        } else
            throw new LockException("Lock with this name already exists");
    }

    public Lock activateLock(String identifier, Duration duration) throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Login first to activate a lock");
        Optional<Lock> requestedLock = getRequestedLock(identifier);
        if (requestedLock.isPresent()) {
            Lock lock = requestedLock.get();
            lock.activate(duration);
            locks.save(lock);
            return lock;
        } else {
            throw new LockException("Lock with this name doesn't exist");
        }
    }

    public String openLock(String identifier) throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Login first to open a lock");
        Optional<Lock> requestedLock = getRequestedLock(identifier);
        if (requestedLock.isPresent()) {
            Lock lock = requestedLock.get();
            String password = lock.getDecryptedPassword();
            locks.save(lock);
            return password;
        } else
            throw new LockException("Lock with this name doesn't exist");
    }

    public Lock extendLock(String identifier, Duration duration) throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Login first to extend a lock");
        Optional<Lock> requestedLock = getRequestedLock(identifier);
        if (requestedLock.isPresent()) {
            Lock lock = requestedLock.get();
            lock.extend(duration);
            locks.save(lock);
            return lock;
        } else
            throw new LockException("Lock with this name doesn't exist");
    }

    public void removeLock(String identifier) throws LoginException {
        removeLock(identifier, LockRemovalMode.NORMAL);
    }

    public void forceRemoveLock(String identifier) throws LoginException {
        removeLock(identifier, LockRemovalMode.FORCE);
    }

    private void removeLock(String identifier, LockRemovalMode removalMode) throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Login first to remove a lock");
        Optional<Lock> requestedLock = getRequestedLock(identifier);
        requestedLock.ifPresentOrElse(lock -> {
            if (removalMode == LockRemovalMode.NORMAL) lock.remove();
            else if (removalMode == LockRemovalMode.FORCE) lock.forceRemove();
            locks.save(lock);
        }, () -> {
            throw new LockException("Lock with this name doesn't exist");
        });
    }

    public List<Lock> status() throws LoginException {
        if (userNotLoggedIn()) throw new LoginException("Login first to see locks");
        return locks.getLocksByUser(sessionSpec.getLoggedUser());
    }

    private Optional<Lock> getRequestedLock(String identifier) {
        return locks.getLockByIdentifierAndUser(identifier, sessionSpec.getLoggedUser());
    }

    private boolean userNotLoggedIn() {
        return !sessionSpec.isUserLogged();
    }

    private enum LockRemovalMode {
        NORMAL, FORCE
    }

}
