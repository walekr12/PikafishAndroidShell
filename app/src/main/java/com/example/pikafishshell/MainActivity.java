package com.example.pikafishshell;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements XiangqiBoardView.Listener {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private XiangqiGame game;
    private XiangqiBoardView boardView;
    private TextView statusView;
    private PikafishEngine engine;
    private boolean engineThinking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        game = new XiangqiGame();
        statusView = new TextView(this);
        statusView.setGravity(Gravity.CENTER);
        statusView.setTextSize(16);
        statusView.setPadding(16, 18, 16, 18);
        statusView.setText("Initializing Pikafish...");

        boardView = new XiangqiBoardView(this);
        boardView.setGame(game);
        boardView.setListener(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF6EFE2);
        root.addView(statusView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(boardView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f));
        setContentView(root);

        startEngine();
    }

    private void startEngine() {
        new Thread(() -> {
            try {
                engine = new PikafishEngine(this);
                engine.start();
                postStatus("Ready. You play red.");
            } catch (Exception e) {
                postStatus("Engine failed: " + e.getMessage());
            }
        }, "pikafish-start").start();
    }

    @Override
    public void onMoveChosen(String move) {
        if (engineThinking || engine == null || !game.isRedTurn()) {
            return;
        }

        char piece = game.pieceAtMoveSource(move);
        if (!Character.isUpperCase(piece)) {
            postStatus("Select a red piece.");
            return;
        }

        game.applyMove(move);
        boardView.invalidate();
        statusView.setText("You: " + move + ". Pikafish thinking...");
        requestEngineMove();
    }

    private void requestEngineMove() {
        engineThinking = true;
        new Thread(() -> {
            try {
                String bestMove = engine.bestMove(game.moves(), 1200);
                mainHandler.post(() -> {
                    engineThinking = false;
                    if (bestMove == null || bestMove.length() < 4 || "(none)".equals(bestMove)) {
                        statusView.setText("No engine move returned.");
                        return;
                    }
                    game.applyMove(bestMove.substring(0, 4));
                    boardView.invalidate();
                    statusView.setText("Pikafish: " + bestMove.substring(0, 4));
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    engineThinking = false;
                    statusView.setText("Engine error: " + e.getMessage());
                });
            }
        }, "pikafish-search").start();
    }

    private void postStatus(String message) {
        mainHandler.post(() -> statusView.setText(message));
    }

    @Override
    protected void onDestroy() {
        if (engine != null) {
            engine.close();
        }
        super.onDestroy();
    }
}
