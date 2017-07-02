package maps.matrix;

import android.content.Context;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

class MatrixEncryption {
    private Context myContext;
    private String passkey;

    MatrixEncryption(Context myContext, String passkey) {
        this.myContext = myContext;
        this.passkey = passkey;
    }


    /*private SecretKeySpec getSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passkey, salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
    }*/











    private String decrypt(String matrixString){
        try {
            return decryptMsg(matrixString);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("ERROR DECRYPTING: " + e.getMessage());
        }
        return "";
    }

    private String encrypt(String matrixString){
        try {
            return encryptMsg(matrixString);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            System.out.println("ERROR ENCRYPTING: " + e.getMessage());
        }
        return "";
    }







    private String encryptMsg(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        //byte[] message64 = Base64.decode(message,Base64.DEFAULT);
        //SecretKeySpec secret = new SecretKeySpec(secret64, "AES");
        Cipher cipher  = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretFromKey());
        byte[] encVal = cipher.doFinal(message.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    private String decryptMsg(String cipherTextString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        byte[] cipherText = Base64.decode(cipherTextString, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getSecretFromKey());
        return new String(cipher.doFinal(cipherText), "UTF-8");
    }


    private SecretKeySpec getSecretFromKey() {
        byte[] key;
        MessageDigest sha;
        try {
            key = (passkey).getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException  | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        return new SecretKeySpec(key, "AES");
    }
}
