package com.example.boardgamerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.boardgamerapp.User;

import org.w3c.dom.Text;

import java.util.List;

public class UserSpinnerAdapter extends ArrayAdapter<User> {

    LayoutInflater layoutInflater;

    public UserSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<User> users) {
        super(context, resource, users);
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = layoutInflater.inflate(R.layout.user_spinner_adapter, null, true);
        User user = getItem(position);
        TextView userName = (TextView) rowView.findViewById(R.id.nameTextView);
        userName.setText(user.getName());
        userName.setTag(user.getId());
        return rowView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
        convertView = layoutInflater.inflate(R.layout.user_spinner_adapter, parent, false);
        User user = getItem(position);
        TextView userName = (TextView) convertView.findViewById(R.id.nameTextView);
        userName.setText(user.getName());
        return convertView;
    }
}


