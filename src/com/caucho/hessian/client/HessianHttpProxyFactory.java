/*
 * Copyright (C) 2009 hessdroid@gmail.com
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
package com.caucho.hessian.client;

import com.ast.util.CookieParser;
import com.ast.util.CookieParser.Cookie;
import com.caucho.hessian.io.HessianRemoteObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * <code>HessianHttpProxyFactory</code> extends the
 * <code>HessianProxyFactory</code>'s capabilities, by adding support for HTTP
 * Cookies. A cookie map is used to map Server/Paths to cookies
 *
 * @author <a href="mailto:wolf@wolfpaulus.com">Wolf Paulus</a>
 * @version 1.0 Date: Nov 17, 2009
 */

public class HessianHttpProxyFactory extends HessianProxyFactory {
    /*
      * public Object create2(Class api, String urlName, ClassLoader loader)
      * throws MalformedURLException { InvocationHandler handler = new
      * HessianHttpProxy(this, new URL(urlName)); return
      * Proxy.newProxyInstance(api.getClassLoader(), new Class[]{api,
      * HessianRemoteObject.class}, handler); }
      */

    /**
     * Creates a new proxy with the specified URL. The returned object is a
     * proxy with the interface specified by api.
     * <p/>
     * <p/>
     * <pre>
     * String url = "http://localhost:8080/ejb/hello");
     * HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
     * </pre>
     *
     * @param api     the interface the proxy class needs to implement
     * @param urlName the URL where the client object is located.
     * @param loader  <code>ClassLoader</code> to be used loading the proxy
     *                instance's class
     * @return a proxy to the object with the specified interface.
     * @throws java.net.MalformedURLException if URL object cannot be created with the provided urlName
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public <T> T create(Class<T> api, String urlName, ClassLoader loader) throws MalformedURLException {
        //
        // todo: since the api is already loaded, maybe the api class' classloader should be tried,
        // in case the provided classloader fails.

        InvocationHandler handler = new HessianHttpProxy(this, new URL(urlName));
        return (T) Proxy.newProxyInstance(loader, new Class[]{api, HessianRemoteObject.class}, handler);
    }

    /**
     * Clear the Cookie Cache, should be run on every logout.
     */
    public static void clearCookieCache() {
        HessianHttpProxy.cookieMap.clear();
    }

    /**
     * The <code>HessianHttpProxy</code> intercepts request and response, so that
     * cookies can be read in the incoming response and written before an
     * outgoing request is sent. Cookie strings are stored in a
     * <code>Hashtable</code>; and the URL's host+path is used for the key.
     * <p/>
     * When the cookie string is retrieved, the path is shortened, all the way
     * to / until a match is found.
     * <p/>
     * Available Header Fields are:
     * <p/>
     * Header Field: date (Wed, 18 Nov 2009 21:01:44 GMT)
     * Header Field: content-type (application/x-hessian)
     * Header Field: transfer-encoding (chunked)
     * Header Field: server (Apache-Coyote/1.1)
     * Header Field: set-cookie (JSESSIONID=5930D0459F0CE1B769ED5D08B031F9D2; Path=/Server)
     */
    private static class HessianHttpProxy extends HessianProxy {
        private static final Logger log = Logger.getLogger(HessianHttpProxy.class.getName());
        private static final HashMap<String, Cookie> cookieMap = new HashMap<String, Cookie>();

        private static final String COOKIE_SET = "set-cookie";

        HessianHttpProxy(HessianProxyFactory factory, URL url) {
            super(url, factory);
        }

        /**
         * Read cookies found in a server's response, so we can send them back
         * with the next request. The response-header field names are the key
         * values of the map.
         *
         * @param conn <code>URLConnection</code>
         */
        protected void parseResponseHeaders(URLConnection conn) {

            List<String> cookieStrings = conn.getHeaderFields().get(HessianHttpProxy.COOKIE_SET);
            if (cookieStrings != null) {
                String host = conn.getURL().getHost();
                for (String s : cookieStrings) {
                    Cookie cookie = CookieParser.parse(host, s);
                    HessianHttpProxy.cookieMap.put(cookie.host + cookie.path, cookie);
                    log.finest("Cookie cached: " + cookie.host + cookie.path + ":" + s);
                }
            }
        }

        /**
         * Add the cookies we received in a previous response, into the current
         * request. The getUrl().getPath() might return something like this:
         * /Server/comm Here we look for a close match in the cookiemap,
         * starting with the most specific.
         *
         * @param conn <code>URLConnection</code>
         */
        protected void addRequestHeaders(URLConnection conn) {

            String host = conn.getURL().getHost();
            String path = conn.getURL().getPath();

            while (path != null && 0 < path.length()) {
                log.info("Host:+" + host +",Path:"+path);
                Cookie cookie = HessianHttpProxy.cookieMap.get(host + path);
                if (cookie != null) {
                    conn.setRequestProperty("Cookie", cookie.value);
                    log.finest("Cookie set in request:" + cookie.value);
                    break;
                }
                int i = path.lastIndexOf("/");
				if (0==i && 1<path.length()) {
                    path = "/";
                } else {
                    path = path.substring(0, i);
                }
			}
		}
	}
}
