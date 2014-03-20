package eu.nullbyte.android.urllib;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

public class CertPinningTrustManager implements X509TrustManager {
    private Certificate[] certificates;
    private String host;

    public CertPinningTrustManager(Certificate[] certificates, String host) {
        this.certificates = certificates;
        this.host = host;
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
        throw new CertificateException(host == null ? "Server certificate not trusted." : String.format("Server certificate not trusted for host: %s.", host));
	}

    public void setHost(String host) {
        this.host = host;
    }
}
