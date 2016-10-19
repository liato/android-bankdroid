package eu.nullbyte.android.urllib;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

public class CertPinningSSLSocketFactory extends SSLSocketFactory {

    private SSLContext sslcontext = null;

    private Certificate[] certificates;

    private String lastHost;

    private CertPinningTrustManager mTrustManager;

    private ClientCertificate mClientCertificate;

    public CertPinningSSLSocketFactory(ClientCertificate clientCertificate,
            Certificate[] certificates)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
        super(null);
        this.certificates = certificates;
        this.mClientCertificate = clientCertificate;
        setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }

    private SSLContext createSSLContext() throws IOException {
        //Log.v(TAG, "createSSLContext()");
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            mTrustManager = new CertPinningTrustManager(certificates, lastHost);
            KeyManager[] keyManagers = null;
            if (mClientCertificate != null) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(mClientCertificate.getKeyStore(),
                        mClientCertificate.getPassword().toCharArray());
                keyManagers = kmf.getKeyManagers();
            }
            context.init(keyManagers, new TrustManager[]{mTrustManager}, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

//    private static KeyManager2 extends KeyManager

    private SSLContext getSSLContext() throws IOException {
        //Log.v(TAG, "getSSLContext()");
        if (this.sslcontext == null) {
            this.sslcontext = createSSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#connectSocket(java.net.Socket,
     * String, int, java.net.InetAddress, int,
     * org.apache.http.params.HttpParams)
     */
    @Override
    public Socket connectSocket(Socket sock, String host, int port,
            InetAddress localAddress, int localPort, HttpParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        //Log.v(TAG, "connectSocket(socket: " + sock + ", host: " + host + ", port: " + port + ", localAddress: " + localAddress + ", localPort: " + localPort + ", params: " + params);
        int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        int soTimeout = HttpConnectionParams.getSoTimeout(params);

        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

        if ((localAddress != null) || (localPort > 0)) {
            // we need to bind explicitly
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }
            InetSocketAddress isa = new InetSocketAddress(localAddress,
                    localPort);
            sslsock.bind(isa);
        }

        sslsock.connect(remoteAddress, connTimeout);
        sslsock.setSoTimeout(soTimeout);
        try {
            getHostnameVerifier().verify(host, sslsock);
            // verifyHostName() didn't blowup - good!
        } catch (IOException iox) {
            // close the socket before re-throwing the exception
            try {
                sslsock.close();
            } catch (Exception x) { /*ignore*/ }
            throw iox;
        }
        return sslsock;
    }

    /**
     * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
     */
    @Override
    public Socket createSocket() throws IOException {
        //Log.v(TAG, "createSocket()");
        return secureSocket(getSSLContext().getSocketFactory().createSocket());
    }


    /**
     * @see org.apache.http.conn.scheme.LayeredSocketFactory#createSocket(java.net.Socket,
     * String, int, boolean)
     */
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
        //Log.v(TAG, "createSocket(socket: " + socket + ", host: " + host + ", port: " + port + ", autoClose: " + autoClose);
        lastHost = host;
        return secureSocket(
                getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose));
    }

    public void setHost(String host) {
        lastHost = host;
        if (mTrustManager != null) {
            mTrustManager.setHost(host);
        }
    }

    private Socket secureSocket(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }

        SSLSocket vSocket = (SSLSocket) socket;

        // Remove SSLv3 support.
        // See https://code.google.com/p/android/issues/detail?id=78187
        List<String> supportedProtocols = new ArrayList<String>(
                Arrays.asList(vSocket.getSupportedProtocols()));
        supportedProtocols.remove("SSLv3");
        vSocket.setEnabledProtocols(
                supportedProtocols.toArray(new String[supportedProtocols.size()]));

        // Fix for supporting old servers.
        // See https://code.google.com/p/android-developer-preview/issues/detail?id=1200#c23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            List<String> ciphers = new ArrayList<String>(
                    Arrays.asList(vSocket.getEnabledCipherSuites()));
            ciphers.remove("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA");
            ciphers.remove("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA");
            vSocket.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
        }
        return vSocket;
    }
}
