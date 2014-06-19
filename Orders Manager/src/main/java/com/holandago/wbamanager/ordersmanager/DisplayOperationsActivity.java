package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.holandago.wbamanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class DisplayOperationsActivity extends Activity {
    public final static String STATUS_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.STATUS_MESSAGE";
    public final static String ID_MESSAGE =
            "com.holandago.wbamanager.ordersmanager.ID_MESSAGE";
    private ListView listView;
    private static final String NAME_TAG = "operation_name";
    private static final String MACHINE_TAG = "machine";
    private static final String STATUS_TAG = "status";
    private static final String ID_TAG = "id";
    private ArrayList<HashMap<String,String>> operationsList =
            new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_operations);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra(OrdersList.ORDER_TITLE_MESSAGE));
        String operations = intent.getStringExtra(OrdersList.OPERATIONS_MESSAGE);
        createList(operations);

    }

    public void createList(String json){

        try{
            JSONArray array = new JSONArray(json);
            for(int i = 0; i< json.length(); i++){
                JSONObject object = array.getJSONObject(i);
                String name = object.getString(NAME_TAG);
                String machine = object.getString(MACHINE_TAG);
                String status = object.getString(STATUS_TAG);
                String id = object.getString(ID_TAG);
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(NAME_TAG,name);
                map.put(MACHINE_TAG,machine);
                map.put(STATUS_TAG,status);
                map.put(ID_TAG,id);
                //Assuming the title is the ID
                if(!operationsList.contains(map)) {
                    operationsList.add(map);
                }
                listView = (ListView)findViewById(R.id.operationsList);
                ListAdapter adapter = new CustomAdapter(
                        DisplayOperationsActivity.this, //Context
                        operationsList//Data
                );
                listView.setAdapter(adapter);

            }
        }catch(JSONException e){
            e.printStackTrace();
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
                holder.operation_name = (TextView)convertView.findViewById(R.id.operation_name);
                holder.operation_machine =
                        (TextView)convertView.findViewById(R.id.operation_machine);
                holder.operation_status = (TextView)convertView.findViewById(R.id.operation_status);
                holder.start_operation_button =
                        (Button) convertView.findViewById(R.id.start_operation_button);
                holder.start_operation_button.setOnClickListener
                        (new ButtonListener(position,"start"));
                holder.finish_operation_button =
                        (Button) convertView.findViewById(R.id.finish_operation_button);
                holder.finish_operation_button.setOnClickListener(
                        new ButtonListener(position,"finish"));
                convertView.setTag(holder);
            }else{
                holder = (OperationViewHolder)convertView.getTag();
            }

            holder.operation_name.setText(data.get(+position).get(NAME_TAG));
            holder.operation_machine.setText(data.get(+position).get(MACHINE_TAG));
            holder.operation_status.setText(data.get(+position).get(STATUS_TAG));

            if(getItem(position).get(STATUS_TAG).equals("1")){
                holder.start_operation_button.setEnabled(false);
                holder.start_operation_button.setEnabled(true);
            }else{
                holder.start_operation_button.setEnabled(true);
                holder.finish_operation_button.setEnabled(false);
            }

            return convertView;
        }


    }

    public class ButtonListener implements OnClickListener {
        private int position;
        private String handle;

        public ButtonListener(int position, String handle){
            this.position = position;
            this.handle = handle;
        }

        @Override
        public void onClick(View v){

        }
    }


    private static class OperationViewHolder {
        TextView operation_name;
        TextView operation_machine;
        TextView operation_status;
        Button start_operation_button;
        Button finish_operation_button;
    }



}
