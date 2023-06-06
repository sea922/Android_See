package com.example.mobile_scratch.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_scratch.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.ktx.Firebase;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;
import com.stripe.android.view.BillingAddressFields;
import com.stripe.android.view.PaymentMethodsActivityStarter;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.LateinitKt;

class FirebaseEphemeralKeyProvider2 implements EphemeralKeyProvider {


    @Override
    public void createEphemeralKey(@NonNull String apiVersion, @NonNull EphemeralKeyUpdateListener ephemeralKeyUpdateListener) {
        Map<String, String> data = new HashMap<>();
        data.put("api_version", apiVersion);
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        mFunctions
                .getHttpsCallable("createEphemeralKey")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) e).getCode();
                                String message = e.getMessage();
                                Log.e("EphemeralKey", String.format("Ephemeral key provider returns error: %s %",code, message ));
                            }

                        }
                        String key = task.getResult().getData().toString();
                        Log.d("EphemeralKey", String.format("Ephemeral key provider returns %s", key));
                        ephemeralKeyUpdateListener.onKeyUpdate(key);
                        return key;
                    }
                });

    }
}

public class PaymentActivity extends AppCompatActivity {

    String totalStr;

    TextView totalTextview;

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;

    FirebaseUser currentUser;

    AtomicReference<PaymentMethod> selectedPaymentMethod;

    FirebaseFirestore db;

    Stripe stripe;

     PaymentSession paymentSession;



     Button buttonPay;

    TextView selectMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_layout);
        totalStr = getIntent().getExtras().getString("total");
        totalTextview = findViewById(R.id.totalTextview);
        totalTextview.setText(totalStr);
        buttonPay = findViewById(R.id.buttonPay);
        buttonPay.setClickable(false);
        selectMethod = findViewById(R.id.paymentmethod);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51MIEwYJsrg1S63j9EkIMm1004xfjWvaZTCYANl7h2cZMRWivukdhRhlUeJogFb1SEiNukqONbya2EEfLLwy4oTg400Mi6FHqwF"
        );
        stripe = new Stripe(getApplicationContext(), PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());
//        selectedPaymentMethod = new PaymentMethod()
//        buttonPay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (selectedPaymentMethod != null) {
//                    confirmPayment(selectedPaymentMethod.get().id);
//                }
//
//            }
//        });
        selectMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentSession.presentPaymentMethodSelection(null);


//                if (selectedPaymentMethod == null) {
//                    Log.d("select method", "null");
//                    paymentSession.presentPaymentMethodSelection(null);
//                } else {
//                    Log.d("select method", "not null");
//                    paymentSession.presentPaymentMethodSelection(selectedPaymentMethod.get().id);
//                }

            }
        });


        setupPaymentSession();
    }

    void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        // implemented in the next steps
    }

    private void setupPaymentSession() {
        CustomerSession.initCustomerSession(this, new FirebaseEphemeralKeyProvider());


        paymentSession = new PaymentSession(this, new PaymentSessionConfig.Builder()
                .setShippingInfoRequired(false)
                .setShippingMethodsRequired(false)
                .setBillingAddressFields(BillingAddressFields.None)
                .setShouldShowGooglePay(true)
                .setPaymentMethodTypes(
                        Arrays.asList(PaymentMethod.Type.Card)
                )
                .build());
        Log.d("create payment session", paymentSession.toString());


        paymentSession.init(new PaymentSession.PaymentSessionListener() {
            @Override
            public void onCommunicatingStateChanged(boolean b) {
                Log.d("PaymentSession",  String.format("isCommunicating %s", b));
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.e("PaymentSession",  String.format("erro %s %s", i, s));

            }

            @Override
            public void onPaymentSessionDataChanged(@NonNull PaymentSessionData paymentSessionData) {
                Log.d("PaymentSessionDataChanged", paymentSessionData.toString());

                if(paymentSessionData.isPaymentReadyToCharge()) {
                    Log.d("PaymentSession", "Ready to charge");
                    Button buttonPay = findViewById(R.id.buttonPay);
                    buttonPay.setEnabled(true);

                    if (paymentSessionData.getPaymentMethod() != null) {
                        Log.d("PaymentSession", "Set payment method");
                        selectedPaymentMethod.set(paymentSessionData.getPaymentMethod());
                        String method = selectedPaymentMethod.get().card.brand.toString() + " ends with " + selectedPaymentMethod.get().card.last4.toString();
                        TextView selectMethod = findViewById(R.id.paymentmethod);
                        selectMethod.setText(method);
                    }

                }
            }
        });

    }


    void confirmPayment(String paymentMethodId) {
        CollectionReference paymentCollection = db
                .collection("stripe_customers")
                .document(currentUser.getUid())
                .collection("payments");
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", Integer.valueOf(totalStr));
        paymentData.put("currency", "usd");

        paymentCollection.add(paymentData).addOnSuccessListener(task->{
            task.addSnapshotListener((snapshot, e)->{
               if (e != null) {
                   Log.w("payment", "Listen failed.", e);
                   return;
               }
               if (snapshot.exists()) {
                   Log.d("payment", snapshot.getData().toString());
                   String clientSecret = (String) snapshot.getData().get("client_secret");
                   if (!clientSecret.isEmpty()) {
                        stripe.confirmPayment(this, ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                paymentMethodId,
                                clientSecret
                        ));
                       Toast.makeText(getApplicationContext(), "Payment Done!!", Toast.LENGTH_LONG).show();
                   }
               } else {
                   Log.e("payment", "Current payment intent : null");
               }
            });
        }).addOnFailureListener(e->{
            Log.w("payment", e.getMessage());
        });

    }


}