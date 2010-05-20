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
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class Urllib {
	private DefaultHttpClient httpclient;
	private HttpContext context;
	private String currentURI;
	
    public Urllib() {
    	httpclient = new DefaultHttpClient();
    	context = new BasicHttpContext();
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
    		response = httpclient.execute(urlConnection, responseHandler, context); 
    	}
    	else {
    		HttpPost urlConnection = new HttpPost(url);
    		urlConnection.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));
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