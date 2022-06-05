package com.example.boardgamerapp;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingListAdapter extends ArrayAdapter<Rating> {

    public RatingListAdapter(@NonNull Context context, @NonNull ArrayList<Rating> ratings) {
        super(context, 0, ratings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Rating rating = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.rating_list_item, parent, false);
        }
        TextView userName = (TextView) convertView.findViewById(R.id.ratingUser);
        TextView comment = (TextView) convertView.findViewById(R.id.userComment);
        RatingBar mealRating = (RatingBar) convertView.findViewById(R.id.mealRatingStars);
        RatingBar nightRating = (RatingBar) convertView.findViewById(R.id.nightRatingStars);

        userName.setText(rating.getRatingUserName());
        mealRating.setRating(rating.getMealRating());
        nightRating.setRating(rating.getGamenightRating());
        comment.setText(rating.getComment());


        return convertView;
    }

}

