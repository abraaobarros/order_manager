package test;

import android.content.Context;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import com.holandago.wbamanager.library.Utils;
import com.holandago.wbamanager.model.UserOperations;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maestro on 26/07/14.
 */

public class UserOperationsTest extends InstrumentationTestCase {

    /*
    Test Script:
    1- Initialization
    2- Adding operations
    3- Getting an operation
    4- Flushing the list
    5- Change Status
     */


    public void testInitializeOperationList() throws Exception{
        Context context = new MockContext();
        UserOperations.flush();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();

        assertEquals(true, operationList.isEmpty());
    }

    public void testAddOperation() throws Exception{
        Context context = new MockContext();
        UserOperations.flush();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        HashMap<String,String> operation = new HashMap<String, String>();
        operationList.add(operation);

        assertEquals(1, UserOperations.getOperationsList().size());
    }

    public void testGetOperation() throws Exception{

        Context context = new MockContext();
        UserOperations.flush();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        HashMap<String,String> operation = new HashMap<String, String>();
        String operationID = "1";
        String operationLotNumber = "1";
        operation.put(Utils.ID_TAG,operationID);
        operation.put(Utils.LOT_NUMBER_TAG,operationLotNumber);
        operationList.add(operation);
        HashMap<String, String> getOp =
                UserOperations.getOperation(operationID, operationLotNumber);
        assertEquals(operation,getOp);

    }

    public void testFlushOperationList() throws Exception {
        Context context = new MockContext();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        HashMap<String,String> operation = new HashMap<String, String>();
        operationList.add(operation);
        UserOperations.flush();

        assertEquals(0, UserOperations.getOperationsList().size());
    }

    public void testStartOperation() throws Exception {
        Context context = new MockContext();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        String doneAt = String.format("%d",SystemClock.elapsedRealtime());
        HashMap<String,String> operation = new HashMap<String, String>();
        String operationID = "1";
        String operationLotNumber = "1";
        operation.put(Utils.ID_TAG,operationID);
        operation.put(Utils.LOT_NUMBER_TAG,operationLotNumber);
        operation.put(Utils.STATUS_TAG,"0");
        operationList.add(operation);
        UserOperations.changeOperationStatus(
                operationID,operationLotNumber,UserOperations.START,doneAt);

        assertEquals("1",
                UserOperations.getOperation(operationID,operationLotNumber).get(Utils.STATUS_TAG));
    }

    public void testStopOperation() throws Exception {
        Context context = new MockContext();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        String doneAt = String.format("%d",SystemClock.elapsedRealtime());
        HashMap<String,String> operation = new HashMap<String, String>();
        String operationID = "1";
        String operationLotNumber = "1";
        operation.put(Utils.ID_TAG,operationID);
        operation.put(Utils.LOT_NUMBER_TAG,operationLotNumber);
        operation.put(Utils.STATUS_TAG,"0");
        operationList.add(operation);
        UserOperations.changeOperationStatus(
                operationID,operationLotNumber,UserOperations.START,doneAt);

        assertEquals("true",
                UserOperations.getOperation(operationID,operationLotNumber).get(Utils.STOPPED_TAG));
    }


    public void testFinishOperation() throws Exception {
        Context context = new MockContext();
        ArrayList<HashMap<String, String>> operationList = UserOperations.getOperationsList();
        HashMap<String,String> operation = new HashMap<String, String>();
        String operationID = "1";
        String operationLotNumber = "1";
        operation.put(Utils.ID_TAG,operationID);
        operation.put(Utils.LOT_NUMBER_TAG,operationLotNumber);
        operation.put(Utils.STATUS_TAG,"1");
        operationList.add(operation);
        try {
            UserOperations.changeOperationStatus(
                    operationID, operationLotNumber, UserOperations.FINISH, "");
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        assertEquals("2",
                operation.get(Utils.STATUS_TAG));
    }

}
