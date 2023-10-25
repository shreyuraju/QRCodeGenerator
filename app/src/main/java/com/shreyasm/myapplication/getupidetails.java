package com.shreyasm.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class getupidetails extends AppCompatActivity {

    EditText upiId, upiName, upiDesc;

    Button saveBtn;

    SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "upidetails";

    private static final String KEY_UPIID = "upiid";

    private static final String KEY_UPINAME = "upiname";

    private static final String KEY_UPIDESC = "upidesc";

    String upiid, upiname, upidesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getupidetails);

        upiId = findViewById(R.id.upiIdText);
        upiName = findViewById(R.id.upiNameText);
        upiDesc = findViewById(R.id.upiDescText);
        saveBtn = findViewById(R.id.saveBtn);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);

        String checkText = sharedPreferences.getString(KEY_UPIID,null);

        if (checkText != null) {
            Intent i = new Intent(getupidetails.this, MainActivity.class);
        }


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting String Values
                upiid = upiId.getText().toString();

                upiname = upiName.getText().toString();

                upidesc = upiDesc.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();

                //Checking wether text is empty or not
                if (upiid.isEmpty()) upiId.setError("UPI ID IS REQUIRED");

                if (upiname.isEmpty()) upiName.setError("UPI Name IS REQUIRED");

                if (upidesc.isEmpty()) upiDesc.setError("UPI Description IS REQUIRED");

                editor.putString(KEY_UPIID,upiid);
                editor.putString(KEY_UPINAME, upiname);
                editor.putString(KEY_UPIDESC, upidesc);
                editor.apply();

                Intent i = new Intent(getupidetails.this, MainActivity.class);
                startActivity(i);
                finish();
                Toast.makeText(getBaseContext(), "Details Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}