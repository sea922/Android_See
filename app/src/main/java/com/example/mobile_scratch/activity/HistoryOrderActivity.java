package com.example.mobile_scratch.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_scratch.R;

public class HistoryOrderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_order_activity);


        TextView textViewOrderDetails = findViewById(R.id.textViewOrderDetails);
        TextView textViewPrice = findViewById(R.id.textViewPrice);
        TextView textViewQuantity = findViewById(R.id.textViewQuantity);
        TextView textViewTimestamp = findViewById(R.id.textViewTimestamp);
        TextView textViewTotal = findViewById(R.id.textViewTotal);

        textViewOrderDetails.setText("Order Details:...");
        textViewPrice.setText("Price: $25.86");
        textViewQuantity.setText("Quantity: 2");
        textViewTimestamp.setText("Timestamp: June 5, 2023 at 10:33:57 PM UTC+7");
        textViewTotal.setText("Total: $51.72");

    }
}
