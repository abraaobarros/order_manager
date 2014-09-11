package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends ActionBarActivity {
    private EditText user = null;
    private EditText pass = null;
    private UserLoginTask authTask= null;
    private ProgressDialog pDialog;
    private String targetUrl = Utils.BASE_URL+"/rest/login";
    public final static String USERID_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.USERID_MESSAGE";
    SessionManager session;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = (EditText)findViewById(R.id.user);
        pass = (EditText)findViewById(R.id.pass);
        Button login = (Button)findViewById(R.id.sign_in_button);
        session = new SessionManager(getApplicationContext());

        try {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }catch(Exception e){
            e.printStackTrace();
        }

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }else{
            handleIntent(getIntent());
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d("NFC", "Wrong mime type: " + type);
            }
        }
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     * @author Ralf Wondratschek
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("NFC", "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                boolean success = false;
                String username = "";
                String password = "";
                if(result.contains("user")) {
                    success = true;
                    try {
                        username = new JSONObject(result).getString("user");
                        password = new JSONObject(result).getString("pass");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (success) {
                    ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
                            || conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
                        authTask = new UserLoginTask(username, password);
                        authTask.execute((Void) null);
                    }
                    else if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                            && conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
                        TextView error = (TextView)findViewById(R.id.login_error_message);
                        error.setText("Please connect to the internet to login");
                        error.setTextColor(Color.RED);
                    }
                } else {
                    pass.setText("");
                    user.setText("");
                    TextView error = (TextView)findViewById(R.id.login_error_message);
                    error.setText("NFC Tag wrongly written");
                    error.setTextColor(Color.RED);
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void attemptLogin() {
        if (authTask != null) {
            return;
        }

        // Reset errors.
        user.setError(null);
        pass.setError(null);

        // Store values at the time of the login attempt.
        String username = user.getText().toString();
        String password = pass.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            pass.setError(getString(R.string.error_field_required));
            focusView = pass;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            user.setError(getString(R.string.error_field_required));
            focusView = user;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(Utils.isNetworkAvailable(LoginActivity.this)){
                authTask = new UserLoginTask(username, password);
                authTask.execute((Void) null);
            }

        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;
        private String userID;


        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Signing in...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = makePostToUrl(targetUrl);
            boolean success = false;
            if(response.contains("sucess")) {
                success = true;
                try {
                    userID = new JSONObject(response).getString("user_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return success;
        }

        private String makePostToUrl(String url) {
            try {
                DefaultHttpClient client = HttpClient.getDefaultHttpClient();
                HttpPost post = new HttpPost(url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("user", username));
                nameValuePairs.add(new BasicNameValuePair("pass", password));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(post);
                HttpEntity resEntity = response.getEntity();
                String res = EntityUtils.toString(resEntity);
                return res;
            }catch(IOException e) {
                e.printStackTrace();
            }
            return "invalid";
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            pDialog.dismiss();
            if (success) {
                session.createLoginSession(username,userID);
                Intent intent = new Intent();
                intent.putExtra(USERID_MESSAGE,userID);
                if(intent.hasExtra("isResult")) {
                    setResult(RESULT_OK, intent);
                    finish();
                }else{
                    Intent intent1 = new Intent(LoginActivity.this,OperationsList.class);
                    startActivity(intent1);
                    finish();
                }
            } else {
                pass.setText("");
                user.setText("");
                TextView error = (TextView)findViewById(R.id.login_error_message);
                error.setText("Wrong password or username");
                error.setTextColor(Color.RED);
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
        }
    }
}
