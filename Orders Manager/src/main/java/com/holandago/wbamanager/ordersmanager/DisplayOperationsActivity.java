package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.holandago.wbamanager.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayOperationsActivity extends Activity {
    private ListView listView;
    private static final String NAME_TAG = "operation_name";
    private static final String MACHINE_TAG = "machine";
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

        try {
            JSONArray jArray = new JSONArray(json);
        }catch(JSONException e){
            e.printStackTrace();
        }

        listView = (ListView)findViewById(R.id.operationsList);


    }



}
