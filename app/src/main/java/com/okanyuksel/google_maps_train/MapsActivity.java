package com.okanyuksel.google_maps_train;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("User Location Changing, New Location: " + location.toString());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                mapGoToUserLocation();
            }
        } else {
            mapGoToUserLocation();
        }
        mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Hi Dear User");

        // set dialog message
        alertDialogBuilder
                .setMessage("Long press on the map to add a marker to the desired location on the map.")
                .setCancelable(false)
                .setPositiveButton("I Got It!",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (requestCode == 1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("ACCESS_FINE_LCOATION Permission Granted...");
                    mapGoToUserLocation();
                } else {
                    System.out.println("ACCESS_FINE_LCOATION Permission Denied...");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    public void mapGoToUserLocation() {
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().title(AddressCreator(userLastLocation)).position(userLastLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15));
    }

    public String AddressCreator(LatLng latlng) {
        String address = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                System.out.println("Address: " + addressList.get(0).toString());
                if (addressList.get(0).getAddressLine(0) != null) {
                  address+=addressList.get(0).getAddressLine(0);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return address;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        String address = AddressCreator(latLng);

        if (address.matches("")) {
            {
                address = "No Address";
            }
        }
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
    }
}
