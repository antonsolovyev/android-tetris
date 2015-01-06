/*
 * $Id: ScoreManager.java,v 1.1 2010/01/27 21:37:45 solovam Exp $
 */
package com.solovyev.android.games.tetris;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;


public class ScoreManager
{
    public static final int MAX_SCORES = 10;
    private static final String SCORE_FILE_NAME = "scores.bin";
    private static ScoreManager instance;

    public static ScoreManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new ScoreManager(context);
        }

        return instance;
    }

    private Context context;
    private List<ScoreEntry> scores;

    private ScoreManager(Context context)
    {
        this.context = context;
        scores = readScores();
    }

    public List<ScoreEntry> getScores()
    {
        return new ArrayList<ScoreEntry>(scores);
    }

    public void addScore(String name, int score)
    {
        ScoreEntry entry = new ScoreEntry(name, score, System.currentTimeMillis());

        scores = addScore(entry);

        writeScores(scores);
    }

    public int getScorePosition(int score)
    {
        ScoreEntry entry = new ScoreEntry("", score, System.currentTimeMillis());

        List<ScoreEntry> res = addScore(entry);

        return res.indexOf(entry);
    }

    private List<ScoreEntry> addScore(ScoreEntry entry)
    {
        ArrayList<ScoreEntry> res = (ArrayList<ScoreEntry>) getScores();

        res.add(entry);

        Collections.sort(res, Collections.reverseOrder());

        if (res.size() > MAX_SCORES)
        {
            res = new ArrayList<ScoreEntry>(res.subList(0, MAX_SCORES));
        }

        return res;
    }

    public int getLowestScore()
    {
        if (scores.size() == 0)
        {
            return 0;
        }

        return scores.get(scores.size() - 1).getScore();
    }

    public void clearScores()
    {
        scores = new ArrayList<ScoreEntry>();

        writeScores(scores);
    }

    @SuppressWarnings("unchecked")
    private List<ScoreEntry> readScores()
    {
        ObjectInputStream objectInputStream = null;
        List<ScoreEntry> res = new ArrayList<ScoreEntry>();
        try
        {
            objectInputStream = new ObjectInputStream(new BufferedInputStream(context.openFileInput(SCORE_FILE_NAME)));
            res = (ArrayList<ScoreEntry>) objectInputStream.readObject();
        }
        catch (Exception e)
        {
            Log.e(getClass().getName(), "error reading score file: ", e);
        }
        finally
        {
            try
            {
                if (objectInputStream != null)
                {
                    objectInputStream.close();
                }
            }
            catch (IOException e)
            {
            }
        }

        return res;
    }

    private void writeScores(List<ScoreEntry> scores)
    {
        ObjectOutputStream objectOutputStream = null;
        try
        {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput(SCORE_FILE_NAME, Context.MODE_PRIVATE)));
            objectOutputStream.writeObject(scores);
        }
        catch (Exception e)
        {
            Log.e(getClass().getName(), "error writing score file: ", e);
        }
        finally
        {
            try
            {
                if (objectOutputStream != null)
                {
                    objectOutputStream.close();
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    public static class ScoreEntry implements Serializable, Comparable<ScoreEntry>
    {
        private static final long serialVersionUID = 1L;
        private String name;
        private Integer score;
        private long timestamp;

        public ScoreEntry(String name, Integer score, Long timestamp)
        {
            this.name = name;
            this.score = score;
            this.timestamp = timestamp;
        }

        public String getName()
        {
            return name;
        }

        public Integer getScore()
        {
            return score;
        }

        public Long getTimestamp()
        {
            return timestamp;
        }

        @Override
        public int compareTo(ScoreEntry e)
        {
            return score.compareTo(e.getScore());
        }
    }
}
