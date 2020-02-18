package pl.jakubraban.desist.exceptions;

public class LockReactivationException extends LockException {
    public LockReactivationException(String message) {
        super(message);
    }
}
