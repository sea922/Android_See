package com.example.mobile_scratch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.activity.LoginActivity;
import com.example.mobile_scratch.activity.UserActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;

class Order {
    String orderID;
    long price;

    String status;


    public Order(String orderID, long price, String status) {
        this.orderID = orderID;
        this.price = price;
        this.status = status;
    }

    public String getOrderID() {
        return orderID;
    }

    public long getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}

class OderListViewAdapter extends BaseAdapter {

    final ArrayList<Order> orders;

    OderListViewAdapter(ArrayList<Order> orders) {
        this.orders = orders;
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int i) {
        return orders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View viewOrder;
        if (view == null) {
            viewOrder = View.inflate(viewGroup.getContext(), R.layout.order_item_view, null);
        } else viewOrder = view;
        Order order = getItem(i);
        ((TextView) viewOrder.findViewById(R.id.textOrderID)).setText(order.getOrderID());

        Double amountDolar = ((double) order.getPrice())*1.0 / 100;

        ((TextView) viewOrder.findViewById(R.id.textOrderAMount)).setText(String.valueOf(amountDolar));
        ((TextView) viewOrder.findViewById(R.id.textOrderStatus)).setText(order.getStatus());
        return viewOrder;
    }
}
public class UserFragment extends Fragment {
    Button logout;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth simpleAuth;



    FirebaseUser currentUser;

    TextView nameTextView;
    TextView emailTextView;

    Button changePassword;
    Button history;
    EditText oldPasswordEditText;
    EditText newPasswordEditText;



    ListView orderList;

    FirebaseFirestore db;
    CollectionReference paymentRef;

    String userID;


    OderListViewAdapter oderListViewAdapter;
    ArrayList<Order> orderItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user, container, false);

        logout = rootView.findViewById(R.id.logout);

        emailTextView = rootView.findViewById(R.id.textViewEmail);
        changePassword = rootView.findViewById(R.id.buttonChangePassword);
        oldPasswordEditText = rootView.findViewById(R.id.editTextOldPassword);
        newPasswordEditText = rootView.findViewById(R.id.editTextNewPassword);


        simpleAuth = FirebaseAuth.getInstance();


        orderList = rootView.findViewById(R.id.listOrder);
        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        paymentRef = db.collection("stripe_customers").document(userID).collection("payments");

        orderItems = new ArrayList<>();



        System.out.println("CONC");
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);

        currentUser = simpleAuth.getCurrentUser();

        if (currentUser != null) {
//            String email = currentUser.getEmail();
            String email = "Phone: 0989999999";

            emailTextView.setText(email);


            Log.d("UserAcc", "Current user: " + ", " + email);
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
                                                oldPasswordEditText.setText("");
                                                newPasswordEditText.setText("");
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oderListViewAdapter = new OderListViewAdapter(orderItems);
        orderList.setAdapter(oderListViewAdapter);

        loadOrders(new IOrder() {
            @Override
            public void onAnOrder(Order foundOrder) {
                orderItems.add(foundOrder);
                Log.d("order now", orderItems.toString());
                oderListViewAdapter.notifyDataSetChanged();
            }
        });
    }
    interface IOrder {
        void onAnOrder(Order foundOrder);
    }
    void loadOrders(IOrder iOrder) {
        Log.d("ref string",paymentRef.getPath().toString());

        paymentRef.get().addOnSuccessListener(task->{
            if(task.isEmpty()) {
                Log.d("order list","empty");
                return;
            }
            task.getDocuments().forEach((snapshot)->{
                String orderId = snapshot.getId();
                long amount = (long) snapshot.get("amount");
                String status = "success";
                if (snapshot.get("error") != null) {
                    status = "failed";
                }
                Order order = new Order(orderId, amount, status);
                iOrder.onAnOrder(order);
                Log.d("order list", order.toString());
            });
        });
    }
}