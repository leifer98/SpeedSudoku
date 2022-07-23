package leifer.example.speedsoduko.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GameRecord implements Serializable {

    private int timestamp;
    private String user1Name, user2Name;
    private String user1Key, user2Key;
    private int user1Score, user2Score;
    private int user1overallScore, user2overallScore;
    private int user1MMR, user2MMR;
    private int mmrChange;
    private String whoWon, reason;
    private String key;
    private String gameKey;
    private List<List<Integer>> board = new ArrayList<List<Integer>>();
    private List<List<Integer>> solvedBoard = new ArrayList<List<Integer>>();

    public GameRecord() {    }

    public GameRecord(Game game, String whoWon, String reason) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        this.timestamp = (int) ts.getTime();
        this.user1Name = game.getGameUser1().getUserName();
        this.user2Name = game.getGameUser2().getUserName();
        this.user1Key = game.getGameUser1().getUserKey();
        this.user2Key = game.getGameUser2().getUserKey();
        this.user1Score = game.getUser1Score();
        this.user2Score = game.getUser2Score();
        this.user1overallScore = game.getOverallUser1Score();
        this.user2overallScore = game.getOverallUser2Score();
        this.user1MMR = game.getGameUser1().getUserMMR();
        this.user2MMR = game.getGameUser2().getUserMMR();
        this.mmrChange = 9;
        this.whoWon = whoWon;
        this.reason = reason;
        this.key = String.valueOf(this.timestamp);
        this.gameKey = game.getGameKey();
        this.board = game.getBoard();
        this.solvedBoard = game.getSolvedBoard();
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
    }

    public String getUser1Key() {
        return user1Key;
    }

    public void setUser1Key(String user1Key) {
        this.user1Key = user1Key;
    }

    public String getUser2Key() {
        return user2Key;
    }

    public void setUser2Key(String user2Key) {
        this.user2Key = user2Key;
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

    public int getMmrChange() {
        return mmrChange;
    }

    public void setMmrChange(int mmrChange) {
        this.mmrChange = mmrChange;
    }

    public String getWhoWon() {
        return whoWon;
    }

    public void setWhoWon(String whoWon) {
        this.whoWon = whoWon;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public int getUser1MMR() {
        return user1MMR;
    }

    public void setUser1MMR(int user1MMR) {
        this.user1MMR = user1MMR;
    }

    public int getUser2MMR() {
        return user2MMR;
    }

    public void setUser2MMR(int user2MMR) {
        this.user2MMR = user2MMR;
    }

    public String getGameKey() {
        return gameKey;
    }

    public void setGameKey(String gameKey) {
        this.gameKey = gameKey;
    }

    public int getUser1overallScore() {
        return user1overallScore;
    }

    public void setUser1overallScore(int user1overallScore) {
        this.user1overallScore = user1overallScore;
    }

    public int getUser2overallScore() {
        return user2overallScore;
    }

    public void setUser2overallScore(int user2overallScore) {
        this.user2overallScore = user2overallScore;
    }

    public List<List<Integer>> convertToList(int [][] board) {
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

    public int [][] convertToMatrices(List<List<Integer>> board) {
        int [][] list = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                list[i][j] = board.get(i).get(j);
            }
        }

        return list;
    }
}
