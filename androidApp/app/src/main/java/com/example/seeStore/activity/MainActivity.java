package com.example.seeStore.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.seeStore.R;
import com.example.seeStore.adapter.CategoryImageListAdapter;
import com.example.seeStore.provider.Provider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        setupCategoryLists();
    }

    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = findViewById(R.id.mainMaleCategoryImageList);
        RecyclerView femaleView = findViewById(R.id.mainFemaleCategoryImageList);

        // category list for male
        CategoryImageListAdapter maleAdapter = new CategoryImageListAdapter(this);
        maleAdapter.setCategoryList(Provider.getInstance().getCategoryList("nam"));
        maleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        maleView.setAdapter(maleAdapter);

        // category list for female
        CategoryImageListAdapter femaleAdapter = new CategoryImageListAdapter(this);
        femaleAdapter.setCategoryList(Provider.getInstance().getCategoryList("nu"));
        femaleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        femaleView.setAdapter(femaleAdapter);
    }

    private void initViews() {
        // assign view members (if any)
    }

    public void onMainSearchBarClicked(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void onMainCartBtnClicked(View view) {
        // TODO: go to Cart Activity
    }
}