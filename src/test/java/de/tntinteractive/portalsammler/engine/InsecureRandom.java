package de.tntinteractive.portalsammler.engine;

import java.security.SecureRandom;

public class InsecureRandom extends SecureRandom {

    private static final long serialVersionUID = -6115532431134419014L;

    public InsecureRandom() {
        super(new byte[] {1, 2, 3, 4});
    }

}
