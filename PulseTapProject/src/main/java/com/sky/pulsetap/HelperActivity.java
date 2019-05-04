package com.sky.pulsetap;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HelperActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    LocationListener locationListener;
    ParseGeoPoint parseGeoPoint;
    ArrayList<Double> requestLat = new ArrayList<>();
    ArrayList<Double> requestLong = new ArrayList<>();
    ArrayList<String> userContact = new ArrayList<>();
    Boolean Login=true;
    private static long back_pressed;

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
            case R.id.profile:
                Intent intent2=new Intent(getBaseContext(),EditProfileActivity.class);
                startActivity(intent2);
                return true;
            case R.id.about:
                Intent intent3=new Intent(getBaseContext(),aboutActivity.class);
                startActivity(intent3);
                return true;
            case R.id.logout:
                logout();
                Login=false;
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }
    @Override
    public void onBackPressed()
    {
        if (back_pressed + 2000 > System.currentTimeMillis()) finishAffinity();
        else Toast.makeText(getBaseContext(), "Press back once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    public void logout() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    finish();
                    Toast.makeText(getBaseContext(), "LogOut Successful!", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateListView(Location location) {
        if (location != null) {

            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Request");
            parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            parseQuery.whereNear("location", parseGeoPoint);
            parseQuery.whereWithinKilometers("location", parseGeoPoint, 10);
            parseQuery.whereDoesNotExist("helperUsername");
            parseQuery.setLimit(10);
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(getBaseContext())
                                                .setSmallIcon(R.drawable.pulse)
                                                .setContentTitle("Request Pending")
                                                .setContentText("Someone needs CPR Nearby");
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                mNotificationManager.notify(001, mBuilder.build());


                            arrayList.clear();
                            requestLong.clear();
                            requestLat.clear();
                            userContact.clear();
                            for (ParseObject ob : objects) {
                                String fullName;
                                ParseGeoPoint requestLoc = (ParseGeoPoint) ob.get("location");
                                if (requestLoc != null) {
                                    Double distanceInKM = parseGeoPoint.distanceInKilometersTo(requestLoc);
                                    Double distInOD = (double) Math.round(distanceInKM * 10) / 10;
                                    fullName = (String) ob.get("fullName");
                                    arrayList.add(distInOD.toString() + " KM | " + fullName);
                                    requestLat.add( requestLoc.getLatitude());
                                    requestLong.add( requestLoc.getLongitude());
                                    userContact.add((String) ob.get("username"));
                                }
                            }
                            arrayAdapter.notifyDataSetChanged();
                        } else {
                            arrayList.clear();
                            arrayList.add("No pending Requests");
                            arrayAdapter.notifyDataSetChanged();
                        }

                    }
                }
            });

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateListView(lastKnownLoc);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        setTitle("Nearby Requests");
        listView = (ListView) findViewById(R.id.requestList);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.WHITE);
                Typeface face = ResourcesCompat.getFont(getBaseContext(), R.font.advent_pro_semibold);
                textView.setTypeface(face);
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        arrayList.clear();
        arrayList.add("Fetching Requests");
        arrayAdapter.notifyDataSetChanged();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(Build.VERSION.SDK_INT < 23||ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (requestLat.size() > i && requestLong.size() > i && lastKnownLocation != null) {
                        Log.i("Info","Intent Pressed");
                        Intent intent=new Intent(getApplicationContext(),HelperMapActivity.class);
                        intent.putExtra("currentLat",lastKnownLocation.getLatitude());
                        intent.putExtra("currentLong",lastKnownLocation.getLongitude());
                        intent.putExtra("requestLat",requestLat.get(i));
                        intent.putExtra("requestLong",requestLong.get(i));
                        intent.putExtra("userContact",userContact.get(i));
                        startActivity(intent);
                    }
                }
            }
        });


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(Login) {
                    updateListView(location);
                    ParseUser.getCurrentUser().put("currentLocation", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                    ParseUser.getCurrentUser().saveInBackground();
                }
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
                    updateListView(lastKnownLoc);
                }
            }
        }

    }
    public void cprHelper(View view){
        Intent intent=new Intent(getBaseContext(),cprHelperActivity.class);
        startActivity(intent);
    }
    public void activeRequest(View view){
        ParseQuery<ParseObject> query2=new ParseQuery<ParseObject>("Request");
        query2.whereExists("helperUsername");
        query2.whereEqualTo("helperUsername",ParseUser.getCurrentUser().getUsername());
        query2.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if (objects.size()>0){
                        if(Build.VERSION.SDK_INT < 23||ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Log.i("Info","Intent Pressed");
                            for(ParseObject obj:objects) {
                                Intent intent = new Intent(getApplicationContext(), HelperMapActivity.class);
                                intent.putExtra("currentLat", lastKnownLocation.getLatitude());
                                intent.putExtra("currentLong", lastKnownLocation.getLongitude());
                                ParseGeoPoint reqLoc = (ParseGeoPoint) obj.get("location");
                                intent.putExtra("requestLat", reqLoc.getLatitude());
                                intent.putExtra("requestLong", reqLoc.getLongitude());
                                intent.putExtra("userContact", (String) obj.get("username"));
                                startActivity(intent);
                            }
                        }
                    }
                    else{
                        //disable button
                        Toast.makeText(HelperActivity.this, "No Active Request!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}
