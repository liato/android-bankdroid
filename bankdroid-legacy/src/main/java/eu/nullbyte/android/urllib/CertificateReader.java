package eu.nullbyte.android.urllib;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        List<Certificate> certificates = new ArrayList<>();
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
}
