package com.sky.pulsetap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {
    private static long back_pressed;
    Boolean userType;
    @Override
    public void onBackPressed()
    {
        if (back_pressed + 2000 > System.currentTimeMillis()) finishAffinity();
        else Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }


    public void redirect(){
        if(userType==false){
            //User Intent
            Intent intent1=new Intent(getBaseContext(), UserActivity.class);
            startActivity(intent1);
        }
        else if(userType==true){
            //Helper Intent
            Intent intent1=new Intent(getBaseContext(), HelperActivity.class);
            startActivity(intent1);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();
        Switch switch1 = (Switch)findViewById(R.id.switch1);
        TextView existingUser= (TextView) findViewById(R.id.textView3);
        final EditText username=(EditText)findViewById(R.id.username);
        final EditText password=(EditText)findViewById(R.id.password);
        final EditText cpassword=(EditText)findViewById(R.id.cPassword);
        final EditText fName=(EditText)findViewById(R.id.fullName);
        final EditText age=(EditText)findViewById(R.id.age);
        final EditText bloodGroup=(EditText)findViewById(R.id.bloodGroup);
        Button signUp=(Button)findViewById(R.id.loginButton);
        userType=false;

        existingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userType=b;
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String bg=bloodGroup.getText().toString().toUpperCase();
                    ParseUser parseUser = new ParseUser();
                    parseUser.setUsername(username.getText().toString());
                    parseUser.setPassword(password.getText().toString());

                    parseUser.put("userType", userType);
                    parseUser.put("fullName", fName.getText().toString());
                    parseUser.put("age", age.getText().toString());
                    parseUser.put("bloodGroup",bg);
                    if(password.getText().toString().equals(cpassword.getText().toString())){
                        if(bg.equals("A+")||bg.equals("A-")||bg.equals("B+")||bg.equals("B-")
                                ||bg.equals("O+")||bg.equals("O-")||bg.equals("AB+")||bg.equals("AB-")){
                    parseUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(getApplicationContext(),"SignUp Success!",Toast.LENGTH_LONG).show();
                                redirect();

                            }
                            else{
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                        }
                    else{
                            Toast.makeText(getApplicationContext(),"Incorrect Blood Group",Toast.LENGTH_LONG).show();
                        }
                    }

                    else {
                        Toast.makeText(getApplicationContext(),"Passwords do not match!",Toast.LENGTH_LONG).show();
                    }

            }
        });

    }

}
