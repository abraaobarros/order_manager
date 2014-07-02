package com.holandago.wbamanager.ordersmanager;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * Created by maestro on 01/07/14.
 */
public class HttpClient {
    private static DefaultHttpClient defaultHttpClient;
    private static String session_id;
    private static HttpClient me;
    private HttpClient(){

    }
    public static DefaultHttpClient getDefaultHttpClient(){
        if(defaultHttpClient == null){
            defaultHttpClient = new DefaultHttpClient();
            me = new HttpClient();
            defaultHttpClient.addResponseInterceptor(me.new SessionKeeper());
            defaultHttpClient.addRequestInterceptor(me.new SessionAdder());
        }
        return defaultHttpClient;
    }

    private class SessionKeeper implements HttpResponseInterceptor{
        @Override
        public void process(HttpResponse response, HttpContext context)
        throws HttpException, IOException{
            Header[] headers = response.getHeaders("Set-Cookie");
            if(headers!=null && headers.length == 1){
                session_id = headers[0].getValue();
            }
        }
    }

    private class SessionAdder implements HttpRequestInterceptor{
        @Override
        public void process(HttpRequest request, HttpContext context)
        throws HttpException, IOException{
            if(session_id != null){
                request.setHeader("Cookie", session_id);
            }
        }
    }
}
