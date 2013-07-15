package com.liato.bankdroid.banking.banks.nordea.api;

import java.io.IOException;

import android.os.Build;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.liato.bankdroid.banking.banks.nordea.api.exception.NordeaException;
import com.liato.bankdroid.banking.banks.nordea.api.mapper.NordeaObjectMapper;
import com.liato.bankdroid.banking.banks.nordea.api.model.AuthenticationToken;

public abstract class AbstractNordeaService {
	
	private AuthenticationToken mAuthenticationToken;

	private ObjectMapper mObjectMapper;
	
	protected AbstractNordeaService() {
		mObjectMapper = new NordeaObjectMapper();
	}
	
	protected HttpRequest setRequestHeaders(HttpRequest request) {
		request.contentType("application/json", "utf-8");
		request.header("x-App-Language", "en");
		request.header("x-App-Name","MBA-SE");
		request.header("x-Device-Model",Build.MODEL);
		request.header("x-Platform-Version",Build.VERSION.RELEASE);
		request.header("x-App-Version","1.1.0");
		request.header("x-Platform-Type","Android");
		request.header("x-Request-Id","1");
		request.header("x-Device-Make",Build.MANUFACTURER);
		request.header("x-App-Country","SE");
		if(mAuthenticationToken != null) {
			request.header("x-Security-Token",mAuthenticationToken.getToken());
		}
		return request;
	}
	
	public void setAuthenticationToken(AuthenticationToken pAuthenticationToken) {
		mAuthenticationToken = pAuthenticationToken;
	}
	
	public boolean isLoggedIn() {
	    return this.mAuthenticationToken != null 
	            && this.mAuthenticationToken.getToken() != null;
	    //TODO fix check with login time.
	}
	
	protected ObjectMapper getObjectMapper() {
		return mObjectMapper;
	}
	
	protected <T> T sendRequest(HttpRequest pHttpRequest,String input,Class<T> pResponseModel) throws NordeaException {
		String response = null;
		try {
			pHttpRequest = setRequestHeaders(pHttpRequest);
			if(HttpRequest.METHOD_POST.equalsIgnoreCase(pHttpRequest.method())) {
				pHttpRequest.send(input);
			}
			response = pHttpRequest.body();
			return mObjectMapper.readValue(response, pResponseModel);
		}
		catch(JsonParseException parserException) {
			NordeaException ex = new NordeaException("An error occured while parsing the response");
			ex.setHttpResponse(response);
			throw ex;
		}
		catch(JsonMappingException mapperException) {
			NordeaException exception = new NordeaException("An error occured while mapping the response");
			exception.setHttpResponse(response);
			throw exception;
			
		}
		catch(IOException ioException) {
			throw new NordeaException("An error occured while reading the response from the server.");
		}
	}
	
	protected <T> T request(HttpRequest pHttpRequest, Class<T> pResponseModel) throws NordeaException {
		return sendRequest(pHttpRequest, null, pResponseModel);
	}
	
}
