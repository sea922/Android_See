package com.example.mobile_scratch.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_scratch.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginSubmit, registerSwitch;

    ImageButton loginGoogle;
    ProgressBar progressBar;
    FirebaseAuth simpleAuth;

    TextView bySigning, forgetPassword;

    GoogleSignInClient googleSignInClient;

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

        loginGoogle = findViewById(R.id.btn_login_gg);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("714743922182-5p5rb7gioagcp78fqqhpl3mf5hdae52a.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                // Start activity for result
                startGoogleLoginActivityIntent.launch(intent);

            }
        });

        if (simpleAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }


    }
    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    ActivityResultLauncher<Intent> startGoogleLoginActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Add same code that you want to add in onActivityResult method
                    Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    if (signInAccountTask.isSuccessful()) {
                        // When google sign in successful initialize string
                        String s = "Google sign in successful";
                        // Display Toast
                        displayToast(s);
                        // Initialize sign in account
                        try {
                            // Initialize sign in account
                            GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                            // Check condition
                            if (googleSignInAccount != null) {
                                // When sign in account is not equal to null initialize auth credential
                                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                                // Check credential
                                simpleAuth.signInWithCredential(authCredential).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // Check condition
                                        if (task.isSuccessful()) {
                                            // When task is successful redirect to profile activity display Toast
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                            displayToast("Firebase authentication successful");
                                        } else {
                                            // When task is unsuccessful display Toast
                                            displayToast("Authentication Failed :" + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
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