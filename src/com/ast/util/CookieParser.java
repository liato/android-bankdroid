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
package com.ast.util;

/**
 * Parses Set-Cookie HTTP response header and returns the corresponding {@link Cookie} which can be
 * used for storing cookie information in java collections.
 * <p/>
 * <a href="http://www.ietf.org/rfc/rfc2109">RFC 2109</a>
 *
 * @author hessdroid@gmail.com
 */
public class CookieParser {

    /**
     * Abstract representation of an HTTP cookie.
     */
    public static class Cookie {

        public String host;
        public String value; // cookie string without the leading intro: "Set-Cookie: "
        public String expires;
        public String path;
        public String domain;
        public String sessionId;
        public boolean secure;
    }

    /**
     * Parses the given <tt>setCookieString</tt> from an HTTP response and creates a {@link Cookie} from it.
     * The {@link Cookie} can be used by clients to send cookie on subsequent requests.
     *
     * @param host <code>String</code> cookie host
     * @param setCookieString <code>String</code> complete cookie string
     * @return a {@link Cookie} that was created from the given <tt>setCookieString</tt>
     */
    public static Cookie parse(String host, String setCookieString) {

        if (host == null) throw new IllegalArgumentException("Parameter \"host\" must not be null");
        if (setCookieString == null)
            throw new IllegalArgumentException("Parameter \"setCookieString\" must not be null");

        Cookie result = new Cookie();

        // split the cookie string on any semicolon or space(s)
        String[] fields = setCookieString.split(";\\s*");
        result.host = host;

        // ignore leading cookie intro 
        result.value = fields[0].startsWith("Set-Cookie: ") ? fields[0].substring(12) : fields[0];
        if (result.value.startsWith("JSESSIONID=")) {
            result.sessionId = result.value.substring(11);
        }

        // Parse each field
        for (int j = 1; j < fields.length; j++) {
            if ("secure".equalsIgnoreCase(fields[j])) {
                result.secure = true;
            } else if (fields[j].indexOf('=') > 0) {
                String[] f = fields[j].split("=");
                if ("expires".equalsIgnoreCase(f[0])) {
                    result.expires = f[1];
                } else if ("domain".equalsIgnoreCase(f[0])) {
                    result.domain = f[1];
                } else if ("path".equalsIgnoreCase(f[0])) {
                    result.path = f[1];
                }
            }
        }
        return result;
    }

}
