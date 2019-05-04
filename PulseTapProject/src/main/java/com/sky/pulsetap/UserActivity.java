package com.sky.pulsetap;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button callHelperButton;
    Button callHelper;
    Boolean requestActive=false;
    private static long back_pressed;
    Handler handler=new Handler();
    TextView infoText;
    Boolean LogIn;
    Boolean driverActive;
    Boolean alreadyDisplayedNotification = false;

    @Override
    public void onBackPressed()
    {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finishAffinity();

        }
        else Toast.makeText(getBaseContext(), "Press back once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
    public void logout(){
        handler.removeCallbacksAndMessages(null);
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    finish();
                    Toast.makeText(getBaseContext(),"LogOut Successful!",Toast.LENGTH_LONG).show();
                    LogIn=false;
                }
            else{
                Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnownLoc);
                }
            }
        }
    }


    public void checkLocUpdates(){
        if(LogIn==true) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.whereExists("helperUsername");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects.size() > 0) {
                        if(!alreadyDisplayedNotification) {
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getBaseContext())
                                            .setSmallIcon(R.drawable.pulse)
                                            .setContentTitle("Responder Found")
                                            .setContentText("Your Responder is on his way");
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(001, mBuilder.build());
                            alreadyDisplayedNotification=true;
                        }
                        driverActive=true;
                        ParseQuery<ParseUser> query2 = ParseUser.getQuery();
                        query2.whereEqualTo("username", objects.get(0).getString("helperUsername"));
                        query2.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e == null && objects.size() > 0) {
                                    ParseGeoPoint helperLocation = objects.get(0).getParseGeoPoint("currentLocation");
                                    if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (lastKnownLoc != null) {
                                            ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                                            Double distanceInKM = helperLocation.distanceInKilometersTo(userLocation);
                                            if (distanceInKM < 0.01) {
                                                infoText.setText("Your Responder is here");
                                                ParseQuery<ParseObject> query=new ParseQuery("Request");
                                                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if(e==null){
                                                            for(ParseObject p:objects){
                                                                p.deleteInBackground();
                                                            }
                                                        }
                                                    }
                                                });
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        callHelperButton.setVisibility(View.VISIBLE);
                                                        callHelperButton.setText("CPR");
                                                        requestActive=false;
                                                        driverActive=false;
                                                        alreadyDisplayedNotification=false;
                                                        infoText.setText("");
                                                    }
                                                },5000);

                                            }
                                            else {
                                                Double distInOD = (double) Math.round(distanceInKM * 10) / 10;
                                                infoText.setText("Your Responder is " + distInOD.toString() + " KMs away!");
                                                LatLng helperLocationLatLang=new LatLng(helperLocation.getLatitude(),helperLocation.getLongitude());
                                                LatLng userLocationLatLang=new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
                                                mMap.clear();
                                                ArrayList<Marker> markers = new ArrayList<>();
                                                markers.add(mMap.addMarker(new MarkerOptions().position(helperLocationLatLang).title("Helper Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                                markers.add(mMap.addMarker(new MarkerOptions().position(userLocationLatLang).title("User Location")));

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers) {
                                                    builder.include(marker.getPosition());
                                                }
                                                LatLngBounds bounds = builder.build();
                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                                                mMap.animateCamera(cu);

                                            }
                                        }
                                    }
                                }
                            }
                        });

                        callHelperButton.setVisibility(View.INVISIBLE);
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkLocUpdates();
                        }
                    }, 2000);
                }
            });
        }
    }
    public void callHelper(View view) {
        if(requestActive){
            ParseQuery<ParseObject> parseQuery=new ParseQuery<ParseObject>("Request");
            parseQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            for(ParseObject ob:objects){
                                ob.deleteInBackground();
                            }
                            requestActive=false;
                            callHelperButton.setText("CPR");

                        }

                    }
                }
            });
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLoc != null) {
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    request.put("fullName", ParseUser.getCurrentUser().get("fullName"));
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                    request.put("location", parseGeoPoint);

                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            callHelperButton.setText("Cancel");
                            requestActive = true;
                            checkLocUpdates();
                        }
                    });
                } else {
                    Toast.makeText(this, "Location Not Found!", Toast.LENGTH_LONG).show();

                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.blood:
                Intent intent=new Intent(getBaseContext(),BloodActivity.class);
                startActivity(intent);
                return true;
            case R.id.about:
                Intent intent3=new Intent(getBaseContext(),aboutActivity.class);
                startActivity(intent3);
                return true;
            case R.id.profile:
                Intent intent2=new Intent(getBaseContext(),EditProfileActivity.class);
                startActivity(intent2);
                return true;
            case R.id.logout:
                logout();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }
    public void hospitalFinder(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLoc != null) {
                String uri = "geo:"+ lastKnownLoc.getLatitude() + "," + lastKnownLoc.getLongitude() +"?q=hospitals+near+me";
                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
            }
            else{
                Toast.makeText(this,"Location Not Found!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setTitle("PulseTap");
        LogIn=true;
        driverActive=false;
        mapFragment.getMapAsync(this);
        infoText=(TextView) findViewById(R.id.textView4);
        callHelperButton = (Button) findViewById(R.id.cpr);
        callHelper = (Button) findViewById(R.id.helper);
        callHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getBaseContext(),cprHelperActivity.class);
                startActivity(intent);
            }
        });
        if(LogIn) {
            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Request");
            parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            requestActive = true;
                            callHelperButton.setText("Cancel");
                            checkLocUpdates();
                        }
                    }
                }
            });

        }
    }

    public void updateMap(Location location) {
        if(driverActive==false){
        LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 15));
        mMap.addMarker(new MarkerOptions().position(userLoc).title("User Location"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
                checkLocUpdates();

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLoc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLoc!=null){
                    updateMap(lastKnownLoc);
                }

            }
        }
        }


}
