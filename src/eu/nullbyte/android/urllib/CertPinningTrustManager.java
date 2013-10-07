package eu.nullbyte.android.urllib;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

public class CertPinningTrustManager implements X509TrustManager {
    private Certificate[] certificates;

    public CertPinningTrustManager(Certificate[] certificates) {
        this.certificates = certificates;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
        throw new CertificateException("Client authentication not implemented.");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
        for (X509Certificate certificate : chain) {
            byte[] publicKey = certificate.getPublicKey().getEncoded();
            for (Certificate pinnedCert : certificates) {
                if (Arrays.equals(publicKey, pinnedCert.getPublicKey().getEncoded())) {
                    return;
                }
            }
        }
        throw new CertificateException("Server certificate not trusted.");
	}
}
