package com.example.smart_bicycle20;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LocationDisplay extends AppCompatActivity {
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView amountTV, cycleTV, unameTV, longitudeTV, latitudeTV, userLonTV,userLatTV;

    private TextView usernameTextView;
    Button scan_button, amount_button , showUserLocation, showStationLocation;
    double amount = 0.0;
    String cycleId = "\0";
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    public static final int DEFAULT_INTERVAL = 30;
    public static final int FAST_INTERVAL = 5;
    Switch locationSwitch;
    private Handler timeHandler;
    private Runnable updateTimeRunnable;

    private ArrayList<String> stationIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_display);

        // Assigning TextViews and UI elements
        unameTV = findViewById(R.id.username_textview);
        cycleTV = findViewById(R.id.cycle_textview);
        longitudeTV = findViewById(R.id.longitude_textview);
        latitudeTV = findViewById(R.id.latitude_textview);
        scan_button = findViewById(R.id.scan_button);
        amount_button = findViewById(R.id.addAmountButton);
        amountTV = findViewById(R.id.amount_textview);
        usernameTextView = findViewById(R.id.toolbar_username_textview);
        showStationLocation = findViewById(R.id.station_location_btn);
        showUserLocation = findViewById(R.id.user_location_btn);
        locationSwitch = findViewById(R.id.userLocation_switch);
        userLonTV = findViewById(R.id.userLon_tv);
        userLatTV = findViewById(R.id.userLat_tv);
        fetchStationIdsFromFirebase();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_INTERVAL );

        //locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationSwitch.isChecked()){
                    timeHandler = new Handler();
                    updateTimeRunnable = new Runnable() {
                        @Override
                        public void run() {
                            updateUserLocation();
                            timeHandler.postDelayed(this,10000);
                        }
                    };
                    timeHandler.post(updateTimeRunnable);
                }else{
                    Toast.makeText(LocationDisplay.this,"Location was turned off",Toast.LENGTH_SHORT).show();
                }
            }
        });

        showStationLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract latitude and longitude from TextViews
                String latitude = latitudeTV.getText().toString();
                String longitude = longitudeTV.getText().toString();

                // Load Google Maps with station location
                String stationLocation = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Station)";
                Uri gmmIntentUri = Uri.parse(stationLocation);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Open location in web browser
                    String webUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                    startActivity(webIntent);
                }
            }
        });

        showUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract latitude and longitude from TextViews
                String latitude = userLatTV.getText().toString();
                String longitude = userLonTV.getText().toString();

                // Load Google Maps with user location
                String userLocation = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(User)";
                Uri gmmIntentUri = Uri.parse(userLocation);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Open location in web browser
                    String webUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                    startActivity(webIntent);
                }
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setActionBar(toolbar);

        String jsonString = "{\"channel\":{\"id\":2422845,\"name\":\"IOT rfid\",\"description\":\"ioy based rfid lock system\",\"latitude\":\"0.0\",\"longitude\":\"0.0\",\"field1\":\"UID\",\"field2\":\"Latitude\",\"field3\":\"Longitude\",\"field4\":\"message\",\"created_at\":\"2024-02-05T17:41:05Z\",\"updated_at\":\"2024-02-24T18:55:58Z\",\"last_entry_id\":3571},\"feeds\":[{\"created_at\":\"2024-03-10T16:27:12Z\",\"entry_id\":3570,\"field1\":null,\"field2\":\"33.61119\",\"field3\":null,\"field4\":null},{\"created_at\":\"2024-03-10T16:27:28Z\",\"entry_id\":3571,\"field1\":null,\"field2\":\"33.61109\",\"field3\":null,\"field4\":null}]}";

        parseAndSetLocation(jsonString);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String fname = extras.getString("fname");
            String uname = extras.getString("uname");

            usernameTextView.setText(String.valueOf(uname));

            if (unameTV != null) {
                unameTV.setText("Username: " + uname + "           Name: " + fname);

            }
            getAmountFromFirebase(uname);
        }


        scan_button.setOnClickListener(v -> scanCode());

        amount_button.setOnClickListener(v -> showAmountDialog());
    }

    private void getAmountFromFirebase(String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Customer").child(username).child("amount");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    amount = snapshot.getValue(Double.class);
                    amountTV.setText("Balance: " + String.format("%.2f", amount) + " Pkr");
                } else {
                    amount = 0.0;
                    amountTV.setText("Balance: 0.00 Pkr");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateUserLocation();
                }else{
                   Toast.makeText(this,"This app needs location to work properly",Toast.LENGTH_SHORT).show();
                   finish();
                }
        }
    }

    private void updateUserLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationDisplay.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUserTextViews(location);
                }
            });
        }else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);
            }
        }

    }

    private void updateUserTextViews(Location location) {
        userLatTV.setText(String.valueOf(location.getLatitude()));
        userLonTV.setText(String.valueOf(location.getLongitude()));

    }

    private void parseAndSetLocation(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray feeds = jsonObject.getJSONArray("feeds");
            JSONObject latestFeed = feeds.getJSONObject(feeds.length() - 1);
            String latitude = latestFeed.getString("field2");
            String longitude = latestFeed.getString("field3");

            //latitudeTV.setText("Latitude: " + latitude);
            //longitudeTV.setText("Longitude: " + longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationDisplay.this);
        builder.setTitle("Enter Amount");

        final EditText input = new EditText(LocationDisplay.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                try {
                    amount += Double.parseDouble(amountStr);
                    amountTV.setText("Balance: " + String.format("%.2f", amount) + " Pkr");
                    addAmountToDataBase(amount);
                } catch (NumberFormatException e) {
                    Toast.makeText(LocationDisplay.this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LocationDisplay.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addAmountToDataBase(double amount) {
        String username = String.valueOf(usernameTextView.getText()); // Replace with your logic to get the user ID
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Customer").child(username);

        userRef.child("amount").setValue(amount)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(LocationDisplay.this, "Amount added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(LocationDisplay.this, "Failed to add amount: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void scanCode() {
        if(locationSwitch.isChecked()){
        if (amount >= 50.0) {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to flash on");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(options);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LocationDisplay.this);
            builder.setTitle("Insufficient Balance");
            builder.setMessage("You do not have enough balance to scan. Please add funds.");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        }}else{
            Toast.makeText(LocationDisplay.this,"you need to turn on location",Toast.LENGTH_SHORT).show();
        }
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result != null && result.getContents() != null) {
            String scannedId = result.getContents();
            handleScannedId(scannedId);
        }
    });

    private void fetchStationIdsFromFirebase() {
        DatabaseReference stationsRef = FirebaseDatabase.getInstance().getReference().child("Station");
        stationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    stationIds.add(stationSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationDisplay.this, "Failed to fetch station IDs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleScannedId(String scannedId) {
        if (stationIds.contains(scannedId)) {
            String preId = cycleTV.getText().toString();
            handleCycleUnbooking(scannedId, preId);
        } else {
            checkIfCycleExists(scannedId, exists -> handleCycleBooking(exists, scannedId));
        }
    }


    private void handleCycleUnbooking(String sid,String cid) {
        if (!cycleTV.getText().toString().isEmpty()) {
            Toast.makeText(LocationDisplay.this, "Cycle "+ cid +" was dropped" , Toast.LENGTH_SHORT).show();
            cycleTV.setText(""); // Clear the cycle ID TextView
            String url = "https://api.thingspeak.com/update?api_key=YL65VHFLDQN8XULB&field1=" + cid;
            addDropTimeToCycle(cid);
            makeNetworkRequestToLock(url);
            addCycleToStation(sid,cid);
        } else {
            Toast.makeText(LocationDisplay.this, "No cycle is being used by you", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCycleToStation(String stationId, String cycleId) {
        DatabaseReference stationRef = FirebaseDatabase.getInstance().getReference().child("Station").child(stationId);
        stationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> cyclesAtStation = new ArrayList<>();
                    if (snapshot.child("cycles").exists()) {
                        for (DataSnapshot cycleSnapshot : snapshot.child("cycles").getChildren()) {
                            cyclesAtStation.add(cycleSnapshot.getValue(String.class));
                        }
                    }
                    cyclesAtStation.add(cycleId);
                    stationRef.child("cycles").setValue(cyclesAtStation);
                    Toast.makeText(LocationDisplay.this, "Cycle added to station successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LocationDisplay.this, "Station does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationDisplay.this, "Failed to update station data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleCycleBooking(boolean exists, String scannedId) {
        if(cycleTV.getText().toString().isEmpty()){
            if (exists) {
                String url = "https://api.thingspeak.com/update?api_key=YL65VHFLDQN8XULB&field1=" + scannedId;
                String username = usernameTextView.getText().toString();
                addUserToCycle(scannedId, username);
                makeNetworkRequestToUnLock(url);
                removeCycleFromStation(scannedId);
                displayResultDialog(scannedId);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Code to be executed after 20 seconds delay
                        String secondUrl = "https://api.thingspeak.com/update?api_key=YL65VHFLDQN8XULB&field1=" + "Salam_Bhai";
                        makeNetworkRequestToUnLock(secondUrl);
                    }
                }, 20000); // 20000 milliseconds = 20 seconds
            } else {
                Toast.makeText(LocationDisplay.this, "Invalid Data", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LocationDisplay.this,"You already have booked a cycle",Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCycleFromStation(String cycleId) {
        DatabaseReference stationsRef = FirebaseDatabase.getInstance().getReference().child("Station");
        stationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    String stationId = stationSnapshot.getKey();
                    if (stationSnapshot.child("cycles").exists()) {
                        ArrayList<String> cyclesAtStation = new ArrayList<>();
                        for (DataSnapshot cycleSnapshot : stationSnapshot.child("cycles").getChildren()) {
                            cyclesAtStation.add(cycleSnapshot.getValue(String.class));
                        }
                        if (cyclesAtStation.remove(cycleId)) {
                            stationsRef.child(stationId).child("cycles").setValue(cyclesAtStation);
                            Toast.makeText(LocationDisplay.this, "Cycle removed from station successfully", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationDisplay.this, "Failed to update station data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void makeNetworkRequestToLock(String url) {
        RequestQueue queue = Volley.newRequestQueue(LocationDisplay.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> Toast.makeText(LocationDisplay.this, "Cycle locked successfully", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(LocationDisplay.this, "Failed to lock cycle: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }

    private void makeNetworkRequestToUnLock(String url) {
        RequestQueue queue = Volley.newRequestQueue(LocationDisplay.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> Toast.makeText(LocationDisplay.this, "Cycle unlocked successfully", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(LocationDisplay.this, "Failed to unlock cycle: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }

    private void displayResultDialog(String scannedId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationDisplay.this);
        builder.setTitle("Result");
        cycleId = scannedId;
        cycleTV.setText(String.valueOf(cycleId));
        amount -= 50;
        amountTV.setText("Balance: " + String.format("%.2f", amount) + " Pkr");
        updateAmountInFirebase(amount);
        builder.setMessage(scannedId);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    private void addUserToCycle(String cycleId, String username) {
        DatabaseReference cycleRef = FirebaseDatabase.getInstance().getReference().child("Cycle").child(cycleId);
        cycleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> customersTaken = new ArrayList<>();
                ArrayList<String> bookedTimes = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());

                if (snapshot.exists()) {
                    if (snapshot.child("customersTaken").exists()) {
                        for (DataSnapshot customerSnapshot : snapshot.child("customersTaken").getChildren()) {
                            customersTaken.add(customerSnapshot.getValue(String.class));
                        }
                    }
                    if (snapshot.child("bookedTimes").exists()) {
                        for (DataSnapshot timeSnapshot : snapshot.child("bookedTimes").getChildren()) {
                            bookedTimes.add(timeSnapshot.getValue(String.class));
                        }
                    }
                }
                customersTaken.add(username);
                bookedTimes.add(currentDateTime);

                cycleRef.child("customersTaken").setValue(customersTaken);
                cycleRef.child("bookedTimes").setValue(bookedTimes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationDisplay.this, "Failed to update cycle data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDropTimeToCycle(String cycleId) {
        DatabaseReference cycleRef = FirebaseDatabase.getInstance().getReference().child("Cycle").child(cycleId);
        cycleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> dropTimes = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDateTime = sdf.format(new Date());

                if (snapshot.exists() && snapshot.child("dropTimes").exists()) {
                    for (DataSnapshot timeSnapshot : snapshot.child("dropTimes").getChildren()) {
                        dropTimes.add(timeSnapshot.getValue(String.class));
                    }
                }
                dropTimes.add(currentDateTime);

                cycleRef.child("dropTimes").setValue(dropTimes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationDisplay.this, "Failed to update drop time: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfCycleExists(String scannedId, final CycleExistenceCallback callback) {
        DatabaseReference cyclesRef = FirebaseDatabase.getInstance().getReference().child("Cycle");
        cyclesRef.child(scannedId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCycleExistenceChecked(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCycleExistenceChecked(false);
            }
        });
    }

    private interface CycleExistenceCallback {
        void onCycleExistenceChecked(boolean exists);
    }


    private void updateAmountInFirebase(double amount) {
        String username = String.valueOf(usernameTextView.getText()); // Replace with your logic to get the user ID
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Customer").child(username);

        userRef.child("amount").setValue(amount)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(LocationDisplay.this, "Amount updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(LocationDisplay.this, "Failed to update amount: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static void Location(TextView longitudeTextView, TextView latitudeTextView) {

        String jsonString = "{\"channel\":{\"id\":2422845,\"name\":\"IOT rfid\",\"description\":\"ioy based rfid lock system\",\"latitude\":\"0.0\",\"longitude\":\"0.0\",\"field1\":\"UID\",\"field2\":\"Latitude\",\"field3\":\"Longitude\",\"field4\":\"message\",\"created_at\":\"2024-02-05T17:41:05Z\",\"updated_at\":\"2024-02-24T18:55:58Z\",\"last_entry_id\":3571},\"feeds\":[{\"created_at\":\"2024-03-10T16:27:12Z\",\"entry_id\":3570,\"field1\":null,\"field2\":\"33.61119\",\"field3\":null,\"field4\":null},{\"created_at\":\"2024-03-10T16:27:28Z\",\"entry_id\":3571,\"field1\":null,\"field2\":\"33.61109\",\"field3\":null,\"field4\":null}]}";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray feedsArray = jsonObject.getJSONArray("feeds");

            for (int i = 0; i < feedsArray.length(); i++) {
                JSONObject entry = feedsArray.getJSONObject(i);
                String latitude = entry.getString("field2");
                String longitude = entry.getString("field3");

                latitudeTextView.setText(latitude);
                longitudeTextView.setText(longitude);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*public static void Location(TextView longitudeTextView, TextView latitudeTextView, String jsonString) {
        try {
            // Parse the JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract latitude and longitude from the "channel" object
            JSONObject channelObject = jsonObject.getJSONObject("channel");
            String latitude = channelObject.optString("latitude"); // Retrieve latitude
            String longitude = channelObject.optString("longitude"); // Retrieve longitude

            // Set latitude and longitude in corresponding TextViews
            //latitudeTextView.setText(latitude);
            //longitudeTextView.setText(longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}