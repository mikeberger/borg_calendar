package net.sf.borg.model.ical;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <p>
 * EasyX509TrustManager unlike default {@link X509TrustManager} accepts
 * self-signed certificates.
 * </p>
 * 
 * <p>
 * This trust manager SHOULD NOT be used for productive systems due to security
 * reasons, unless it is a conscious decision and you are perfectly aware of
 * security implications of accepting self-signed certificates
 * </p>
 * 
 * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a> DISCLAIMER:
 *         HttpClient developers DO NOT actively support this component. The
 *         component is provided as a reference material, which may be
 *         inappropriate to be used without additional customization.
 */
public class EasyX509TrustManager implements X509TrustManager {
	/** Log object for this class. */
	static private final Logger LOG = Logger.getLogger("net.sf.borg");

	private X509TrustManager standardTrustManager = null;

	/**
	 * Constructor for EasyX509TrustManager.
	 * 
	 */
	public EasyX509TrustManager(KeyStore keystore)
			throws NoSuchAlgorithmException, KeyStoreException {
		super();

		TrustManagerFactory factory = TrustManagerFactory
				.getInstance("SunX509");
		factory.init(keystore);

		TrustManager[] trustmanagers = factory.getTrustManagers();

		if (trustmanagers.length == 0) {
			throw new NoSuchAlgorithmException(
					"SunX509 trust manager not supported");
		}

		this.standardTrustManager = (X509TrustManager) trustmanagers[0];
	}

	/**
	 * @see com.sun.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public X509Certificate[] getAcceptedIssuers() {
		return this.standardTrustManager.getAcceptedIssuers();
	}

	/**
	 * @see com.sun.net.ssl.X509TrustManager#isClientTrusted(X509Certificate[])
	 */
	public boolean isClientTrusted(X509Certificate[] certificates) {
		return true;
	}

	/**
	 * @see com.sun.net.ssl.X509TrustManager#isServerTrusted(X509Certificate[])
	 */
	public boolean isServerTrusted(X509Certificate[] certificates) {
		if ((certificates != null)) {
			LOG.fine("Server certificate chain:");

			for (int i = 0; i < certificates.length; i++) {

				LOG.fine("X509Certificate[" + i + "]=" + certificates[i]);

			}
		}

		if ((certificates != null) && (certificates.length == 1)) {
			X509Certificate certificate = certificates[0];

			try {
				certificate.checkValidity();
			} catch (CertificateException e) {
				LOG.severe(e.toString());

				return false;
			}

			return true;
		} else {
			return true;
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub
		
	}
}