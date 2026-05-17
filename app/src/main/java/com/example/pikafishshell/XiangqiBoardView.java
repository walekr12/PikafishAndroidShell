package com.example.pikafishshell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

final class XiangqiBoardView extends View {
    interface Listener {
        void onMoveChosen(String move);
    }

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint piecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private XiangqiGame game;
    private Listener listener;
    private float left;
    private float top;
    private float cell;
    private int selectedRow = -1;
    private int selectedCol = -1;

    XiangqiBoardView(Context context) {
        super(context);
        linePaint.setColor(0xFF6F4A24);
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);
        piecePaint.setColor(0xFFFDF7EC);
        piecePaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        selectedPaint.setColor(0x553F7AE0);
        selectedPaint.setStyle(Paint.Style.FILL);
    }

    void setGame(XiangqiGame game) {
        this.game = game;
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        cell = Math.min(width / 10.2f, height / 11.4f);
        left = (width - cell * 8) / 2f;
        top = (height - cell * 9) / 2f;

        drawBoard(canvas);
        drawPieces(canvas);
    }

    private void drawBoard(Canvas canvas) {
        for (int row = 0; row < 10; row++) {
            float y = top + row * cell;
            canvas.drawLine(left, y, left + 8 * cell, y, linePaint);
        }
        for (int col = 0; col < 9; col++) {
            float x = left + col * cell;
            canvas.drawLine(x, top, x, top + 4 * cell, linePaint);
            canvas.drawLine(x, top + 5 * cell, x, top + 9 * cell, linePaint);
        }
        drawPalace(canvas, 0);
        drawPalace(canvas, 7);
    }

    private void drawPalace(Canvas canvas, int startRow) {
        float x3 = left + 3 * cell;
        float x5 = left + 5 * cell;
        float y0 = top + startRow * cell;
        float y2 = top + (startRow + 2) * cell;
        canvas.drawLine(x3, y0, x5, y2, linePaint);
        canvas.drawLine(x5, y0, x3, y2, linePaint);
    }

    private void drawPieces(Canvas canvas) {
        if (game == null) {
            return;
        }
        float radius = cell * 0.38f;
        textPaint.setTextSize(cell * 0.42f);
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                char piece = game.pieceAt(row, col);
                if (piece == '.') {
                    continue;
                }
                float cx = left + col * cell;
                float cy = top + row * cell;
                if (row == selectedRow && col == selectedCol) {
                    canvas.drawCircle(cx, cy, radius * 1.18f, selectedPaint);
                }
                piecePaint.setColor(0xFFFFF8E8);
                canvas.drawCircle(cx, cy, radius, piecePaint);
                linePaint.setColor(Character.isUpperCase(piece) ? 0xFFB3261E : 0xFF202124);
                canvas.drawCircle(cx, cy, radius, linePaint);
                textPaint.setColor(Character.isUpperCase(piece) ? 0xFFB3261E : 0xFF202124);
                Paint.FontMetrics fm = textPaint.getFontMetrics();
                canvas.drawText(label(piece), cx, cy - (fm.ascent + fm.descent) / 2f, textPaint);
                linePaint.setColor(0xFF6F4A24);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || game == null) {
            return true;
        }
        int col = Math.round((event.getX() - left) / cell);
        int row = Math.round((event.getY() - top) / cell);
        if (row < 0 || row >= 10 || col < 0 || col >= 9) {
            return true;
        }

        char piece = game.pieceAt(row, col);
        if (selectedRow < 0) {
            if (piece != '.') {
                selectedRow = row;
                selectedCol = col;
                invalidate();
            }
            return true;
        }

        String move = XiangqiGame.cellToSquare(selectedRow, selectedCol)
                + XiangqiGame.cellToSquare(row, col);
        selectedRow = -1;
        selectedCol = -1;
        invalidate();
        if (listener != null) {
            listener.onMoveChosen(move);
        }
        return true;
    }

    private static String label(char piece) {
        switch (piece) {
            case 'K':
                return "帥";
            case 'A':
                return "仕";
            case 'B':
                return "相";
            case 'N':
                return "馬";
            case 'R':
                return "車";
            case 'C':
                return "炮";
            case 'P':
                return "兵";
            case 'k':
                return "將";
            case 'a':
                return "士";
            case 'b':
                return "象";
            case 'n':
                return "馬";
            case 'r':
                return "車";
            case 'c':
                return "砲";
            case 'p':
                return "卒";
            default:
                return String.valueOf(piece);
        }
    }
}
