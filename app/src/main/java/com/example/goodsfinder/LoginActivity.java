package com.example.goodsfinder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText ETemail;
    private EditText ETpassword;

    boolean isConnected() {

        ConnectivityManager cm = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }else{
            return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    startActivity(intent);
                    finish();
                    // User is signed in

                } else {
                    // User is signed out
                }

            }
        };

        ETemail = (EditText) findViewById(R.id.et_email);
        ETpassword = (EditText) findViewById(R.id.et_password);


        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {

        if (isConnected()){
            if(view.getId() == R.id.btn_sign_in)
            {
                if (ETemail.getText().toString().length()<1 || ETpassword.getText().toString().length()<1){
                    Toast.makeText(LoginActivity.this, this.getString(R.string.all_fields), Toast.LENGTH_SHORT).show();
                } else {
                    signIn(ETemail.getText().toString(),ETpassword.getText().toString());
                }

            }else if (view.getId() == R.id.btn_registration)
            {

                if (ETemail.getText().toString().length()<1 || ETpassword.getText().toString().length()<1){
                    Toast.makeText(LoginActivity.this, this.getString(R.string.all_fields), Toast.LENGTH_SHORT).show();
                } else {
                    if (ETemail.getText().toString().contains("@") && ETemail.getText().toString().replace("@", "").length()>1){
                        if (ETpassword.getText().toString().contains(" ") || ETpassword.getText().toString().length()<5){
                            Toast.makeText(LoginActivity.this, this.getString(R.string.password_label), Toast.LENGTH_SHORT).show();
                        } else {
                            registration(ETemail.getText().toString(),ETpassword.getText().toString());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,  this.getString(R.string.correct_email), Toast.LENGTH_SHORT).show();
                    }
                }

                //Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();


            }
        } else {
            Toast.makeText(LoginActivity.this, this.getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
        }


    }

    public void signIn(String email , String password)
    {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.auth_success), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    startActivity(intent);
                    finish();
                }else

                    Toast.makeText(LoginActivity.this, getString(R.string.auth_fail), Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void registration (String email , String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();

                    //mDatabase.child("Users").push().setValue(ETemail.getText().toString());
                    Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(LoginActivity.this, getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
