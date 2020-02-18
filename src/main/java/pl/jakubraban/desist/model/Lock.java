package pl.jakubraban.desist.model;

import pl.jakubraban.desist.encryption.AES;
import pl.jakubraban.desist.exceptions.LockException;
import pl.jakubraban.desist.exceptions.LockRemovalException;

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
    private User lockOwner;
    @Column(name = "LOCK_NAME", nullable = false)
    private String lockIdentifier;
    @Column(name = "LOCKED_PASSWORD", nullable = false)
    private String encryptedLockedPassword;
    @Column(name = "DATE_CREATED", nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = "ACTIVATION_DATE")
    private LocalDateTime activationDate;
    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;
    @Column(name = "LOCK_STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private LockStatus lockStatus;
    @Column(name = "REMOVED", nullable = false)
    private boolean isRemoved;

    public Lock() { }

    public Lock(User lockingUser, String lockIdentifier, String plainTextLockedPassword) {
        this.lockOwner = lockingUser;
        this.lockIdentifier = lockIdentifier;
        this.dateCreated = LocalDateTime.now();
        this.lockStatus = CREATED;
        this.isRemoved = false;
        this.encryptedLockedPassword = new AES(getEncryptionKey()).encrypt(plainTextLockedPassword);
    }

    public String getDecryptedPassword() {
        if (LocalDateTime.now().isBefore(expiryDate)) throw new LockException("This lock is active. It will expire on " + getFormattedDate(expiryDate));
        this.lockStatus = OPENED;
        return new AES(getEncryptionKey()).decrypt(this.encryptedLockedPassword);
    }

    public void activate(Duration duration) {
        if (this.lockStatus == ACTIVE) throw new LockException("This lock was already activated");
        if (this.lockStatus == OPENED) throw new LockException("This lock is already opened; remove this and create a new lock for this service to store another password");
        this.lockStatus = ACTIVE;
        this.activationDate = LocalDateTime.now();
        this.expiryDate = this.activationDate.plus(duration);
    }

    public void extend(Duration duration) {
        if (this.isExpired() || this.lockStatus != ACTIVE) throw new LockException("This lock is not active");
        this.expiryDate = this.expiryDate.plus(duration);
    }

    public void remove() {
        if (this.lockStatus == ACTIVE) throw new LockRemovalException("Attempted to remove active or not opened lock");
        this.isRemoved = true;
    }

    public void forceRemove() {
        this.isRemoved = true;
    }

    public String getFormattedExpiryDate() {
        return expiryDate == null ? "" : getFormattedDate(this.expiryDate);
    }

    public String getFormattedActivationDate() {
        return activationDate == null ? "" : getFormattedDate(this.activationDate);
    }

    public String getLockIdentifier() {
        return lockIdentifier;
    }

    public String getStatusSummary() {
        if (isRemoved) return "Removed";
        if (lockStatus == CREATED) return "Created";
        if (lockStatus == ACTIVE && isExpired()) return "Expired";
        if (lockStatus == ACTIVE) return "Active";
        if (lockStatus == OPENED) return "Opened";
        return "";
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    private String getEncryptionKey() {
        return lockIdentifier.toUpperCase() + "#" +
                lockOwner.getUsername() + "@" +
                dateCreated.minusMonths(dateCreated.getDayOfYear()).toString().substring(0, 19) + "4$eG6a)";
    }

    private String getFormattedDate(LocalDateTime date) {
        var datePattern = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault());
        return date.format(datePattern);
    }
}
