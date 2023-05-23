package com.example.mobile_scratch.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginSubmit, registerSwitch;
    ProgressBar progressBar;
    FirebaseAuth simpleAuth;

    TextView bySigning, forgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        simpleAuth = FirebaseAuth.getInstance();


        //asign view
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_pw);
        loginSubmit = findViewById(R.id.btn_submit_login);
        progressBar = findViewById(R.id.loginProgressBar);
        registerSwitch = findViewById(R.id.btn_register);
        bySigning = findViewById(R.id.by_signing_);
        bySigning.setMovementMethod(LinkMovementMethod.getInstance());
        forgetPassword = findViewById(R.id.forget_password);
        forgetPassword.setMovementMethod(LinkMovementMethod.getInstance());

        //asign click handler
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUserAccount();
            }
        });

        registerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
            }
        });

        if (simpleAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    //helper function
    private  void loginUserAccount() {
        progressBar.setVisibility(View.VISIBLE);

        String email, pw;
        email = loginEmail.getText().toString();
        pw = loginPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(), "Please enter correct email!", Toast.LENGTH_LONG).show();
            loginEmail.setText("");
//            loginEmail.setHint("Enter valid email");
//            loginEmail.setHintTextColor(ContextCompat.getColor(this, R.color.red));
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(pw)) {
            Toast.makeText(this, "Please enter password!", Toast.LENGTH_LONG).show();
            loginPassword.setText("");
//            loginPassword.setHint("Enter valid password");
//            loginPassword.setHintTextColor(ContextCompat.getColor(this, R.color.red));
            progressBar.setVisibility(View.GONE);
            return;
        }

        simpleAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            Helper helper = new Helper();
                            helper.toastAuthException(LoginActivity.this, errorCode);
                            if (helper.key == 0) {
                                loginEmail.setError(helper.value);
                                loginEmail.requestFocus();
                            }
                            if (helper.key == 1) {
                                loginPassword.setError(helper.value);
                                loginPassword.requestFocus();
                            }

                        }

                    }
                }
        );
    }
}