package com.example.boardgamerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class GamesListAdapter extends ArrayAdapter<Game> {

    public GamesListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Game> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Game game = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_list_item, parent, false);
        }
        TextView gameName = (TextView) convertView.findViewById(R.id.gameName);
        TextView gameVotes = (TextView) convertView.findViewById(R.id.gameVotes);

        gameName.setText(game.getName());
        if(game.getVotes() != 0){
            gameVotes.setText("Vote(s): " + String.valueOf(game.getVotes()));
        } else {
            gameVotes.setText("");
        }

        return convertView;
    }
}
