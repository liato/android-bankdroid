package com.liato.urllib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

public class Urllib {
	private DefaultHttpClient httpclient;
	private HttpContext context;
	private String currentURI;
	private final static String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.1; en-us; Nexus One Build/ERD62) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17";
	
    public Urllib() {
    	httpclient = new DefaultHttpClient();
    	context = new BasicHttpContext();
    	HttpParams params = new BasicHttpParams(); 
        HttpProtocolParams.setContentCharset(params, "UTF-8");     	
    	httpclient.setParams(params);
    }
    
    public String open(String url) throws ClientProtocolException, IOException {
    	return this.open(url, new ArrayList <NameValuePair>());
    }
    
    public String open(String url, List<NameValuePair> postData) throws ClientProtocolException, IOException {
    	this.currentURI = url;
    	String response;
    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	if (postData.isEmpty()) {
    		HttpGet urlConnection = new HttpGet(url);
    		urlConnection.addHeader("User-Agent", USER_AGENT);
    		response = httpclient.execute(urlConnection, responseHandler, context); 
    	}
    	else {
    		HttpPost urlConnection = new HttpPost(url);
    		urlConnection.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));
    		urlConnection.addHeader("User-Agent", USER_AGENT);
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
    
    
}