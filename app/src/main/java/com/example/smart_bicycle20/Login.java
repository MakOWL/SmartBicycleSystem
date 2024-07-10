package com.example.smart_bicycle20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    TextInputLayout userName, password;
    Button loginBtn, forgotPassBtn,signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.go);
        forgotPassBtn = findViewById(R.id.forgot);
        signUpBtn = findViewById(R.id.sign_up);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,SignUp.class);
                startActivity(intent);
            }
        });

    }

    private Boolean validateUserName(){
        String val = userName.getEditText().getText().toString();
        String whiteSpace = "\\A\\w{4,20}\\z";
        if(val.isEmpty()){
            userName.setError("Field cannot be empty");
            return false;
        } else{
            userName.setError(null);
            userName.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(){
        String val = password.getEditText().getText().toString();
        if(val.isEmpty()){
            password.setError("Field cannot be empty");
            return false;
        }else{
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public void LoginUser(View view){
        if(!validateUserName() | !validatePassword()){
          return;
        }else{
            isUser();
        }
    }

    private void isUser() {
        String userEnteredUserName = userName.getEditText().getText().toString();
        String userEnteredPassword = password.getEditText().getText().toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Customer");
        Query checkUser = reference.orderByChild("uname").equalTo(userEnteredUserName);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userName.setError(null);
                    userName.setErrorEnabled(false);
                    String passwordFromDB = snapshot.child(userEnteredUserName).child("pass").getValue().toString();
                  if(passwordFromDB.equals(userEnteredPassword)){
                      password.setError(null);
                      password.setErrorEnabled(false);
                      String fnameFromDB = snapshot.child(userEnteredUserName).child("fname").getValue().toString();
                      String unameFromDB = snapshot.child(userEnteredUserName).child("uname").getValue().toString();
                      String phoneFromDB = snapshot.child(userEnteredUserName).child("phone").getValue().toString();
                      String emailFromDB = snapshot.child(userEnteredUserName).child("email").getValue().toString();

                      Intent intent = new Intent(getApplicationContext(),LocationDisplay.class);
                      intent.putExtra("fname",fnameFromDB);
                      intent.putExtra("uname",unameFromDB);
                      intent.putExtra("phone",phoneFromDB);
                      intent.putExtra("email",emailFromDB);

                      startActivity(intent);
                  } else {
                      password.setError("Wrong Password");
                      password.requestFocus();
                  }
                }else{
                    userName.setError("No Such User");
                    userName.requestFocus();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}