package eu.nullbyte.android.urllib;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
                        e.printStackTrace();
                    }
                }
            }
        } catch (CertificateException e1) {
            e1.printStackTrace();
        }
        return certificates.toArray(new Certificate[certificates.size()]);
    }

    public static String[] getPins(Context context, int... rawResCerts) {
        Certificate[] certs = getCertificates(context, rawResCerts);
        if (certs != null && certs.length > 0) {
            String[] pins = new String[certs.length];
            for (int i = 0; i < certs.length; i++) {
                Certificate cert = certs[i];
                String hash = getCertificateHash(cert);
                pins[i] = hash;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA1");
                    byte[] publicKey = cert.getPublicKey().getEncoded();
                    byte[] pin = digest.digest(publicKey);
                    pins[i] = CertificateReader.byteArrayToHexString(pin);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            return pins;
        }
        return null;
    }

    private static String byteArrayToHexString(byte[] b) {
        int len = b.length;
        String data = new String();

        for (int i = 0; i < len; i++){
            data += Integer.toHexString((b[i] >> 4) & 0xf);
            data += Integer.toHexString(b[i] & 0xf);
        }
        return data;
    }

    public static String getCertificateHash(Certificate cert) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] publicKey = cert.getPublicKey().getEncoded();
            byte[] pin = digest.digest(publicKey);
            return CertificateReader.byteArrayToHexString(pin);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
