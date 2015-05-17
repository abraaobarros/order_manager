package com.holandago.wbamanager.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.holandago.wbamanager.library.Utils;
import com.holandago.wbamanager.ordersmanager.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by razu on 14/01/15.
 */
public class Operation {

    HashMap<String,String> map = new HashMap<String, String>();
    String json;

    public Operation(JsonObject object){
        json = object.toString();
        parseJson(object);
    }

    public Operation(JsonElement jsonElement) {
        JsonObject o = jsonElement.getAsJsonObject();
        json = o.toString();
        parseJson(o);
    }

    private void parseJson(JsonObject object){
        map.put(Utils.WBA_NUMBER_TAG,object.get(Utils.WBA_NUMBER_TAG).getAsString());
        map.put(Utils.REAL_TIME_TAG,object.get(Utils.REAL_TIME_TAG).getAsString());
        map.put(Utils.PROJECT_NAME_TAG,object.get(Utils.PROJECT_NAME_TAG).getAsString());
        map.put(Utils.PART_TAG,object.get(Utils.PART_TAG).getAsString());
        map.put(Utils.OPERATION_NUMBER_TAG,object.get(Utils.OPERATION_NUMBER_TAG).getAsString());
        map.put(Utils.PROGRESS_ID_TAG, object.get(Utils.PROGRESS_ID_TAG).getAsString());
        map.put(Utils.CUSTOMER_TAG,object.get(Utils.CUSTOMER_TAG).getAsString());
        map.put(Utils.OWNER_ID_TAG,object.get(Utils.OWNER_ID_TAG).getAsString());
        map.put(Utils.MACHINE_TAG,object.get(Utils.MACHINE_TAG).getAsString());
        map.put(Utils.STATUS_TAG,object.get(Utils.STATUS_TAG).getAsString());
        map.put(Utils.ORDER_ID_TAG,object.get(Utils.ORDER_ID_TAG).getAsString());
        map.put(Utils.TIME_TAG,object.get(Utils.TIME_TAG).getAsString());
        map.put(Utils.LOT_NUMBER_TAG,object.get(Utils.LOT_NUMBER_TAG).getAsString());
        map.put(Utils.STARTED_AT_TAG,object.get(Utils.STARTED_AT_TAG).getAsString());
        map.put(Utils.TIME_SWAP_TAG,object.get(Utils.TIME_SWAP_TAG).getAsString());
    }

    public String toJson(){
        return json;
    }
}
