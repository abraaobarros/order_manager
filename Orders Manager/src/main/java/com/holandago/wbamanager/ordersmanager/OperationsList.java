package com.holandago.wbamanager.ordersmanager;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.JSONParser;
import com.holandago.wbamanager.library.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class OperationsList extends ActionBarActivity {
    private static String targetUrl = Utils.BASE_URL+"/rest/operations";
    public final static String OPERATIONS_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.OPERATIONS_MESSAGE";
    public final static String ORDER_TITLE_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ORDER_TITLE_MESSAGE";
    public final static String ORDER_ID_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ORDER_ID_MESSAGE";
    public final static String OPERATION_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.OPERATION_MESSAGE";
    public final static String IS_FINALIZED_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.IS_FINALIZED_MESSAGE";

    SessionManager session;
    ListView listView;
    boolean isFinalized=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_orders_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        session = new SessionManager(getApplicationContext());
        Intent intent = getIntent();
        if (intent.hasExtra(IS_FINALIZED_MESSAGE))
            isFinalized = true;
        if(session.isLoggedIn())
            getOperations();
        else{
            //startLoginActivity();
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflating the actionBar menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.order_list_actions,menu);
        if(isFinalized){
            MenuItem item = menu.findItem(R.id.action_finalized);
            item.setVisible(false);
            //this.invalidateOptionsMenu();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getOperations();
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
                updateOrCreateList();
                session.logoutUser();
                startLoginActivity();
                return true;
            case R.id.action_finalized:
                seeFinalized();
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
                    if(!operationsFromOrder.isEmpty()) {
                        for(HashMap<String,String> operation : operationList){
                            if(operation.get(Utils.STATUS_TAG).equals("2"))
                                operationsFromOrder.remove(operation);
                        }
                        if(!operationsFromOrder.isEmpty()) {
                            Collections.sort(operationsFromOrder, new NumberComparator());
                            sendOperationMessage(
                                    //Sends first element of the ordered Array
                                    operationsFromOrder.get(0));
                        }else{
                            Toast.makeText(
                                    OperationsList.this,
                                    "All operations finished", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        new SetOwnerTask(lot,id).execute();
                        getOperations();
                        operationList = UserOperations.getOperationsList();
                        for(HashMap<String,String> operation : operationList){
                            if(operation.get(Utils.ORDER_ID_TAG).equals(id)){
                                if(operation.get(Utils.LOT_NUMBER_TAG).equals(lot)){
                                    operationsFromOrder.add(operation);
                                }
                            }
                        }
                        if(!operationsFromOrder.isEmpty()) {
                            final HashMap<String, String> firstOperation = operationsFromOrder.get(0);
                            new AlertDialog.Builder(this)
                                    .setTitle("Added operations of this order")
                                    .setNeutralButton("Go to first", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sendOperationMessage(firstOperation);
                                        }
                                    }).create().show();
                        }else{
                            Toast.makeText(
                                    OperationsList.this,
                                    "All operations finished", Toast.LENGTH_LONG).show();
                        }
                    }
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
                getOperations();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!session.isLoggedIn())
            startLoginActivity();
    }

    private void seeFinalized(){
        Intent intent = new Intent(this, OperationsList.class);
        intent.putExtra(IS_FINALIZED_MESSAGE,"true");
        startActivityForResult(intent, 1);
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
        ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        LotNumberComparator lotComparator = new LotNumberComparator();
        Collections.sort(operationList,lotComparator);
        ArrayList<String> uniqueID = new ArrayList<String>();
        //Holds the value of the uniqueOperations that are unfinished
        ArrayList<HashMap<String,String>> uniqueOperations =
                new ArrayList<HashMap<String, String>>();
        for(HashMap<String,String> operation : operationList){
            if(!isFinalized) {
                if (!uniqueID.contains(operation.get(Utils.ID_TAG)) &&
                        !operation.get(Utils.STATUS_TAG).equals("2")) {
                    uniqueID.add(operation.get(Utils.ID_TAG));
                    uniqueOperations.add(operation);
                }
            }else{
                if (!uniqueID.contains(operation.get(Utils.ID_TAG)) &&
                        operation.get(Utils.STATUS_TAG).equals("2")) {
                    uniqueID.add(operation.get(Utils.ID_TAG));
                    uniqueOperations.add(operation);
                }
            }
        }
        Collections.sort(uniqueOperations,new NumberComparator());

        if(isFinalized){
            for(HashMap<String,String> operation : uniqueOperations){
                if(!operation.get(Utils.STATUS_TAG).equals("2")){
                    uniqueOperations.remove(operation);
                }
            }
        }else{
            for(HashMap<String,String> operation : uniqueOperations){
                if(operation.get(Utils.STATUS_TAG).equals("2")){
                    uniqueOperations.remove(operation);
                }
            }
        }

        Collections.sort(uniqueOperations,new PartComparator());
        int firstOperationFromLastPartPointer=-1;
        //Setting the next and last operations
        //Next Operation
        for (int i = 0; i < uniqueOperations.size() - 1; i++) {
            JSONObject jsonNext = new JSONObject();
            if(uniqueOperations.get(i+1).get(Utils.PART_TAG).equals(uniqueOperations.get(i).get(Utils.PART_TAG))) {
                try {
                    int nextOp = i+1;
                    jsonNext.put(Utils.ID_TAG, uniqueOperations.get(nextOp).get(Utils.ID_TAG));
                    jsonNext.put(
                            Utils.LOT_NUMBER_TAG,
                            uniqueOperations.get(nextOp).get(Utils.LOT_NUMBER_TAG));
                    jsonNext.put(
                            Utils.OPERATION_NAME_TAG,
                            uniqueOperations.get(nextOp).get(Utils.OPERATION_NAME_TAG)
                    );
                    uniqueOperations.get(i).put(
                            Utils.NEXT_OPERATION_TAG,
                            jsonNext.toString()
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    int nextOp = firstOperationFromLastPartPointer + 1;
                    if(nextOp != (i)) {
                        jsonNext.put(Utils.ID_TAG, uniqueOperations.get(nextOp).get(Utils.ID_TAG));
                        jsonNext.put(
                                Utils.LOT_NUMBER_TAG,
                                uniqueOperations.get(nextOp).get(Utils.LOT_NUMBER_TAG));
                        jsonNext.put(
                                Utils.OPERATION_NAME_TAG,
                                uniqueOperations.get(nextOp).get(Utils.OPERATION_NAME_TAG)
                        );
                        uniqueOperations.get(i).put(
                                Utils.NEXT_OPERATION_TAG,
                                jsonNext.toString()
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                firstOperationFromLastPartPointer = i;
            }

        }
        //Last Operation
        for (int i = 1; i < uniqueOperations.size(); i++) {
            JSONObject jsonNext = new JSONObject();
            if(uniqueOperations.get(i-1).get(Utils.PART_TAG).equals(uniqueOperations.get(i).get(Utils.PART_TAG))) {
                try {
                    int lastOp = i-1;
                    jsonNext.put(Utils.ID_TAG, uniqueOperations.get(lastOp).get(Utils.ID_TAG));
                    jsonNext.put(
                            Utils.LOT_NUMBER_TAG,
                            uniqueOperations.get(lastOp).get(Utils.LOT_NUMBER_TAG));
                    jsonNext.put(
                            Utils.OPERATION_NAME_TAG,
                            uniqueOperations.get(lastOp).get(Utils.OPERATION_NAME_TAG)
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                uniqueOperations.get(i).put(
                        Utils.LAST_OPERATION_TAG,
                        jsonNext.toString()
                );
            }
        }


        //Needs to be a final because it's called from an inner class
        final ArrayList<HashMap<String,String>> uniqueOperationsf = uniqueOperations;
        listView = (ListView)findViewById(R.id.orderList);
        ListAdapter adapter = new OperationListAdapter(
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

    public class PartComparator implements Comparator<HashMap<String,String>> {
        @Override
        public int compare(HashMap<String,String> operation1, HashMap<String,String> operation2){
            return operation1.get(Utils.PART_TAG).compareTo(operation2.get(Utils.PART_TAG));
        }
    }

    public class NumberComparator implements Comparator<HashMap<String,String>>{
        @Override
        public int compare(HashMap<String,String> operation1, HashMap<String,String>operation2){
            return
              operation1.get(Utils.OPERATION_NUMBER_TAG).compareTo(operation2.get(Utils.OPERATION_NUMBER_TAG));
        }
    }

    public void createList(String operations){
        ArrayList<HashMap<String,String>> operationList = UserOperations.getOperationsList();
        try{
            //Gets the operation from a JSONArray and put them in an ArrayList of Hashmaps with
            //the key value pairs.
            JSONArray json = new JSONArray(operations);
            for(int i = 0; i< json.length(); i++){
                JSONObject object = json.getJSONObject(i);
                String realTime = object.getString(Utils.REAL_TIME_TAG);
                String wbaNo = object.getString(Utils.WBA_NUMBER_TAG);
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
                String timeSwap = object.getString(Utils.TIME_SWAP_TAG);
                //String updatedAt = object.getString(Utils.UPDATED_AT_TAG);
                String operation_name = object.getString(Utils.OPERATION_NAME_TAG);
                String id = object.getString(Utils.ID_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(Utils.WBA_NUMBER_TAG,wbaNo);
                map.put(Utils.FINISHED_AT_TAG,"0");
                map.put(Utils.REAL_TIME_TAG,realTime);
                map.put(Utils.MY_STARTED_AT_TAG,"0");
                //map.put(Utils.UPDATED_AT_TAG,updatedAt);
                map.put(Utils.PROJECT_NAME_TAG,projectName);
                map.put(Utils.TIME_SWAP_TAG,timeSwap);
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
                map.put(Utils.NEXT_OPERATION_TAG,"null");
                map.put(Utils.ID_TAG,id);
                map.put(Utils.STARTED_AT_TAG,startedAt);
                map.put(Utils.STOPPED_TAG,"false");
                map.put(Utils.OWNER_NAME_TAG,session.getUserDetails().get(SessionManager.KEY_USER));
                //Assuming the title is the ID
                operationList.add(map);
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
                .setTitle("There are no operations from server")
                .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOperations();
                    }
                }).create().show();
    }

    public void getOperations(){
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
            new GetOperationsTask(session.getUserDetails()
                    .get(SessionManager.KEY_ID)).execute();
        }
        else if ( conMgr.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                && conMgr.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
            Toast.makeText(this,
                    "Please connect to the internet to get operations",Toast.LENGTH_LONG).show();
        }
    }

    public void emptyOperations(){
        new AlertDialog.Builder(this)
                .setTitle("There are no operations assigned to you")
                .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOperations();
                    }
                }).create().show();
    }


    public class GetOperationsTask extends AsyncTask<String,String, JSONObject>{
        private ProgressDialog pDialog;
        private String userID;
        private JSONParser parser = new JSONParser();

        public GetOperationsTask(String userID){
            this.userID = userID;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
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
        protected void onPostExecute(JSONObject json){
            pDialog.dismiss();
            try {
                String size = json.getString("size");
                if(!size.equals("0")) {
                    String operations = json.getString("data");
                    createList(operations);
                    updateOrCreateList();
                }else{
                    emptyOperations();
                }
            }catch (NullPointerException e){
                cannotGetOperations();
            }catch(JSONException e){
                e.printStackTrace();
            }
        }

    }

    //Asynchronous get request to access the url
    private class SetOwnerTask extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        private final String setOwnerUrl =
                "http://wba-urbbox.herokuapp.com/rest/set-progresses-owner-by-lot/";
        private String lotNumber;
        private String orderID;


        private SetOwnerTask(String lotNumber, String orderID){
            this.lotNumber = lotNumber;
            this.orderID = orderID;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(OperationsList.this);
            pDialog.setMessage("Making the request");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... urls){
            String output = null;
            output = getOutputFromUrl(setOwnerUrl+
                    "/"+session.getUserDetails().get(SessionManager.KEY_ID)+
                    "/"+orderID+
                    "/"+lotNumber
            );
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

    private class OperationListAdapter extends BaseAdapter{
        private LayoutInflater inflater;
        private ArrayList<HashMap<String,String>> data;

        public OperationListAdapter(Context context, ArrayList<HashMap<String, String>> data){
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
                holder.operation_wbaNo = (TextView)convertView.findViewById(R.id.operation_list_wba_no);
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
            holder.operation_wbaNo.setTypeface(font);
            holder.operation_time.setTypeface(font);
            holder.operation_project.setTypeface(font);

            holder.operation_name.setText(data.get(+position).get(Utils.PART_TAG));
            holder.operation_machine.setText(data.get(+position).get(Utils.MACHINE_TAG));
            holder.operation_wbaNo.setText("WBA Nr.: "+data.get(+position).get(Utils.WBA_NUMBER_TAG));
            holder.operation_time.setText("Zeit: "+data.get(+position).get(Utils.TIME_TAG));
            holder.operation_project.setText(
                    data.get(+position).get(Utils.PROJECT_NAME_TAG));

            if(getItem(position).get(Utils.STATUS_TAG).equals("1") ||
                    getItem(position).get(Utils.STATUS_TAG).equals("3")){
                holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_ORANGE_COLOR));
            }else
            if(getItem(position).get(Utils.STATUS_TAG).equals("2")){
                holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                holder.operation_name.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
                holder.operation_machine.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
                holder.operation_wbaNo.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
                holder.operation_time.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
                holder.operation_project.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
            }else{
                holder.background.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }

            return convertView;
        }

    }

    private static class OperationViewHolder {
        TextView operation_name;
        TextView operation_machine;
        TextView operation_wbaNo;
        TextView operation_time;
        TextView operation_project;
        LinearLayout background;
    }

}
