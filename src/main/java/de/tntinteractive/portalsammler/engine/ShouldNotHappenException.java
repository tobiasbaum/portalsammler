package de.tntinteractive.portalsammler.engine;

public class ShouldNotHappenException extends RuntimeException {

    private static final long serialVersionUID = 8822353977934338722L;

    public ShouldNotHappenException(Throwable cause) {
        super(cause);
    }

    public ShouldNotHappenException(String string) {
        super(string);
    }

}
