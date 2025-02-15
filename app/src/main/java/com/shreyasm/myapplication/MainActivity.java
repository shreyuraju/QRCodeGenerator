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
    TextView selectupi, upitextview, amountText;
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
        selectupi = findViewById(R.id.selectupi);
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

                if (n < 1 || n > 2000) {
                    amount.setError("minimum amount is ₹1 & receive upto ₹2000");
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

        if (item.getItemId() == R.id.editupi) {
            showEditUPIDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showEditUPIDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select UPI to EDIT");

        // Create a layout for the dialog with a Spinner to select UPI ID
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // Get the UPI details from SharedPreferences
        Map<String, String> upiDetails = getUpiDetails();
        ArrayList<String> upiIds = new ArrayList<>(upiDetails.keySet());

        if (upiIds.isEmpty()) {
            Toast.makeText(this, "No UPI IDs available to edit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Spinner to show existing UPI IDs
        final Spinner upiIdSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, upiIds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        upiIdSpinner.setAdapter(adapter);
        layout.addView(upiIdSpinner);

        // EditText for UPI ID (this will allow the user to edit the UPI ID)
        final EditText upiIdInput = new EditText(this);
        upiIdInput.setHint("UPI ID");
        layout.addView(upiIdInput);

        // EditText for UPI Name
        final EditText upiNameInput = new EditText(this);
        upiNameInput.setHint("UPI Name");
        layout.addView(upiNameInput);

        // Populate the fields with the current data of the selected UPI ID
        upiIdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedUpiId = upiIdSpinner.getSelectedItem().toString();
                String selectedUpiName = upiDetails.get(selectedUpiId);

                // Populate the UPI ID and Name EditTexts with the current values
                upiIdInput.setText(selectedUpiId);
                upiNameInput.setText(selectedUpiName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle case if nothing is selected (though it shouldn't happen)
            }
        });

        builder.setView(layout);

        // Add buttons to the dialog
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newUpiId = upiIdInput.getText().toString();
            String newUpiName = upiNameInput.getText().toString();

            if (newUpiId.isEmpty() || newUpiName.isEmpty()) {
                Toast.makeText(this, "Please fill both UPI ID and UPI Name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure the new UPI ID is different from the selected one
            String selectedUpiId = (String) upiIdSpinner.getSelectedItem();
            if (!newUpiId.equals(selectedUpiId)) {
                // Remove the old UPI entry (if the ID is changed)
                upiDetails.remove(selectedUpiId);
            }

            // Update the UPI details with the new ID and name
            upiDetails.put(newUpiId, newUpiName);  // Update or add new UPI

            // Save updated UPI details in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_UPI_DETAILS, serializeUpiDetails(upiDetails));  // Serialize and save the map
            editor.apply();

            // Update the UI
            updateUI();
            Toast.makeText(this, "UPI Details Updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
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

        builder.setView(layout);

        // Add buttons to the dialog
        builder.setPositiveButton("Save", (dialog, which) -> {
            String upiId = upiIdInput.getText().toString();
            String upiName = upiNameInput.getText().toString();

            // Check if any field is empty
            if (upiId.isEmpty() || upiName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save UPI details to SharedPreferences
            Map<String, String> upiDetails = getUpiDetails();
            upiDetails.put(upiId, upiName);

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
        } else {
            upiname = "Unknown";
        }

        // Update the UI with the selected UPI details
        upitextview.setText("UPI: " + upiId + "\nName: " + upiname);
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

    private void updateUI() {
        Map<String, String> upiDetails = getUpiDetails();
        String selectedUpiId = sharedPreferences.getString(KEY_SELECTED_UPI, null);

        if (selectedUpiId != null && upiDetails.containsKey(selectedUpiId)) {
            updateUpiDetails(selectedUpiId);
            upitextview.setText("Selected UPI ID: " + selectedUpiId);
            amount.setVisibility(View.VISIBLE);
            btnGenerate.setVisibility(View.VISIBLE);
            selectupi.setVisibility(View.GONE);
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

    private void link(double amount) {
        try {
            String selectedUpiId = sharedPreferences.getString(KEY_SELECTED_UPI, null);
            String upiDetails = getUpiDetails().get(selectedUpiId);

            String qrCodeContent = "upi://pay?pa=" + selectedUpiId + "&pn=" + upiname + "&mc=0000&tid=1234567890&tr=txn123&tn=Payment&am=" + amount + "&cu=INR";

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeContent, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            QR.setImageBitmap(bitmap);
            amountText.setText("₹" + amount);
            btnClear.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
