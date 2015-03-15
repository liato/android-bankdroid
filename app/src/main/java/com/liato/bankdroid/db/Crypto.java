package com.liato.bankdroid.db;

public class Crypto {

    /*
     * The key used to encrypt all the account passwords before storing them in the database.
     * This key is not used in the market app.
     *
     */
    private final static String KEY
            = "KGLRqraqThYniEtasoCqfbjFDwctomjmiY4rvSJThyyU4qUTIPXNLhPxkivpFLgr";

    public final static String getKey() {
                /*
                 * Manipulate the key before returning it.
		 * 
		 * ...
		 * 
		 * Code omitted.
		 * 
		 * ...
		 * 
		 */

        return KEY;
    }
}
