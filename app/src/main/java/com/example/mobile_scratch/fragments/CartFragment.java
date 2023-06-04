package com.example.mobile_scratch.fragments;

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
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.adapter.CartAdapter;
import com.example.mobile_scratch.models.CartItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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





    public CartFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cartItems = new ArrayList<>();


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.leftTopBarBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed(); // Finish the current activity and navigate back to the previous activity
            }
        });
        // Inflate the layout for this fragment
        view.findViewById(R.id.rightTopBarBtn).setEnabled(false);
        ((TextView) view.findViewById(R.id.titleTopBarText)).setText("Check Out");

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        cartAdapter = new CartAdapter(getContext(), cartItems);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems(new IProductListener() {
            @Override
            public void onSucess(List<CartItem> data) {
                cartItems = data;
                cartAdapter.notifyDataSetChanged();
                Log.d("data on override", cartItems.toString());
            }
        });



    }

    private void loadCartItems(IProductListener productListener) {
        String path = String.format("cart/%s", user);

        DocumentReference holeCartRef = db.document(path);

        List<CartItem> tempListCart = new ArrayList<>();

        List<Map<String, Object>> testList = new ArrayList<>();

        holeCartRef.get().addOnSuccessListener(test->{
            if (!test.exists()) {
                return;
            }
            test.getData().forEach((id,item)->{
                String[] variant = extractItemID(id);
                AtomicReference<Map<String, Object>> wraperData = new AtomicReference<>();

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

                    Log.d("temp data", dataForCartItem.toString());
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

    interface IItemNameImage {
        void storeDataString(Map<String, String> data);
    }
    private void retrieveItemNameImage(String productID,Map<String, Object> preData,  IItemNameImage iItemNameImage ) {
        DocumentReference productDocRef = db.collection("products").document(productID);

        productDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot task) {
                if(!task.exists()) {
                    return;
                }
                Map<String, Object> productData = task.getData();
                try {
                    Map<String, String> temp = new HashMap<>();
                    String productName = productData.get("name").toString();
                    String productImage= ((ArrayList<String>) productData.get("img")).get(0);
                    if (productName == null || productImage == null) return;
                    temp.put("name", productName);
                    temp.put("img", productImage);


                    iItemNameImage.storeDataString(temp);


                } catch (Exception e) {
                    Log.d("img assign err", e.getMessage());
                };
            }
        });
    }


    public interface IProductListener {


        void onSucess(List<CartItem> data);

    }

}