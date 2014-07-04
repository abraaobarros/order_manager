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
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;



    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: CHANGE THE LAYOUT, NEED TO MAKE IT IN A BETTER WAY (ENCAPSULATING ELEMENTS)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_operation);
        Intent intent = getIntent();
        try {
            operationJson =
                    new JSONObject(intent.getStringExtra(OperationsList.OPERATION_MESSAGE));
        }catch(JSONException e){
            e.printStackTrace();
        }
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                return gestureDetector.onTouchEvent(event);
            }
        };
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

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onBackPressed();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    public void fillLayout(JSONObject json){
        try {
            //Using the json to fill the layout
            setTitle(json.getString(Utils.OPERATION_NAME_TAG));
            String machine = json.getString(Utils.MACHINE_TAG);
            String ownerName = json.getString(Utils.OWNER_NAME_TAG);
            lotNumber = json.getString(Utils.LOT_NUMBER_TAG);
            String nextProcess = json.getString(Utils.NEXT_OPERATION_TAG);
            String expectedTime = json.getString(Utils.TIME_TAG);
            String customer = json.getString(Utils.CUSTOMER_TAG);
            String part = json.getString(Utils.PART_TAG);
            String operation = json.getString(Utils.OPERATION_NAME_TAG);
            String projectName = json.getString(Utils.PROJECT_NAME_TAG);
            status = json.getString(Utils.STATUS_TAG);
            id = json.getString(Utils.ID_TAG);
            pID = json.getString(Utils.PROGRESS_ID_TAG);
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
                String stopped = json.getString(Utils.STOPPED_TAG);
                setColorsStart();
                startTime = Long.parseLong(json.getString(Utils.MY_STARTED_AT_TAG));
                if(startTime == 0){
                    startTime = TimeDifferenceInMillis(json.getString(Utils.STARTED_AT_TAG));
                }
                customHandler.postDelayed(updateTimer, 0);
                timeSwapBuff = Long.parseLong(json.getString(Utils.TIME_SWAP_TAG));
                holder.action1.setText("Stop");
                holder.action1.setEnabled(true);
                holder.action1.setOnClickListener(new ButtonListener("stop",holder.action2));
                if(stopped.equals("true")){
                    holder.action1.setText("Start");
                    holder.action1.setOnClickListener(new ButtonListener("start",holder.action2));
                    customHandler.removeCallbacks(updateTimer);
                    updatedTime = timeSwapBuff;
                    int secs = (int)(updatedTime/1000);
                    int mins = secs/60;
                    int hours = mins/60;
                    mins = mins%60;
                    secs = secs%60;
                    holder.timer.setText(String.format("%02dh%02dm%02ds",hours,mins,secs));
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private long TimeDifferenceInMillis(String date){
        String[] yearMonthDay = date.split("-");
        String[] day = yearMonthDay[2].split(" ");
        String[] hours = day[1].split(":");
        for(String s : day){
            Log.e("TESTING VALUES: ",s);
        }
        Calendar updated = Calendar.getInstance();
        Calendar epoch = Calendar.getInstance();
        epoch.set(Calendar.YEAR, 1970);
        epoch.set(Calendar.MONTH, Calendar.JANUARY);
        epoch.set(Calendar.DAY_OF_MONTH, 1);
        epoch.set(Calendar.HOUR_OF_DAY, 0);
        epoch.set(Calendar.MINUTE,0);
        epoch.set(Calendar.SECOND,0);
        updated.set(Calendar.YEAR, Integer.parseInt(yearMonthDay[0]));
        updated.set(Calendar.MONTH, Integer.parseInt(yearMonthDay[1])-1);
        updated.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day[0]));
        //fine adjustment of time to fit the fuse of the server's clock
        //TODO: FIND A BETTER WAY TO DO THIS
        updated.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[0])-2);
        updated.set(Calendar.MINUTE, Integer.parseInt(hours[1])+1);
        updated.set(Calendar.SECOND, Integer.parseInt(hours[2])-19);
        return updated.getTimeInMillis() - epoch.getTimeInMillis();
    }

    private String convertTime(String time){
        if(time.equals(""))
            time = "0";
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
        holder.background.setOnTouchListener(gestureListener);
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
            thisButton.setBackgroundColor(Color.parseColor(Utils.WBA_BLUE_COLOR));
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
                holder.action1.setEnabled(true);
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
                timeSwapBuff += timeInMillis;
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        UserOperations.STOP,
                        String.format("%d",timeSwapBuff)
                );
                holder.action1.setText("Start");
                holder.action1.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                holder.action1.setOnClickListener(new ButtonListener("start2",holder.action2));
            }else
            if(handle.equals("start2")){
                startTime = System.currentTimeMillis();
                holder.action1.setEnabled(true);
                otherButton.setEnabled(true);
                customHandler.postDelayed(updateTimer, 0);
                holder.action1.setText("Stop");
                holder.action1.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                holder.action1.setOnClickListener(new ButtonListener("stop",holder.action2));
                UserOperations.changeOperationStatus(
                        id,
                        lotNumber,
                        UserOperations.START,
                        String.format("%d", startTime)
                );
            }
        }
    }

    private void setColorsStart(){
        holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_ORANGE_COLOR));
        holder.expected_time.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
        holder.lot_number.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
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
                if(start){
                    output = getOutputFromUrl(url);
                }else{
                    output = postTime(url);
                }
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

        private String postTime(String url){
            String output = null;
            try{
                DefaultHttpClient httpClient = HttpClient.getDefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair(
                        "real_time",String.format("%d",updatedTime/1000)));
                nameValuePairs.add(new BasicNameValuePair(
                        Utils.PROGRESS_ID_TAG,pID));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpClient.execute(httpPost);
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
                holder.action1.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                customHandler.postDelayed(updateTimer, 0);
                holder.action1.setText("Stop");
                holder.action1.setOnClickListener(new ButtonListener("stop",holder.action2));
            }else {
                holder.action2.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                onBackPressed();
            }
        }
    }
}
