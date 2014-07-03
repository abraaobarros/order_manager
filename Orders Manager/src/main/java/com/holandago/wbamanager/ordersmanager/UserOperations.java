package com.holandago.wbamanager.ordersmanager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maestro on 02/07/14.
 */
public class UserOperations {
    private static ArrayList<HashMap<String,String>> operationsList;
    private static UserOperations me;
    private static final String ID_TAG = "id";
    private static final String LOT_NUMBER_TAG = "lot";
    private static final String STATUS_TAG = "status";
    private static final String STARTED_AT_TAG = "started_at";
    private static final String STOPPED_AT_TAG = "stopped_at";
    private static final String FINISHED_AT_TAG = "finished_at";
    public static final int START = 0;
    public static final int FINISH = 1;
    public static final int STOP = 2;

    private UserOperations(){

    }

    public static ArrayList<HashMap<String,String>> getOperationsList(){
        if(operationsList==null){
            me = new UserOperations();
            operationsList = new ArrayList<HashMap<String, String>>();
        }
        return operationsList;
    }

    public static JSONObject getOperationJSON(int operationID, int lotNumber){
        //Never call this method before having the operationsList setup
        for(HashMap<String,String> map : operationsList){
            if(map.get(ID_TAG).equals(operationID)){
                if(map.get(LOT_NUMBER_TAG).equals(lotNumber))
                    return new JSONObject(map);
            }
        }
        return null;
    }

    public static void changeOperationStatus(
        String operationID,String lotNumber,int action, String doneAt){
        if(action == START){
            for(HashMap<String,String> map : operationsList){
                if(map.get(ID_TAG).equals(operationID)){
                    if(map.get(LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(STATUS_TAG,"1");
                        map.put(STARTED_AT_TAG,doneAt);
                    }
                }
            }
        }else
        if(action == FINISH){
            for(HashMap<String,String> map : operationsList){
                if(map.get(ID_TAG).equals(operationID)){
                    if(map.get(LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(STATUS_TAG,"2");
                        map.put(FINISHED_AT_TAG,doneAt);
                    }
                }
            }
        }else
        if(action == STOP){
            for(HashMap<String,String> map : operationsList){
                if(map.get(ID_TAG).equals(operationID)){
                    if(map.get(LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(STOPPED_AT_TAG,doneAt);
                    }
                }
            }
        }
    }
}
