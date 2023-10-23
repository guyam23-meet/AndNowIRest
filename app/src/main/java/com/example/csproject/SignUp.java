package com.example.csproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;


public class SignUp extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private EditText name;
    private EditText email_signup;
    private EditText password_signup;
    private TextView sign_up;
    private Button submit;
    private TextView to_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");
        name = findViewById(R.id.name_signup);
        email_signup = findViewById(R.id.email_signup);
        password_signup = findViewById(R.id.password_signup);
        sign_up = findViewById(R.id.SignUpText);
        submit = findViewById(R.id.submit);
        to_signin = findViewById(R.id.goto_signin);
        submit.setOnClickListener(this);
        to_signin.setOnClickListener(this);
    }
    //creates user
    private void create_user( final String email,final String password){
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String uid = mAuth.getCurrentUser().getUid();
                            //adds the name to the database under the current user
                            database.getReference("users").child(uid).child("name").setValue(name.getText().toString());
                            Intent i = new Intent(SignUp.this,HomePage.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(SignUp.this,"Authentication failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    //submit button
    @Override
    public void onClick(View view) {
        if(view==submit){
            if(email_signup.getText().length()==0 || password_signup.getText().length()==0|| name.getText().length()==0){
                Toast.makeText(SignUp.this,"Please fill all the given fields",Toast.LENGTH_LONG).show();
            }
            else {
                create_user(email_signup.getText().toString(), password_signup.getText().toString());
            }
        } else if (view==to_signin) {
            startActivity(new Intent(SignUp.this,MainActivity.class));
        }
    }
}