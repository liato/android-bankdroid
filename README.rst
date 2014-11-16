Bankdroid
=========

Bankdroid is an Android app for Swedish banks, payment cards and similar services. Key features include:

* Automatic updates of your balance and transactions
* Notifications whenever your balance changes
* Widgets to show your balance anywhere on your home screen

More information can be found at:

* `Bankdroid on Google Play <https://play.google.com/store/apps/details?id=com.liato.bankdroid>`_
* `Bankdroid thread at the Swedroid Forum <http://goo.gl/9tJeH>`_ (Swedish)

Contribute or reporting broken banks
------------------------------------
The following information is needed for troubleshooting a broken bank or if you want a new bank to be supported 
by Bankdroid.

1. Address to login page.
2. Address and html code for the landing page after a successful login.
3. Address and html code for the page with the accounts overview.
4. Address and html code for the page with an account's transaction history.

NOTE. Do not forget to replace your personal information in the html code with random 
information before you send everything to android [at] nullbyte.eu. 
You can also open an issue here at Github with the required files included as an attachment.

Development environment
-----------------------

Bankdroid is written for Android Studio 0.8.2 and Gradle 1.12. Here's how to get the code up and
running on your computer:

1. Make sure you have `Android Studio 0.8.2 or later <https://developer.android.com/sdk/installing/studio.html>`_
2. `Clone <https://help.github.com/articles/which-remote-url-should-i-use>`_ the project (if you want to contribute you should `fork <https://help.github.com/articles/fork-a-repo>`_ the project first and then clone your fork)
3. Open the project's settings.gradle file in Android Studio and select "Use default gradle wrapper (recommended)"
4. Select "Make project" from the Build menu
5. Select Run from the Run menu

License
-------

The Bankdroid source code is licensed under the
`Apache License, Version 2.0 <http://www.apache.org/licenses/LICENSE-2.0>`_.
