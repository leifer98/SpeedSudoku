package leifer.example.speedsoduko.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Game implements Serializable {
    private String gameKey;
    private User gameUser1, gameUser2;
    private String turn;
    private int user1Time, user2Time;
    private int user1Score, user2Score;
    private int change;
    private String changeOfPlayer;
    private List<List<Integer>> board = new ArrayList<List<Integer>>();
    private List<List<Integer>> solvedBoard = new ArrayList<List<Integer>>();
    private int overallUser1Score, overallUser2Score;
    private int user2signal;
    private int user1signal;
    private int user1lastUpdate;
    private int user2lastUpdate;
    private int user1discLeft;
    private int user2discLeft;
    private boolean gameOver;

    public Game() {
    }

    public Game(User gameUser1,User gameUser2) {
        this.gameUser1 = gameUser1;
        this.gameUser2 = gameUser2;
        this.gameKey = gameUser1.getUserName() + "|VS|" + gameUser2.getUserName();
        this.turn = "gameUser1";
        this.changeOfPlayer = "gameUser1";
        this.user1Time = 0;
        this.user2Time = 0;
        this.user1Score = 0;
        this.user2Score = 0;
        this.change = 0;
        this.overallUser1Score = 0;
        this.overallUser2Score = 0;
        this.board = null;
        this.solvedBoard = null;
        this.user1signal = 1;
        this.user2signal = 1;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.user1lastUpdate = (int) timestamp.getTime();
        this.user2lastUpdate = (int) timestamp.getTime();
        this.user1discLeft = 3;
        this.user2discLeft = 3;
        this.gameOver = false;
    }


    public User getGameUser1() {
        return gameUser1;
    }

    public void setGameUser1(User gameUser1) {
        this.gameUser1 = gameUser1;
    }

    public User getGameUser2() {
        return gameUser2;
    }

    public void setGameUser2(User gameUser2) {
        this.gameUser2 = gameUser2;
    }

    public String getGameKey() {
        return gameKey;
    }

    public void setGameKey(String gameKey) {
        this.gameKey = gameKey;
    }

    public int getUser1Time() {
        return user1Time;
    }

    public void setUser1Time(int user1Time) {
        this.user1Time = user1Time;
    }

    public int getUser2Time() {
        return user2Time;
    }

    public void setUser2Time(int user2Time) {
        this.user2Time = user2Time;
    }

    public int getUser1Score() {
        return user1Score;
    }

    public void setUser1Score(int user1Score) {
        this.user1Score = user1Score;
    }

    public int getUser2Score() {
        return user2Score;
    }

    public void setUser2Score(int user2Score) {
        this.user2Score = user2Score;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public List<List<Integer>> getBoard() {
        return board;
    }

    public void setBoard(List<List<Integer>> board) {
        this.board = board;
    }

    public List<List<Integer>> getSolvedBoard() {
        return solvedBoard;
    }

    public void setSolvedBoard(List<List<Integer>> solvedBoard) {
        this.solvedBoard = solvedBoard;
    }

    public int getOverallUser1Score() {
        return overallUser1Score;
    }

    public void setOverallUser1Score(int overallUser1Score) {
        this.overallUser1Score = overallUser1Score;
    }

    public int getOverallUser2Score() {
        return overallUser2Score;
    }

    public void setOverallUser2Score(int overallUser2Score) {
        this.overallUser2Score = overallUser2Score;
    }

    public int getUser2signal() {
        return user2signal;
    }

    public void setUser2signal(int user2signal) {
        this.user2signal = user2signal;
    }

    public int getUser1signal() {
        return user1signal;
    }

    public void setUser1signal(int user1signal) {
        this.user1signal = user1signal;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getChangeOfPlayer() {
        return changeOfPlayer;
    }

    public void setChangeOfPlayer(String changeOfPlayer) {
        this.changeOfPlayer = changeOfPlayer;
    }

    public int getUser1lastUpdate() {
        return user1lastUpdate;
    }

    public void setUser1lastUpdate(int user1lastUpdate) {
        this.user1lastUpdate = user1lastUpdate;
    }

    public int getUser2lastUpdate() {
        return user2lastUpdate;
    }

    public void setUser2lastUpdate(int user2lastUpdate) {
        this.user2lastUpdate = user2lastUpdate;
    }

    public int getUser1discLeft() {
        return user1discLeft;
    }

    public void setUser1discLeft(int user1discLeft) {
        this.user1discLeft = user1discLeft;
    }

    public int getUser2discLeft() {
        return user2discLeft;
    }

    public void setUser2discLeft(int user2discLeft) {
        this.user2discLeft = user2discLeft;
    }

    public List<List<Integer>> convertToList(int[][] board) {
        List<List<Integer>> list = new ArrayList<List<Integer>>(9);

        for (int i = 0; i < 9; i++) {
            List<Integer> temp = new ArrayList<Integer>(9);
            for (int j = 0; j < 9; j++) {
                temp.add(j,board[i][j]);
            }
            list.add(i,temp);
        }

        return list;
    }

    public int[][] convertToMatrices(List<List<Integer>> board) {
        int[][] list = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                list[i][j] = board.get(i).get(j);
            }
        }

        return list;
    }

    public int getChange() {
        return change;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public boolean exists() {
        if (this.gameUser1 == null) return false;
        if (this.gameUser2 == null) return false;
        if (this.gameKey.isEmpty()) return false;
        if (this.turn.isEmpty()) return false;
        if (this.changeOfPlayer == null) return false;
        if (this.user1Time == 0 && this.user2Time == 0 && this.user1Score == 0 && this.user2Score == 0 && this.change == 0 &&
                this.user1signal == 0 && this.user2signal == 0 && this.user1lastUpdate == 0 && this.user2lastUpdate == 0)
            return false;

        return true;
    }

}

