package com.example.mobile_scratch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;

class Order {
    String orderID;
    long price;


    public Order(String orderID, long price) {
        this.orderID = orderID;
        this.price = price;
    }

    public String getOrderID() {
        return orderID;
    }

    public long getPrice() {
        return price;
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
        return viewOrder;
    }
}
public class UserFragment extends Fragment {
    Button logout;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth simpleAuth;


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

        simpleAuth = FirebaseAuth.getInstance();

        orderList = rootView.findViewById(R.id.listOrder);
        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        paymentRef = db.collection("customers").document(userID).collection("payments");

        orderItems = new ArrayList<>();



        System.out.println("CONC");
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);

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

                Order order = new Order(orderId, amount);
                iOrder.onAnOrder(order);
                Log.d("order list", order.toString());
            });
        });
    }
}
