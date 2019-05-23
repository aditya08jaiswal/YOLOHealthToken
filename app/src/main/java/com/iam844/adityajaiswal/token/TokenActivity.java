package com.iam844.adityajaiswal.token;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class TokenActivity extends AppCompatActivity {

    private static final String KIOSKID_KEY = "kioskid";

    TextView tvTokenNumber;

    Bundle bundle;

    String EnteredKioskID;

    Thread updateTokenThread;

    boolean STOP = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tvTokenNumber = findViewById(R.id.tokenNumber);

        bundle = getIntent().getExtras();

        String JSONString = bundle.getString("JSONString");
        EnteredKioskID = bundle.getString("EnteredKioskID");

        Log.d("TOKEN ACTIVITY ENTERED ", " KioskID : " + EnteredKioskID);

        try{
            JSONObject jsonObject =(new JSONObject(JSONString)).getJSONObject("body");
            int tokenNumber = jsonObject.getInt("current_token_number");

            String strTokenNumber = "" + tokenNumber;
            tvTokenNumber.setText(strTokenNumber);

        }catch (Exception e) {e.printStackTrace();}

        // Call API in ever 30 sec to check token update
        updateTokenThread = new Thread() {
            @Override
            public void run(){
                while(!STOP){
                    try {
                        Thread.sleep(30000);  //30000ms = 30 sec
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                new HTTPAsyncTask().execute("https://healthatm.in/api/Utils/get/current/token/?kioskid=" + EnteredKioskID);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        updateTokenThread.start();

    }

    private String httpPost(String myUrl) throws IOException, JSONException {

        String jsonResponse = "";
        int responseCode;

        URL url = new URL(myUrl);

        // create HttpURLConnection
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);

        // build JSON object
        JSONObject jsonObject = buildJsonObject();
        Log.d("JSON Object ",jsonObject.toString());

        // add JSON content to POST request body
        setPostRequestContent(httpURLConnection, jsonObject);

        // make POST request to the given URL
        httpURLConnection.connect();

        // return response code
        responseCode = httpURLConnection.getResponseCode();

        // get JSON response
        jsonResponse = getRequestContent(httpURLConnection, responseCode, jsonResponse);

        return jsonResponse;
    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    String params =  httpPost(urls[0]);
                    Log.d("HTTPAsyncTask ", params);
                    return params;

                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            String strToken = "NULL";

            try{
                JSONObject jsonObject =(new JSONObject(result)).getJSONObject("body");
                int token = jsonObject.getInt("current_token_number");

                strToken = "" + token;
                tvTokenNumber.setText(strToken);

            }catch (Exception e) {e.printStackTrace();}

            Log.d("UPDATE TOKEN NUMBER ", strToken);

        }
    }

    private JSONObject buildJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate(KIOSKID_KEY, EnteredKioskID);

        return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(TokenActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    private String getRequestContent(HttpURLConnection httpURLConnection, int responseCode, String jsonResponse) throws IOException {

        if (responseCode == HttpsURLConnection.HTTP_OK) {

            String line;

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            while ((line = bufferedReader.readLine()) != null) {
                jsonResponse += line;
            }
            Log.d("JSON Response ", jsonResponse);
        } else {
            jsonResponse = "";
            Log.d("JSON Response ",jsonResponse);
        }

        return  jsonResponse;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //set flag to 0 to start checking for connection again
        MainActivity.STOP_CONNECTION_UPDATE_ON_ACTIVITY_CHANGE = 0;

        STOP = true;
    }
}
