package com.shreyasm.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText amount;
    Button btnGenerate, btnClear;
    ImageView QR;
    TextView upitextview, amountText;
    Spinner upiSpinner; // Spinner for multiple UPI IDs

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "upidetails";
    private static final String KEY_UPI_DETAILS = "upi_details"; // Store UPI details as a Map
    private static final String KEY_SELECTED_UPI = "selected_upi"; // Store the selected UPI ID

    String upiid, upiname, upidesc;

    boolean isPressed = false;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amount = findViewById(R.id.EtAmount);
        btnGenerate = findViewById(R.id.BtnGenerate);
        btnClear = findViewById(R.id.BtnClear);
        QR = findViewById(R.id.IvQR);
        upitextview = findViewById(R.id.upiTextview);
        amountText = findViewById(R.id.amontText);
        upiSpinner = findViewById(R.id.upiSpinner); // Initialize the Spinner

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Retrieve stored UPI details
        Map<String, String> upiDetails = getUpiDetails();
        String selectedUpiId = sharedPreferences.getString(KEY_SELECTED_UPI, null);

        // Set the spinner with available UPI IDs
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(upiDetails.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        upiSpinner.setAdapter(adapter);

        // Select the saved UPI ID in the spinner, if available
        if (selectedUpiId != null) {
            int position = new ArrayList<>(upiDetails.keySet()).indexOf(selectedUpiId);
            upiSpinner.setSelection(position);
        }

        // Handle spinner selection
        upiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedUpiId = (String) parentView.getItemAtPosition(position);
                sharedPreferences.edit().putString(KEY_SELECTED_UPI, selectedUpiId).apply();
                updateUpiDetails(selectedUpiId);

                // Generate QR with the new UPI details after selection change
                String amnt = amount.getText().toString();
                if (!amnt.isEmpty()) {
                    try {
                        double n = Double.parseDouble(amnt);
                        link(n); // Regenerate the QR code with the updated details
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // Show amount input fields only if UPI details are set
        if (selectedUpiId == null) {
            amount.setVisibility(View.GONE);
            btnGenerate.setVisibility(View.GONE);
            upitextview.setText("No UPI ID selected");
        } else {
            amount.setVisibility(View.VISIBLE);
            btnGenerate.setVisibility(View.VISIBLE);
            updateUpiDetails(selectedUpiId);
        }

        btnClear.setVisibility(View.GONE);

        btnGenerate.setOnClickListener(v -> {
            String amnt = amount.getText().toString();
            try {
                double n = Double.parseDouble(amnt);

                if (n < 1 || n > 5000) {
                    amount.setError("minimum amount is ₹1 & receive upto ₹5000");
                    return;
                }

                link(n);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnClear.setOnClickListener(v -> {
            QR.setImageDrawable(null);
            amountText.setText(null);
            btnClear.setVisibility(View.GONE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addupi) {
            showAddUPIDialog();
        }

//        if (item.getItemId() == R.id.viewupi) {
//            Toast.makeText(this, "View UPI", Toast.LENGTH_SHORT).show();
//        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddUPIDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter UPI Details");

        // Create a layout for the dialog with input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // Create EditText fields for UPI details
        final EditText upiIdInput = new EditText(this);
        upiIdInput.setHint("UPI ID");
        layout.addView(upiIdInput);

        final EditText upiNameInput = new EditText(this);
        upiNameInput.setHint("UPI Name");
        layout.addView(upiNameInput);

        final EditText upiDescInput = new EditText(this);
//        upiDescInput.setHint("UPI Description");
//        layout.addView(upiDescInput);

        builder.setView(layout);

        // Add buttons to the dialog
        builder.setPositiveButton("Save", (dialog, which) -> {
            String upiId = upiIdInput.getText().toString();
            String upiName = upiNameInput.getText().toString();
            String upiDesc = upiDescInput.getText().toString();

            // Check if any field is empty
            if (upiId.isEmpty() || upiName.isEmpty() || upiDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save UPI details to SharedPreferences
            Map<String, String> upiDetails = getUpiDetails();
            upiDetails.put(upiId, upiName + "," + upiDesc); // Save Name and Description as a comma-separated string

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SELECTED_UPI, upiId); // Set the newly added UPI ID as selected
            editor.putString(KEY_UPI_DETAILS, serializeUpiDetails(upiDetails)); // Store the entire details map
            editor.apply();

            // Update the Spinner and UI immediately
            updateUI();

            Toast.makeText(this, "UPI Details Saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateUpiDetails(String upiId) {
        // Fetch UPI details from SharedPreferences
        Map<String, String> upiDetails = getUpiDetails();
        String details = upiDetails.get(upiId);
        if (details != null) {
            String[] parts = details.split(",");
            upiname = parts[0];
            upidesc = parts[1];
        } else {
            upiname = "Unknown";
            upidesc = "";
        }

        // Update the UI with the selected UPI details
        upitextview.setText("Selected UPI ID: " + upiId + "\nName: " + upiname);
    }

    private Map<String, String> getUpiDetails() {
        // Retrieve UPI details from SharedPreferences as a serialized String
        String serializedDetails = sharedPreferences.getString(KEY_UPI_DETAILS, "{}");
        return deserializeUpiDetails(serializedDetails);
    }

    private String serializeUpiDetails(Map<String, String> upiDetails) {
        // Convert Map to a JSON-like string for storage
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Map.Entry<String, String> entry : upiDetails.entrySet()) {
            builder.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\", ");
        }
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 2); // Remove the last comma
        }
        builder.append("}");
        return builder.toString();
    }

    private Map<String, String> deserializeUpiDetails(String serializedDetails) {
        Map<String, String> upiDetails = new HashMap<>();
        try {
            if (serializedDetails != null && !serializedDetails.isEmpty() && !serializedDetails.equals("{}")) {
                serializedDetails = serializedDetails.replaceAll("[{}\"]", ""); // Remove extra characters
                String[] pairs = serializedDetails.split(", ");
                for (String pair : pairs) {
                    String[] entry = pair.split(": ");
                    if (entry.length == 2) {
                        upiDetails.put(entry[0], entry[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return upiDetails;
    }

    private void link(double n) {
        // Use the selected UPI ID for generating the QR code
        Map<String, String> upiDetails = getUpiDetails();
        String selectedUpiId = sharedPreferences.getString(KEY_SELECTED_UPI, null);

        if (selectedUpiId != null && upiDetails.containsKey(selectedUpiId)) {
            String details = upiDetails.get(selectedUpiId);
            String[] parts = details.split(",");
            String url = "upi://pay?pa=" + selectedUpiId +
                    "&pn=" + parts[0] +
                    "&am=" + n +
                    "&cu=INR";
//                    "&tn=" + parts[1]

            generateQR(url, n);
        } else {
            Toast.makeText(this, "No UPI details found for selected ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQR(String upiLink, double n) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(upiLink, BarcodeFormat.QR_CODE, 800, 800);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            QR.setImageBitmap(bitmap);
            amountText.setText("Amount: ₹" + n);
            btnClear.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        Map<String, String> upiDetails = getUpiDetails();
        String selectedUpiId = sharedPreferences.getString(KEY_SELECTED_UPI, null);

        // If UPI ID is selected and available in the details map
        if (selectedUpiId != null && upiDetails.containsKey(selectedUpiId)) {
            updateUpiDetails(selectedUpiId);
            upitextview.setText("Selected UPI ID: " + selectedUpiId);
            amount.setVisibility(View.VISIBLE);
            btnGenerate.setVisibility(View.VISIBLE);
        } else {
            upitextview.setText("No UPI ID selected");
            amount.setVisibility(View.GONE);
            btnGenerate.setVisibility(View.GONE);
        }

        // Update the Spinner with UPI IDs
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(upiDetails.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        upiSpinner.setAdapter(adapter);
    }
}
