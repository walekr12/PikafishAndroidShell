package com.example.pikafishshell;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class PikafishEngine {
    private final Context context;
    private Process process;
    private BufferedWriter input;
    private BufferedReader output;
    private Thread stderrThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    PikafishEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    void start() throws IOException {
        File net = copyAsset("engine/pikafish.nnue", "pikafish.nnue");
        ApplicationInfo info = context.getApplicationInfo();
        File engineFile = new File(info.nativeLibraryDir, "libpikafish.so");
        if (!engineFile.exists()) {
            throw new IOException("Missing " + engineFile.getAbsolutePath());
        }
        engineFile.setExecutable(true, false);

        ProcessBuilder builder = new ProcessBuilder(engineFile.getAbsolutePath());
        builder.directory(net.getParentFile());
        process = builder.start();
        running.set(true);
        input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        output = new BufferedReader(new InputStreamReader(process.getInputStream()));
        stderrThread = new Thread(() -> drain(process.getErrorStream()), "pikafish-stderr");
        stderrThread.start();

        send("uci");
        waitFor("uciok", 8000);
        send("setoption name EvalFile value " + net.getAbsolutePath());
        send("setoption name Threads value 2");
        send("isready");
        waitFor("readyok", 8000);
        send("ucinewgame");
    }

    synchronized String bestMove(List<String> moves, int movetimeMs) throws IOException {
        ensureRunning();
        StringBuilder position = new StringBuilder("position startpos");
        if (!moves.isEmpty()) {
            position.append(" moves");
            for (String move : moves) {
                position.append(' ').append(move);
            }
        }
        send(position.toString());
        send("go movetime " + movetimeMs);

        String line;
        while ((line = output.readLine()) != null) {
            if (line.startsWith("bestmove ")) {
                String[] parts = line.split("\\s+");
                return parts.length >= 2 ? parts[1] : null;
            }
        }
        throw new IOException("Engine exited before bestmove");
    }

    synchronized void close() {
        running.set(false);
        try {
            if (input != null) {
                send("quit");
            }
        } catch (IOException ignored) {
        }
        if (process != null) {
            process.destroy();
        }
    }

    private void send(String command) throws IOException {
        input.write(command);
        input.newLine();
        input.flush();
    }

    private void waitFor(String token, long timeoutMs) throws IOException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        String line;
        while (System.currentTimeMillis() < deadline && (line = output.readLine()) != null) {
            if (line.equals(token)) {
                return;
            }
        }
        throw new IOException("Timed out waiting for " + token);
    }

    private void ensureRunning() throws IOException {
        if (!running.get() || process == null || input == null || output == null) {
            throw new IOException("Engine is not running");
        }
    }

    private File copyAsset(String assetName, String outName) throws IOException {
        File dir = new File(context.getFilesDir(), "engine");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create " + dir.getAbsolutePath());
        }
        File out = new File(dir, outName);
        try (InputStream in = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return out;
    }

    private void drain(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            while (running.get() && reader.readLine() != null) {
                // Keep stderr from blocking the engine process.
            }
        } catch (IOException ignored) {
        }
    }
}
