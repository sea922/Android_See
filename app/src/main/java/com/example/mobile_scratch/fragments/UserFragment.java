package com.example.mobile_scratch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.activity.HistoryOrderActivity;
import com.example.mobile_scratch.activity.LoginActivity;
import com.example.mobile_scratch.activity.PaymentActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {
    Button logout;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth simpleAuth;
    TextView nameTextView;
    TextView emailTextView;

    FirebaseUser currentUser;
    Button changePassword;
    Button history;
    EditText oldPasswordEditText;
    EditText newPasswordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user, container, false);

        logout = rootView.findViewById(R.id.logout);

        nameTextView = rootView.findViewById(R.id.textViewName);
        emailTextView = rootView.findViewById(R.id.textViewEmail);
        changePassword = rootView.findViewById(R.id.buttonChangePassword);
        oldPasswordEditText = rootView.findViewById(R.id.editTextOldPassword);
        newPasswordEditText = rootView.findViewById(R.id.editTextNewPassword);
        history = rootView.findViewById(R.id.buttonHistory);


        simpleAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);

        currentUser = simpleAuth.getCurrentUser();

        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            nameTextView.setText(name);
            emailTextView.setText(email);

            Log.d("UserAcc", "Current user: " + name + ", " + email);
        } else {
            Log.d("UserAcc", "No user login");
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Check condition
                        if (task.isSuccessful()) {
                            // When task is successful sign out from firebase
                            simpleAuth.signOut();
                            // Display Toast
                            Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                            // Finish activity
                            requireActivity().finish();
                        }
                    }
                });
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }

        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistoryOrderActivity.class);

                startActivity(intent);
            }
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String oldPassword = oldPasswordEditText.getText().toString().trim();
                final String newPassword = newPasswordEditText.getText().toString().trim();

                if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter old and new passwords", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(requireContext(), "Authentication failed. Please check your old password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        return rootView;
    }
}
