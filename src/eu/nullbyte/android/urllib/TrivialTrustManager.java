package eu.nullbyte.android.urllib;

public class TrivialTrustManager implements javax.net.ssl.X509TrustManager {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
    }
	@Override
	public void checkClientTrusted(
			java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void checkServerTrusted(
			java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub
		
	}
}
