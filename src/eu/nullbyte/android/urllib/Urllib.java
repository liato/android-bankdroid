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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class Urllib {
    public final static String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17";

    private DefaultHttpClient httpclient;
	private HttpContext context;
	private String currentURI;
	private boolean acceptInvalidCertificates = false;
	private String charset = HTTP.UTF_8;
	private HashMap<String, String> headers;
	
	public Urllib() {
		this(false);
	}
	public Urllib(boolean acceptInvalidCertificates) {
		this(acceptInvalidCertificates, false);
	}	

	public Urllib(boolean acceptInvalidCertificates, boolean allowCircularRedirects) {
		this.acceptInvalidCertificates = acceptInvalidCertificates;
		this.headers = new HashMap<String, String>();
    	HttpParams params = new BasicHttpParams(); 
    	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, this.charset);
        params.setBooleanParameter("http.protocol.expect-continue", false);
        if (allowCircularRedirects) params.setBooleanParameter("http.protocol.allow-circular-redirects", true);
		if (acceptInvalidCertificates) {
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
	        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
	        httpclient = new DefaultHttpClient(manager, params);
		}
		else {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager  manager = new ThreadSafeClientConnManager(params, registry);
			httpclient = new DefaultHttpClient(manager, params);
		}
    	context = new BasicHttpContext();
    }
    
    public String open(String url) throws ClientProtocolException, IOException {
    	return this.open(url, new ArrayList <NameValuePair>());
    }
    
    public String open(String url, List<NameValuePair> postData) throws ClientProtocolException, IOException {
    	this.currentURI = url;
    	String response;
        String[] headerKeys = (String[]) this.headers.keySet().toArray(new String[headers.size()]);
        String[] headerVals = (String[]) this.headers.values().toArray(new String[headers.size()]);
    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	if (postData.isEmpty()) {
    		//URL urli = new URL(url); 
    		HttpGet urlConnection = new HttpGet(url);
    		urlConnection.addHeader("User-Agent", USER_AGENT);
            for (int i = 0; i < headerKeys.length; i++) {
                urlConnection.addHeader(headerKeys[i], headerVals[i]);
            }
    		response = httpclient.execute(urlConnection, responseHandler, context);
    	}
    	else {
    		HttpPost urlConnection = new HttpPost(url);
    		urlConnection.setEntity(new UrlEncodedFormEntity(postData, this.charset));
    		urlConnection.addHeader("User-Agent", USER_AGENT);
            for (int i = 0; i < headerKeys.length; i++) {
                urlConnection.addHeader(headerKeys[i], headerVals[i]);
            }
    		response = httpclient.execute(urlConnection, responseHandler, context); 
    	}

        HttpUriRequest currentReq = (HttpUriRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
        HttpHost currentHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        this.currentURI = currentHost.toURI() + currentReq.getURI();
    	
    	return response;
    }
    
    public void close() {
        httpclient.getConnectionManager().shutdown();
    }
    
    public HttpContext getContext() {
    	return context;
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
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    public void setKeepAliveTimeout(final int seconds) {
        httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() { 
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext arg1) {
                // TODO Auto-generated method stub
                return seconds;
            }});
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

    
    public boolean acceptsInvalidCertificates() {
    	return acceptInvalidCertificates;
    }
}