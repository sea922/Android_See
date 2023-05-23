package com.example.mobile_scratch.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_scratch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import com.example.mobile_scratch.activity.Helper;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText registerEmail, registerPassword, registerRePassword;
    Button registerSubmit, loginSwitch;
    ProgressBar progressBar;
    FirebaseAuth simpleAuth;

    TextView bySigning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        simpleAuth = FirebaseAuth.getInstance();

        //assign editText
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_pw);
        registerRePassword = findViewById(R.id.register_re_pw);

        bySigning = findViewById(R.id.by_signing_);
        bySigning.setMovementMethod(LinkMovementMethod.getInstance());
        //assign button
        registerSubmit = findViewById(R.id.btn_submit_register);
        loginSwitch = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.registerProgressBar);

        //assign click handler
        loginSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();
            }
        });

    }

    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email, pw, rpw;
        email = registerEmail.getText().toString();
        pw = registerPassword.getText().toString();
        rpw = registerRePassword.getText().toString();
        
        if (!TextUtils.equals(pw, rpw)) {
            Toast.makeText(this, "Password does not match!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            registerRePassword.setText("");

            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter correct email!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            registerPassword.setText("");
            return;
        }
        if (TextUtils.isEmpty(pw)) {
            Toast.makeText(this, "Please enter password!", Toast.LENGTH_LONG).show();
            registerPassword.setText("");
            progressBar.setVisibility(View.GONE);
            return;
        }

        simpleAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Register new account successful!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                    Helper helper = new Helper();
                    helper.toastAuthException(RegisterActivity.this, errorCode);
                    if (helper.key == 0) {
                        registerEmail.setError(helper.value);
                        registerEmail.requestFocus();
                    }
                    if (helper.key == 1) {
                        registerPassword.setError(helper.value);
                        registerPassword.requestFocus();
                    }

                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}