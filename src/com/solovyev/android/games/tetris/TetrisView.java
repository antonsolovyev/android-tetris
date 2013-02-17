/*
 * $Id: TetrisView.java,v 1.8 2010/12/17 22:17:37 solovam Exp $
 */
package com.solovyev.android.games.tetris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.solovyev.android.games.tetris.R;
import com.solovyev.games.tetris.Cell;
import com.solovyev.games.tetris.TetrisEngine;
import com.solovyev.games.tetris.TetrisEvent;
import com.solovyev.games.tetris.TetrisListener;

/**
 * Draw tetris
 * 
 * There are two "fields", the "glass" and the "preview" (where next piece is shown), placed side by side in
 * the center of the screen.
 * 
 * @author solovam
 *
 */
public class TetrisView extends View implements TetrisListener
{
        private Context context;
        private TetrisEngine tetrisEngine;
        private Map<Cell.Color, Drawable> colorToDrawable = new HashMap<Cell.Color, Drawable>();
        private final static int FIELD_PADDING = 5;
        private final static int FIELD_BORDER = 2;
        private static final int PREVIEW_WIDTH = 5;
        private static final int PREVIEW_HEIGHT = 5;
        
        private static final String SCORE_HEADING = "Score";
        private static final String LINES_HEADING = "Lines";
        private static final String SPEED_HEADING = "Speed";
        private static final String PIECES_HEADING = "Pieces";
        
        private Rect previewRect;
        private Rect glassRect;
        private Rect statsRect;
        private int cellSize;
        
        private boolean isPreviewShown = true;
        private boolean isGridShown = true;
        
        private TouchEventHandler touchEventHandler;
        private TrackballEventHandler trackballEventHandler;
        
        private boolean destroyed = false;
        
        public TetrisView(Context context, AttributeSet attributeSet)
        {
                super(context, attributeSet);
         
                this.context = context;
        
                touchEventHandler = new TouchEventHandler();
                trackballEventHandler = new TrackballEventHandler();
                
                initColorToDrawable(context.getResources());

                setFocusable(true);
                
                setFocusableInTouchMode(true);
                                
                Log.d(this.getClass().getName(), "created: " + this);
        }
        
        public void setPreviewShown(boolean isPreviewShown)
        {
                this.isPreviewShown = isPreviewShown;                
                postInvalidate();
        }

        public void setGridShown(boolean isGridShown)
        {
                this.isGridShown = isGridShown;                
                postInvalidate();
        }

        public boolean isPreviewShown()
        {
                return isPreviewShown;
        }

        public boolean isGridShown()
        {
                return isGridShown;
        }

        private void initColorToDrawable(Resources resources)
        {
                colorToDrawable.put(Cell.Color.BLUE, resources.getDrawable(R.drawable.cell_blue));
                colorToDrawable.put(Cell.Color.CYAN, resources.getDrawable(R.drawable.cell_cyan));
                colorToDrawable.put(Cell.Color.GREEN, resources.getDrawable(R.drawable.cell_green));
                colorToDrawable.put(Cell.Color.ORANGE, resources.getDrawable(R.drawable.cell_orange));
                colorToDrawable.put(Cell.Color.PURPLE, resources.getDrawable(R.drawable.cell_magenta));
                colorToDrawable.put(Cell.Color.RED, resources.getDrawable(R.drawable.cell_red));
                colorToDrawable.put(Cell.Color.YELLOW, resources.getDrawable(R.drawable.cell_yellow));
        }
        
        @Override
        public void onDraw(Canvas canvas)
        {
                long start = System.currentTimeMillis();
                if(tetrisEngine == null)
                        return;
                
                drawField(canvas, glassRect);
                
                if(isGridShown)
                        drawGrid(canvas, glassRect);
                
                drawField(canvas, previewRect);
                
                drawField(canvas, statsRect);
                
                for(Cell c : tetrisEngine.getSea())
                {
                        drawCell(canvas, c);
                }
                
                for(Cell c : tetrisEngine.getPiece().getCells())
                {
                        drawCell(canvas, c);
                }

                if(isPreviewShown)
                {
                        for(Cell c : tetrisEngine.getNextPiece().getCells())
                        {
                                drawPreviewCell(canvas, c);
                        }
                }

                drawStats(canvas);
                Log.d(this.getClass().getName(), "delta: " + (System.currentTimeMillis() - start));
        }
        
        public void setTetrisEngine(TetrisEngine tetrisEngine)
        {
                this.tetrisEngine = tetrisEngine;
                tetrisEngine.addTetrisListener(this);
        }

