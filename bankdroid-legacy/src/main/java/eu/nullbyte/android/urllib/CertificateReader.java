package eu.nullbyte.android.urllib;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CertificateReader {

    public static Certificate[] getCertificates(Context context,
            int... rawResCerts) {
        List<Certificate> certificates = new ArrayList<Certificate>();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int resId : rawResCerts) {
                InputStream is = new BufferedInputStream(context.getResources()
                        .openRawResource(resId));
                try {
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
                    certificates.add(cert);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Timber.w(e, "Failed to close input stream");
                    }
                }
            }
        } catch (CertificateException e) {
            Timber.w(e, "Generating certificate failed");
        }
        return certificates.toArray(new Certificate[certificates.size()]);
    }

    public static ClientCertificate getClientCertificate(Context context, int rawResCert,
            String password) {
        InputStream is = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            is = new BufferedInputStream(context.getResources().openRawResource(rawResCert));
            keyStore.load(is, password.toCharArray());
            return new ClientCertificate(keyStore, password);
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            Timber.w(e, "Failed to get client certificate");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //noop
                }
            }
        }
        return null;
    }
}
