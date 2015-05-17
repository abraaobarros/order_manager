package com.holandago.wbamanager.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.holandago.wbamanager.library.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maestro on 02/07/14.
 */
public class UserOperations {

    private static UserOperations cache = null;
    private static ArrayList<HashMap<String,String>> operationsList = null;
    public static final int START = 0;
    public static final int FINISH = 1;
    public static final int STOP = 2;
    ArrayList<Operation> operationList = new ArrayList<Operation>();
    ArrayList<Operation> filteredOperations = new ArrayList<Operation>();

    private shopFloorSQLiteHelper helperMySQLite;

    public UserOperations(shopFloorSQLiteHelper helper) throws shopFloorSQLiteHelper.URLNotFound{
        Log.d("DEBUGGING HELP","UserOperations initialized with helper");
        setHelperMySQLite(helper);
    }

    public UserOperations(Context c) throws shopFloorSQLiteHelper.URLNotFound{
        load(c);
    }

    public UserOperations getInstace(Context c) throws shopFloorSQLiteHelper.URLNotFound{
        if(cache == null){
            cache = new UserOperations(c);
        }
        return cache;
    }

    public ArrayList<Operation> fetchAll() throws shopFloorSQLiteHelper.URLNotFound{
        if(operationList.isEmpty()){
            operationList = parseRestResponse(helperMySQLite.get(Utils.URLs.LIST_OPERATIONS.url()));
        }
        return operationList;
    }

    public void load(final Context c) throws shopFloorSQLiteHelper.URLNotFound {

        if (helperMySQLite==null){
            helperMySQLite = new shopFloorSQLiteHelper(c);
        }
        helperMySQLite.context = c;

        operationList = parseRestResponse(helperMySQLite.get(Utils.URLs.LIST_OPERATIONS.url()));
    }

    /**
     * takes a jsonString with a jsonArray of operations and add them to the cache
     * @param jsonString
     */
    private ArrayList<Operation> parseRestResponse(String jsonString){
        ArrayList<Operation> op = new ArrayList<Operation>();
        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(jsonString);
        JsonArray jsonArray = tradeElement.getAsJsonObject().get("data").getAsJsonArray();
        for(int i = 0; i<jsonArray.size();i++){
            op.add(new Operation(jsonArray.get(i).getAsJsonObject()));
        }
        return op;
    }



    public  void setHelperMySQLite(shopFloorSQLiteHelper helperMySQLite) throws shopFloorSQLiteHelper.URLNotFound {
        this.helperMySQLite = helperMySQLite;
        load(helperMySQLite.context);
    }


    public static ArrayList<HashMap<String,String>> getOperationsList(){
        if(operationsList==null){
            operationsList = new ArrayList<HashMap<String, String>>();
        }
        return operationsList;
    }



    public static HashMap<String,String> getOperation(String operationID, String lotNumber){
        for(HashMap<String,String> operation : operationsList){
            if(operation.get(Utils.ID_TAG).equals(operationID)){
                if(operation.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                    return operation;
                }
            }
        }
        return null;
    }

    public static void flush(){
        operationsList = null;
    }

    public static void changeOperationStatus(
        String operationID,String lotNumber,int action, String doneAt){
        if(action == START){
            for(HashMap<String,String> map : operationsList){
                if(map.get(Utils.ID_TAG).equals(operationID)){
                    if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(Utils.STATUS_TAG,"1");
                        map.put(Utils.STOPPED_TAG,"false");
                        map.put(Utils.MY_STARTED_AT_TAG,doneAt);
                    }
                }
            }
        }else
        if(action == FINISH){
            for(HashMap<String,String> map : operationsList){
                if(map.get(Utils.ID_TAG).equals(operationID)){
                    if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(Utils.STATUS_TAG,"2");
                        map.put(Utils.FINISHED_AT_TAG,doneAt);
                        map.put(Utils.TIME_SWAP_TAG,"0");
                        map.put(Utils.MY_STARTED_AT_TAG,"0");
                    }
                }
            }
        }else
        if(action == STOP){
            for(HashMap<String,String> map : operationsList){
                if(map.get(Utils.ID_TAG).equals(operationID)){
                    if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(Utils.TIME_SWAP_TAG,doneAt);
                        map.put(Utils.STATUS_TAG,"3");
                    }
                }
            }
        }

    }

}
