package eu.nullbyte.android.urllib;

import java.security.KeyStore;

public class ClientCertificate {
    private String mPassword;
    private KeyStore mKeyStore;

    public ClientCertificate(KeyStore keyStore, String password) {
        mKeyStore = keyStore;
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public KeyStore getKeyStore() {
        return mKeyStore;
    }
}
