package fr.cdrochon.smamonolithe.event.command.services;

import java.security.SecureRandom;

public class RandomStringGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
    
//    public static void main(String[] args) {
//        int length = 10; // Change this to the desired length of the random string
//        String randomString = generateRandomString(length);
//        System.out.println("Generated Random String: " + randomString);
//    }
}

