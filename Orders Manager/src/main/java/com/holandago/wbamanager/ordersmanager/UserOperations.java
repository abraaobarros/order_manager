package com.holandago.wbamanager.ordersmanager;

import com.holandago.wbamanager.library.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maestro on 02/07/14.
 */
public class UserOperations {
    private static ArrayList<HashMap<String,String>> operationsList;
    private static UserOperations me;
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
            if(map.get(Utils.ID_TAG).equals(operationID)){
                if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber))
                    return new JSONObject(map);
            }
        }
        return null;
    }

    public static void flush(){
        me = new UserOperations();
        operationsList = new ArrayList<HashMap<String, String>>();
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
                    }
                }
            }
        }else
        if(action == STOP){
            for(HashMap<String,String> map : operationsList){
                if(map.get(Utils.ID_TAG).equals(operationID)){
                    if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                        map.put(Utils.TIME_SWAP_TAG,doneAt);
                        map.put(Utils.STOPPED_TAG,"true");
                    }
                }
            }
        }
    }
}
