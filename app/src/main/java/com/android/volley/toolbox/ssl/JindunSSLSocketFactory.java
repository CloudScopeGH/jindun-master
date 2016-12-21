package com.android.volley.toolbox.ssl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


public class JindunSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {

    private SSLContext sslContext = SSLContext.getInstance("TLS");

    public JindunSSLSocketFactory(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
            UnrecoverableKeyException {

        super(truststore);

        TrustManager[] tm = {new JindunX509TrustManager()};
        sslContext.init(null, tm, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}
