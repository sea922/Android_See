package com.example.seeStore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.example.seeStore.adapter.CategoryTagListAdapter;
import com.example.seeStore.adapter.WishListAdapter;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.model.Product;
import com.example.seeStore.provider.Provider;
import com.example.seeStore.wishList.WishListController;
import com.example.seeStore.wishList.wishListItem.WishlistItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class WishListFragment extends Fragment {
    public static final String TAG = "WishlistFragment";

    private FrameLayout parentView;
    private ImageButton navbarBtn;
    private RecyclerView wishlistRecyclerView;
    private WishListController wishlistController;
    private LinearLayout wishlistEmptyWrapper, wishlistLoadingWrapper;
    private NestedScrollView wishlistLayout;
    private ImageButton wishlistBackBtn;
    private MaterialButton wishlistGoToShopBtn;

    public WishListFragment() {
        // Required empty public constructor
        super(R.layout.fragment_wishlist);
    }

    public WishListFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_home);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Provider.with(this.getContext()).setCurrentFragment(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_wishlist_filled);

        wishlistController = WishListController.with(this.getContext());

        initViews();
        setEvents();
        showWishlistItems();
    }

    private void initViews() {
        parentView = requireView().findViewById(R.id.wishlistParentView);
        wishlistLayout = requireView().findViewById(R.id.wishlistLayout);
        wishlistEmptyWrapper = requireView().findViewById(R.id.wishlistEmptyWrapper);
        wishlistLoadingWrapper = requireView().findViewById(R.id.wishlistLoadingWrapper);
        wishlistRecyclerView = requireView().findViewById(R.id.wishlistRecyclerView);
        wishlistBackBtn = requireView().findViewById(R.id.wishlistBackBtn);
        wishlistGoToShopBtn = requireView().findViewById(R.id.wishlistGoToShopBtn);
    }

    private void setEvents() {
        wishlistBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        wishlistGoToShopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                fragment.setArguments(bundle);

                // Back pressed handling
                FragmentActivity activity = getActivity();
                Intent searchIntent = new Intent(activity, ProductListFragment.class);
                searchIntent.putExtra("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                Provider.with(activity).setSearchIntent(searchIntent);

                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
            }
        });
    }



    private void showWishlistItems() {
        if (wishlistController.getWishlistItemList().size() == 0) {
            wishlistEmptyWrapper.setVisibility(View.VISIBLE);
            wishlistLoadingWrapper.setVisibility(View.GONE);
            wishlistLayout.setVisibility(View.GONE);
            return;
        }

        ArrayList<Long> productIds = new ArrayList<>();
        for (WishlistItem wishlistItem : wishlistController.getWishlistItemList()) {
            productIds.add(wishlistItem.getProductId());
        }

        // call API to get products
        wishlistLoadingWrapper.setVisibility(View.VISIBLE);
        String entry = "product-list";
        String params = "id=";
        for (int i = 0; i < productIds.size(); i++) {
            params += String.valueOf(productIds.get(i));
            if (i != productIds.size() - 1)
                params += ",";
        }
        String url = BuildConfig.SERVER_URL + entry + "?" + params;
        JsonObjectRequest getRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        wishlistLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this.getContext()).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        wishlistLoadingWrapper.setVisibility(View.GONE);

        // Get the information of products in cart
        List<Product> wishlistProducts = Product.parseProductListFromResponse(response);
        wishlistController.setProductList(wishlistProducts);

        WishListAdapter adapter = new WishListAdapter(wishlistController, this.getContext(), new ChangeNumberItem() {
            @Override
            public void onChanged() {
                showWishlistLayout(wishlistController.getProductList().isEmpty());
            }
        });

        wishlistRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        wishlistRecyclerView.setLayoutManager(layoutManager);

        showWishlistLayout(wishlistProducts.isEmpty());
    }

    private void showWishlistLayout(boolean isEmpty) {
        if (isEmpty) {
            wishlistEmptyWrapper.setVisibility(View.VISIBLE);
            wishlistLayout.setVisibility(View.GONE);
        } else {
            wishlistEmptyWrapper.setVisibility(View.GONE);
            wishlistLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_wishlist);
        super.onDestroyView();
    }
}