package com.holandago.wbamanager.ordersmanager;

import android.content.Context;
import android.content.SharedPreferences;

import com.holandago.wbamanager.library.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maestro on 02/07/14.
 */
public class UserOperations {
    private static ArrayList<HashMap<String,String>> operationsList = null;
    private static UserOperations me;
    public static final int START = 0;
    public static final int FINISH = 1;
    public static final int STOP = 2;
    public static final String ISTRACKING = "isTracking";
    public static String UPDATE_PREF = "updatePREF";
    public static String UPDATE_REST = "updateREST";
    public static String UPDATE_NUMBER = "updateNUMBER";
    public static boolean isTracking = false;
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    private UserOperations(){

    }

    public static ArrayList<HashMap<String,String>> getOperationsList(){
        if(operationsList==null){
            me = new UserOperations();
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
        me = new UserOperations();
        operationsList = null;
    }

    public static void changeOperationStatus(
        String operationID,String lotNumber,int action, String doneAt){
        //String oldOperationJson = "";
        //HashMap<String,String> newOperationMap = new HashMap<String, String>();
        if(action == START){
            for(HashMap<String,String> map : operationsList){
                if(map.get(Utils.ID_TAG).equals(operationID)){
                    if(map.get(Utils.LOT_NUMBER_TAG).equals(lotNumber)){
                        if(isTracking) {
                            //oldOperationJson = new JSONObject(map).toString();
                        }
                        map.put(Utils.STATUS_TAG,"1");
                        map.put(Utils.STOPPED_TAG,"false");
                        map.put(Utils.MY_STARTED_AT_TAG,doneAt);
                        //newOperationMap = map;
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
                        //newOperationMap = map;
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
                        //newOperationMap = map;
                    }
                }
            }
        }
        /*
        if(isTracking) {
            writeUpdateToPreferences(oldOperationJson, new JSONObject(newOperationMap).toString());
        }
        */

    }

    /*
    public static void writeUpdateToPreferences(String oldOperationJson, String newOperationJson){
        editor.putBoolean(ISTRACKING,isTracking);
        editor.putString(UPDATE_REST,getUpdatesJsonString()+
                "||UPDATE"
                +pref.getInt(UPDATE_NUMBER,1)
                +">>old:"+oldOperationJson
                +">>new:"+newOperationJson);
        editor.putInt(UPDATE_NUMBER,pref.getInt(UPDATE_NUMBER,1)+1);
        editor.commit();
    }

    public static String getUpdatesJsonString() throws NullPointerException{
        if(pref.getString(UPDATE_REST,null)!=null)
            return pref.getString(UPDATE_REST,null);
        else return "";
    }

    public static void startTrackingOfflineActivity(Context context){
        isTracking = true;
        pref = context.getSharedPreferences(UPDATE_PREF,Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static void stopTracking(){
        isTracking = false;
    }
    */
}
