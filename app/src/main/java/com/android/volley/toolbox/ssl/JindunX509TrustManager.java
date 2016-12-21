package com.android.volley.toolbox.ssl;

import android.content.Context;

import com.cloudspace.jindun.UCAPIApp;

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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author jgao
 */
public class JindunX509TrustManager implements X509TrustManager {

    private static int[] CAPaths = new int[]{
    };
    /*
     * The default X509TrustManager returned by SunX509. We鈥檒l delegate
     * decisions to it, and fall back to the logic in this class if the default
     * X509TrustManager doesn鈥檛 trust it.
     */
    private X509TrustManager sunJSSEX509TrustManager;
    private List<X509Certificate> ourCertList;

    public JindunX509TrustManager() throws KeyStoreException, NoSuchAlgorithmException {

        ourCertList = getLocalTrustX509Certificates();

        // Create a TrustManager that trusts the default CAs
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init((KeyStore) null);

        TrustManager tms[] = tmf.getTrustManagers();

		/*
         * Iterate over the returned trustmanagers, look for an instance of
		 * X509TrustManager. If found, use that as our "default" trust manager.
		 */
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                sunJSSEX509TrustManager = (X509TrustManager) tms[i];
                return;
            }
        }

		/*
		 * Find some other way to initialize, or else we have to fail the
		 * constructor.
		 */
        System.err.println("Could not initialize ssl CA");
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // XXX No need to check client side.
		/*
		 * try { sunJSSEX509TrustManager.checkClientTrusted(chain, authType); }
		 * catch (CertificateException excep) { // do any special handling here,
		 * or rethrow exception. }
		 */
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            // If Server Certificate error, check our own certificates.
            if (chain != null) {
                for (X509Certificate cert : chain) {
                    for (X509Certificate ourcert : ourCertList) {
                        if (cert.equals(ourcert)) {
                            //XXX PASS
                            System.out.println("Trust Cert: " + cert.getSubjectDN());
                            return;
                        }
                    }
                }
            }
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * Merely pass this through.
     */
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certs = sunJSSEX509TrustManager.getAcceptedIssuers();
        return certs;
    }

    private List<X509Certificate> getLocalTrustX509Certificates() {

        X509TrustManager tmp;
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        try {
            // Create a KeyStore containing our trusted CAs
            Context _context = UCAPIApp.getInstance();
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            for (int path : CAPaths) {
                Certificate ca = getCertificateById(_context, path);
                keyStore.setCertificateEntry("ca", ca);
                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                TrustManager tms[] = tmf.getTrustManagers();

				/*
				 * Iterate over the returned trustmanagers, look for an instance
				 * of X509TrustManager. If found, use that as our "default"
				 * trust manager.
				 */
                for (int i = 0; i < tms.length; i++) {
                    if (tms[i] instanceof X509TrustManager) {
                        tmp = (X509TrustManager) tms[i];
                        for (X509Certificate cert : tmp.getAcceptedIssuers()) {
                            certList.add(cert);
                        }
                        break;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return certList;
    }


    public static Certificate getCertificateById(Context context, int certificateId) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = context.getResources().openRawResource(certificateId);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            if (caInput != null) {
                caInput.close();
            }
        }
        return ca;
    }
}