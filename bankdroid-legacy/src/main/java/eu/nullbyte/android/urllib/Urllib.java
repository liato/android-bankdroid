/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.nullbyte.android.urllib;

import com.liato.bankdroid.legacy.R;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Urllib {

    private static int MAX_RETRIES = 5;

    public static String DEFAULT_USER_AGENT
            = "Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17";

    private String userAgent = null;

    private DefaultHttpClient httpclient;

    private HttpContext mHttpContext;

    private String currentURI;

    private String charset = HTTP.UTF_8;

    private HashMap<String, String> headers;

    private Context mContext;

    private CertPinningSSLSocketFactory mSSLSocketFactory;


    public Urllib(Context context) {
        this(context, null);
    }

    public Urllib(Context context, Certificate[] pins) {
        this(context, null, pins);
    }

    public Urllib(Context context, ClientCertificate clientCert, Certificate[] pins) {
        mContext = context;
        this.headers = new HashMap<String, String>();
        userAgent = createUserAgentString();
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, this.charset);
        params.setBooleanParameter("http.protocol.expect-continue", false);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean trustSystemKeystore = prefs.getBoolean("debug_mode", false) && prefs
                .getBoolean("no_cert_pinning", false);
        try {
            mSSLSocketFactory = new CertPinningSSLSocketFactory(clientCert, pins);
            registry.register(new Scheme("https",
                    pins != null && !trustSystemKeystore ? mSSLSocketFactory
                            : SSLSocketFactory.getSocketFactory(), 443));
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
        httpclient = new BankdroidHttpClient(manager, params);
        mHttpContext = new BasicHttpContext();

    }

    public String open(String url) throws ClientProtocolException, IOException {
        return this.open(url, new ArrayList<NameValuePair>());
    }

    public String post(String url) throws ClientProtocolException, IOException {
        return this.open(url, new ArrayList<NameValuePair>(), true);
    }

    public String open(String url, List<NameValuePair> postData)
            throws ClientProtocolException, IOException {
        return open(url, postData, false);
    }

    public String open(String url, List<NameValuePair> postData, boolean forcePost)
            throws ClientProtocolException, IOException {
        HttpEntity entity = openAsHttpResponse(url, postData, forcePost).getEntity();
        if (entity == null) {
            return "";
        }
        return EntityUtils.toString(entity);
    }

    public HttpResponse openAsHttpResponse(String url, List<NameValuePair> postData,
            boolean forcePost) throws ClientProtocolException, IOException {
        HttpEntity entity = (postData == null || postData.isEmpty()) && !forcePost ? null
                : new UrlEncodedFormEntity(postData, this.charset);
        return openAsHttpResponse(url, entity, forcePost);
    }

    public HttpResponse openAsHttpResponse(String url, boolean forcePost)
            throws ClientProtocolException, IOException {
        return openAsHttpResponse(url, Collections.<NameValuePair>emptyList(), forcePost);
    }

    public HttpResponse openAsHttpResponse(String url, HttpEntity entity, boolean forcePost)
            throws ClientProtocolException, IOException {
        if ((entity == null) && !forcePost) {
            return openAsHttpResponse(url, entity, HttpMethod.GET);
        } else {
            return openAsHttpResponse(url, entity, HttpMethod.POST);
        }
    }

    public HttpResponse openAsHttpResponse(String url, HttpMethod method)
            throws ClientProtocolException, IOException {
        return openAsHttpResponse(url, null, method);
    }

    public HttpResponse openAsHttpResponse(String url, HttpEntity entity, HttpMethod method)
            throws ClientProtocolException, IOException {
        this.currentURI = url;
        HttpResponse response;
        String[] headerKeys = (String[]) this.headers.keySet().toArray(new String[headers.size()]);
        String[] headerVals = (String[]) this.headers.values().toArray(new String[headers.size()]);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        HttpUriRequest request;
        switch (method) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                ((HttpPost) request).setEntity(entity);
                break;
            case PUT:
                request = new HttpPut(url);
                ((HttpPut) request).setEntity(entity);
                break;
            default:
                request = new HttpGet(url);
        }
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }

        for (int i = 0; i < headerKeys.length; i++) {
            request.addHeader(headerKeys[i], headerVals[i]);
        }

        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(IOException exception, int executionCount,
                                        HttpContext context) {
                // retry a max of 5 times
                if (executionCount >= MAX_RETRIES) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    return true;
                } else if (exception instanceof ClientProtocolException) {
                    return true;
                }
                return false;
            }
        };
        httpclient.setHttpRequestRetryHandler(retryHandler);

        response = httpclient.execute(request, mHttpContext);

        //HttpUriRequest currentReq = (HttpUriRequest)mHttpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
        //HttpHost currentHost = (HttpHost)mHttpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        //this.currentURI = currentHost.toURI() + currentReq.getURI();
        this.currentURI = request.getURI().toString();

        return response;
    }

    public InputStream openStream(String url) throws ClientProtocolException, IOException {
        return openStream(url, (HttpEntity) null, false);
    }

    public HttpEntity toEntity(List<NameValuePair> postData) {
        if (postData != null && !postData.isEmpty()) {
            try {
                return new UrlEncodedFormEntity(postData, this.charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public InputStream openStream(String url, List<NameValuePair> postData, boolean forcePost)
            throws ClientProtocolException, IOException {
        return openStream(url, toEntity(postData), forcePost);
    }

    public InputStream openStream(String url, String postData, boolean forcePost)
            throws ClientProtocolException, IOException {
        return openStream(url, postData != null ? new StringEntity(postData, this.charset) : null,
                forcePost);
    }

    public InputStream openStream(String url, HttpEntity postData, boolean forcePost)
            throws ClientProtocolException, IOException {
        this.currentURI = url;
        String[] headerKeys = (String[]) this.headers.keySet().toArray(new String[headers.size()]);
        String[] headerVals = (String[]) this.headers.values().toArray(new String[headers.size()]);
        HttpUriRequest request;
        if (!forcePost && postData == null) {
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
            ((HttpPost) request).setEntity(postData);
        }
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }

        for (int i = 0; i < headerKeys.length; i++) {
            request.addHeader(headerKeys[i], headerVals[i]);
        }
        this.currentURI = request.getURI().toString();
        HttpResponse response = httpclient.execute(request);
        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    public void close() {
        httpclient.getConnectionManager().shutdown();
    }

    public HttpContext getHttpContext() {
        return mHttpContext;
    }

    public String getCurrentURI() {
        return currentURI;
    }

    public DefaultHttpClient getHttpclient() {
        return httpclient;
    }

    public void setContentCharset(String charset) {
        this.charset = charset;
        HttpProtocolParams.setContentCharset(httpclient.getParams(), this.charset);
    }


    public void setAllowCircularRedirects(boolean allow) {
        httpclient.getParams().setBooleanParameter("http.protocol.allow-circular-redirects", allow);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setKeepAliveTimeout(final int seconds) {
        httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext arg1) {
                return seconds;
            }
        });
    }

    public String removeHeader(String key) {
        return this.headers.remove(key);
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public void setFollowRedirects(boolean follow) {
        httpclient
                .setRedirectHandler(follow ? new DefaultRedirectHandler() : new RedirectHandler() {
                    public URI getLocationURI(HttpResponse response,
                            HttpContext context) throws ProtocolException {
                        return null;
                    }

                    public boolean isRedirectRequested(HttpResponse response,
                            HttpContext context) {
                        return false;
                    }
                });
    }


    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    private String createUserAgentString() {
        String appName = mContext.getString(R.string.app_name);
        String packageName = "";
        String appVersion = "";

        try {
            PackageInfo packageInfo = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            packageName = packageInfo.packageName;
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
        }

        Configuration config = mContext.getResources().getConfiguration();
        return String
                .format("%1$s/%2$s (%3$s; U; Android %4$s; %5$s-%6$s; %10$s Build/%7$s; %8$s) %9$s %10$s"
                        , appName
                        , appVersion
                        , System.getProperty("os.name", "Linux")
                        , Build.VERSION.RELEASE
                        , config.locale.getLanguage().toLowerCase()
                        , config.locale.getCountry().toLowerCase()
                        , Build.ID
                        , Build.BRAND
                        , Build.MANUFACTURER
                        , Build.MODEL);
    }

    private void updateSocketFactoryHost(HttpHost host) {
        if (mSSLSocketFactory != null && host != null) {
            mSSLSocketFactory.setHost(host.getHostName());
        }
    }

    class BankdroidHttpClient extends DefaultHttpClient {

        BankdroidHttpClient(ClientConnectionManager conman, HttpParams params) {
            super(conman, params);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request,
                ResponseHandler<? extends T> responseHandler)
                throws IOException, ClientProtocolException {
            updateSocketFactoryHost(target);
            return super.execute(target, request, responseHandler);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request,
                ResponseHandler<? extends T> responseHandler, HttpContext context)
                throws IOException, ClientProtocolException {
            updateSocketFactoryHost(target);
            return super.execute(target, request, responseHandler, context);
        }

        @Override
        protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec,
                ClientConnectionManager conman, ConnectionReuseStrategy reustrat,
                ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan,
                HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler,
                RedirectHandler redirectHandler, AuthenticationHandler targetAuthHandler,
                AuthenticationHandler proxyAuthHandler, UserTokenHandler stateHandler,
                HttpParams params) {
            return new DefaultishRequestDirector(requestExec, conman, reustrat, kastrat, rouplan,
                    httpProcessor, retryHandler, redirectHandler, targetAuthHandler,
                    proxyAuthHandler, stateHandler, params);
        }
    }

    class DefaultishRequestDirector extends DefaultRequestDirector {

        public DefaultishRequestDirector(HttpRequestExecutor requestExec,
                ClientConnectionManager conman, ConnectionReuseStrategy reustrat,
                ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan,
                HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler,
                RedirectHandler redirectHandler, AuthenticationHandler targetAuthHandler,
                AuthenticationHandler proxyAuthHandler, UserTokenHandler userTokenHandler,
                HttpParams params) {
            super(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler,
                    redirectHandler, targetAuthHandler, proxyAuthHandler, userTokenHandler, params);
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            updateSocketFactoryHost(target);
            return super.execute(target, request, context);
        }
    }

}