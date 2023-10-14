package com.shreyasm.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {

    EditText amount;

    Button btnGenerate;
    ImageView QR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amount = findViewById(R.id.EtAmount);
        btnGenerate = findViewById(R.id.BtnGenerate);
        QR = findViewById(R.id.IvQR);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amnt = amount.getText().toString();
                try {
                    int n= Integer.parseInt(amnt);
                    link(n);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void link(int n) {
        String url = "upi://pay?pa="+  //payment method
                "9206581025@axisb" +  //VPA number
                "&pn=Shreyas%20M"+ //receivernama
                "&am="+n+   //receiveable amount
                "&cu=INR"   //current Indian Rupees
                ;

        generateQR(url);
    }

    private void generateQR(String url) {
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