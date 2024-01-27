package com.example.csproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public Button editProfileButton;
    public EditText email;
    public EditText password;
    public EditText name;
    public FirebaseDatabase database;
    public FirebaseAuth mAuth;
    public TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CommonFunctions.fullscreenSetup(getWindow());
        setContentView(R.layout.activity_edit_profile);
        CommonFunctions.systemUiChangeManager(getWindow().getDecorView());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        email = findViewById(R.id.et_email_activity_edit);
        password = findViewById(R.id.et_password_activity_edit);
        name = findViewById(R.id.et_name_activity_edit);
        editProfileButton = findViewById(R.id.btn_submit_activity_edit);
        backButton = findViewById(R.id.btn_back_activity_edit);

        editProfileButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if(view == editProfileButton)
            updateProfile();

        else if(view == backButton)
            startActivity(new Intent(EditProfileActivity.this, HomePageActivity.class));
    }

    public void updateProfile()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userId_reference = database.getReference("users").child(userId);

        String editNameInput = name.getText().toString();
        if (editNameInput.length() != 0)
            userId_reference.child("name").setValue(editNameInput);

        String editEmailInput = email.getText().toString();
        if (editEmailInput.length() != 0)
            mAuth.getCurrentUser().updateEmail(editEmailInput);

        String editPasswordInput = password.getText().toString();
        if (editPasswordInput.length() != 0)
            mAuth.getCurrentUser().updatePassword(editPasswordInput);
    }
}