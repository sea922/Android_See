package com.example.mobile_scratch.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.activity.CartActivity;
import com.example.mobile_scratch.activity.MainActivity;
import com.example.mobile_scratch.activity.PaymentActivity;
import com.example.mobile_scratch.activity.ProductDetailActivity;
import com.example.mobile_scratch.adapter.CartAdapter;
import com.example.mobile_scratch.adapter.PaymentRequest;
import com.example.mobile_scratch.models.CartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    String user;
    FirebaseFirestore db;

    TextView totalTextView;
    AtomicDouble AtomicTotal;

    Button buttonProceedToPayment;


    public CartFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cartItems = new ArrayList<>();



        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        AtomicTotal = new AtomicDouble(0.00);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        buttonProceedToPayment = view.findViewById(R.id.buttonProceedToPayment);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        totalTextView = view.findViewById(R.id.textViewTotalPrice);
        ImageButton backButton = view.findViewById(R.id.leftTopBarBtn);

        Button buttonEmptyCart = view.findViewById(R.id.buttonEmptyCart);
        buttonEmptyCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear all items in the cart
                clearCart();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        view.findViewById(R.id.rightTopBarBtn).setEnabled(false);
        ((TextView) view.findViewById(R.id.titleTopBarText)).setText("Check Out");

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setHasFixedSize(true);

        cartAdapter = new CartAdapter(getContext(), cartItems, totalTextView, AtomicTotal);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems(new IProductListener() {
            @Override
            public void onSucess(List<CartItem> data) {
                cartItems = data;
                cartAdapter.notifyDataSetChanged();
//                AtomicTotal.set(total);
//                totalTextView.setText(total.toString());
                //Log.d("data on override", cartItems.toString());
            }
        });

        buttonProceedToPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);

                startActivity(intent);
            }
        });




    }

    private void loadCartItems(IProductListener productListener) {
        String path = String.format("cart/%s", user);

        DocumentReference holeCartRef = db.document(path);



        holeCartRef.get().addOnSuccessListener(test->{
            if (!test.exists()) {
                return;
            }
            test.getData().forEach((id,item)->{
                String[] variant = extractItemID(id);

                Map<String, Object> dataForCartItem = (Map<String, Object>) item;
                dataForCartItem.put("productId", variant[0]);
                dataForCartItem.put("size", variant[1]);

                DocumentReference productDetail = db.collection("products").document(variant[0]);
                productDetail.get().addOnSuccessListener(task->{
                    if(!task.exists()) {return;}
                    String img = ((ArrayList<String>) task.get("img")).get(0);
                    dataForCartItem.put("img", img);
                    dataForCartItem.put("name", task.get("name"));
                    int quantity = ((Number) dataForCartItem.get("quantity")).intValue();
                    dataForCartItem.replace("quantity", quantity);
                    Double itemTotal = Double.valueOf(dataForCartItem.get("price").toString())*quantity;
                    itemTotal = Math.round(itemTotal * 100.0) / 100.0;
                    AtomicTotal.getAndAdd(itemTotal);
                    totalTextView.setText(String.format("%.2f", AtomicTotal.get()));
                    //Log.d("temp data", dataForCartItem.toString());
                    CartItem cartItem = new CartItem(dataForCartItem);
                    cartItems.add(cartItem);
                    productListener.onSucess(cartItems);

                });
            });
        });

    }

    private String[] extractItemID(String id) {
        String productID = id.trim().substring(0,20);
        String size = id.trim().substring(20);
        return new String[]{productID, size};
    }




    public interface IProductListener {


        //        void onSucess(List<CartItem> data, Double total);
        void onSucess(List<CartItem> data);

    }

    private void clearCart() {
        String path = String.format("cart/%s", user);
        DocumentReference cartRef = db.document(path);

        cartRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Cart cleared successfully
                        Toast.makeText(getContext(), "Cart cleared", Toast.LENGTH_SHORT).show();

                        // Clear the cartItems list and update the adapter
                        cartItems.clear();
                        cartAdapter.notifyDataSetChanged();

                        // Reset the total amount
                        AtomicTotal.set(0.00);
                        totalTextView.setText("0.00");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to clear cart
                        Toast.makeText(getContext(), "Failed to clear cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }




}