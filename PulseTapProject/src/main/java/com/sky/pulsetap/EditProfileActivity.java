package com.sky.pulsetap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("Edit Profile");
        Button save = (Button) findViewById(R.id.saveButton);
        Button delete = (Button) findViewById(R.id.deleteButton);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText cpassword = (EditText) findViewById(R.id.cPassword);
        final EditText username = (EditText) findViewById(R.id.username);
        final EditText bloodGroup = (EditText) findViewById(R.id.bloodGroup);
        final EditText age = (EditText) findViewById(R.id.age);
        final EditText fullName = (EditText) findViewById(R.id.fullName);
        username.setText((String) ParseUser.getCurrentUser().get("username"));
        fullName.setText((String) ParseUser.getCurrentUser().get("fullName"));
        age.setText((String) ParseUser.getCurrentUser().get("age"));
        bloodGroup.setText((String) ParseUser.getCurrentUser().get("bloodGroup"));
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bg=bloodGroup.getText().toString().toUpperCase();
                ParseUser parseUser = ParseUser.getCurrentUser();
                parseUser.setUsername(username.getText().toString());
                if(!password.getText().toString().equals("")){
                    parseUser.setPassword(password.getText().toString());
                }
                parseUser.put("fullName", fullName.getText().toString());
                parseUser.put("age", age.getText().toString());
                parseUser.put("bloodGroup", bg);
                if (password.getText().toString().equals(cpassword.getText().toString())) {
                    if (bg.equals("A+") || bg.equals("A-") || bg.equals("B+") || bg.equals("B-")
                            || bg.equals("O+") || bg.equals("O-") || bg.equals("AB+") || bg.equals("AB-")) {
                        parseUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getApplicationContext(), "Update Success!", Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Incorrect Blood Group", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_LONG).show();
                }
            }
        });
    delete.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    ParseUser.logOut();
                    Toast.makeText(getApplicationContext(), "Deleted Successfully!", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
            });
        }
    });
    }
}
