package com.example.seeStore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.seeStore.BuildConfig;
import com.example.seeStore.CustomWidget.MySnackbar;
import com.example.seeStore.R;
import com.example.seeStore.activity.SearchActivity;
import com.example.seeStore.adapter.CategoryImageListAdapter;
import com.example.seeStore.adapter.TrendingAdapter;
import com.example.seeStore.model.Product;
import com.google.android.material.button.MaterialButton;

import com.example.seeStore.provider.Provider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private FrameLayout parentView;
    private LinearLayout homeSearchBar;
    private ImageButton navbarBtn;
    private MaterialButton maleSeeAllBtn;
    private MaterialButton femaleSeeAllBtn;
    private RecyclerView trendingView;
    private FirebaseAuth mAuth;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    public HomeFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_home);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_home_filled);
        initViews();
        setEvents();
        setupCategoryLists();
        //getTrendingProducts();
        welcomeUser();
    }
    private void welcomeUser() {
        mAuth = FirebaseAuth.getInstance();
        // TODO: do something with current user (if necessary)
        // just demo
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getBoolean("hasJustLoggedIn", false)) {
            System.out.println(bundle.getBoolean("hasJustLoggedIn", false));
            FirebaseUser currentUser = mAuth.getCurrentUser();
            MySnackbar.inforSnackar(getContext(), parentView, "Jodern xin chào " + currentUser.getDisplayName() + "!").setAnchorView(R.id.mainNavBarSearchBtn).show();
        }
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.homeParentView);
        homeSearchBar = getView().findViewById(R.id.homeSearchBar);
        maleSeeAllBtn = getView().findViewById(R.id.homeMaleSeeAllBtn);
        femaleSeeAllBtn = getView().findViewById(R.id.homeFemaleSeeAllBtn);
        trendingView = getView().findViewById(R.id.homeTrendingWrapper);
    }

    private void setEvents() {
        homeSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        maleSeeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
            }
        });

        femaleSeeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nu");
                bundle.putString("categoryName", "Thời trang nữ");
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
            }
        });
    }

    private void getTrendingProducts() {
        String entry = "trending";
        String params = "8";
        String url = BuildConfig.SERVER_URL + entry + "/" + params;
        JsonObjectRequest postRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Product> trendingProducts = Product.parseProductListFromResponse(response);
                        setupTrendingProducts(trendingProducts);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this.getContext()).addToRequestQueue(postRequest);
    }

    private void setupTrendingProducts(ArrayList<Product> trendingProducts) {
        TrendingAdapter trendingAdapter = new TrendingAdapter(this.getContext());
        trendingAdapter.setProductList(trendingProducts);
        trendingView.setAdapter(trendingAdapter);
        trendingView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = getView().findViewById(R.id.mainMaleCategoryImageList);
        RecyclerView femaleView = getView().findViewById(R.id.mainFemaleCategoryImageList);

        // category list for male
        CategoryImageListAdapter maleAdapter = new CategoryImageListAdapter(this);
        maleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nam"));
        maleView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        maleView.setAdapter(maleAdapter);

        // category list for female
        CategoryImageListAdapter femaleAdapter = new CategoryImageListAdapter(this);
        femaleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nu"));
        femaleView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        femaleView.setAdapter(femaleAdapter);
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_home);
        super.onDestroyView();
    }
}