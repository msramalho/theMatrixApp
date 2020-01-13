package maps.bank_matrix;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Manage cryptographic key in keystore
 * requires previous user authentication to have been performed
 */
public class Cryptography {
    private static final String TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String SEPARATOR = ",";
    private static final Integer TAG_LENGTH = 128;

    private String keyName;
    private KeyStore keyStore;
    private SecretKey secretKey;


    public Cryptography(String keyName) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
        this.keyName = keyName;
        initKeystore();
        loadOrGenerateKey();
    }

    private void loadOrGenerateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        getKey();
        if (secretKey == null) generateKey();
    }

    private void initKeystore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
    }

    private void getKey() {
        try {
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyName, null);
            if (secretKeyEntry != null) secretKey = secretKeyEntry.getSecretKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            // failed to retrieve -> will generate new
            e.printStackTrace();
        }
    }

    private void generateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

        final KeyGenParameterSpec keyGenParameterSpec =
                new KeyGenParameterSpec.Builder(
                        keyName,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        //.setUserAuthenticationRequired(true)
                        //.setUserAuthenticationValidityDurationSeconds(this.validity)
                        .build();
        keyGenerator.init(keyGenParameterSpec);
        secretKey = keyGenerator.generateKey();
    }

    public String encrypt(String toEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        String iv = Base64.encodeToString(cipher.getIV(), Base64.DEFAULT);
        String encrypted = Base64.encodeToString(cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);
        return encrypted + SEPARATOR + iv;
    }


    public String decrypt(String toDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String[] parts = toDecrypt.split(SEPARATOR);
        if (parts.length != 2)
            throw new AssertionError("String to decrypt must be of the form: 'BASE64_DATA" + SEPARATOR + "BASE64_IV'");
        byte[] encrypted = Base64.decode(parts[0], Base64.DEFAULT),
                iv = Base64.decode(parts[1], Base64.DEFAULT);
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        //final GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        IvParameterSpec spec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }


}
