package com.example.messagingproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;


public class ViewImageFragment extends Fragment {
    private ImageView mainImage;
    private String imageUrl;
    private String imageText;
    private TextView imageTextView;
    private View textBackground;
    private boolean isTextShow=true;

    public ViewImageFragment(String imageUrl,String imageText) {
        this.imageUrl=imageUrl;
        this.imageText=imageText;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_view_image,container,false);

        textBackground=view.findViewById(R.id.imageTextBackground);
        imageTextView=view.findViewById(R.id.imageText);
        imageTextView.setText(imageText);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show or disappear of text
                if(isTextShow){
                    textBackground.setVisibility(View.INVISIBLE);
                    isTextShow=false;
                }else{
                    textBackground.setVisibility(View.VISIBLE);
                    isTextShow=true;
                }
            }
        });


        return view;

    }

    // after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainImage=view.findViewById(R.id.mainImage);

        mainImage.setImageResource(R.drawable.contact_image1);

        Picasso.get().load(imageUrl).into(mainImage);



    }

}