package pl.jakubraban.desist.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
        this("User with this username doesn't exist");
    }

}
