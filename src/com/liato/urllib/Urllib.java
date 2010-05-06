package com.liato.urllib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class Urllib {
	private DefaultHttpClient httpclient;
	
    public Urllib() {
    	httpclient = new DefaultHttpClient();
    }
    
    public String open(String url) throws ClientProtocolException, IOException {
    	return this.open(url, new ArrayList <NameValuePair>());
    }
    
    public String open(String url, List<NameValuePair> postData) throws ClientProtocolException, IOException {
    	//HttpResponse response;
    	String response;
    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	if (postData.isEmpty()) {
    		HttpGet urlConnection = new HttpGet(url);
    		response = httpclient.execute(urlConnection, responseHandler);
    	}
    	else {
    		HttpPost urlConnection = new HttpPost(url);
    		urlConnection.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));
    		response = httpclient.execute(urlConnection, responseHandler);
    	}

    	return response;
    }
    
    public void close() {
        httpclient.getConnectionManager().shutdown();
    }
}