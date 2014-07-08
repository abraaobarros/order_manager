package com.holandago.wbamanager.ordersmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.holandago.wbamanager.R;

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
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends ActionBarActivity {
    private EditText user = null;
    private EditText pass = null;
    private UserLoginTask authTask= null;
    private ProgressDialog pDialog;
    private String targetUrl = "http://wba-urbbox.herokuapp.com/rest/login";
    public final static String USERID_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.USERID_MESSAGE";
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = (EditText)findViewById(R.id.user);
        pass = (EditText)findViewById(R.id.pass);
        Button login = (Button)findViewById(R.id.sign_in_button);
        session = new SessionManager(getApplicationContext());
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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
            authTask = new UserLoginTask(username, password);
            authTask.execute((Void) null);
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
                setResult(RESULT_OK, intent);
                finish();
            } else {
                pass.setError(getString(R.string.error_incorrect_password));
                pass.setText("");
                pass.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
        }
    }
}
