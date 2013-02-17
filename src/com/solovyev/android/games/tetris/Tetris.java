/*
 * $Id: Tetris.java,v 1.6 2010/12/17 22:17:33 solovam Exp $
 */
package com.solovyev.android.games.tetris;

import com.solovyev.android.games.tetris.R;
import com.solovyev.games.tetris.TetrisEngine;
import com.solovyev.games.tetris.TetrisEngineImpl;
import com.solovyev.games.tetris.TetrisEvent;
import com.solovyev.games.tetris.TetrisListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

public class Tetris extends Activity implements TetrisListener
{
        private static final String IS_PREVIEW_SHOWN_KEY = "isPreviewShown";
        private static final String IS_FIRST_RUN_KEY = "isFirstRun";
        private static final int GAME_OVER_DIALOG_ID = 0;
        private static final int SCORE_DIALOG_ID = 1;
        private static final int NAME_ENTRY_DIALOG_ID = 2;
        private static final int HELP_DIALOG_ID = 3;
        public static final String PREFERENCES_FILE_NAME = "tetrisPrefrences";
        private TetrisView tetrisView;
        private TetrisEngine tetrisEngine;
        private TetrisEngine.GameState previousGameState;
        private Integer highlightedScorePosition = -1;
        private boolean isFirstRun;
        private boolean destroyed = false;
        
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
        
                setContentView(R.layout.tetris);
        
                tetrisEngine = new TetrisEngineImpl(10, 20);
                
                tetrisView = (TetrisView) findViewById(R.id.tetris);
                
                tetrisView.setTetrisEngine(tetrisEngine);
                
                previousGameState = tetrisEngine.getGameState();
                
                readPreferences();
                
                tetrisEngine.addTetrisListener(this);
                
                if(isFirstRun)
                        showDialog(HELP_DIALOG_ID);
                else
                        tetrisEngine.start();
                
