package com.example.mobile_scratch.activity;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_scratch.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapActivity1 extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gm;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMap();
    }
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void createMap() {
        SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        smf.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        createMap();
    }
    @Override

    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;

        db.collection("stores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve latitude and longitude from Firestore document
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            String name = document.getString("name");

                            // Create LatLng object for the marker
                            LatLng location = new LatLng(latitude, longitude);

                            // Add marker to the map
                            gm.addMarker(new MarkerOptions().position(location).title(name));
                        }

                        // Set camera position
                        LatLng hcm = new LatLng(10.787703, 106.702658);
                        CameraPosition cp = new CameraPosition.Builder().target(hcm).zoom(13).build();
                        gm.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

}