package com.holandago.wbamanager.ordersmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.holandago.wbamanager.R;

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
import java.util.HashMap;


public class DisplayOperationsActivity extends Activity {
    private ProgressDialog pDialog;
    private ListView listView;
    private static final String NAME_TAG = "operation_name";
    private static final String MACHINE_TAG = "machine";
    private static final String TIME_TAG = "time";
    private static final String STATUS_TAG = "status";
    private static final String ID_TAG = "progress_id";
    private static final String LOT_NUMBER_TAG = "lot";
    private ArrayList<HashMap<String,String>> operationsList =
            new ArrayList<HashMap<String, String>>();
    private String startUrl;
    private String finishUrl;
    private String operations;
    private String lotNumber;
    private String orderTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_operations);
        Intent intent = getIntent();
        orderTitle = intent.getStringExtra(OrdersList.ORDER_TITLE_MESSAGE);
        lotNumber = intent.getStringExtra(OrdersList.LOT_NUMBER_MESSAGE);
        setTitle(lotNumber+", from order:"+orderTitle);
        operations = intent.getStringExtra(OrdersList.OPERATIONS_MESSAGE);
        createList(operations,lotNumber);

    }

    @Override
    public void onBackPressed(){
        sendOperationsMessage(operations,orderTitle);
    }

    public void createList(String operations,String lotNumber){

        try{
            JSONArray array = new JSONArray(operations);
            operationsList = new ArrayList<HashMap<String, String>>();
            for(int i = 0; i< operations.length(); i++){
                JSONObject object = array.getJSONObject(i);
                String lot = object.getString(LOT_NUMBER_TAG);
                if(("Lot Number: "+lot).equals(lotNumber)) {
                    String name = object.getString(NAME_TAG);
                    String machine = object.getString(MACHINE_TAG);
                    String status = object.getString(STATUS_TAG);
                    String time = object.getString(TIME_TAG);
                    String id = object.getString(ID_TAG);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(NAME_TAG, name);
                    map.put(MACHINE_TAG, machine);
                    map.put(STATUS_TAG, status);
                    map.put(TIME_TAG, time);
                    map.put(ID_TAG, id);
                    //Assuming the title is the ID
                    operationsList.add(map);
                    listView = (ListView) findViewById(R.id.operationsList);
                    ListAdapter adapter = new CustomAdapter(
                            DisplayOperationsActivity.this, //Context
                            operationsList//Data
                    );
                    listView.setAdapter(adapter);
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void sendOperationsMessage(String message, String orderTitle){
        //Travel to DisplayLotsActivity
        Intent intent = new Intent(this, DisplayLotsActivity.class);
        intent.putExtra(OrdersList.OPERATIONS_MESSAGE,message);
        intent.putExtra(OrdersList.ORDER_TITLE_MESSAGE, orderTitle);
        startActivity(intent);
    }

    public void changeStatus(String progressID, boolean start, int position){
        //Updates the strings and maps used
        if(start) {
            operationsList.get(position).put(STATUS_TAG,"1");
            try {
                JSONArray operationsJson = new JSONArray(operations);
                for(int i = 0; i<operations.length();i++){
                    JSONObject object = operationsJson.getJSONObject(i);
                    String lot = object.getString(LOT_NUMBER_TAG);
                    if(("Lot Number: "+lot).equals(lotNumber)){
                        String pID = object.getString(ID_TAG);
                        if(pID.equals(progressID)){
                            object.put(STATUS_TAG,"1");
                            break;
                        }
                    }
                }
                operations = operationsJson.toString();
            }catch(JSONException e){
                e.printStackTrace();
            }
        }else{
            operationsList.get(position).put(STATUS_TAG,"2");
            try {
                JSONArray operationsJson = new JSONArray(operations);
                for(int i = 0; i<operations.length();i++){
                    JSONObject object = operationsJson.getJSONObject(i);
                    String lot = object.getString(LOT_NUMBER_TAG);
                    if(("Lot Number: "+lot).equals(lotNumber)){
                        String pID = object.getString(ID_TAG);
                        if(pID.equals(progressID)){
                            object.put(STATUS_TAG,"2");
                            break;
                        }
                    }
                }
                operations = operationsJson.toString();
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    //----- Adapter for the ListItem ------------------
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
                //Start and Finish Buttons
                holder.start_operation_button =
                        (Button) convertView.findViewById(R.id.start_operation_button);
                holder.finish_operation_button =
                        (Button) convertView.findViewById(R.id.finish_operation_button);
                //Setting the listeners
                holder.start_operation_button.setOnClickListener
                        (new ButtonListener(position,"start",holder.finish_operation_button));
                holder.finish_operation_button.setOnClickListener(
                        new ButtonListener(position,"finish",holder.start_operation_button));
                convertView.setTag(holder);
            }else{
                holder = (OperationViewHolder)convertView.getTag();
            }

            holder.operation_name.setText(data.get(+position).get(NAME_TAG));
            holder.operation_machine.setText(data.get(+position).get(MACHINE_TAG));
            holder.operation_status.setText(data.get(+position).get(STATUS_TAG));

            if(getItem(position).get(STATUS_TAG).equals("1")){
                holder.start_operation_button.setVisibility(View.INVISIBLE);
                holder.finish_operation_button.setVisibility(View.VISIBLE);
            }else if(getItem(position).get(STATUS_TAG).equals("0")){
                holder.start_operation_button.setText("Start");
                holder.start_operation_button.setVisibility(View.VISIBLE);
                holder.finish_operation_button.setVisibility(View.INVISIBLE);
            }else{
                holder.start_operation_button.setText("Restart");
                holder.start_operation_button.setVisibility(View.VISIBLE);
                holder.finish_operation_button.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        public class ButtonListener implements OnClickListener {
            private int position;
            private String handle;
            private Button otherButton;

            public ButtonListener(int position, String handle,Button otherButton){
                this.position = position;
                this.handle = handle;
                this.otherButton = otherButton;
            }

            @Override
            public void onClick(View thisButton){
                String id = getItem(position).get(ID_TAG);
                startUrl = "http://wba-urbbox.herokuapp.com/rest/progress/"+id+"/start";
                finishUrl = "http://wba-urbbox.herokuapp.com/rest/progress/"+id+"/finish";
                AsyncGetRequest requester = new AsyncGetRequest();
                if(handle.equals("start")){
                    requester.execute(new String[]{startUrl});
                    changeStatus(id,true,position);
                }else{
                    requester.execute(new String[]{finishUrl});
                    changeStatus(id,false,position);
                }
                thisButton.setVisibility(View.INVISIBLE);
                if(handle.equals("start")) {
                    otherButton.setVisibility(View.VISIBLE);
                }
                else {
                    otherButton.setText("Restart");
                    otherButton.setVisibility(View.VISIBLE);
                }
            }
        }

    }
    //End of the Adapter ------

    //Asynchronous get request to access the url
    private class AsyncGetRequest extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(DisplayOperationsActivity.this);
            pDialog.setMessage("Making the request");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... urls){
            String output = null;
            for(String url:urls){
                output = getOutputFromUrl(url);
            }

            return output;
        }

        private String getOutputFromUrl(String url){
            String output = null;
            try{
                DefaultHttpClient httpClient = new DefaultHttpClient();
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

    private static class OperationViewHolder {
        TextView operation_name;
        TextView operation_machine;
        TextView operation_status;
        Button start_operation_button;
        Button finish_operation_button;
    }



}
