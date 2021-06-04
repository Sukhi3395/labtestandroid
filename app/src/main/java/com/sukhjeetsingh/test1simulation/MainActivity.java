package com.sukhjeetsingh.test1simulation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button mode1,mode2,mGo,mShareAddress;
    ConstraintLayout view1,view2;
    EditText mLat, mLong, mPhone;

    Boolean permissionGranted;
    String sharingAddress;

    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        mode1 = findViewById(R.id.mode1);
        mode2 = findViewById(R.id.mode2);
        mGo = findViewById(R.id.btnGo);
        mShareAddress = findViewById(R.id.btnShareAddress);
        mLat = findViewById(R.id.edtxtLat);
        mLong = findViewById(R.id.edtxtLong);
        mPhone = findViewById(R.id.edtxtPhone);

        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.GONE);
        mShareAddress.setEnabled(false);

        isPermissionGranted();

        if(permissionGranted){
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            supportMapFragment.getMapAsync(this);
        }

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkGivenCoordinates()){
                    double lat = Double.parseDouble(mLat.getText().toString().trim());
                    double lon = Double.parseDouble(mLong.getText().toString().trim());
                    LatLng long_and_lat = new LatLng(lat, lon);
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(long_and_lat).title("Your Place is here"));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(long_and_lat.latitude, long_and_lat.longitude)).zoom(16).build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                    try {
                        List<Address> addresses = geocoder.getFromLocation(long_and_lat.latitude, long_and_lat.longitude, 1);
                        if(addresses.size() > 0){
                            sharingAddress = addresses.get(0).getAddressLine(0);
                            mShareAddress.setEnabled(true);
                            Toast.makeText(getApplicationContext(),sharingAddress,Toast.LENGTH_SHORT).show();

                            enableSMS();

                        }else{
                            mShareAddress.setEnabled(false);
                        }

                        /*Log.d(TAG, "Lattitude: " + addresses.get(0).getLatitude());
                        Log.d(TAG, "Longitude: " + addresses.get(0).getLongitude());
                        Log.d(TAG, "Country: " + addresses.get(0).getCountryName());
                        Log.d(TAG, "Locality: " + addresses.get(0).getLocality());
                        Log.d(TAG, "Address: " + addresses.get(0).getAddressLine(0));*/

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mShareAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mPhone.getText().toString().trim().equals("")){
                    String phone_number = mPhone.getText().toString();
                    if (checkPermission(Manifest.permission.SEND_SMS)){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone_number, null, sharingAddress, null, null);
                        Toast.makeText(getApplicationContext(), "Message Sent!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.GONE);
            }
        });

        mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view2.setVisibility(View.VISIBLE);
                view1.setVisibility(View.GONE);
            }
        });



    }

    public void enableSMS(){
        if (checkPermission(Manifest.permission.SEND_SMS)) {
            mShareAddress.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }

    private boolean checkGivenCoordinates(){
        return !mLat.getText().toString().equals("") && !mLong.getText().toString().equals("");
    }

    private boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    private void isPermissionGranted() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(getApplicationContext(),"Permission Granted",Toast.LENGTH_SHORT).show();
                permissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent myIntent = new Intent();
                myIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",getPackageName(),"");
                myIntent.setData(uri);
                startActivity(myIntent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest,
                                                           PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }


}