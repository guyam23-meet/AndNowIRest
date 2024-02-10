package com.example.csproject.Activities;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //the views in the page
    private EditText email;
    private EditText password;
    private TextView signUp;
    private Button signIn;
    //end of the views in the page
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_main);
        systemUiChangeManager(getWindow().getDecorView());

        connectViews();
    }
    //connects the views to the code
    private void connectViews()
    {
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
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
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
                        startActivity(new Intent(MainActivity.this, HomePageActivity.class));

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
    //end of sign in functions
}