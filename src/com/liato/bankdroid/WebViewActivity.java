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

package com.liato.bankdroid;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.liato.bankdroid.Bank.SessionPackage;
import com.liato.urllib.Urllib;

public class WebViewActivity extends LockableActivity {
    private final static String TAG = "WebViewActivity";
    private static WebView mWebView;
    private boolean mFirstPageLoaded = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        final CookieSyncManager csm = CookieSyncManager.createInstance(this);
        mWebView = (WebView)findViewById(R.id.wvBank);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true); 
        mWebView.getSettings().setUserAgentString(Urllib.USER_AGENT);
        mWebView.setWebViewClient(new BankWebViewClient());
        String preloader = "<html><head></head><body>" +
                            "Bankdroid..." +
                            "</body></html>";
        mWebView.loadData(preloader, "text/html", "utf-8");

        Bundle extras = getIntent().getExtras();
        final long bankId = extras.getLong("bankid", -1);
        if (bankId >= 0) {
            Runnable generateLoginPage = new Runnable() {
                public void run() {
                    Bank bank = BankFactory.bankFromDb(bankId, WebViewActivity.this, false);
                    SessionPackage loginPackage = bank.getSessionPackage();
                    CookieStore cookieStore = loginPackage.getCookiestore();
                    if ((cookieStore != null) && !cookieStore.getCookies().isEmpty()) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookieString;
                        for (Cookie cookie : cookieStore.getCookies()) {
                            cookieString = String.format("%s=%s;%spath=%s; domain=%s;",
                                    cookie.getName(), cookie.getValue(),
                                    cookie.getExpiryDate() == null ? "" : "expires="+cookie.getExpiryDate()+"; ",
                                    cookie.getPath() == null ? "/" : cookie.getPath(),
                                    cookie.getDomain());
                            Log.d(TAG, "Cookiestring: " + cookieString);
                            cookieManager.setCookie(cookie.getDomain(), cookieString);
                        }
                        csm.sync();
                    }
                    mWebView.loadData(loginPackage.getHtml(), "text/html", "utf-8");
                }
              };
              new Thread(generateLoginPage).start();
        }
    }

    public void onResume() {
        super.onResume();
    }

    // Make sure clicked links are loaded in our webview.
    private class BankWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!mFirstPageLoaded) {
                //This is the generated POST page.
                if (url.startsWith("data:text")) return;
                
                //This is the first real page.
                //Remove the generated page from history.
                mWebView.clearHistory();
                mFirstPageLoaded = true;
                return;
            }
        }
        

        @Override
        public void onFormResubmission(WebView view, Message dontResend,
                Message resend) {
            // TODO Auto-generated method stub
            //super.onFormResubmission(view, dontResend, resend);
            resend.sendToTarget();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }	

    //Handle the back key
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView != null) {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}