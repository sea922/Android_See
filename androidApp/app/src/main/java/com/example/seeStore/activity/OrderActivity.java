package com.example.seeStore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.seeStore.CustomWidget.MySnackbar;
import com.example.seeStore.CustomWidget.MyToast;
import com.example.seeStore.R;
import com.example.seeStore.cart.CartController;
import com.example.seeStore.cart.cartItem.CartItem;
import com.example.seeStore.cart.cartItem.CartItemDB;
import com.example.seeStore.fragment.CartFragment;
import com.example.seeStore.provider.Provider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Pattern;

public class OrderActivity extends AppCompatActivity {

    private TextInputEditText orderName, orderEmail, orderPhone, orderAddress;
    private MaterialButton orderCheckout;
    private RelativeLayout orderParentView;
    private LinearLayout orderLoadingWrapper;
    private ImageButton orderBackBtn;

    private List<CartItem> cartInfo;

    private static final int FORM_VALIDATED = 0;
    private static final int BLANK_INPUT = 1;
    private static final int EMAIL_INVALID = 2;
    private static final int PHONE_INVALID = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        cartInfo = CartController.with(this).getCartList();
        initViews();
        setEvents();
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        List<Address> addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
//        String cityName = addresses.get(0).getAddressLine(0);
//        String stateName = addresses.get(0).getAddressLine(1);
//        String countryName = addresses.get(0).getAddressLine(2);


    }

    private void initViews() {
        orderParentView = findViewById(R.id.orderParentView);
        orderLoadingWrapper = findViewById(R.id.orderLoadingWrapper);
        orderName = findViewById(R.id.orderName);
        orderEmail = findViewById(R.id.orderEmail);
        orderPhone = findViewById(R.id.orderPhone);
        orderAddress = findViewById(R.id.orderAddress);
        orderCheckout = findViewById(R.id.orderCheckout);
        orderBackBtn = findViewById(R.id.orderBackBtn);
    }
    private void setEvents() {
        orderCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: get all the input text and cart information to call post API
                String entry = "process-order";

                try {
                    JSONObject cartParams = new JSONObject();
                    JSONObject itemHM = new JSONObject();
                    for(CartItem item: cartInfo) {
                        JSONObject sizeHM = new JSONObject();
                        sizeHM.put(item.getSize(), item.getQuantity());
                        itemHM.put(item.getProductId().toString(), sizeHM);
                    }
                    JSONObject info = new JSONObject();

                    String customer_name = String.valueOf(orderName.getText());
                    String email = String.valueOf(orderEmail.getText());
                    String phone = String.valueOf(orderPhone.getText());
                    String location = String.valueOf(orderAddress.getText());

                    int formValidation = formValidator(customer_name, email, phone, location);
                    switch (formValidation) {
                        case BLANK_INPUT: {
                            MySnackbar.inforSnackar(OrderActivity.this, orderParentView, "Bạn vui lòng cung cấp đầy đủ thông tin nhé").show();
                            return;
                        }
                        case EMAIL_INVALID: {
                            MySnackbar.inforSnackar(OrderActivity.this, orderParentView, "Địa chỉ email không hợp lệ. Bạn vui lòng thử lại nhé").show();
                            return;
                        }
                        case PHONE_INVALID: {
                            MySnackbar.inforSnackar(OrderActivity.this, orderParentView, "Số điện thoại không hợp lệ. Bạn vui lòng thử lại nhé").show();
                            return;
                        }
                        case FORM_VALIDATED: {
//                            MySnackbar.inforSnackar(OrderActivity.this, orderParentView, "Đang xử lý đơn hàng. Bạn vui lòng chờ nhé").show();
                            break;
                        }
                    }
                    info.put("customer_name", customer_name);
                    info.put("email", email);

                    phone = "+84" + phone.substring(1);
                    info.put("phone_number", phone);
                    info.put("location", location);

                    cartParams.put("items", itemHM);
                    cartParams.put("info", info);

                    JSONObject params = new JSONObject();
                    params.put("cart", cartParams);

                    System.out.println("OrderActivity: " + params.toString());

                    String url = "http://192.168.88:8000/api/" + entry + "/";
                    JsonObjectRequest postRequest = new JsonObjectRequest (
                            url,
                            params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    System.out.println("POST success");
                                    handleSuccess(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println("POST error");
                                    handleError(error);
                                }
                            }
                    );
                    Provider.with(OrderActivity.this).addToRequestQueue(postRequest);
                } catch (JSONException e) {
                    System.out.println(e.getStackTrace());
                }

            }
        });
        orderBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void handleError(VolleyError error) {
        MySnackbar.inforSnackar(OrderActivity.this, orderParentView, getString(R.string.error_message)).show();
    }

    private void handleSuccess(JSONObject response) {
        try {
            if (response.getString("message").equals("Done!")) {
                // TODO: get rid of products in checked out cart
                for (CartItem item : cartInfo)
                    CartItemDB.with(OrderActivity.this).orderItemDao().delete(item);

                System.out.println("Clear cart successfully");

                //Toast.makeText(OrderActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                // TODO: bug when trying to return back to cart fragment
                intent.putExtra("nextFragment", CartFragment.TAG);
                intent.putExtra("message", "Đặt hàng thành công. Bạn vui lòng kiểm tra email nhé!");
                startActivity(intent);
            } else {
                MySnackbar.inforSnackar(OrderActivity.this, orderParentView, getString(R.string.error_message)).show();
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private int formValidator(String name, String email, String phone, String address) {
        System.out.println(name + " " + email + " " + phone + " " + address);
        if (name.trim().length() == 0) return BLANK_INPUT;
        if (email.trim().length() == 0) return BLANK_INPUT;
        if (phone.trim().length() == 0) return BLANK_INPUT;
        if (address.trim().length() == 0) return BLANK_INPUT;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return EMAIL_INVALID;
        if (!Pattern.compile("(84|0[3|5|7|8|9])+([0-9]{8})\\b", Pattern.CASE_INSENSITIVE).matcher(phone).find()) return PHONE_INVALID;

        return FORM_VALIDATED;
    }
}