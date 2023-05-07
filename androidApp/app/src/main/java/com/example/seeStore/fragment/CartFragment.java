package com.example.seeStore.fragment;

import static com.example.seeStore.utils.StringUtils.vndFormatPrice;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.seeStore.CustomWidget.MySnackbar;
import com.example.seeStore.R;
import com.example.seeStore.adapter.CartAdapter;
import com.example.seeStore.cart.CartController;
import com.example.seeStore.cart.cartItem.CartItem;
import com.example.seeStore.interfaces.ChangeNumberItem;
import com.example.seeStore.model.Product;

import com.example.seeStore.provider.Provider;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private final Long SHIPPING_FEE = 30000L;

    public static final String TAG = "CartFragment";
    private FrameLayout parentView;

    private ImageButton navbarBtn;
    private RecyclerView cartRecyclerView;
    private CartController cartController;
    private LinearLayout cartLayout, cartEmptyWrapper, cartLoadingWrapper;
    private ImageButton cartBackBtn;
    TextView subTotalText, shippingText, totalText;

    public CartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_cart);
    }

    public CartFragment(ImageButton navbarBtn) {
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
        // Inflate the layout for this fragment
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart_filled);
        cartController = CartController.with(this.getContext());

        initViews();
        setEvents();
        showCartItems();
    }

    private void setEvents() {
        cartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.cartParentView);
        cartRecyclerView = getView().findViewById(R.id.cartRecyclerView);
        cartLayout = getView().findViewById(R.id.cartLayout);
        subTotalText = getView().findViewById(R.id.cartSubTotalText);
        shippingText = getView().findViewById(R.id.cartShippingText);
        totalText = getView().findViewById(R.id.cartTotalText);
        cartEmptyWrapper = getView().findViewById(R.id.cartEmptyWrapper);
        cartLoadingWrapper = getView().findViewById(R.id.cartLoadingWrapper);
        cartBackBtn = getView().findViewById(R.id.cartBackBtn);
    }

    private void showCartItems() {
        if (cartController.getCartList().size() == 0) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartLoadingWrapper.setVisibility(View.GONE);
            cartLayout.setVisibility(View.GONE);
            return;
        }

        ArrayList<Long> productIds = new ArrayList<>();
        for (CartItem cartItem : cartController.getCartList()) {
            productIds.add(cartItem.getProductId());
        }

        // call API to get products
        cartLoadingWrapper.setVisibility(View.VISIBLE);
        String entry = "product-list";
        String params = "id=";
        for (int i = 0; i < productIds.size(); i++) {
            params += String.valueOf(productIds.get(i));
            if (i != productIds.size() - 1)
                params += ",";
        }
        String url = "http://192.68.88:8000/api/" + entry + "?" + params;
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
                        cartLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this.getContext()).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        cartLoadingWrapper.setVisibility(View.GONE);

        // Get the information of products in cart
        List<Product> cartProducts = Product.parseProductListFromResponse(response);
        cartController.setProductList(cartProducts);

        updateTotalPrice();

        CartAdapter adapter = new CartAdapter(cartController, this.getContext(), new ChangeNumberItem() {
            @Override
            public void onChanged() {
                updateTotalPrice();
            }
        });
        cartRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        cartRecyclerView.setLayoutManager(layoutManager);

        showCartLayout(cartProducts.isEmpty());
    }

    private void updateTotalPrice() {
        Long subTotal = 0L;
        List<CartItem> cartItems = cartController.getCartList();
        List<Product> cartProducts = cartController.getProductList();
        for (int i = 0; i < cartItems.size(); i++) {
            subTotal += cartItems.get(i).getQuantity() * cartProducts.get(i).getPrice();
        }
        subTotalText.setText(vndFormatPrice(subTotal));
        totalText.setText(vndFormatPrice(subTotal + SHIPPING_FEE));

        if (subTotal == 0L) {
            showCartLayout(true);
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartLayout.setVisibility(View.GONE);
        } else {
            cartEmptyWrapper.setVisibility(View.GONE);
            cartLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart);
        super.onDestroyView();
    }
}