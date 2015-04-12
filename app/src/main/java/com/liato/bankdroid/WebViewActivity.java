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

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Bank.SessionPackage;
import com.liato.bankdroid.banking.BankFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import eu.nullbyte.android.urllib.Urllib;

import static android.graphics.Color.WHITE;

public class WebViewActivity extends LockableActivity implements OnClickListener {

    private final static String TAG = "WebViewActivity";

    private static WebView mWebView;

    private final LockableActivity activity = this;

    private boolean mFirstPageLoaded = false;

    private Handler mMainThreadhandler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        //TODO
//        this.addTitleButton(R.drawable.title_icon_back, "back", this);
//        this.addTitleButton(R.drawable.title_icon_forward, "forward", this);
//        this.addTitleButton(R.drawable.title_icon_refresh, "refresh", this);
//        this.setTitleButtonEnabled("forward", false);
//        this.setTitleButtonEnabled("back", false);
//        this.setTitleButtonEnabled("refresh", false);

        final CookieSyncManager csm = CookieSyncManager.createInstance(this);
        mWebView = (WebView) findViewById(R.id.wvBank);
        mWebView.setBackgroundColor(0);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUserAgentString(Urllib.DEFAULT_USER_AGENT);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
//                activity.setProgressBar(progress);
                if (progress == 100) {
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        public void run() {
//                            activity.hideProgressBar();
                            if (mFirstPageLoaded) {
                                mWebView.setBackgroundColor(WHITE);
                            }
                        }
                    };
                    // Let the progress bar hit 100% before we hide it.
                    handler.postDelayed(runnable, 100);

                } else if (mFirstPageLoaded) {
//                    activity.showProgressBar();
                }
            }
        });
        mWebView.setWebViewClient(new BankWebViewClient());
        String preloader = "Error...";
        try {
            preloader = IOUtils.toString(getResources().openRawResource(R.raw.loading));
            preloader = String.format(preloader,
                    "", // Javascript function
                    "" // HTML
            );
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mWebView.loadDataWithBaseURL("what://is/this/i/dont/even", preloader, "text/html", "utf-8",
                null);
        Bundle extras = getIntent().getExtras();
        final long bankId = extras.getLong("bankid", -1);
        //final long bankId = -1;
        if (bankId >= 0) {
            Runnable generateLoginPage = new Runnable() {
                public void run() {
                    Bank bank = BankFactory.bankFromDb(bankId, WebViewActivity.this, false);
                    final SessionPackage loginPackage = bank
                            .getSessionPackage(WebViewActivity.this);
                    final CookieStore cookieStore = loginPackage.getCookiestore();
                    mMainThreadhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((cookieStore != null) && !cookieStore.getCookies().isEmpty()) {
                                CookieManager cookieManager = CookieManager.getInstance();
                                CookieManager.getInstance().removeSessionCookie();
                                String cookieString;
                                for (Cookie cookie : cookieStore.getCookies()) {
                                    cookieString = String.format("%s=%s;%spath=%s; domain=%s;",
                                            cookie.getName(), cookie.getValue(),
                                            cookie.getExpiryDate() == null ? ""
                                                    : "expires=" + cookie.getExpiryDate() + "; ",
                                            cookie.getPath() == null ? "/" : cookie.getPath(),
                                            cookie.getDomain());
                                    cookieManager.setCookie(cookie.getDomain(), cookieString);
                                }
                                csm.sync();
                            }
                            mWebView.loadDataWithBaseURL("what://is/this/i/dont/even",
                                    loginPackage.getHtml(), "text/html", "utf-8", null);
                        }
                    });
                }
            };
            new Thread(generateLoginPage).start();
        }
    }

    public void onResume() {
        super.onResume();
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

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if ("refresh".equals(tag)) {
            mWebView.reload();
        } else if ("back".equals(tag)) {
            mWebView.goBack();
        } else if ("forward".equals(tag)) {
            mWebView.goForward();
        }
    }

    // Make sure clicked links are loaded in our webview.
    private class BankWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (mFirstPageLoaded) {
                handleHistoryChange();
            }
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!mFirstPageLoaded) {
                //This is the generated POST page.
                if (url.startsWith("what:")) {
                    return;
                }

                //This is the first real page.
                //Remove the generated page from history.
                mWebView.clearHistory();
                mFirstPageLoaded = true;
//                activity.setTitleButtonEnabled("refresh", true);
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

        public void handleHistoryChange() {
//            activity.setTitleButtonEnabled("back", mWebView.canGoBack());
//            activity.setTitleButtonEnabled("forward", mWebView.canGoForward());
        }
    }
}
