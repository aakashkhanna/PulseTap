package com.sky.pulsetap;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class BloodActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    LocationListener locationListener;
    ParseGeoPoint parseGeoPoint;
    ArrayList<String> userContact = new ArrayList<>();
    Boolean Login=true;
    Boolean bloodActive=false;
    Button req;

    public void newRequest(View view){
        if(bloodActive){

            ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Blood");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            for(ParseObject ob:objects){
                                ob.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            req.setText("Place New Request");
                                            bloodActive=false;
                                        }
                                    }
                                });
                            }
                        }
                    }
                    else{
                        Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                final Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLoc != null) {
                    ParseObject request = new ParseObject("Blood");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    request.put("fullName", ParseUser.getCurrentUser().get("fullName"));
                    request.put("age", ParseUser.getCurrentUser().get("age"));
                    request.put("bloodGroup", ParseUser.getCurrentUser().get("bloodGroup"));
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                    request.put("location", parseGeoPoint);

                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                updateListView(lastKnownLoc);
                                req.setText("Cancel Request");
                                bloodActive=true;
                            } else {
                                Toast.makeText( getBaseContext(),e.getMessage(),Toast.LENGTH_LONG ).show();
                            }

                        }
                    });
                } else {
                    Toast.makeText(this, "Location Not Found!", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void updateListView(Location location) {
        if (location != null) {

            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Blood");
            parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            parseQuery.whereNear("location", parseGeoPoint);
            parseQuery.whereWithinKilometers("location", parseGeoPoint, 20);
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
                                            .setContentText("Someone needs Blood Nearby").setOnlyAlertOnce(true);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(001, mBuilder.build());


                            arrayList.clear();
                            userContact.clear();
                            for (ParseObject ob : objects) {
                                String fullName;
                                ParseGeoPoint requestLoc = (ParseGeoPoint) ob.get("location");
                                if (requestLoc != null) {
                                    Double distanceInKM = parseGeoPoint.distanceInKilometersTo(requestLoc);
                                    Double distInOD = (double) Math.round(distanceInKM * 10) / 10;
                                    fullName = (String) ob.get("fullName");
                                    String bloodGroup=(String) ob.get("bloodGroup");
                                    arrayList.add(bloodGroup+" | "+distInOD.toString() + " KM | " + fullName);
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
        setContentView(R.layout.activity_blood);
        setTitle("Blood Donation Requests");
        req=(Button)findViewById(R.id.req);

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
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Blood");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        bloodActive=true;
                        req.setText("Cancel Request");
                        if(Build.VERSION.SDK_INT < 23||ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastKnownLocation != null) {
                                updateListView(lastKnownLocation);
                            }
                        }
                    }
                    else{
                        req.setText("Place New Request");
                    }
                }
                else{
                    Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("Blood");
                    query.whereEqualTo("username",userContact.get(i));
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(final List<ParseObject> objects, ParseException e) {

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BloodActivity.this);
                            alertDialog.setTitle("Blood Donation Request");

                            alertDialog.setMessage("Name: "+objects.get(0).get("fullName")+"\nBlood Group: "+objects.get(0).get("bloodGroup")+"\nAge: "+objects.get(0).get("age")+"\nTimeStamp: "+objects.get(0).getCreatedAt());

                            alertDialog.setIcon(R.drawable.pulse);

                            alertDialog.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ParseGeoPoint requestLoc= (ParseGeoPoint) objects.get(0).get("location");
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("http://maps.google.com/maps?daddr="+requestLoc.getLatitude()+","+requestLoc.getLongitude()));
                                    startActivity(intent);
                                }
                            });

                            alertDialog.setNegativeButton("CALL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // User pressed No button. Write Logic Here
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91" + objects.get(0).get("username")));
                                    startActivity(callIntent);
                                }
                            });

                            alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // User pressed Cancel button. Write Logic Here
                                }
                            });
                            AlertDialog alert11 = alertDialog.create();
                            alert11.show();

                            Button buttonbackground = alert11.getButton(DialogInterface.BUTTON_NEGATIVE);
                            buttonbackground.setBackgroundColor(Color.BLACK);

                            Button buttonbackground1 = alert11.getButton(DialogInterface.BUTTON_POSITIVE);
                            buttonbackground1.setBackgroundColor(Color.BLACK);

                            Button buttonbackground2 = alert11.getButton(DialogInterface.BUTTON_NEUTRAL);
                            buttonbackground2.setBackgroundColor(Color.BLACK);

                        }
                    });
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

}