        /**
         * Get cell size based on screen size. There are 2 borders, 2 paddings and tetris height cells
         * from top to bottom. There are 3 paddings, 4 borders and tetris width plus preview width
         * cells from left to right.
         * 
         * @return
         */
        private int getCellSize()
        {
                int width = getWidth() - FIELD_PADDING * 3 + FIELD_BORDER * 4;
                int height = getHeight() - (FIELD_PADDING + FIELD_BORDER) * 2;
                int res = Math.min(height / tetrisEngine.getHeight(), width / (tetrisEngine.getWidth() + PREVIEW_WIDTH));
                return res;
        }
        
        private Rect getPreviewRect()
        {
                int left = getWidth() / 2 - (getGlassWidth() + getPreviewWidth() + FIELD_PADDING * 3 + FIELD_BORDER * 4) / 2 + FIELD_PADDING + FIELD_BORDER;
                int top = getHeight() / 2 - getGlassHeight() / 2;
                int right = left + getPreviewWidth();
                int bottom = top + getPreviewHeight();

                return new Rect(left, top, right, bottom);
        }
        
        private Rect getGlassRect()
        {
                int left = getPreviewRect().right + FIELD_BORDER * 2 + FIELD_PADDING;
                int top = getPreviewRect().top;
                int right = left + getGlassWidth();
                int bottom = top + getGlassHeight();

                return new Rect(left, top, right, bottom);
        }
        
        private Rect getStatsRect()
        {
                int left = getPreviewRect().left;
                int top = getPreviewRect().bottom + FIELD_BORDER * 2 + FIELD_PADDING;
                int right = previewRect.right;
                int bottom = glassRect.bottom;
                
                return new Rect(left, top, right, bottom);
        }
        
        /**
         * Get cell bounding rectangle
         * 
         * @param cell -- cell to bound
         * @param field -- grid field
         * @param offset -- arbitrary offset
         * @return
         */
        private Rect getCellRect(Cell cell, Rect field, Point offset)
        {
                int left = field.left + offset.x + cell.getX() * cellSize;
                int top = field.top + offset.y + cell.getY() * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;
                
                return new Rect(left, top, right, bottom);
        }

        private int getGlassHeight()
        {
                return tetrisEngine.getHeight() * cellSize;
        }
        
        private int getGlassWidth()
        {
                return tetrisEngine.getWidth() * cellSize;
        }

        private int getPreviewHeight()
        {
                return PREVIEW_HEIGHT * cellSize;
        }

        private int getPreviewWidth()
        {
                return PREVIEW_WIDTH * cellSize;
        }
        
        /**
         * Get offset for preview cells, we need to center piece in the preview field for aestetics
         * 
         * @return -- offset
         */
        private Point getPreviewCellOffset()
        {
                int minX = PREVIEW_WIDTH;
                int maxX = 0;
                int minY = PREVIEW_HEIGHT;
                int maxY = 0;
                for(Cell c : tetrisEngine.getNextPiece().getCells())
                {
                        if(c.getX() > maxX)
                                maxX = c.getX();
                        if(c.getX() < minX)
                                minX = c.getX();
                        if(c.getY() > maxY)
                                maxY = c.getY();
                        if(c.getY() < minY)
                                minY = c.getY();
                }
                int x = PREVIEW_WIDTH * cellSize / 2 - cellSize * (minX + maxX + 1) / 2;
                int y = PREVIEW_HEIGHT * cellSize / 2 - cellSize * (minY + maxY + 1) / 2;        
                
                return new Point(x, y);
        }

        private void drawCell(Canvas canvas, Cell cell)
        {
                Drawable drawable = colorToDrawable.get(cell.getColor());
                drawable.setBounds(getCellRect(cell, glassRect, new Point(0, 0)));
                drawable.draw(canvas);
        }
        
        private void drawPreviewCell(Canvas canvas, Cell cell)
        {
                Drawable drawable = colorToDrawable.get(cell.getColor());
                drawable.setBounds(getCellRect(cell, previewRect, getPreviewCellOffset()));
                drawable.draw(canvas);
        }
        
        private void drawField(Canvas canvas, Rect rect)
        {
                Rect outerGlass = new Rect(rect.left - FIELD_BORDER,  rect.top - FIELD_BORDER, rect.right + FIELD_BORDER, rect.bottom + FIELD_BORDER);

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                
                paint.setColor(context.getResources().getColor(R.color.field_frame_color));
                canvas.drawRect(outerGlass, paint);
                
                paint.setColor(context.getResources().getColor(R.color.field_interior_color));
                canvas.drawRect(rect, paint);
        }
        
