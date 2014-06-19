package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.holandago.wbamanager.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class OperationHandlingActivity extends Activity {
    private Button stateBtn;
    String startUrl;
    String finishUrl;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_handling);
        Intent intent = getIntent();
        final String status = intent.getStringExtra(DisplayOperationsActivity.STATUS_MESSAGE);
        String id = intent.getStringExtra(DisplayOperationsActivity.ID_MESSAGE);
        startUrl = "http://wba-urbbox.herokuapp.com/rest/operation/"+id+"/start";
        finishUrl = "http://wba-urbbox.herokuapp.com/rest/operation/"+id+"/finish";
        stateBtn = (Button)findViewById(R.id.state_handler_button);

        try {
            stateBtn.setText(status.equals("1") ? "Finish Operation" : "Start Operation");
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        stateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGetRequest(status);
                stateBtn.setText("Task Completed");
                stateBtn.setEnabled(false);
            }
        });
    }

    private class asyncGetRequest extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(OperationHandlingActivity.this);
            pDialog.setMessage("Making the request");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... urls){
            String output = null;
            for(String url:urls){
                output = getOutputFromUrl(url);
            }

            return output;
        }

        private String getOutputFromUrl(String url){
            String output = null;
            try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                output = EntityUtils.toString(httpEntity);
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output){
            pDialog.dismiss();
        }
    }

    public void makeGetRequest(String status){
        String url = null;
        if(status.equals("1")){
            url = finishUrl;
        }else{
            url = startUrl;
        }
        asyncGetRequest get = new asyncGetRequest();
        get.execute(new String[]{url});
    }

}
