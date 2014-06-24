package com.holandago.wbamanager.ordersmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.holandago.wbamanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayLotsActivity extends ActionBarActivity {

    private ListView listView;
    private static final String LOT_NUMBER_TAG = "lot";
    private static final String OPERATIONS_TAG = "operations";
    private ArrayList<HashMap<String,String>> lotsList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_lots);
        Intent intent = getIntent();
        final String operations = intent.getStringExtra(OrdersList.OPERATIONS_MESSAGE);
        final String orderTitle = intent.getStringExtra(OrdersList.ORDER_TITLE_MESSAGE);
        setTitle(orderTitle);
        createList(operations, orderTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflating the actionBar menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_lots_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==0){
            if(resultCode == RESULT_OK){
                final String operations = data.getStringExtra(OrdersList.OPERATIONS_MESSAGE);
                final String title = data.getStringExtra(OrdersList.ORDER_TITLE_MESSAGE);
                createList(operations,title);
            }
        }
    }


    public void sendOperationsMessage(String message, String orderTitle,String lotNumber){
        Intent intent = new Intent(this, DisplayOperationsActivity.class);
        intent.putExtra(OrdersList.OPERATIONS_MESSAGE,message);
        intent.putExtra(OrdersList.ORDER_TITLE_MESSAGE, orderTitle);
        intent.putExtra(OrdersList.LOT_NUMBER_MESSAGE, lotNumber);
        startActivityForResult(intent, 0);
    }

    public void createList(final String operations,final String orderTitle){

        try{
            JSONArray array = new JSONArray(operations);
            lotsList = new ArrayList<HashMap<String, String>>();
            for(int i = 0; i< operations.length(); i++){
                JSONObject object = array.getJSONObject(i);
                String lot = object.getString(LOT_NUMBER_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(LOT_NUMBER_TAG,"Lot Number: "+lot);
                //Assuming the title is the ID
                if(!lotsList.contains(map))
                    lotsList.add(map);
                listView = (ListView)findViewById(R.id.lotsList);
                ListAdapter adapter = new SimpleAdapter(
                        DisplayLotsActivity.this, //Context
                        lotsList, //Data
                        R.layout.lots_list_v, //Layout
                        new String[]{LOT_NUMBER_TAG}, //from
                        new int[]{R.id.lot_number} //to
                );
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        //Sends the operations part of the JSONObject to the next activity
                        sendOperationsMessage(operations,
                                orderTitle,
                                lotsList.get(+position).get(LOT_NUMBER_TAG));
                    }
                });

            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

}
