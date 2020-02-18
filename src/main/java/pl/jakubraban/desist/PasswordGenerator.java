package pl.jakubraban.desist;

import java.util.Random;

public class PasswordGenerator {

    public static String generateRandomPassword(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int asciiCode = r.nextInt(94) + 33;
            sb.append((char) asciiCode);
        }
        return sb.toString();
    }

}
