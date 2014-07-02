package com.holandago.wbamanager.ordersmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holandago.wbamanager.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;


public class DisplayOperationActivity extends ActionBarActivity {

    private JSONObject operationJson;
    private OperationViewHolder holder;
    private String startUrl;
    private String finishUrl;
    private ProgressDialog pDialog;
    private String id;
    private String lotNumber;
    private String status;
    private long timeInMillis = 0L;
    private long startTime = 0L;
    private long updatedTime = 0L;
    private long timeSwapBuff = 0L;
    private Handler customHandler = new Handler();
    private static final String WBA_DARK_GREY_COLOR = "666767";
    private static final String WBA_ORANGE_COLOR = "#FBB03B";
    private static final String WBA_LIGHT_GREY_COLOR = "#E9E9EA";
    private static final String NEXT_OPERATION_TAG = "next_operation";
    private static final String MACHINE_TAG = "machine";
    private static final String OWNER_ID_TAG = "owner_id";
    private static final String OWNER_NAME_TAG = "owner_name";
    private static final String STATUS_TAG = "status";
    private static final String CUSTOMER_TAG = "customer";
    private static final String PART_TAG = "part";
    private static final String PROJECT_NAME_TAG = "project_name";
    private static final String TIME_TAG = "time";
    private static final String ORDER_ID_TAG = "order_id";
    private static final String ID_TAG = "id";
    private static final String OPERATION_NAME_TAG = "operation_name";
    private static final String LOT_NUMBER_TAG = "lot";
    private static final String STARTED_AT_TAG = "started_at";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_operation);
        Intent intent = getIntent();
        try {
            operationJson =
                    new JSONObject(intent.getStringExtra(OperationsList.OPERATION_MESSAGE));
        }catch(JSONException e){
            e.printStackTrace();
        }
        fillLayout(operationJson);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_operation, menu);
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    public void fillLayout(JSONObject json){
        try {
            setTitle(json.getString(OPERATION_NAME_TAG));
            String machine = json.getString(MACHINE_TAG);
            String ownerName = json.getString(OWNER_NAME_TAG);
            lotNumber = json.getString(LOT_NUMBER_TAG);
            String nextProcess = json.getString(NEXT_OPERATION_TAG);
            String expectedTime = json.getString(TIME_TAG);
            status = json.getString(STATUS_TAG);
            id = json.getString(ID_TAG);
            holder = new OperationViewHolder();
            fillHolder(holder);
            holder.machine.setText(machine);
            holder.owner_name.setText(ownerName);
            holder.lot_number.setText(lotNumber);
            holder.next_process.setText(nextProcess);
            holder.expected_time.setText(convertTime(expectedTime));
            holder.action1.setOnClickListener(
                    new ButtonListener("start", holder.action2));
            holder.action2.setOnClickListener(
                    new ButtonListener("finish", holder.action1));
            holder.action2.setEnabled(false);
            if(status.equals("1")){
                setColorsStart();
                startTime = Long.parseLong(json.getString(STARTED_AT_TAG));
                customHandler.postDelayed(updateTimer, 0);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private String convertTime(String time){
        double t = Double.parseDouble(time);
        int hour = (int)Math.floor(t);
        double fractional = t - Math.floor(t);
        int minutes = (int)Math.floor(fractional*60);
        int seconds = (int)Math.floor((fractional*60-minutes)*60);
        return String.format("%02dh%02dm%02ds",hour,minutes,seconds);

    }

    private void fillHolder(OperationViewHolder holder){
        holder.part = (TextView)findViewById(R.id.part);
        holder.machine = (TextView)findViewById(R.id.machine);
        holder.client = (TextView)findViewById(R.id.client);
        holder.project = (TextView)findViewById(R.id.project);
        holder.owner_name = (TextView)findViewById(R.id.owner_name);
        holder.lot_number = (TextView)findViewById(R.id.lot_number);
        holder.next_process = (TextView)findViewById(R.id.next_process);
        holder.expected_time = (TextView)findViewById(R.id.expected_time);
        holder.timer = (TextView)findViewById(R.id.timer);
        holder.action1 = (Button)findViewById(R.id.action1);
        holder.action2 = (Button)findViewById(R.id.action2);
        holder.background = (RelativeLayout)findViewById(R.id.operation_background);
    }

    private static class OperationViewHolder {
        RelativeLayout background;
        TextView part;
        TextView machine;
        TextView client;
        TextView project;
        TextView owner_name;
        TextView lot_number;
        TextView next_process;
        TextView expected_time;
        TextView timer;
        Button action1;
        Button action2;
    }

    public class ButtonListener implements View.OnClickListener {
        private String handle;
        private Button otherButton;

        public ButtonListener(String handle,Button otherButton){
            this.handle = handle;
            this.otherButton = otherButton;
        }

        @Override
        public void onClick(View thisButton){
            startUrl = "http://wba-urbbox-teste.herokuapp.com/rest/progress/"+id+"/start";
            finishUrl = "http://wba-urbbox-teste.herokuapp.com/rest/progress/"+id+"/finish";
            AsyncGetRequest requester = new AsyncGetRequest();
            if(handle.equals("start")){
                requester.execute(new String[]{startUrl});
                setColorsStart();
                startTime = SystemClock.uptimeMillis();
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        true,
                        String.format("%d", startTime)
                );
                customHandler.postDelayed(updateTimer, 0);
            }else{
                requester.execute(new String[]{finishUrl});
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        false,
                        String.format("%d",SystemClock.uptimeMillis())
                );
                customHandler.removeCallbacks(updateTimer);
            }
            thisButton.setEnabled(false);
            if(handle.equals("start")) {
                otherButton.setEnabled(true);
            }
        }
    }

    private void setColorsStart(){
        holder.background.setBackgroundColor(Color.parseColor(WBA_ORANGE_COLOR));
        holder.expected_time.setTextColor(Color.parseColor(WBA_LIGHT_GREY_COLOR));
        holder.lot_number.setTextColor(Color.parseColor(WBA_LIGHT_GREY_COLOR));
        holder.action1.setEnabled(false);
        holder.action2.setEnabled(true);
    }

    private Runnable updateTimer = new Runnable(){
        public void run(){
            timeInMillis = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff+timeInMillis;
            int secs = (int)(updatedTime/1000);
            int mins = secs/60;
            int hours = mins/60;
            mins = mins%60;
            secs = secs%60;
            holder.timer.setText(String.format("%02dh%02dm%02ds",hours,mins,secs));
            customHandler.postDelayed(this, 0);
        }
    };

    //Asynchronous get request to access the url
    private class AsyncGetRequest extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(DisplayOperationActivity.this);
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
                DefaultHttpClient httpClient = HttpClient.getDefaultHttpClient();
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
}
