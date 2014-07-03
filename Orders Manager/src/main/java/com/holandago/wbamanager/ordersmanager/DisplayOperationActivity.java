package com.holandago.wbamanager.ordersmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;


public class DisplayOperationActivity extends ActionBarActivity {

    private JSONObject operationJson;
    private OperationViewHolder holder;
    private String startUrl;
    private String finishUrl;
    private String id;
    private String pID;
    private String lotNumber;
    private String status;
    private long timeInMillis = 0L;
    private long startTime = 0L;
    private long updatedTime = 0L;
    private long timeSwapBuff = 0L;
    private Handler customHandler = new Handler();
    private static final String WBA_DARK_GREY_COLOR = "#666767";
    private static final String WBA_BLUE_COLOR = "#72A7C4";
    private static final String WBA_ORANGE_COLOR = "#FBB03B";
    private static final String WBA_LIGHT_GREY_COLOR = "#E9E9EA";
    private static final String NEXT_OPERATION_TAG = "next_operation";
    private static final String MACHINE_TAG = "machine";
    private static final String OWNER_ID_TAG = "owner_id";
    private static final String OWNER_NAME_TAG = "owner_name";
    private static final String STATUS_TAG = "status";
    private static final String CUSTOMER_TAG = "costumer";
    private static final String PART_TAG = "part";
    private static final String PROJECT_NAME_TAG = "title";
    private static final String TIME_TAG = "time";
    private static final String ORDER_ID_TAG = "order_id";
    private static final String ID_TAG = "id";
    private static final String PROGRESS_ID_TAG = "progress_id";
    private static final String OPERATION_NAME_TAG = "operation_name";
    private static final String LOT_NUMBER_TAG = "lot";
    private static final String STARTED_AT_TAG = "started_at";
    private static final String STOPPED_AT_TAG = "stopped_at";
    private static final String UPDATED_AT_TAG = "updated_at";

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
            //Using the json to fill the layout
            setTitle(json.getString(OPERATION_NAME_TAG));
            String machine = json.getString(MACHINE_TAG);
            String ownerName = json.getString(OWNER_NAME_TAG);
            lotNumber = json.getString(LOT_NUMBER_TAG);
            String nextProcess = json.getString(NEXT_OPERATION_TAG);
            String expectedTime = json.getString(TIME_TAG);
            String customer = json.getString(CUSTOMER_TAG);
            String part = json.getString(PART_TAG);
            String operation = json.getString(OPERATION_NAME_TAG);
            String projectName = json.getString(PROJECT_NAME_TAG);
            status = json.getString(STATUS_TAG);
            id = json.getString(ID_TAG);
            pID = json.getString(PROGRESS_ID_TAG);
            holder = new OperationViewHolder();
            fillHolder(holder);
            holder.project.setText(projectName);
            holder.operation.setText(operation);
            holder.part.setText(part);
            holder.machine.setText(machine);
            holder.owner_name.setText(ownerName);
            holder.lot_number.setText(lotNumber);
            holder.next_process.setText(nextProcess);
            holder.client.setText(customer);
            holder.expected_time.setText(convertTime(expectedTime));
            holder.action1.setOnClickListener(
                    new ButtonListener("start", holder.action2));
            holder.action2.setOnClickListener(
                    new ButtonListener("finish", holder.action1));
            holder.action2.setEnabled(false);
            //Setting up font
            //TODO: Find a smarter way to do this
            Typeface font = Typeface.createFromAsset(getAssets(),"HelveticaNeue_Lt.ttf");
            holder.part.setTypeface(font,Typeface.BOLD);
            holder.machine.setTypeface(font);
            holder.owner_name.setTypeface(font);
            holder.lot_number.setTypeface(font);
            holder.next_process.setTypeface(font);
            holder.client.setTypeface(font);
            holder.expected_time.setTypeface(font);

            if(status.equals("1")){
                setColorsStart();
                startTime = Long.parseLong(json.getString(STARTED_AT_TAG));
                if(startTime == 0){
                    startTime = TimeDifferenceInMillis(json.getString(UPDATED_AT_TAG));
                }
                customHandler.postDelayed(updateTimer, 0);
                timeSwapBuff = Long.parseLong(json.getString(STOPPED_AT_TAG));
                if(timeSwapBuff != 0){
                    holder.action1.setText("Stop");
                    holder.action1.setOnClickListener(new ButtonListener("stop",holder.action2));
                    customHandler.removeCallbacks(updateTimer);
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private long TimeDifferenceInMillis(String date){
        String[] yearMonthDay = date.split("-");
        Calendar updated = Calendar.getInstance();
        Calendar epoch = Calendar.getInstance();
        epoch.set(Calendar.YEAR, 1970);
        epoch.set(Calendar.MONTH, Calendar.JANUARY);
        epoch.set(Calendar.DAY_OF_MONTH, 1);
        updated.set(Calendar.YEAR, Integer.parseInt(yearMonthDay[0]));
        updated.set(Calendar.MONTH, Integer.parseInt(yearMonthDay[1]));
        updated.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yearMonthDay[2]));
        return updated.getTimeInMillis()-epoch.getTimeInMillis();
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
        holder.part = (TextView)findViewById(R.id.part_name);
        holder.operation = (TextView)findViewById(R.id.operation);
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
        TextView operation;
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
            startUrl = "http://wba-urbbox-teste.herokuapp.com/rest/progress/"+pID+"/start";
            finishUrl = "http://wba-urbbox-teste.herokuapp.com/rest/progress/"+pID+"/finish";
            thisButton.setBackgroundColor(Color.parseColor(WBA_BLUE_COLOR));
            if(handle.equals("start")){
                AsyncGetRequest requester = new AsyncGetRequest(true);
                requester.execute(new String[]{startUrl});
                setColorsStart();
                startTime = System.currentTimeMillis();
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        UserOperations.START,
                        String.format("%d", startTime)
                );
                otherButton.setEnabled(true);
            }else
            if(handle.equals("finish")){
                new AlertDialog.Builder(DisplayOperationActivity.this)
                        .setTitle("Really Finish?")
                        .setMessage("Are you sure you want to finish this operation?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                AsyncGetRequest requester = new AsyncGetRequest(false);
                                requester.execute(new String[]{finishUrl});
                                UserOperations.changeOperationStatus(
                                        id,
                                        lotNumber,
                                        UserOperations.FINISH,
                                        String.format("%d",System.currentTimeMillis())
                                );

                            }
                        }).create().show();
            }else
            if(handle.equals("stop")){
                customHandler.removeCallbacks(updateTimer);
                timeSwapBuff = System.currentTimeMillis();
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        UserOperations.STOP,
                        String.format("%d",System.currentTimeMillis())
                );
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
            timeInMillis = System.currentTimeMillis() - startTime;
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
        private ProgressDialog pDialog;
        private boolean start;

        private AsyncGetRequest(boolean start){
            this.start = start;
        }
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
            if(start){
                holder.action1.setBackgroundColor(Color.parseColor(WBA_DARK_GREY_COLOR));
                customHandler.postDelayed(updateTimer, 0);
            }else {
                holder.action2.setBackgroundColor(Color.parseColor(WBA_DARK_GREY_COLOR));
                onBackPressed();
            }
        }
    }
}
