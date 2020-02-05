package com.bitpops.barcode.Helpers;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpRequest {

    public static String Request(Context ct,String type, HashMap<String, String> _params)
    {
        type = type == null ? "POST" : type; // if type null, default to post request
        String url = PreferenceManager.getDefaultSharedPreferences(ct).getString("ApiUrl", "");
        String token = PreferenceManager.getDefaultSharedPreferences(ct).getString("DeviceAuthToken", "");
        HttpURLConnection conn = null;
        HashMap<String, String> params = new HashMap<String,String>();
        params.put("token",token);
        params.putAll(_params);
        //Once the params HashMap is populated, create the StringBuilder that will be used to send them to the server:

        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
        //Then, create the HttpURLConnection, open the connection, and send the POST parameters:

        try{
            String paramsString = sbParams.toString();

            if (type == "GET")
                url += "?" + paramsString;
            URL urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(type);
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            conn.connect();


            Log.e("BARDO url", url);
            Log.e("BARDO type", type);
            Log.e("BARDO params", paramsString);
            if (type == "POST")
            {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
       // Then receive the result that the server sends back:

    try {
        InputStream in = new BufferedInputStream(conn.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        Log.d("test", "result from server: " + result.toString());
        return result.toString();

    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.disconnect();
        }
    }

        return url;
    }
}
