package com.example.mobile_scratch.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.mobile_scratch.R;
import com.example.mobile_scratch.ultis.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class ProductDetailAdapter extends PagerAdapter {

    Context context;

    ArrayList<String> imageURLs;

    LayoutInflater mLayoutInflater;

    FirebaseStorage storageFB;

    public ProductDetailAdapter(Context context, ArrayList<String> imageURLs) {
        this.context = context;
        this.imageURLs = imageURLs;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        storageFB = FirebaseStorage.getInstance();
    }

    @Override
    public int getCount() {
        return imageURLs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int pos) {
        View itemView = mLayoutInflater.inflate(R.layout.product_detail_view, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.productDetailView);

        StorageReference imgURL = storageFB.getReferenceFromUrl(imageURLs.get(pos).trim());

        GlideApp
                .with(context)
                .load(storageFB.getReferenceFromUrl(imgURL.toString()))
                .apply(new RequestOptions()
                .fitCenter()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL))
                .into(imageView);

        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }
}
