package eu.nullbyte.android.urllib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class CertificateReader {

	public static List<Certificate> getCertificates(Context context,
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
					System.out.println("ca="
							+ ((X509Certificate) cert).getSubjectDN());
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

		return certificates;
	}
}
