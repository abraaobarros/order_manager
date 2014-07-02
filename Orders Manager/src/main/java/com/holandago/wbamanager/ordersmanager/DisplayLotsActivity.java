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

import com.holandago.wbamanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayLotsActivity extends ActionBarActivity {

    private ListView listView;
    private static final String LOT_NUMBER_TAG = "lot";
    private static final String ORDER_ID_TAG = "lot";
    private static final String OPERATIONS_TAG = "operations";
    private ArrayList<HashMap<String,String>> lotsList = new ArrayList<HashMap<String, String>>();
    String operations;
    String orderID;
    String orderTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_lots);
        Intent intent = getIntent();
        orderID = intent.getStringExtra(OperationsList.ORDER_ID_MESSAGE);
        final String operationsFinal = intent.getStringExtra(OperationsList.OPERATIONS_MESSAGE);
        final String orderTitleFinal = intent.getStringExtra(OperationsList.ORDER_TITLE_MESSAGE);
        operations = intent.getStringExtra(OperationsList.OPERATIONS_MESSAGE);
        orderTitle = intent.getStringExtra(OperationsList.ORDER_TITLE_MESSAGE);
        setTitle(orderTitleFinal);
        createList(operationsFinal, orderTitleFinal);
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
                operations = data.getStringExtra(OperationsList.OPERATIONS_MESSAGE);
                orderTitle = data.getStringExtra(OperationsList.ORDER_TITLE_MESSAGE);
                final String operationsFinal = data.getStringExtra(OperationsList.OPERATIONS_MESSAGE);
                final String orderTitleFinal = data.getStringExtra(OperationsList.ORDER_TITLE_MESSAGE);
                createList(operationsFinal,orderTitleFinal);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        intent.putExtra(OperationsList.OPERATIONS_MESSAGE,operations);
        intent.putExtra(OperationsList.ORDER_TITLE_MESSAGE, orderTitle);
        intent.putExtra(OperationsList.ORDER_ID_MESSAGE, orderID);
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }


    public void sendOperationsMessage(String operations, String orderTitle,String lotNumber){
        Intent intent = new Intent(this, DisplayProgressActivity.class);
        String orderId = "";
        try {
            orderId = new JSONArray(operations).getJSONObject(0).getString(ORDER_ID_TAG);
        }catch(JSONException e){
            e.printStackTrace();
        }
        intent.putExtra(OperationsList.OPERATIONS_MESSAGE,operations);
        intent.putExtra(OperationsList.ORDER_TITLE_MESSAGE, orderTitle);
        intent.putExtra(OperationsList.LOT_NUMBER_MESSAGE, lotNumber);
        intent.putExtra(OperationsList.ORDER_ID_MESSAGE,orderId);
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
