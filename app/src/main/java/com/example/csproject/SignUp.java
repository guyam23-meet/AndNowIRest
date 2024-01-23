package com.example.csproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUp extends AppCompatActivity implements View.OnClickListener {

    public FirebaseAuth mAuth;
    public FirebaseDatabase database;

    public EditText name;
    public EditText email;
    public EditText password;
    public TextView signUpTitle;
    public Button signUpButton;
    public TextView goToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CommonFunctions.fullscreenSetup(getWindow());
        setContentView(R.layout.activity_sign_up);
        CommonFunctions.systemUiChangeManager(getWindow().getDecorView());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        name = findViewById(R.id.et_name_activity_signUp);
        email = findViewById(R.id.et_email_activity_signUp);
        password = findViewById(R.id.et_password_activity_signUp);
        signUpTitle = findViewById(R.id.tv_signUp_activity_signUp);
        signUpButton = findViewById(R.id.btn_submit_activity_signUp);
        goToSignIn = findViewById(R.id.tv_toSignIn_activity_signUp);

        signUpButton.setOnClickListener(this);
        goToSignIn.setOnClickListener(this);
    }

    //creates user


    //submit button
    @Override
    public void onClick(View view)
    {
        if (view == signUpButton)
            checkFieldsAndSignUp();

        else if (view == goToSignIn)
            startActivity(new Intent(SignUp.this, MainActivity.class));
    }

    //sign up functions
    public void createUser(final String email, final String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        insertUserToDatabase();
                        startActivity(new Intent(SignUp.this, HomePage.class));
                    }
                    else
                        Toast.makeText(SignUp.this, "Authentication failed", Toast.LENGTH_LONG).show();
                });
    }
    public void insertUserToDatabase()
    {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userId_reference = database.getReference("users").child(uid);

        userId_reference.child("name").setValue(name.getText().toString());
        userId_reference.child("games_played").setValue(0);
        userId_reference.child("wins").setValue(0);
    }
    public void checkFieldsAndSignUp()
    {
        if(email.getText().length() != 0 &&
           password.getText().length() != 0 &&
           name.getText().length() != 0)
        {
            String emailInput = email.getText().toString();
            String passwordInput = password.getText().toString();
            createUser(emailInput, passwordInput);
        }
        else
            Toast.makeText(SignUp.this, "Please fill all the given fields", Toast.LENGTH_LONG).show();
    }
    //end sign up functions
}