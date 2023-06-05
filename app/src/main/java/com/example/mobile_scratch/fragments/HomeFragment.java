package com.example.mobile_scratch.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobile_scratch.R;
import com.example.mobile_scratch.adapter.ProductAdapter;
import com.example.mobile_scratch.models.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db;
    private ViewPager imageSlider;

    private ViewPager sliderViewPager;
    private RecyclerView productRecyclerView;

    private ProductAdapter adapter;
    private DatabaseReference productsRef;



    private CollectionReference productCollectionRef;
    private List<ProductModel> products;
    private DatabaseReference databaseRef;

    ArrayList<ProductModel> itemList = new ArrayList<>();
    List<ProductModel> filteredItemList;

    private EditText searchEditText;
    private String searchQuery;
    private static final int SPEECH_REQUEST_CODE = 123;
    private Button buttonVoiceInput;



    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("products", itemList);
        fragment.setArguments(args);

//        Bundle extras =  this.getArguments();
//        itemList = extras.getParcelableArrayList("products");

        Bundle extras = getArguments();
        if (extras != null) {
            itemList = extras.getParcelableArrayList("products");
            // Perform further operations with the itemList
        }
        // Rest of your code
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);
        productRecyclerView = getView().findViewById(R.id.productsRecyclerView);
        searchEditText = view.findViewById(R.id.editTextSearch);
        buttonVoiceInput = view.findViewById(R.id.buttonVoiceInput);
        buttonVoiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

//        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    searchQuery = searchEditText.getText().toString().trim();
//                    performSearch();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button buttonSearch = view.findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchQuery = searchEditText.getText().toString();

                performSearch(searchQuery);
            }
        });

        Log.d("mockupItem", itemList.toString());
        productRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        filteredItemList = new ArrayList<ProductModel>(itemList);
        Log.d("adapter init data", filteredItemList.toString());
        adapter = new ProductAdapter(this.getContext(), (ArrayList<ProductModel>) filteredItemList);

        productRecyclerView.setAdapter(adapter);


    }
    public void onResume() {
        super.onResume();
        Log.d("on resume", "category fragment");

    }

    private void performSearch(String searchQuery) {
        ArrayList<ProductModel> searchResults = new ArrayList<>();

        for (ProductModel product : itemList) {
            if (product.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                searchResults.add(product);
            }
        }

        adapter.updateData(searchResults);

    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói đi nào...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> voiceResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (voiceResults != null && !voiceResults.isEmpty()) {
                searchQuery = voiceResults.get(0);
                searchEditText.setText(searchQuery);
                performSearch(searchQuery);
            }
        }
    }




}