                Log.d(this.getClass().getName(), "created: " + this);
        }
        
        @Override
        public void onDestroy()
        {
                destroy();
                
                super.onDestroy();
        }
        
        @Override
        public void onStop()
        {
                savePreferences();
                
                tetrisEngine.pause();
                
                super.onStop();
        }
        
        @Override
        public void onRestart()
        {
                tetrisEngine.resume();

                super.onRestart();
        }
                
        @Override
        public void onPause()
        {
                tetrisEngine.pause();
                
                super.onPause();
        }
        
        @Override
        public void onResume()
        {
                tetrisEngine.resume();
                
                super.onResume();
        }
        
        private void readPreferences()
        {
                SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                tetrisView.setPreviewShown(preferences.getBoolean(IS_PREVIEW_SHOWN_KEY, true));
                isFirstRun = preferences.getBoolean(IS_FIRST_RUN_KEY, true);
        }
        
        private void savePreferences()
        {
                SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE_NAME, 0);
                SharedPreferences.Editor editor = preferences.edit();                
                editor.putBoolean(IS_PREVIEW_SHOWN_KEY, tetrisView.isPreviewShown());
                editor.putBoolean(IS_FIRST_RUN_KEY, isFirstRun);
                editor.commit();
        }
        
        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.options_menu, menu);
                
                MenuItem showPreviewItem = menu.findItem(R.id.show_preview);
                showPreviewItem.setChecked(tetrisView.isPreviewShown());

                MenuItem showGridItem = menu.findItem(R.id.show_grid);
                showGridItem.setChecked(tetrisView.isGridShown());

                return true;
        }
        
        @Override
        public boolean onPrepareOptionsMenu(Menu menu)
        {
                MenuItem pauseItem = menu.findItem(R.id.pause);

                switch(tetrisEngine.getGameState())
                {
                case PAUSED:
                        pauseItem.setTitle(R.string.resume_menu_title);
                        break;
                case RUNNING:
                        pauseItem.setTitle(R.string.pause_menu_title);
                        break;
                }
                return true;
        }
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
                switch(item.getItemId())
                {
                case R.id.new_game:
                        return newGameItemHandler();
                        
                case R.id.pause:
                        return pauseItemHandler(item);
                        
                case R.id.scores:
                        return scoresItemHandler();
                        
                case R.id.quit:
                        return quitItemHandler();
                        
                case R.id.show_preview:
                        return showPreviewItemAction(item);
                        
                case R.id.show_grid:
                        return showGridItemAction(item);
                        
                case R.id.clear_scores:
                        return clearScoresItemHandler();
                        
                case R.id.help:
                        return helpItemHandler();
                }
                return false;
        }
        
        private boolean clearScoresItemHandler()
        {
                ScoreManager.getInstance(this).clearScores();
                return true;
        }

        private boolean scoresItemHandler()
        {
                showDialog(SCORE_DIALOG_ID);

                return true;
        }

        private boolean showPreviewItemAction(MenuItem item)
        {
                if(item.isChecked())
                {
                        tetrisView.setPreviewShown(false);
                        item.setChecked(false);
                }
                else
                {
                        tetrisView.setPreviewShown(true);
                        item.setChecked(true);
                }
                return true;
        }

        private boolean showGridItemAction(MenuItem item)
        {
                if(item.isChecked())
                {
                        tetrisView.setGridShown(false);
                        item.setChecked(false);
                }
                else
                {
                        tetrisView.setGridShown(true);
                        item.setChecked(true);
                }
                return true;
        }

        private boolean quitItemHandler()
        {
                finish();
                return true;
        }

        private boolean newGameItemHandler()
        {
                tetrisEngine.stop();
                tetrisEngine.start();
                return true;
        }

        private boolean pauseItemHandler(MenuItem item)
        {
                switch(tetrisEngine.getGameState())
                {
                case PAUSED:
                        tetrisEngine.resume();
                        break;
                case RUNNING:
                        tetrisEngine.pause();
                        break;
                }
                return true;
        }
        
        private boolean helpItemHandler()
        {
                showDialog(HELP_DIALOG_ID);
                
                return true;
        }
        
         public synchronized void destroy()
         {
                 if(!destroyed)
                 {
                         destroyed = true;
                         
                         if(tetrisView != null)
                                 tetrisView.destroy();
        
                         if(tetrisEngine != null)
                         {
                                 tetrisEngine.removeTetrisListener(this);
                                 ((TetrisEngineImpl) tetrisEngine).destroy();
                         }
                         
                         Log.d(this.getClass().getName(), "deleted: " + this);
                 }
         }

        @Override
        public void stateChanged(final TetrisEvent e)
        {
                runOnUiThread(new Runnable()
                {
                        public void run()
                        {
                                updateOnUiThreadHandler(e);
                        }
                });
        }
        
        /**
         * Handle tetrisEngine update on UI thread
         * 
         * @param observable
         * @param data
         */
        private void updateOnUiThreadHandler(TetrisEvent e)
        {
                TetrisEngine.GameState gameState = tetrisEngine.getGameState();
                
                if(gameState == TetrisEngine.GameState.GAMEOVER && gameState != previousGameState)
                {
                        showDialog(GAME_OVER_DIALOG_ID);
                }
                
                previousGameState = gameState;
        }
        
        @Override
        protected Dialog onCreateDialog(int id)
        {
                Dialog dialog = null;
                
                switch(id)
                {
                case GAME_OVER_DIALOG_ID:
                        dialog = makeGameOverDialog();
                        break;
                case SCORE_DIALOG_ID:
                        dialog = makeScoreDialog();
                        break;
                case NAME_ENTRY_DIALOG_ID:
                        dialog = makeNameEntryDialog();
                        break;
                case HELP_DIALOG_ID:
                        dialog = makeHelpDialog();
                        break;
                default:
                        dialog = null;
                }
                
                return dialog;
        }
        
        @Override
        protected void onPrepareDialog(int id, Dialog dialog)
        {
                super.onPrepareDialog(id, dialog);
                switch(id)
                {
                case SCORE_DIALOG_ID:
                        ScoreView scoreView = (ScoreView) dialog.findViewById(R.id.score);
                        scoreView.update();
                        scoreView.setHighlightedPosition(highlightedScorePosition);
                        highlightedScorePosition = -1;
                        break;
                default:
                        break;
                }
        }

        private Dialog makeGameOverDialog()
        {
                Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.game_over_dialog_message).setCancelable(false).setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener()
                        {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                        checkScore();
                                }
                        });
                return builder.create();
        }

        private Dialog makeScoreDialog()
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.score, (ViewGroup) findViewById(R.id.score)));
                builder.setTitle(R.string.score_dialog_title);
                builder.setCancelable(false).setPositiveButton(R.string.ok_button_label, null);
                return builder.create();
        }
        
        private Dialog makeNameEntryDialog()
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.name_entry, (ViewGroup) findViewById(R.id.name_entry)));
                builder.setTitle(R.string.name_entry_dialog_title);
                builder.setCancelable(true).setNegativeButton(R.string.cancel_button_label, null).setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener()
                        {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.name_entry);
                                        String name = editText.getText().toString();
                                        if(name.length() != 0)
                                                recordScore(name);
                                }
                        });
                
                return builder.create();
        }
        
        private Dialog makeHelpDialog()
        {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(getLayoutInflater().inflate(R.layout.help, (ViewGroup) findViewById(R.id.help)));
                builder.setTitle(R.string.help_dialog_title);
                builder.setCancelable(false).setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener()
                {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                                if(isFirstRun)
                                {
                                        tetrisEngine.start();
                                        isFirstRun = false;
                                }
                        }
                });
                return builder.create();
        }
        
        private void checkScore()
        {
                int position = ScoreManager.getInstance(Tetris.this).getScorePosition(tetrisEngine.getScore());
                
                highlightedScorePosition = position;

                if(position != -1)
                        showDialog(NAME_ENTRY_DIALOG_ID);
        }
        
        private void recordScore(String name)
        {
                ScoreManager.getInstance(Tetris.this).addScore(name, tetrisEngine.getScore());
                
                showDialog(SCORE_DIALOG_ID);
        }
}