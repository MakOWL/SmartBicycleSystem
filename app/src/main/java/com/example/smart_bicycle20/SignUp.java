package com.example.smart_bicycle20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    TextInputLayout fullName,userName,
            userEmail,userPhone,userPassword,userConfirmPassword;
    Button goButton,regToLogin;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        fullName = findViewById(R.id.name);
        userName = findViewById(R.id.username);
        userEmail = findViewById(R.id.email);
        userPhone = findViewById(R.id.phone);
        userPassword = findViewById(R.id.password);
        userConfirmPassword = findViewById(R.id.confirm_password);

        goButton = findViewById(R.id.signUp);
        regToLogin = findViewById(R.id.goBack);
    }

    public void registerUser(View view) {
        if (!validateName() | !validateUserName() | !validateEmail() | !validatePhone() | !validatePassword() | !validateConfirmPassword()) {
            return;
        }

        String fname = fullName.getEditText().getText().toString();
        String uname = userName.getEditText().getText().toString();
        String email = userEmail.getEditText().getText().toString();
        String phone = userPhone.getEditText().getText().toString();
        String pass = userPassword.getEditText().getText().toString();
        String cpass = userConfirmPassword.getEditText().getText().toString();

        if (!pass.equals(cpass)) {
            Toast.makeText(SignUp.this, "Password and Confirm Password need to be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Customer");

        // Check if username already exists
        reference.child(uname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userName.setError("Username already taken");
                    userName.requestFocus();
                } else {
                    // Proceed with registration
                    UserHelpeClass user = new UserHelpeClass(fname, uname, email, phone, pass, cpass);
                    reference.child(uname).setValue(user);
                    Toast.makeText(SignUp.this, "Welcome " + uname, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, Login.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

   private Boolean validateName(){
        String val = fullName.getEditText().getText().toString();
        if(val.isEmpty()){
            fullName.setError("Field cannot be empty");
            return false;
        }else{
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
   }

    private Boolean validateUserName(){
        String val = userName.getEditText().getText().toString();
        String whiteSpace = "\\A\\w{4,20}\\z";
        if(val.isEmpty()){
            userName.setError("Field cannot be empty");
            return false;
        } else if(val.length()>=15) {
            userName.setError("Username is too long");
            return false;
        } else if (!val.matches(whiteSpace)) {
            userName.setError("No white spaces in username");
            return false;
        } else{
            userName.setError(null);
            userName.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = userEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            userEmail.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
          userEmail.setError("Invalid Email");
          return false;
        } else {
            userEmail.setError(null);
            userEmail.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validatePhone(){
        String val = userPhone.getEditText().getText().toString();
        if(val.isEmpty()){
            userPhone.setError("Field cannot be empty");
            return false;
        }else{
            userPhone.setError(null);
            userPhone.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(){
        String val = userPassword.getEditText().getText().toString();
        if(val.isEmpty()){
            userPassword.setError("Field cannot be empty");
            return false;
        }else{
            userPassword.setError(null);
            userPassword.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateConfirmPassword(){
        String val = userConfirmPassword.getEditText().getText().toString();
        if(val.isEmpty()){
            userConfirmPassword.setError("Field cannot be empty");
            return false;
        }else{
            userConfirmPassword.setError(null);
            userConfirmPassword.setErrorEnabled(false);
            return true;
        }
    }

}