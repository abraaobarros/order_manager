package com.holandago.wbamanager.ordersmanager;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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


public class OrdersList extends ListActivity {
    private static String targetUrl = "http://wba-urbbox.herokuapp.com/rest/orders";
    private ArrayList<HashMap<String,String>> orderList = new ArrayList<HashMap<String, String>>();
    private ListView listView;
    private TextView orderName;
    private Button BtnGetData;
    private static final String ORDER_TITLE_TAG = "title";
    private JSONArray orderJSON = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        BtnGetData = (Button)findViewById(R.id.getData);
        BtnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new JSONParse().execute();
            }
        });

    }

    private class JSONParse extends AsyncTask<String,String, JSONObject>{
        private ProgressDialog pDialog;

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.getJSONfromUrl(targetUrl);
            return json;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            orderName = (TextView)findViewById(R.id.orderName);
            pDialog = new ProgressDialog(OrdersList.this);
            pDialog.setMessage("Getting data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject json){
            pDialog.dismiss();
            try{
                orderJSON = new JSONArray(json);
                for(int i = 0; i< orderJSON.length(); i++){
                    JSONObject object = orderJSON.getJSONObject(i);
                    String title = object.getString(ORDER_TITLE_TAG);
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put(ORDER_TITLE_TAG,title);
                    listView = (ListView)findViewById(R.id.orderList);
                    ListAdapter adapter = new SimpleAdapter(
                            OrdersList.this, //Context
                            orderList, //Data
                            R.layout.order_list_v, //Layout
                            new String[]{ORDER_TITLE_TAG}, //from
                            new int[]{R.id.orderName} //to
                    );
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Toast.makeText(
                                    OrdersList.this, //Context
                                    "You Clicked at " + orderList.get(+position).get("name"), //Msg
                                    Toast.LENGTH_SHORT //Lenght
                            ).show();
                        }
                    });

                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }

    }

}
