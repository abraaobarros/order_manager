package com.holandago.wbamanager.library;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by maestro on 14/06/14.
 */
public class JSONParser {
    static InputStream input = null;
    static JSONArray jArray = null;
    static String json = "";

    public JSONParser(){

    }
    public JSONArray getJSONfromUrl(String url){
        //Making the http request
        try{
            //Default Http Client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            input = httpEntity.getContent();
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        //Reading (Buffered) the HTTP request
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine())!=null){
                sb.append(line+"\n");
            }
            input.close();
            json = sb.toString();
        }catch(Exception e){
            Log.e("Buffer Error", "Error converting http request " + e.toString());
        }
        try{
            jArray = (new JSONObject(json)).getJSONArray("data");
        }catch(JSONException e){
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jArray;
    }
}
