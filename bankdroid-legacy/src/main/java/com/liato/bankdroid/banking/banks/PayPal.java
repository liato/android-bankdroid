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

package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class PayPal extends Bank {
    private static final String TAG = "PayPal";
    private static final String NAME = "PayPal";
    private static final String NAME_SHORT = "paypal";
    private static final String URL = "https://www.paypal.com/";
    private static final int BANKTYPE_ID = IBankTypes.PAYPAL;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT | + InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    private static final boolean STATIC_BALANCE = true;

    private Pattern reFormAction = Pattern.compile("<form.*?action=\"([^\"]+)\".*?login_form.*?>", Pattern.CASE_INSENSITIVE);
    private Pattern reBalance = Pattern.compile("PayPal\\s*balance:\\s*(?:</strong>)?<span\\s*class=\"balance\">[^<]+<[^<]+>\\s*(?:<strong>)?[^0-9,.-]*([0-9,. ]+)([A-Z]+)\\s*(?:</strong>)?\\s*<[^<]+>\\s*</span>", Pattern.CASE_INSENSITIVE);
    private Pattern reAccounts = Pattern.compile("row\">([^>]+)</td>\\s*<td\\s*class=\"textright\">\\s*<[^>]+>\\s*[^0-9,.-]*([0-9,. ]+)([A-Z]+)\\s*<[^>]+>\\s*</td>", Pattern.CASE_INSENSITIVE);
    private String response = null;
    public PayPal(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public PayPal(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_paypal));
        urlopen.setUserAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36");
        //Get cookies and url to post to
        response = urlopen.open("https://www.paypal.com/en");
        Document d = Jsoup.parse(response);
        Element e = d.select("form[name=login_form]").first();
        
        String strPostUrl;
        if (e != null && !TextUtils.isEmpty(e.attr("action"))) {
            strPostUrl = e.attr("action").trim();
        }
        else {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" post url.");
        }
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("login_email", username));
        postData.add(new BasicNameValuePair("login_password", password));
        postData.add(new BasicNameValuePair("target_page", "0"));
        postData.add(new BasicNameValuePair("submit.x", "Log In"));
        postData.add(new BasicNameValuePair("form_charset", "UTF-8"));
        postData.add(new BasicNameValuePair("browser_name", "undefined"));
        postData.add(new BasicNameValuePair("browser_version", "undefined"));
        postData.add(new BasicNameValuePair("operating_system", "Windows"));
        postData.add(new BasicNameValuePair("bp_mid", "v=1;a1=na~a2=na~a3=na~a4=Mozilla~a5=Netscape~a6=5.0 (Windows; en-US)~a7=20100713~a8=na~a9=true~a10=Windows NT 6.1~a11=true~a12=Win32~a13=na~a14=Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.7) Gecko/20100713 Firefox/3.6.7 ( .NET CLR 3.5.30729; .NET4.0C)~a15=true~a16=en-US~a17=na~a18=www.paypal.com~a19=na~a20=na~a21=na~a22=na~a23=1280~a24=720~a25=24~a26=658~a27=na~a28=Sun Oct 31 2010 18:41:07 GMT 0100~a29=1~a30=def|qt1|qt2|qt3|qt4|qt5|qt6|swf|~a31=yes~a32=na~a33=na~a34=no~a35=no~a36=yes~a37=no~a38=online~a39=no~a40=Windows NT 6.1~a41=no~a42=no~"));
        postData.add(new BasicNameValuePair("bp_ks1", "v=1;l=16;Di0:2663Di1:48Ui0:15Ui1:81Di2:176Di3:48Ui2:32Ui3:96Di4:384Ui4:48Di5:352Ui5:48Di6:128Ui6:80Di7:112Ui7:48Di8:113Ui8:79Di9:125Ui9:51Di10:98Ui10:72Di11:227Ui11:51Di12:80Ui12:80Di13:128Ui13:64Di14:48Ui14:80Di15:416Ui15:80"));
        postData.add(new BasicNameValuePair("bp_ks2", ""));
        postData.add(new BasicNameValuePair("bp_ks3", ""));
        postData.add(new BasicNameValuePair("flow_name", "xpt/Marketing_CommandDriven/homepage/IndividualsHome"));
        postData.add(new BasicNameValuePair("fso", "k2TDENTlxEJnhbuYDYFmKMyVq0kUZPsdK6j3V1gPUwuZvyAmzzpRs4Cmjet0z19AwlxXfW"));
        return new LoginPackage(urlopen, postData, response, strPostUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("If you still can't log in") || response.contains("both your email address and password")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
            if (response.contains("your last action could not be completed")) {
                throw new BankException("Error: PPL92");
            }
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        try {
            response = urlopen.open("https://www.paypal.com/en/cgi-bin/webscr?cmd=_login-done&login_access="+((int)(System.currentTimeMillis() / 1000L)));
            Matcher matcher = reAccounts.matcher(response);
            int accId = 1;
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: name              SEK (Primary)
                 * 2: amount            554.70
                 * 3: currency          SEK
                 * 
                 */
                Account account = new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(2)), ""+accId);
                account.setCurrency(matcher.group(3).trim());
                accounts.add(account);
                accId++;
            }
            matcher = reBalance.matcher(response);
            if (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: balance           554.70
                 * 2: currency          SEK
                 * 
                 */
                balance = Helpers.parseBalance(matcher.group(1));
                currency = matcher.group(2).trim();
                if (accounts.isEmpty()) {
                    // Probably a premier account.
                    Account account = new Account(currency, balance, "1");
                    account.setCurrency(currency);
                    accounts.add(account);                }
            }

            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        }
        finally {
            super.updateComplete();
        }
    }

}