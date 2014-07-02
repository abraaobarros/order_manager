package com.holandago.wbamanager.ordersmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class OperationsList extends ActionBarActivity {
    private static String targetUrl = "http://wba-urbbox-teste.herokuapp.com/rest/operations";
    public final static String OPERATIONS_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.OPERATIONS_MESSAGE";
    public final static String ORDER_TITLE_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ORDER_TITLE_MESSAGE";
    public final static String LOT_NUMBER_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.LOT_NUMBER_MESSAGE";
    public final static String ORDER_ID_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ORDER_ID_MESSAGE";
    public final static String OPERATION_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.OPERATION_MESSAGE";
    private ListView listView;
    private TextView orderTitle;
    private static final String NEXT_OPERATION_TAG = "next_operation";
    private static final String MACHINE_TAG = "machine";
    private static final String OWNER_ID_TAG = "owner_id";
    private static final String OWNER_NAME_TAG = "owner_name";
    private static final String STATUS_TAG = "status";
    private static final String START_TIME_TAG = "start_time";
    private static final String CUSTOMER_TAG = "customer";
    private static final String PART_TAG = "part";
    private static final String PROJECT_NAME_TAG = "project_name";
    private static final String TIME_TAG = "time";
    private static final String ORDER_ID_TAG = "order_id";
    private static final String ID_TAG = "id";
    private static final String OPERATION_NAME_TAG = "operation_name";
    private static final String LOT_NUMBER_TAG = "lot";
    private JSONArray orderJSON = null;
    private String operations;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        session = new SessionManager(getApplicationContext());
        if(session.isLoggedIn())
            new JSONParse(session.getUserDetails()
                    .get(SessionManager.KEY_ID)).execute();
        else{
            startLoginActivity();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflating the actionBar menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.order_list_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new JSONParse(session.getUserDetails()
                        .get(SessionManager.KEY_ID)).execute();
                return true;
            case R.id.action_qrcode:
                new JSONParse(session.getUserDetails()
                        .get(SessionManager.KEY_ID)).execute();
                try{
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE","QR_CODE_MODE");
                    startActivityForResult(intent,0);
                }catch(Exception e){
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                    startActivity(marketIntent);
                    e.printStackTrace();
                }
                return true;
            case R.id.action_logout:
                session.logoutUser();
                startLoginActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                String orderUrl = data.getStringExtra("SCAN_RESULT");
                try {
                    JSONObject json = new JSONObject(orderUrl);
                    String id = json.getString(ORDER_ID_TAG);
                    String lot = "Lot Number: "+json.getString(LOT_NUMBER_TAG);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else
            if(resultCode == RESULT_CANCELED){
                onResume();
            }
        }else
        if(requestCode==1 || requestCode == 2){
            updateOrCreateList();
        }else
        if(requestCode == 3){
            if(resultCode == RESULT_OK){
                new JSONParse(session.getUserDetails()
                        .get(SessionManager.KEY_ID)).execute();
            }
        }
    }

    private void startLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 3);
    }


    public void sendOperationMessage(HashMap<String,String> operation){
        //Travels to DisplayLotsActivity
        Intent intent = new Intent(this, DisplayOperationActivity.class);
        JSONObject operationJson = new JSONObject(operation);
        intent.putExtra(OPERATION_MESSAGE,operationJson.toString());
        startActivityForResult(intent,1);
    }

    public void sendLotOperationMessage(String operations, String orderTitle,String lotNumber){
        //Travels to DisplayProgressActivity
        Intent intent = new Intent(this, DisplayProgressActivity.class);
        String orderId = "";
        try {
            orderId = new JSONArray(operations).getJSONObject(1).getString(ORDER_ID_TAG);
        }catch(JSONException e){
            e.printStackTrace();
        }
        intent.putExtra(OPERATIONS_MESSAGE,operations);
        intent.putExtra(ORDER_TITLE_MESSAGE, orderTitle);
        intent.putExtra(LOT_NUMBER_MESSAGE, lotNumber);
        intent.putExtra(ORDER_ID_MESSAGE,orderId);
        startActivityForResult(intent, 2);
    }

    public void updateOrCreateList(){
        //filling the next operations:
        final ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        for(HashMap<String,String> map1 :operationList){
            for(HashMap<String,String> map2:operationList){
                if(!map2.equals(map1)){
                    if(map2.get(LOT_NUMBER_TAG).equals(map1.get(LOT_NUMBER_TAG)) &&
                       !map2.get(STATUS_TAG).equals("2")){
                        map1.put(NEXT_OPERATION_TAG,map2.get(OPERATION_NAME_TAG));
                        break;
                    }
                }
            }
        }
        listView = (ListView)findViewById(R.id.orderList);
        ListAdapter adapter = new SimpleAdapter(
                OperationsList.this, //Context
                operationList, //Data
                R.layout.order_list_v, //Layout
                new String[]{OPERATION_NAME_TAG}, //from
                new int[]{R.id.order_title} //to
        );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Sends the operations part of the JSONObject to the next activity
                sendOperationMessage(operationList.get(+position));
            }
        });
    }

    public void createList(){
        ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        try{
            JSONArray json = new JSONArray(operations);
            ArrayList<String> existingIDs = new ArrayList<String>();
            for(int i = 0; i< json.length(); i++){
                JSONObject object = json.getJSONObject(i);
                //String projectName = object.getString(PROJECT_NAME_TAG);
                //String title = object.getString(PART_TAG);
                //String customer = object.getString(CUSTOMER_TAG);
                String ownerId = object.getString(OWNER_ID_TAG);
                String machine = object.getString(MACHINE_TAG);
                String status = object.getString(STATUS_TAG);
                String time = object.getString(TIME_TAG);
                String lot = object.getString(LOT_NUMBER_TAG);
                String operation_name = object.getString(OPERATION_NAME_TAG);
                String id = object.getString(ID_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                //map.put(PROJECT_NAME_TAG,projectName);
                //map.put(PART_TAG,title);
                //map.put(CUSTOMER_TAG,customer);
                map.put(MACHINE_TAG,machine);
                map.put(OWNER_ID_TAG,ownerId);
                map.put(LOT_NUMBER_TAG,lot);
                map.put(STATUS_TAG,status);
                map.put(TIME_TAG,time);
                map.put(OPERATION_NAME_TAG,operation_name);
                map.put(ID_TAG,id);
                map.put(OWNER_NAME_TAG,session.getUserDetails().get(SessionManager.KEY_USER));
                //Assuming the title is the ID
                if(!existingIDs.contains(id) && !status.equals("2")) {
                    operationList.add(map);
                    existingIDs.add(id);
                }
                updateOrCreateList();
            }
        }catch(JSONException e){
            e.printStackTrace();
            Log.e("JSON Error", "JSON error on create list " + e.toString());
        }finally{
            if(operationList.isEmpty())
                cannotGetOperations();
        }
    }

    public void cannotGetOperations(){
        new AlertDialog.Builder(this)
                .setTitle("Cannot get operations from server")
                .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new JSONParse(session.getUserDetails()
                                .get(SessionManager.KEY_ID))
                                .execute();
                    }
                }).create().show();
    }


    public class JSONParse extends AsyncTask<String,String, JSONArray>{
        private ProgressDialog pDialog;
        private String userID;
        private JSONParser parser = new JSONParser();

        public JSONParse(String userID){
            this.userID = userID;
        }

        @Override
        protected JSONArray doInBackground(String... strings) {
            return parser.getJSONfromUrl(targetUrl+"/"+userID);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            orderTitle = (TextView)findViewById(R.id.title);
            pDialog = new ProgressDialog(OperationsList.this);
            pDialog.setMessage("Getting data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(JSONArray json){
            pDialog.dismiss();
            try {
                operations = json.toString();
                createList();
            }catch (NullPointerException e){
                cannotGetOperations();
            }
        }

    }

}
