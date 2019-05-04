package com.sky.pulsetap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class aboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About");
        ImageButton youtube=(ImageButton)findViewById(R.id.youtube2);
        ImageButton ambulance=(ImageButton)findViewById(R.id.imageButtonCallAmbulance);
        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoClient = new Intent(Intent.ACTION_VIEW);
                videoClient.setData(Uri.parse("http://m.youtube.com/watch?v=O_49wMpdews?start=11"));
                startActivity(videoClient);
            }
        });
        ambulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91-8130894069"));
                startActivity(callIntent);
            }
        });

    }
}
