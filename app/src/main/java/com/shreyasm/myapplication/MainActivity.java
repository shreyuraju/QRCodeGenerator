package com.shreyasm.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class MainActivity extends AppCompatActivity {
    EditText amount;
    Button btnGenerate;
    ImageView QR;

    TextView upitextview, amountText;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "upidetails";

    private static final String KEY_UPIID = "upiid";

    private static final String KEY_UPINAME = "upiname";

    private static final String KEY_UPIDESC = "upidesc";

    String upiid, upiname, upidesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amount = findViewById(R.id.EtAmount);
        btnGenerate = findViewById(R.id.BtnGenerate);
        QR = findViewById(R.id.IvQR);
        upitextview = findViewById(R.id.upiTextview);
        amountText = findViewById(R.id.amontText);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        upiid = sharedPreferences.getString(KEY_UPIID,null);
        upiname = sharedPreferences.getString(KEY_UPINAME, null);
        upidesc = sharedPreferences.getString(KEY_UPIDESC, null);

        if ( upiid!=null || upiname!=null || upidesc!=null ) {
            upitextview.setText("Name: "+upiname+"\nUPI ID: "+upiid);
        } else {
            Intent i = new Intent(MainActivity.this, getupidetails.class);
            startActivity(i);
            finish();
        }

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amnt = amount.getText().toString();
                try {
                    int n= Integer.parseInt(amnt);

                    if (n<=0 || n>5000) {
                        amount.setError("minimum amount is ₹1 & receive upto ₹5000");
                        return;
                    }

//                    if (n>5000) {
//                        amount.setError("You can only receive upto ₹5000");
//                        return;
//                    }

                    link(n);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void link(int n) {
        String url = "upi://pay?pa="+
                upiid +  //VPA number                      //Replace your UPI id HERE
                "&pn="+upiname+ //receivernama                       //Replace your name with "%20" to add space inbetween your name
                "&am="+n+   //receiveable amount
                "&cu=INR"+
                "&tn="+upidesc  //current Indian Rupees
                ;
        generateQR(url,n);
    }

    private void generateQR(String url, int n) {
        amountText.setText("Receiving\nAmount:"+n);
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE,1000,1000);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);

            //set QR to imageView
            QR.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}