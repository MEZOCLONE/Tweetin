package io.github.mthli.Tweetin.Splash;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;
import io.github.mthli.Tweetin.Main.MainActivity;
import io.github.mthli.Tweetin.R;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SplashActivity extends Activity {
    private static final int SIGN_IN_FIRST_SUCCESSFUL = 0x100;
    private static final int SIGN_IN_FIRST_FAILED = 0x101;
    private static final int SIGN_IN_SECOND_SUCCESSFUL = 0x200;
    private static final int SIGN_IN_SECOND_FAILED = 0x201;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager manager = new SystemBarTintManager(this);
            manager.setStatusBarTintEnabled(true);
            int color = getResources().getColor(R.color.teal_default);
            manager.setTintColor(color);
        }

        /* Do something */
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.sp_name),
                MODE_PRIVATE
        );
        long useId = preferences.getLong(
                getString(R.string.sp_use_id),
                -1
        );
        if (useId != -1) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            /* Do something */
            startActivity(intent);
            finish();
        }

        Button signIn = (Button) findViewById(R.id.splash_sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialogFirst();
            }
        });

        Button signUp = (Button) findViewById(R.id.splash_sign_up_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpDialog();
            }
        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SIGN_IN_FIRST_SUCCESSFUL:
                        progressDialog.dismiss();
                        showSignInDialogNext();
                        break;
                    case SIGN_IN_FIRST_FAILED:
                        progressDialog.dismiss();
                        Toast.makeText(
                                SplashActivity.this,
                                R.string.splash_sign_in_authorization_failed,
                                Toast.LENGTH_SHORT
                        ).show();
                        break;
                    case SIGN_IN_SECOND_SUCCESSFUL:
                        progressDialog.dismiss();
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        /* Do something */
                        startActivity(intent);
                        finish();
                        break;
                    case SIGN_IN_SECOND_FAILED:
                        progressDialog.dismiss();
                        Toast.makeText(
                                SplashActivity.this,
                                R.string.splash_sign_in_get_access_token_failed,
                                Toast.LENGTH_SHORT
                        ).show();
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE) {
            /* Do nothing */
        }
        else{
            /* Do nothing */
        }
    }

    private void showSignUpDialog() {
        Uri uri = Uri.parse(getString(R.string.splash_sign_up_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private AlertDialog signInDialog;
    private void showSignInDialogFirst() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                SplashActivity.this
        );
        builder.setCancelable(false);

        final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.splash_dialog_first,
                null
        );
        builder.setView(layout);

        builder.setPositiveButton(
                getString(R.string.splash_sign_in_dialog_next),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signInDialog.dismiss();
                        getAccessTokenFirst(layout);
                    }
                }
        );

        builder.setNegativeButton(
                getString(R.string.splash_sign_in_dialog_help),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Do something */
                    }
                }
        );

        signInDialog = builder.create();
        signInDialog.show();
    }

    private ProgressDialog progressDialog;
    private String conKey;
    private String conSecret;
    private void getAccessTokenFirst(LinearLayout layout) {
        FloatLabeledEditText conKeyText = (FloatLabeledEditText) layout.findViewById(
                R.id.splash_sign_in_dialog_consumer_key
        );
        FloatLabeledEditText conSecretText = (FloatLabeledEditText) layout.findViewById(
                R.id.splash_sign_in_dialog_consumer_secret
        );
        conKey = conKeyText.getTextString();
        conSecret = conSecretText.getTextString();

        if (conKey.length() == 0 || conSecret.length() == 0) {
            Toast.makeText(
                    SplashActivity.this,
                    R.string.splash_sign_in_miss_oauth,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            progressDialog = new ProgressDialog(SplashActivity.this);
            progressDialog.setMessage(getString(R.string.splash_sign_in_start_authorization));
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(getAccessTokenThreadFirst).start();
        }
    }

    private Twitter twitter;
    private RequestToken requestToken;
    private String authUrl;
    Runnable getAccessTokenThreadFirst = new Runnable() {
        @Override
        public void run() {
            try {
                twitter = TwitterFactory.getSingleton();
                twitter.setOAuthConsumer(conKey, conSecret);
                requestToken = twitter.getOAuthRequestToken();
                authUrl = requestToken.getAuthorizationURL();
                Message message = new Message();
                message.what = SIGN_IN_FIRST_SUCCESSFUL;
                handler.sendMessage(message);
            } catch (Exception e) {
                Message message = new Message();
                message.what = SIGN_IN_FIRST_FAILED;
                handler.sendMessage(message);
            }
        }
    };

    private void showSignInDialogNext() {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.splash_dialog_webview,
                null
        );
        WebView webView = (WebView) layout.findViewById(R.id.splash_dialog_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(authUrl);

        Toast.makeText(
                SplashActivity.this,
                R.string.splash_sign_in_wait,
                Toast.LENGTH_SHORT
        ).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(
                SplashActivity.this
        );
        builder.setCancelable(false);
        builder.setView(layout);

        builder.setPositiveButton(
                getString(R.string.splash_sign_in_dialog_next),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signInDialog.dismiss();
                        showSignInDialogSecond();
                    }
                }
        );

        builder.setNegativeButton(
                getString(R.string.splash_sign_in_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Do nothing */
                    }
                }
        );

        signInDialog = builder.create();
        signInDialog.show();
        signInDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    private void showSignInDialogSecond() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                SplashActivity.this
        );
        builder.setCancelable(false);

        final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.splash_dialog_second,
                null
        );
        builder.setView(layout);

        builder.setPositiveButton(
                getString(R.string.splash_sign_in_dialog_accept),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signInDialog.dismiss();
                        getAccessTokenSecond(layout);
                    }
                }
        );

        builder.setNegativeButton(
                getString(R.string.splash_sign_in_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Do nothing */
                    }
                }
        );

        signInDialog = builder.create();
        signInDialog.show();
    }

    private String pin;
    private void getAccessTokenSecond(LinearLayout layout) {
        FloatLabeledEditText pinText = (FloatLabeledEditText) layout.findViewById(
                R.id.splash_sign_in_dialog_pin
        );
        pin = pinText.getTextString();
        if (pin.length() == 0) {
            Toast.makeText(
                    SplashActivity.this,
                    R.string.splash_sign_in_miss_pin,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            progressDialog = new ProgressDialog(SplashActivity.this);
            progressDialog.setMessage(getString(R.string.splash_sign_in_get_access_token));
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(getAccessTokenThreadSecond).start();
        }
    }

    Runnable getAccessTokenThreadSecond = new Runnable() {
        @Override
        public void run() {
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                saveAccessToken(
                        twitter.verifyCredentials().getId(),
                        accessToken
                );
                Message message = new Message();
                message.what = SIGN_IN_SECOND_SUCCESSFUL;
                handler.sendMessage(message);
            } catch (Exception e) {
                Message message = new Message();
                message.what = SIGN_IN_SECOND_FAILED;
                handler.sendMessage(message);
            }
        }
    };

    private void saveAccessToken(long useId, AccessToken accessToken) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.sp_name),
                MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(
                getString(R.string.sp_use_id),
                useId
        );
        editor.putString(
                getString(R.string.sp_access_token),
                accessToken.getToken()
        );
        editor.putString(
                getString(R.string.sp_access_token_secret),
                accessToken.getTokenSecret()
        );
        editor.commit();
    }
}