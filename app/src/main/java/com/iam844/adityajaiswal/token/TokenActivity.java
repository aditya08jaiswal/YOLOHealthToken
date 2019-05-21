package com.iam844.adityajaiswal.token;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

public class TokenActivity extends AppCompatActivity {

    TextView tvTokenNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        tvTokenNumber = findViewById(R.id.tokenNumber);

        Bundle bundle = getIntent().getExtras();

        String JSONString = bundle.getString("JSONString");

        try{
            JSONObject jsonObject =(new JSONObject(JSONString)).getJSONObject("body");
            int tokenNumber= jsonObject.getInt("tokennumber");

            String strTokenNumber= "" + tokenNumber;
            tvTokenNumber.setText(strTokenNumber);

        }catch (Exception e) {e.printStackTrace();}

    }
}