        private void drawStats(Canvas canvas)
        {
                List<String> stats = new ArrayList<String>();
                
                stats.add(SCORE_HEADING);
                stats.add("" + tetrisEngine.getScore());
                stats.add(LINES_HEADING);
                stats.add("" + tetrisEngine.getLineCount());
                stats.add(SPEED_HEADING);
                stats.add("" + tetrisEngine.getSpeed());
                stats.add(PIECES_HEADING);
                stats.add("" + tetrisEngine.getPieceCount());

                Paint paint = new Paint();
                paint.setColor(context.getResources().getColor(R.color.stats_text_color));                
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setAntiAlias(true);
                paint.setSubpixelText(true);
                
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();                
                float fontHeight = fontMetrics.bottom - fontMetrics.top;
                String[] statsAsArray = stats.toArray(new String[]{});
                
                float textOriginX = statsRect.left + statsRect.width() / 2;
                float lineHeight = statsRect.height() / statsAsArray.length;
                
                for(int i = 0; i < statsAsArray.length; i++)
                {
                        float textOriginY = statsRect.top + i * lineHeight + lineHeight / 2 + fontHeight / 2;
                        canvas.drawText(statsAsArray[i], textOriginX, textOriginY, paint);
                }
        }
        
        private void drawGrid(Canvas canvas, Rect rect)
        {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);                
                paint.setColor(context.getResources().getColor(R.color.grid_color));
                
                for(int i = 0; i < tetrisEngine.getWidth(); i++)
                {
                        canvas.drawRect(new Rect(glassRect.left + i * cellSize, glassRect.top, glassRect.left + i * cellSize + 1, glassRect.bottom), paint);
                        canvas.drawRect(new Rect(glassRect.left + cellSize * (i + 1)  - 1, glassRect.top, glassRect.left + cellSize * (i + 1) - 1 + 1, glassRect.bottom), paint);
                }
                
