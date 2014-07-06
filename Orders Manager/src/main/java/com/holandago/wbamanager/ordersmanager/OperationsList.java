package com.holandago.wbamanager.ordersmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import com.holandago.wbamanager.library.Utils;


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

    private String operations;
    SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        session = new SessionManager(getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
                UserOperations.flush();
                session.logoutUser();
                startLoginActivity();
                return true;
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
                    ArrayList<HashMap<String,String>> operationList =
                            UserOperations.getOperationsList();
                    JSONObject json = new JSONObject(orderUrl);
                    String id = json.getString(Utils.ORDER_ID_TAG);
                    Log.e("DEBUGGING", "ORDER ID = "+id);
                    String lot = json.getString(Utils.LOT_NUMBER_TAG);
                    ArrayList<HashMap<String,String>> operationsFromOrder =
                            new ArrayList<HashMap<String, String>>();
                    for(HashMap<String,String> operation : operationList){
                        if(operation.get(Utils.ORDER_ID_TAG).equals(id)){
                            if(operation.get(Utils.LOT_NUMBER_TAG).equals(lot)){
                                operationsFromOrder.add(operation);
                            }
                        }
                    }
                    Collections.sort(operationsFromOrder,new NumberComparator());
                    sendOperationMessage(
                            //Sends first element of the ordered Array
                            operationsFromOrder.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else
            if(resultCode == RESULT_CANCELED){
                onResume();
            }
        }else
        if(requestCode==1){
            //From an DisplayOperationActivity
            updateOrCreateList();
        }else
        if(requestCode == 2){
            //From an LoginActivity
            if(resultCode == RESULT_OK){
                new JSONParse(session.getUserDetails()
                        .get(SessionManager.KEY_ID)).execute();
            }
        }
    }

    private void startLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 2);
    }


    public void sendOperationMessage(HashMap<String,String> operation){
        //Travels to DisplayLotsActivity
        Intent intent = new Intent(this, DisplayOperationActivity.class);
        JSONObject operationJson = new JSONObject(operation);
        intent.putExtra(OPERATION_MESSAGE,operationJson.toString());
        startActivityForResult(intent, 1);
    }

    public void updateOrCreateList(){
        //filling the next operations:
        ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        //Ordering by lot
        Collections.sort(operationList, new LotNumberComparator());
        //Getting the next operation
        for(HashMap<String,String> map1 :operationList){
            for(HashMap<String,String> map2:operationList){
                if(!map2.equals(map1)){
                       //Equal lot
                    if(map2.get(Utils.LOT_NUMBER_TAG).equals(map1.get(Utils.LOT_NUMBER_TAG)) &&
                       //Not finished
                       !map2.get(Utils.STATUS_TAG).equals("2")){
                        map1.put(Utils.NEXT_OPERATION_TAG,map2.get(Utils.OPERATION_NAME_TAG));
                        break;
                    }
                }
            }
        }
        //Will make sure that we only see the first operation of each id
        HashMap<String,HashMap<String,String>> uniqueID =
                new HashMap<String, HashMap<String, String>>();
        //Only one for each ID and only the unfinished ones
        for(int i = operationList.size()-1;i>=0;i--){
            if(!operationList.get(i).get(Utils.STATUS_TAG).equals("2"))
                uniqueID.put(operationList.get(i).get(Utils.ID_TAG),operationList.get(i));
        }
        //Holds the value of the uniqueOperations that are unfinished
        ArrayList<HashMap<String,String>> uniqueOperations =
                new ArrayList<HashMap<String, String>>(uniqueID.values());
        Collections.sort(uniqueOperations,new LotNumberComparator());
        //Needs to be a final because it's called from an inner class
        final ArrayList<HashMap<String,String>> uniqueOperationsf =
                new ArrayList<HashMap<String, String>>(uniqueID.values());
        ListView listView = (ListView)findViewById(R.id.orderList);
        ListAdapter adapter = new CustomAdapter(
                OperationsList.this,
                uniqueOperationsf
        );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Sends the operations part of the JSONObject to the next activity
                sendOperationMessage(uniqueOperationsf.get(+position));
            }
        });
    }

    public class LotNumberComparator implements Comparator<HashMap<String,String>> {
        @Override
        public int compare(HashMap<String,String> operation1, HashMap<String,String> operation2){
            return operation1.get(Utils.LOT_NUMBER_TAG).compareTo(operation2.get(Utils.LOT_NUMBER_TAG));
        }
    }

    public class NumberComparator implements Comparator<HashMap<String,String>>{
        @Override
        public int compare(HashMap<String,String> operation1, HashMap<String,String>operation2){
            return
              operation1.get(Utils.OPERATION_NUMBER_TAG).compareTo(operation2.get(Utils.OPERATION_NUMBER_TAG));
        }
    }

    public void createList(){
        ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        try{
            //Gets the operation from a JSONArray and put them in an ArrayList of Hashmaps with
            //the key value pairs.
            JSONArray json = new JSONArray(operations);
            for(int i = 0; i< json.length(); i++){
                JSONObject object = json.getJSONObject(i);
                String projectName = object.getString(Utils.PROJECT_NAME_TAG);
                String part = object.getString(Utils.PART_TAG);
                String opNo = object.getString(Utils.OPERATION_NUMBER_TAG);
                String pID = object.getString(Utils.PROGRESS_ID_TAG);
                String customer = object.getString(Utils.CUSTOMER_TAG);
                String ownerId = object.getString(Utils.OWNER_ID_TAG);
                String machine = object.getString(Utils.MACHINE_TAG);
                String status = object.getString(Utils.STATUS_TAG);
                String orderID = object.getString(Utils.ORDER_ID_TAG);
                String time = object.getString(Utils.TIME_TAG);
                String lot = object.getString(Utils.LOT_NUMBER_TAG);
                String startedAt = object.getString(Utils.STARTED_AT_TAG);
                String updatedAt = object.getString(Utils.UPDATED_AT_TAG);
                String operation_name = object.getString(Utils.OPERATION_NAME_TAG);
                String id = object.getString(Utils.ID_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(Utils.MY_STARTED_AT_TAG,"0");
                map.put(Utils.UPDATED_AT_TAG,updatedAt);
                map.put(Utils.PROJECT_NAME_TAG,projectName);
                map.put(Utils.TIME_SWAP_TAG,"0");
                map.put(Utils.PART_TAG,part);
                map.put(Utils.PROGRESS_ID_TAG,pID);
                map.put(Utils.OPERATION_NUMBER_TAG,opNo);
                map.put(Utils.ORDER_ID_TAG,orderID);
                map.put(Utils.CUSTOMER_TAG,customer);
                map.put(Utils.MACHINE_TAG,machine);
                map.put(Utils.OWNER_ID_TAG,ownerId);
                map.put(Utils.LOT_NUMBER_TAG,lot);
                map.put(Utils.STATUS_TAG,status);
                map.put(Utils.TIME_TAG,time);
                map.put(Utils.OPERATION_NAME_TAG,operation_name);
                map.put(Utils.ID_TAG,id);
                map.put(Utils.STARTED_AT_TAG,startedAt);
                map.put(Utils.STOPPED_TAG,"false");
                map.put(Utils.OWNER_NAME_TAG,session.getUserDetails().get(SessionManager.KEY_USER));
                //Assuming the title is the ID
                operationList.add(map);
                updateOrCreateList();
            }
        }catch(JSONException e){
            e.printStackTrace();
            Log.e("JSON Error", "JSON error on create list " + e.toString());
        }finally{
            //If for some reason he couldn't get the elements:
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

    private class CustomAdapter extends BaseAdapter{
        private LayoutInflater inflater;
        private ArrayList<HashMap<String,String>> data;

        public CustomAdapter(Context context, ArrayList<HashMap<String,String>> data){
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        public int getCount(){
            return data.size();
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public HashMap<String,String> getItem(int position){
            return data.get(+position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            OperationViewHolder holder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.operations_list_v,null);
                holder = new OperationViewHolder();
                holder.operation_name = (TextView)convertView.findViewById(R.id.operation_list_part);
                holder.operation_machine =
                        (TextView)convertView.findViewById(R.id.operation_list_machine);
                holder.operation_lot = (TextView)convertView.findViewById(R.id.operation_list_lot);
                holder.operation_time = (TextView)convertView.findViewById(R.id.operation_list_time);
                holder.operation_project = (TextView)convertView.findViewById(R.id.operation_list_project);
                holder.background = (LinearLayout)convertView.findViewById(R.id.operation_list_background);
                convertView.setTag(holder);
            }else{
                holder = (OperationViewHolder)convertView.getTag();
            }

            //Setting up font
            //TODO: Find a smarter way to do this
            Typeface font = Typeface.createFromAsset(getAssets(),"HelveticaNeue_Lt.ttf");
            holder.operation_name.setTypeface(font, Typeface.BOLD);
            holder.operation_machine.setTypeface(font);
            holder.operation_lot.setTypeface(font);
            holder.operation_time.setTypeface(font);

            holder.operation_name.setText(data.get(+position).get(Utils.PART_TAG));
            holder.operation_machine.setText(data.get(+position).get(Utils.MACHINE_TAG));
            holder.operation_lot.setText("Lot: "+data.get(+position).get(Utils.LOT_NUMBER_TAG));
            holder.operation_time.setText("Time: "+data.get(+position).get(Utils.TIME_TAG));

            if(getItem(position).get(Utils.STATUS_TAG).equals("1")){
                holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_ORANGE_COLOR));
            }else{
                holder.background.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }

            return convertView;
        }

    }

    private static class OperationViewHolder {
        TextView operation_name;
        TextView operation_machine;
        TextView operation_lot;
        TextView operation_time;
        TextView operation_project;
        LinearLayout background;
    }

}
