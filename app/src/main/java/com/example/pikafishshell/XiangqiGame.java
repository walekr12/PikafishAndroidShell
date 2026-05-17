package com.example.pikafishshell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class XiangqiGame {
    private static final String START_FEN =
            "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR";

    private final char[][] board = new char[10][9];
    private final ArrayList<String> moves = new ArrayList<>();
    private boolean redTurn = true;

    XiangqiGame() {
        reset();
    }

    void reset() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 9; col++) {
                board[row][col] = '.';
            }
        }
        String[] rows = START_FEN.split("/");
        for (int row = 0; row < rows.length; row++) {
            int col = 0;
            for (int i = 0; i < rows[row].length(); i++) {
                char ch = rows[row].charAt(i);
                if (Character.isDigit(ch)) {
                    col += ch - '0';
                } else if (col < 9) {
                    board[row][col++] = ch;
                }
            }
        }
        moves.clear();
        redTurn = true;
    }

    char pieceAt(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 9) {
            return '.';
        }
        return board[row][col];
    }

    char pieceAtMoveSource(String move) {
        int[] from = squareToCell(move.substring(0, 2));
        return pieceAt(from[0], from[1]);
    }

    void applyMove(String move) {
        if (move == null || move.length() < 4) {
            return;
        }
        int[] from = squareToCell(move.substring(0, 2));
        int[] to = squareToCell(move.substring(2, 4));
        char piece = pieceAt(from[0], from[1]);
        if (piece == '.') {
            return;
        }
        board[to[0]][to[1]] = piece;
        board[from[0]][from[1]] = '.';
        moves.add(move);
        redTurn = !redTurn;
    }

    boolean isRedTurn() {
        return redTurn;
    }

    List<String> moves() {
        return Collections.unmodifiableList(moves);
    }

    static String cellToSquare(int row, int col) {
        int rank = 9 - row;
        return String.valueOf((char) ('a' + col)) + rank;
    }

    private static int[] squareToCell(String square) {
        int col = square.charAt(0) - 'a';
        int rank = square.charAt(1) - '0';
        return new int[] {9 - rank, col};
    }
}
