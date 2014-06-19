package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class DisplayOperationsActivity extends Activity {
    private ListView listView;
    private static final String NAME_TAG = "operation_name";
    private static final String MACHINE_TAG = "machine";
    private static final String STATUS_TAG = "status";
    private ArrayList<HashMap<String,String>> operationsList =
            new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_operations);
        Intent intent = getIntent();
        String operations = intent.getStringExtra(OrdersList.EXTRA_MESSAGE);
        createList(operations);

    }

    public void createList(String json){

        try{
            for(int i = 0; i< json.length(); i++){
                JSONArray array = new JSONArray(json);
                JSONObject object = array.getJSONObject(i);
                String name = object.getString(NAME_TAG);
                String machine = object.getString(MACHINE_TAG);
                String status = object.getString(STATUS_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(NAME_TAG,name);
                map.put(MACHINE_TAG,machine);
                map.put(STATUS_TAG,status);
                //Assuming the title is the ID
                if(!operationsList.contains(map)) {
                    operationsList.add(map);
                }
                listView = (ListView)findViewById(R.id.operationsList);
                ListAdapter adapter = new SimpleAdapter(
                        DisplayOperationsActivity.this, //Context
                        operationsList, //Data
                        R.layout.operations_list_v, //Layout
                        new String[]{NAME_TAG, MACHINE_TAG,STATUS_TAG}, //from
                        new int[]{R.id.operation_name,R.id.operation_machine,R.id.operation_status,} //to
                );
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                    }
                });

            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        listView = (ListView)findViewById(R.id.operationsList);


    }



}
