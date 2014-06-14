package com.holandago.wbamanager.ordersmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
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


public class OrdersList extends Activity {
    private static String targetUrl = "http://wba-urbbox.herokuapp.com/rest/orders";
    private ArrayList<HashMap<String,String>> orderList = new ArrayList<HashMap<String, String>>();
    private ListView listView;
    private TextView orderTitle;
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
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put(ORDER_TITLE_TAG,title);
                    //Assuming the title is the ID
                    if(!orderList.contains(map)) {
                        orderList.add(map);
                    }
                    listView = (ListView)findViewById(R.id.orderList);
                    ListAdapter adapter = new SimpleAdapter(
                            OrdersList.this, //Context
                            orderList, //Data
                            R.layout.order_list_v, //Layout
                            new String[]{ORDER_TITLE_TAG}, //from
                            new int[]{R.id.title} //to
                    );
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Toast.makeText(
                                    OrdersList.this, //Context
                                    "You Clicked at " + orderList.get(+position).get(ORDER_TITLE_TAG), //Msg
                                    Toast.LENGTH_SHORT //Length
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
