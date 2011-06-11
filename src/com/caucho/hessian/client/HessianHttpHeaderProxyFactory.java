package com.caucho.hessian.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import com.ast.util.CookieParser.Cookie;
import com.caucho.hessian.io.HessianRemoteObject;

// This class is added in the bankdroid project to enable adding some http-headers required by Skandiabanken
// The class also takes care of a ugly hack for some strange cookie-handling for Skandiabanken (see getCookie)
public class HessianHttpHeaderProxyFactory extends HessianHttpProxyFactory{

	Map<String, String> mHeaderMap;
	
	public HessianHttpHeaderProxyFactory(Map<String, String> headers){
		mHeaderMap = headers;
	}
	
    @SuppressWarnings({"unchecked"})
    @Override
    public <T> T create(Class<T> api, String urlName, ClassLoader loader) throws MalformedURLException {
        InvocationHandler handler = new HessianHttpHeaderProxy(this, new URL(urlName));
        return (T) Proxy.newProxyInstance(loader, new Class[]{api, HessianRemoteObject.class}, handler);
    }
    
    public void addHeader(String key, String value){
    	mHeaderMap.put(key, value);
    }
    
    public void removeHeader(String key){
    	mHeaderMap.remove(key);
    }
	
	class HessianHttpHeaderProxy extends HessianHttpProxy{

		HessianHttpHeaderProxy(HessianProxyFactory factory, URL url) {
			super(factory, url);
		}
		
		@Override
		protected void addRequestHeaders(URLConnection conn) {
			super.addRequestHeaders(conn);

			for (Map.Entry<String, String> header : mHeaderMap.entrySet())
				conn.setRequestProperty(header.getKey(), header.getValue());
		}
		
	    @Override
		protected Cookie getCookie(String host, String path) {
			Cookie cookie = super.getCookie(host, path);
			
			// Ugly hack: For some reason the cookie has the wrong path in the response from Skandiabanken.
			if (cookie == null && 
					host.contentEquals("smartrefill.se") && 
					path.contentEquals("/")){
				cookie = super.getCookie("smartrefill.se", "/BankServices");
			}
			
			return cookie;
		}
	}
}
