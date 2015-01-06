/*
 * $Id: ScoreView.java,v 1.2 2010/02/01 21:46:40 solovam Exp $
 */
package com.solovyev.android.games.tetris;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ScoreView extends ListView
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yy/MM/dd");

    private int highlightedPosition = -1;
    private List<ScoreManager.ScoreEntry> scores;
    private ScoreAdapter scoreAdapter;

    public ScoreView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setFocusable(false);

        initScores();

        addHeaderView(LayoutInflater.from(getContext()).inflate(R.layout.score_header, this, false));

        scoreAdapter = new ScoreAdapter();

        setAdapter(scoreAdapter);

        Log.d(this.getClass().getName(), "created: " + this);
    }

    public void update()
    {
        initScores();
        changeNotify();
    }

    private void changeNotify()
    {
        scoreAdapter.notifyDataSetChanged();
    }

    private void initScores()
    {
        scores = ScoreManager.getInstance(getContext()).getScores();
    }

    public void setHighlightedPosition(int highlightedPosition)
    {
        this.highlightedPosition = highlightedPosition;
        setSelection(highlightedPosition);
        changeNotify();
    }

    public class ScoreAdapter extends BaseAdapter
    {
        private ScoreManager scoreManager = ScoreManager.getInstance(getContext());

        @Override
        public int getCount()
        {
            return scoreManager.getScores().size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.score_row, parent, false);

            TextView textViewPosition = (TextView) view.findViewById(R.id.score_row_position);
            textViewPosition.setText("" + (position + 1) + ".");

            TextView textViewName = (TextView) view.findViewById(R.id.score_row_name);
            textViewName.setText(scores.get(position).getName());

            TextView textViewScore = (TextView) view.findViewById(R.id.score_row_score);
            textViewScore.setText("" + scores.get(position).getScore());

            TextView textViewTimestamp = (TextView) view.findViewById(R.id.score_row_timestamp);
            textViewTimestamp.setText(DATE_FORMAT.format(new Date(scores.get(position).getTimestamp())));

            if ((highlightedPosition != -1) && (position == highlightedPosition))
            {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.score_animation);
                view.startAnimation(animation);
            }

            return view;
        }

    }
}
