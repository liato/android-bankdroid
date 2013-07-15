package com.liato.bankdroid.banking.banks.nordea.api;

import java.io.IOException;

import com.github.kevinsawicki.http.HttpRequest;
import com.liato.bankdroid.banking.banks.nordea.api.exception.NordeaException;
import com.liato.bankdroid.banking.banks.nordea.api.model.request.LightLoginRequest;
import com.liato.bankdroid.banking.banks.nordea.api.model.response.LightLoginResponse;

public class AuthenticationService extends AbstractNordeaService {

	private static final String API_URL = "https://mobilebankingservices.nordea.com/AuthenticationServiceV1.1/";

	public LightLoginResponse securityToken(LightLoginRequest pLightLoginRequest)
			throws NordeaException {
		String requestData = "";
		HttpRequest request = HttpRequest.post(API_URL + "SecurityToken");
		try {
			requestData = getObjectMapper().writeValueAsString(
					pLightLoginRequest);

			LightLoginResponse response = sendRequest(request, requestData,
					LightLoginResponse.class);

			setAuthenticationToken(response.getAuthenticationToken());
			return response;
		} catch (IOException e) {
			throw new NordeaException("A problem occured while mapping LightLoginRequest");
		}
	}

	public LightLoginResponse securityToken(String pUserId, String pPassword)
			throws NordeaException {
		LightLoginRequest vLightLoginRequest = new LightLoginRequest(pUserId,
				pPassword);
		return securityToken(vLightLoginRequest);
	}
}
