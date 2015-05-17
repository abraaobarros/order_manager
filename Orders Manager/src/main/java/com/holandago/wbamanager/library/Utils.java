package com.holandago.wbamanager.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by maestro on 04/07/14.
 */
public class Utils{
    public static final String BASE_URL = "http://wba-urbbox-teste.herokuapp.com";
    public static final String WBA_DARK_GREY_COLOR = "#666767";
    public static final String WBA_BLUE_COLOR = "#62ADE3";
    public static final String WBA_ORANGE_COLOR = "#FBB03B";
    public static final String WBA_LIGHT_GREY_COLOR = "#E9E9EA";
    public static final String REAL_TIME_TAG = "real_time";
    public static final String PROGRESS_ID_TAG = "progress_id";
    public static final String NEXT_OPERATION_TAG = "next_operation";
    public static final String MACHINE_TAG = "machine";
    public static final String OWNER_ID_TAG = "owner_id";
    public static final String OWNER_NAME_TAG = "owner_name";
    public static final String STATUS_TAG = "status";
    public static final String FINISHED_AT_TAG = "finished_at";
    public static final String OPERATION_NUMBER_TAG = "no";
    public static final String CUSTOMER_TAG = "costumer";
    public static final String PART_TAG = "part";
    public static final String PROJECT_NAME_TAG = "title";
    public static final String TIME_TAG = "time";
    public static final String ORDER_ID_TAG = "order_id";
    public static final String ID_TAG = "id";
    public static final String OPERATION_NAME_TAG = "operation_name";
    public static final String LOT_NUMBER_TAG = "lot";
    public static final String TIME_SWAP_TAG = "stop_time";
    public static final String STARTED_AT_TAG = "started_at";
    public static final String UPDATED_AT_TAG = "updated_at";
    public static final String STOPPED_TAG = "stopped?";
    public static final String MY_STARTED_AT_TAG = "my_started_at";
    public static final String LAST_OPERATION_TAG = "last_operation";
    public static final String WBA_NUMBER_TAG = "wba_no";

    public enum URLs {

        LIST_OPERATIONS("http://wba-urbbox.herokuapp.com/rest/operations");

        private String url;

        private URLs(String url) {
            this.url = url;
        }

        public String url() {
            return this.url;
        }
    }


    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
                    || conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                return true;
            } else if (conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                    && conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
                Toast.makeText(context,
                        "Keine Internetverbindund", Toast.LENGTH_LONG).show();
                return false;
            }
        }catch(NullPointerException e){
            e.printStackTrace();
            if (conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                return true;
            } else if (conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
                return false;
            }
        }
        return false;
    }

    public static boolean hasActiveInternetConnection(Context context) {
        String LOG_TAG = "ACTIVE CONNECTION";
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(LOG_TAG, "No network available!");
        }
        return false;
    }

}
