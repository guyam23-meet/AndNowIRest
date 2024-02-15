package com.example.csproject.Activities;

import static com.example.csproject.CommonUtilities.DatabaseUtilities.database;
import static com.example.csproject.CommonUtilities.FullScreenUtilities.fullscreenSetup;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.mAuth;
import static com.example.csproject.CommonUtilities.FullScreenUtilities.systemUiChangeManager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.csproject.R;
import com.google.firebase.database.DatabaseReference;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    //the views in the page
    private Button editProfileButton;
    private EditText email;
    private EditText password;
    private EditText name;
    private TextView backButton;
    //end of the views in the page

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_edit_profile);
        systemUiChangeManager(getWindow().getDecorView());

        connectViews();
    }
    //connects the views to the code
    private void connectViews()
    {
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
        if(view == editProfileButton){
            updateProfile();
        }

        else if(view == backButton)
            startActivity(new Intent(EditProfileActivity.this, HomePageActivity.class));
    }

    //updates the profile info
    private void updateProfile()
    {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userId_reference = database.getReference("users").child(userId);

        boolean hasUpdated = false;
        String editNameInput = name.getText().toString();
        if (editNameInput.length() != 0){
            userId_reference.child("name").setValue(editNameInput);
            hasUpdated = true;}

        String editEmailInput = email.getText().toString();
        if (editEmailInput.length() != 0){
            mAuth.getCurrentUser().updateEmail(editEmailInput);
            hasUpdated = true;}

        String editPasswordInput = password.getText().toString();
        if (editPasswordInput.length() != 0){
            mAuth.getCurrentUser().updatePassword(editPasswordInput);
            hasUpdated = true;}

        if(!hasUpdated)
            Toast.makeText(EditProfileActivity.this, "No field has changed", Toast.LENGTH_LONG).show();

        startActivity(new Intent(EditProfileActivity.this, HomePageActivity.class));
    }
}