                for(int i = 0; i < tetrisEngine.getHeight(); i++)
                {
                        canvas.drawRect(new Rect(glassRect.left, glassRect.top + cellSize * i, glassRect.right, glassRect.top + cellSize * i + 1), paint);
                        canvas.drawRect(new Rect(glassRect.left, glassRect.top + cellSize * (i + 1) - 1, glassRect.right, glassRect.top + cellSize * (i + 1) - 1 + 1), paint);
                }
        }

        @Override
        public void stateChanged(TetrisEvent e)
        {
                postInvalidate();
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event)
        {
                switch(keyCode)
                {
                case KeyEvent.KEYCODE_K:                        
                case KeyEvent.KEYCODE_DPAD_DOWN:
                        tetrisEngine.rotatePieceClockwise();
                        break;
                case KeyEvent.KEYCODE_S:                        
                case KeyEvent.KEYCODE_DPAD_UP:
                        tetrisEngine.rotatePieceCounterclockwise();
                        break;
                case KeyEvent.KEYCODE_A:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                        tetrisEngine.movePieceLeft();
                        break;
                case KeyEvent.KEYCODE_L:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                        tetrisEngine.movePieceRight();
                        break;
                case KeyEvent.KEYCODE_SPACE:        
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_Q:        
                case KeyEvent.KEYCODE_P:        
                        tetrisEngine.dropPiece();
                        break;
                }
                return false;
        }
        
        @Override
        public void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
        {
                super.onSizeChanged(width, height, oldWidth, oldHeight);
                initSizes();
        }
        
        private void initSizes()
        {
                if(tetrisEngine == null)
                        return;
                
                cellSize = getCellSize();
                previewRect = getPreviewRect();
                glassRect = getGlassRect();
                statsRect = getStatsRect();
                
                postInvalidate();
        }
        
         public synchronized void destroy()
         {
                 if(!destroyed)
                 {
                         destroyed = true;
                         
                         if(tetrisEngine != null)
                                 tetrisEngine.removeTetrisListener(this);
                         
                         Log.d(this.getClass().getName(), "deleted: " + this);
                 }
         }
         
         @Override
         public boolean onTrackballEvent(MotionEvent motionEvent)
         {
                 trackballEventHandler.onTrackballEvent(motionEvent);
                 
                 return true;
         }
         
         @Override
         public boolean onTouchEvent(MotionEvent motionEvent)
         {
                 touchEventHandler.onTouchEvent(motionEvent);
                 
                 return true;
         }
         
         private class TouchEventHandler
         {
                 private static final long AUTOREPEAT_DELAY = 400;
                 private static final long AUTOREPEAT_RATE = 50;
                 private boolean keyPressed;
                 private long keyPressedTimestamp;
                 private long keySentTimestamp;
                 
                 public void onTouchEvent(MotionEvent motionEvent)
                 {
                         switch(motionEvent.getAction())
                         {
                         case MotionEvent.ACTION_DOWN:
                                 keyPressed = true;
                                 keyPressedTimestamp = System.currentTimeMillis();
                                 keyHandler(motionEvent);
                                 break;
                                 
                         case MotionEvent.ACTION_UP:
                                 keyPressed = false;
                                 break;
                                 
                         case MotionEvent.ACTION_MOVE:
                                 if(keyPressed && System.currentTimeMillis() - keyPressedTimestamp > AUTOREPEAT_DELAY &&
                                                 System.currentTimeMillis() - keySentTimestamp > AUTOREPEAT_RATE)
                                 {
                                         keySentTimestamp = System.currentTimeMillis();
                                         keyHandler(motionEvent);
                                 }
                                 break;
                         default:
                                 break;
                         }
                 }
                 
                 private void keyHandler(MotionEvent motionEvent)
                 {
                         Rect top = new Rect(0, 0, getWidth(), getHeight() / 2);
                         Rect left = new Rect(0, getHeight() / 2, getWidth() / 3, getHeight());
                         Rect middle = new Rect(getWidth() / 3, getHeight() / 2, 2 * getWidth() / 3, getHeight());
                         Rect right = new Rect(2 * getWidth() / 3, getHeight() / 2, getWidth(), getHeight());

                         if(top.contains((int) motionEvent.getX(), (int) motionEvent.getY()))
                                         tetrisEngine.dropPiece();
                         
                         if(left.contains((int) motionEvent.getX(), (int) motionEvent.getY()))
                                         tetrisEngine.movePieceLeft();
                         
                         if(middle.contains((int) motionEvent.getX(), (int) motionEvent.getY()))
                                         tetrisEngine.rotatePieceCounterclockwise();
                         
                         if(right.contains((int) motionEvent.getX(), (int) motionEvent.getY()))
                                         tetrisEngine.movePieceRight();
                 }
        }
         
        private class TrackballEventHandler
        {
                private static final float SENSITIVITY_X = 0.5f;
                private static final float SENSITIVITY_Y = 1.0f;
                
                private static final long QUEUE_OBSOLESCENSE_TIME = 300;
                private LinkedList<MotionEvent> eventQueue = new LinkedList<MotionEvent>();
                
                public void onTrackballEvent(MotionEvent motionEvent)
                {
                        // Log.d(getClass().getName(), "onTrackballEvent(), event: " + motionEvent);

                        // On click flush event queue and drop piece
                        if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                        {
                                tetrisEngine.dropPiece();
                                eventQueue.clear();
                                return;
                        }
                        
                        // Only motion events are processed down
                        if(motionEvent.getAction() != MotionEvent.ACTION_MOVE)
                                return;
                        
                        // If queue is too old, it was an incomplete action, flush and do nothing
                        if(eventQueue.size() > 0 && motionEvent.getEventTime() - eventQueue.getLast().getEventTime() > QUEUE_OBSOLESCENSE_TIME)
                        {
                                eventQueue.clear();
                                return;
                        }
                        
                        // Put event into queue
                        eventQueue.add(MotionEvent.obtain(motionEvent));

                        // Integrate X and Y over the queue
                        float x = 0;
                        float y = 0;
                        for(MotionEvent e : eventQueue)
                        {
                                x += e.getX();
                                y += e.getY();
                        }

                        // Was that a more of a horizontal or vertical move?
                        if(Math.abs(x) < SENSITIVITY_X && Math.abs(y) < SENSITIVITY_Y)
                                return;

                        // Move or rotate the piece
                        // Log.d(getClass().getName(), "x: " + x + " y: " + y);
                        if(Math.abs(x) > Math.abs(y))
                        {
                                if(x > 0)
                                        tetrisEngine.movePieceRight();
                                if(x < 0)
                                        tetrisEngine.movePieceLeft();
                        }
                        else
                        {
                                if(motionEvent.getY() > 0)
                                        tetrisEngine.rotatePieceClockwise();
                                if(motionEvent.getY() < 0)
                                        tetrisEngine.rotatePieceCounterclockwise();
                        }
                        
                        // Flush the queue
                        eventQueue.clear();
                }
        }
}