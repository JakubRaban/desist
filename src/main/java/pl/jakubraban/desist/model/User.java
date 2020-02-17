package pl.jakubraban.desist.model;

import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
public class User {

    private static final int minimumUsernameLength = 4;
    private static final int maximumUsernameLength = 16;
    private static final int minimumPlainTextPasswordLength = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    private int userId;
    @Column(name = "USERNAME", nullable = false, length = maximumUsernameLength, unique = true)
    private String username;
    @Column(name = "HASHED_PASSWORD", nullable = false, length = 128)
    private String hashedPassword;
    @Column(name = "CREATED_ON", nullable = false)
    private LocalDateTime createdOn;

    public User() {}

    public User(String username, String plainTextPassword) {
        if (username.length() < minimumUsernameLength || username.length() > maximumUsernameLength) {
            throw new IllegalArgumentException("Username is too short or too long (min. " + minimumUsernameLength + ", max. " + maximumUsernameLength + " characters");
        }
        if (plainTextPassword.length() < minimumPlainTextPasswordLength) throw new IllegalArgumentException("Password is too short (min. " + minimumPlainTextPasswordLength + " characters)");
        this.username = username;
        this.createdOn = LocalDateTime.now();
        this.hashedPassword = DigestUtils.sha512Hex(getSaltedPassword(plainTextPassword));
    }

    public boolean validatePassword(String plainTextPassword) {
        String computedPassword = DigestUtils.sha512Hex(getSaltedPassword(plainTextPassword));
        return computedPassword.equals(this.hashedPassword);
    }

    private String getSaltedPassword(String plainTextPassword) {
        int usernameLength = this.username.length();
        StringBuilder saltedPassword = new StringBuilder(plainTextPassword)
                .insert(0, Character.toString(username.charAt(0)).toLowerCase())
                .insert(1, "7%")
                .append(username.charAt(usernameLength - 2))
                .append(Character.toString(username.charAt(usernameLength - 1)).toUpperCase())
                .append("3!")
                .append(this.createdOn.toString(), 0, 19);
        return saltedPassword.toString();
    }

    public String getUsername() {
        return username;
    }
}
