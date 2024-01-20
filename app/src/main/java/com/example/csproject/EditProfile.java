package com.example.csproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity implements View.OnClickListener{
    public Button submit;

    public EditText edit_email;

    public EditText edit_password;

    public EditText edit_name;

    public FirebaseDatabase database;

    public FirebaseAuth mAuth;

    public TextView back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        submit = findViewById(R.id.submit_edit_profile);
        submit.setOnClickListener(this);

        edit_email = findViewById(R.id.email_edit_profile);

        edit_password = findViewById(R.id.password_edit_profile);

        edit_name = findViewById(R.id.name_edit_profile);

        back_button = findViewById(R.id.back_button_edit_profile);
        back_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==submit){
            String userId = mAuth.getCurrentUser().getUid();

            DatabaseReference userId_reference = database.getReference("users").child(userId);
            String edit_name_text = edit_name.getText().toString();
            if(edit_name_text.length()!=0)
                userId_reference.child("name").setValue(edit_name_text);

            String edit_email_text = edit_email.getText().toString();
            if(edit_email_text.length()!=0)
                mAuth.getCurrentUser().updateEmail(edit_email_text);

            String edit_password_text = edit_password.getText().toString();
            if(edit_email_text.length()!=0)
                mAuth.getCurrentUser().updatePassword(edit_password_text);
        }
        if(view==back_button){
            Intent i = new Intent(EditProfile.this,HomePage.class);
            startActivity(i);
        }
    }
}