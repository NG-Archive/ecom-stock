package site.ng_archive.ecom_stock.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Disabled;

public class CreateKeyTest {

    @Disabled
    @org.junit.jupiter.api.Test
    void 암호화키생성() {

        String plainText = "key";
        String key = "test";

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
        encryptor.setPassword(key);
        String encrypted = encryptor.encrypt(plainText);
        System.out.println(encrypted);
    }
}
