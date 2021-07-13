package com.board.wars.utils.identity;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public class SecureRandomIdentityGenerator implements IdentityGenerator {
    private static String result = null;
    private static String PASSWORD = "encryptedpassword";
    private static String TEXT = "text";

    @Override
    public String generate() {
        return generateSecret(true);
    }

    String generate(boolean useCached) {
        return SecureRandomIdentityGenerator.generateSecret(useCached);
    }

    private static String generateSecret(boolean useCached) {
        if(result == null) {
            result = _generateSecret();
        }
        return useCached ? result : _generateSecret();
    }

    //aes-256-cbc key gen
    private static String _generateSecret(){
        final String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.text(PASSWORD, salt);
        return encryptor.encrypt(TEXT);
    }

}
