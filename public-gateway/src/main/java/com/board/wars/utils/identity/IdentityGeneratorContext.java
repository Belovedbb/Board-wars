package com.board.wars.utils.identity;

public class IdentityGeneratorContext {
    private static SequenceIdentityGenerator sequenceIdentityGenerator = new SequenceIdentityGenerator();
    private static JDKIdentityGenerator jdkIdentityGenerator = new JDKIdentityGenerator();
    private static SecureRandomIdentityGenerator secureRandomIdentityGenerator = new SecureRandomIdentityGenerator();
    private static String result = null;

    public static String generate() {
        try {
            return sequenceIdentityGenerator.generate();
        }catch (Exception ex){
            return jdkIdentityGenerator.generate();
        }
    }

    public static String generateRandom() {
        return secureRandomIdentityGenerator.generate();
    }

    public static String generateRandom(boolean useCached) {
        return secureRandomIdentityGenerator.generate(useCached);
    }
}
