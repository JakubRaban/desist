package pl.jakubraban.desist.model;

import pl.jakubraban.desist.encryption.AES;
import pl.jakubraban.desist.exceptions.LockException;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static pl.jakubraban.desist.model.LockStatus.*;

@Entity
@Table(name = "LOCKS")
public class Lock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LOCK_ID")
    private int lockId;
    @ManyToOne
    @Column(name = "OWNER_USER_ID")
    private User lockOwner;
    @Column(name = "SERVICE_NAME", nullable = false)
    private String lockedServiceName;
    @Column(name = "LOCKED_PASSWORD", nullable = false)
    private String encryptedLockedPassword;
    @Column(name = "DATE_CREATED", nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;
    @Column(name = "LOCK_STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private LockStatus lockStatus;
    @Column(name = "REMOVED", nullable = false)
    private boolean isRemoved;

    public Lock() { }

    public Lock(User lockingUser, String lockedServiceName, String plainTextLockedPassword, Duration duration) {
        this.lockOwner = lockingUser;
        this.lockedServiceName = lockedServiceName;
        this.dateCreated = LocalDateTime.now();
        this.expiryDate = this.dateCreated.plus(duration);
        this.lockStatus = CREATED;
        this.isRemoved = false;
        this.encryptedLockedPassword = new AES(getEncryptionKey()).encrypt(plainTextLockedPassword);
    }

    public String getDecryptedPassword() {
        if (LocalDateTime.now().isBefore(expiryDate)) throw new LockException("This lock is active. It will expire on " + getFormattedDate(expiryDate));
        this.lockStatus = OPENED;
        return new AES(getEncryptionKey()).decrypt(this.encryptedLockedPassword);
    }

    public void activate() {
        if (this.lockStatus == ACTIVE) throw new LockException("This lock is already active");
        if (this.lockStatus == OPENED) throw new LockException("This lock is already opened; create new lock for this service to store another password");
        this.lockStatus = ACTIVE;
    }

    public void remove() {
        if (this.lockStatus == ACTIVE) throw new LockException("Attempted to remove active lock");
        this.isRemoved = true;
    }

    public void forceRemove() {
        this.isRemoved = true;
    }

    private String getEncryptionKey() {
        return lockedServiceName.toUpperCase() + "#" +
                lockOwner.getUsername() + "@" +
                dateCreated.minusMonths(expiryDate.getDayOfYear()).toString().substring(0, 19) + "4$eG6a)";
    }

    private String getFormattedDate(LocalDateTime date) {
        var datePattern = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        return date.format(datePattern);
    }

}
