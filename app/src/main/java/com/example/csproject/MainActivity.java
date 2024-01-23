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
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private EditText email;
    private EditText password;
    private TextView signUp;
    private Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CommonFunctions.fullscreenSetup(getWindow());
        setContentView(R.layout.activity_main);
        CommonFunctions.systemUiChangeManager(getWindow().getDecorView());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        email = findViewById(R.id.et_email_activity_main);
        password = findViewById(R.id.et_password_activity_main);
        signIn = findViewById(R.id.btn_signIn_activity_main);
        signUp = findViewById(R.id.tv_signUp_activity_main);

        signIn.setOnClickListener(this);
        signUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (view == signUp)
            startActivity(new Intent(MainActivity.this, SignUp.class));

        if (view == signIn)
            checkFieldsAndSignIn();
    }

    //sign in functions
    public void signInUser(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                        startActivity(new Intent(MainActivity.this, HomePage.class));

                    else
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                });
    }
    public void checkFieldsAndSignIn()
    {
        if(email.getText().length() != 0 &&
           password.getText().length() != 0)
        {
            String emailInput = email.getText().toString();
            String passwordInput = password.getText().toString();
            signInUser(emailInput, passwordInput);
        }
        else
            Toast.makeText(MainActivity.this, "Please fill all the given fields", Toast.LENGTH_LONG).show();
    }
    //end sign in functions
}