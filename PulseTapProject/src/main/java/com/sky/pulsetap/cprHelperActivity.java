package com.sky.pulsetap;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class cprHelperActivity extends AppCompatActivity {
    TextView counterT;
    Handler handler=new Handler();
    int counter=0;
    MediaPlayer mediaPlayer;
    WebView youtubePlayer;
    Boolean handlerStatus=false;
    public void beat(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handlerStatus=true;
                if(counter<30){
                counter++;
                mediaPlayer.start();
                counterT.setText(Integer.toString(counter));
                beat();}
                else{

                    mediaPlayer.pause();
                    counterT.setText("V");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            counter=1;
                            counterT.setText(Integer.toString(counter));
                            beat();
                        }
                    },3000);
                }
            }
        },600);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpr_helper);
        getSupportActionBar().hide();
        counterT=(TextView)findViewById(R.id.textViewCounter);
        mediaPlayer = MediaPlayer.create(this,R.raw.click);
        final ImageView heart= (ImageView) findViewById(R.id.imageViewPulse);
        ImageView youtube=(ImageView)findViewById(R.id.youtube);
        youtubePlayer=(WebView)findViewById(R.id.youtube_web_view);
        ImageButton ambulance=(ImageButton) findViewById(R.id.ImageButtonCallAmbulance);
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(handlerStatus)
                {
                 //Do Nothing
                }
                else {
                    beat();
                }
            }
        });

        heart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                handler.removeCallbacksAndMessages(null);
                handlerStatus=false;
                counterT.setText("0");
                counter=0;
                heart.setEnabled(true);
                return true;
            }
        });
        ambulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+91-8130894069"));
                startActivity(callIntent);
            }
        });
        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (youtubePlayer.getVisibility() == View.VISIBLE) {
                    youtubePlayer.setVisibility(View.INVISIBLE);
                    heart.setVisibility(View.VISIBLE);
                    youtubePlayer.reload();

                } else {
                    youtubePlayer.setVisibility(View.VISIBLE);
                    heart.setVisibility(View.INVISIBLE);
                    handler.removeCallbacksAndMessages(null);
                    counterT.setText("0");
                    counter = 0;
                    handlerStatus=false;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            youtubePlayer.setVisibility(View.INVISIBLE);
                            heart.setVisibility(View.VISIBLE);
                            youtubePlayer.reload();
                        }
                    },52000);
                }
                }
        });
        WebSettings webSettings = youtubePlayer.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        youtubePlayer.loadUrl("https://www.youtube.com/embed/O_49wMpdews?start=11&end=61&controls=0&rel=0");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacksAndMessages(null);
    }
}
