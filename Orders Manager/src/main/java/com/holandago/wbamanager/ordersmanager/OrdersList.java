package com.holandago.wbamanager.ordersmanager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.JSONParser;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class OrdersList extends ActionBarActivity {
    private static String targetUrl = "http://wba-urbbox.herokuapp.com/rest/orders";
    public final static String OPERATIONS_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.OPERATIONS_MESSAGE";
    public final static String ORDER_TITLE_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ORDER_TITLE_MESSAGE";
    public final static String LOT_NUMBER_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.LOT_NUMBER_MESSAGE";
    private ArrayList<HashMap<String,String>> orderList = new ArrayList<HashMap<String, String>>();
    private ArrayList<String> existingOrders = new ArrayList<String>();
    private ListView listView;
    private TextView orderTitle;
    private ImageView BtnGetData;
    private static final String ORDER_TITLE_TAG = "title";
    private static final String ORDER_ID_TAG = "order_id";
    private static final String ID_TAG = "id";
    private static final String OPERATIONS_TAG = "operations";
    private static final String LOT_NUMBER_TAG = "lot";
    private JSONArray orderJSON = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        new JSONParse().execute();
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
                new JSONParse().execute();
                return true;
            case R.id.action_qrcode:
                new JSONParse().execute();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                String orderUrl = data.getStringExtra("SCAN_RESULT");
                try {
                    JSONObject json = new JSONObject(orderUrl);
                    String id = json.getString(ORDER_ID_TAG);
                    String lot = "Lot Number: "+json.getString(LOT_NUMBER_TAG);
                    String title="";
                    String operations="";
                    for(HashMap<String,String> map : orderList){
                        if(map.get(ID_TAG).equals(id)){
                            operations = map.get(OPERATIONS_TAG);
                            title = map.get(ORDER_TITLE_TAG);
                            break;
                        }
                    }
                    sendLotOperationMessage(operations, title, lot);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else
            if(resultCode == RESULT_CANCELED){
                onResume();
            }
        }else{
            if(resultCode == RESULT_OK){
                new JSONParse().execute();
            }
        }
    }

    public void sendOperationsMessage(String message, String orderTitle){
        //Travels to DisplayLotsActivity
        Intent intent = new Intent(this, DisplayLotsActivity.class);
        intent.putExtra(OPERATIONS_MESSAGE,message);
        intent.putExtra(ORDER_TITLE_MESSAGE, orderTitle);
        startActivityForResult(intent, 1);
    }

    public void sendLotOperationMessage(String operations, String orderTitle,String lotNumber){
        //Travels to DisplayOperationsActivity
        Intent intent = new Intent(this, DisplayOperationsActivity.class);
        intent.putExtra(OPERATIONS_MESSAGE,operations);
        intent.putExtra(ORDER_TITLE_MESSAGE, orderTitle);
        intent.putExtra(LOT_NUMBER_MESSAGE, lotNumber);
        startActivityForResult(intent, 2);
    }

    public void createList(String orders){
        orderList = new ArrayList<HashMap<String, String>>();
        try{
            JSONArray json = new JSONArray(orders);
            for(int i = 0; i< json.length(); i++){
                JSONObject object = json.getJSONObject(i);
                String title = object.getString(ORDER_TITLE_TAG);
                String operations = object.getString(OPERATIONS_TAG);
                String id = object.getString(ID_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(ORDER_TITLE_TAG,title);
                map.put(OPERATIONS_TAG,operations);
                map.put(ID_TAG,id);
                //Assuming the title is the ID
                orderList.add(map);
                listView = (ListView)findViewById(R.id.orderList);
                ListAdapter adapter = new SimpleAdapter(
                        OrdersList.this, //Context
                        orderList, //Data
                        R.layout.order_list_v, //Layout
                        new String[]{ORDER_TITLE_TAG}, //from
                        new int[]{R.id.order_title} //to
                );
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        //Sends the operations part of the JSONObject to the next activity
                        sendOperationsMessage(orderList.get(+position).get(OPERATIONS_TAG),
                                orderList.get(+position).get(ORDER_TITLE_TAG));
                    }
                });

            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }


    public class JSONParse extends AsyncTask<String,String, JSONArray>{
        private ProgressDialog pDialog;

        @Override
        protected JSONArray doInBackground(String... strings) {
            return new JSONParser().getJSONfromUrl(targetUrl);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            orderTitle = (TextView)findViewById(R.id.title);
            pDialog = new ProgressDialog(OrdersList.this);
            pDialog.setMessage("Getting data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(JSONArray json){
            pDialog.dismiss();
            createList(json.toString());
        }

    }

}
