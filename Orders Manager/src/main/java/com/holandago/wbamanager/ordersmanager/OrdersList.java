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
    private ArrayList<HashMap<String,String>> orderList = new ArrayList<HashMap<String, String>>();
    private ArrayList<String> existingOrders = new ArrayList<String>();
    private ListView listView;
    private TextView orderTitle;
    private ImageView BtnGetData;
    private static final String ORDER_TITLE_TAG = "title";
    private static final String OPERATIONS_TAG = "operations";
    private JSONArray orderJSON = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        new JSONParse().execute();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        BtnGetData = (ImageView)findViewById(R.id.getData);
        BtnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONParse().execute();
            }
        });

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
                String contents = data.getStringExtra("SCAN_RESULT");
                Toast.makeText(this, contents,Toast.LENGTH_LONG).show();
            }else
            if(resultCode == RESULT_CANCELED){
                //TODO: Handle cancel
            }
        }
    }


    public void sendOperationsMessage(String message, String orderTitle){
        Intent intent = new Intent(this, DisplayOperationsActivity.class);
        intent.putExtra(OPERATIONS_MESSAGE,message);
        intent.putExtra(ORDER_TITLE_MESSAGE, orderTitle);
        startActivity(intent);
    }


    private class JSONParse extends AsyncTask<String,String, JSONArray>{
        private ProgressDialog pDialog;

        @Override
        protected JSONArray doInBackground(String... strings) {
            JSONParser jsonParser = new JSONParser();
            JSONArray json = jsonParser.getJSONfromUrl(targetUrl);
            return json;
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
            try{
                for(int i = 0; i< json.length(); i++){
                    JSONObject object = json.getJSONObject(i);
                    String title = object.getString(ORDER_TITLE_TAG);
                    String operations = object.getString(OPERATIONS_TAG);
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put(ORDER_TITLE_TAG,title);
                    map.put(OPERATIONS_TAG,operations);
                    if(!existingOrders.contains(title)){
                        existingOrders.add(title);
                        orderList.add(map);
                    }else{
                        if(!orderList.contains(map)) {
                            int indexToUpdate = existingOrders.indexOf(title);
                            orderList.remove(indexToUpdate);
                            orderList.add(indexToUpdate,map);
                        }
                    }
                    //Assuming the title is the ID

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

    }

}
