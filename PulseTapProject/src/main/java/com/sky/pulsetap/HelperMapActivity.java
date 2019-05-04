package com.sky.pulsetap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class HelperMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setTitle("Request Details");

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Intent intent=getIntent();
        final LatLng requestLocation=new LatLng(intent.getDoubleExtra("requestLat",0),intent.getDoubleExtra("requestLong",0));
        final LatLng currentLocation=new LatLng(intent.getDoubleExtra("currentLat",0),intent.getDoubleExtra("currentLong",0));
        ArrayList<Marker> markers=new ArrayList<>();
        markers.add( mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request Location")));
        markers.add( mMap.addMarker(new MarkerOptions().position(currentLocation).title("Responder Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 200);
        mMap.animateCamera(cu);
        Button call= (Button) findViewById(R.id.button4);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91" + intent.getStringExtra("userContact")));
                startActivity(callIntent);
            }
        });
        final Button accept= (Button) findViewById(R.id.button3);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<ParseObject> parseQuery=new ParseQuery("Request");
                parseQuery.whereEqualTo("username",intent.getStringExtra("userContact"));
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(e==null){
                            if(objects.size()>0){
                                for(ParseObject object:objects){
                                    object.put("helperUsername", ParseUser.getCurrentUser().getUsername());
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e==null){
                                                Toast.makeText(getBaseContext(), "Accepted", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                        Uri.parse("http://maps.google.com/maps?saddr="+currentLocation.latitude+","+currentLocation.longitude+"&daddr="+requestLocation.latitude+","+requestLocation.longitude));
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });
        ParseQuery<ParseObject> query2=new ParseQuery<ParseObject>("Request");
        query2.whereExists("helperUsername");
        query2.whereEqualTo("helperUsername",ParseUser.getCurrentUser().getUsername());
        query2.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        accept.setText("Open in Maps");
                    }
                    else{
                        accept.setText("Accept Request");
                    }

                }
            }
        });
    }
}

