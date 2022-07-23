package leifer.example.speedsoduko;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import leifer.example.speedsoduko.dao.DAOGame;
//import leifer.example.speedsoduko.dao.DAOGameRequest;
import leifer.example.speedsoduko.dao.DAOUser;
import leifer.example.speedsoduko.dao.DaoGameBackend;
import leifer.example.speedsoduko.objects.Friend;
import leifer.example.speedsoduko.objects.Game;
import leifer.example.speedsoduko.objects.GameRecord;
import leifer.example.speedsoduko.objects.GameRequest;
import leifer.example.speedsoduko.objects.Queue;
import leifer.example.speedsoduko.objects.User;
import leifer.example.speedsoduko.interfaces.grPopUp;
public class OnlineGameActivity extends AppCompatActivity implements grPopUp {

    // game staff
    Button selectedButton;
    Button selectedSquare;
    Button boardButtonMat[][] = new Button[9][9];
    int boardUnsolved[][];
    int boardSolved[][];
    int secs = -1;
    boolean pause = true;
    int time_limit_setting = 30;
    int time_limit = time_limit_setting;
    boolean red = true;
    boolean combo = false;

    TextView playerTime = null;
    TextView playerScore = null;
    TextView rivalTime = null;
    TextView rivalScore = null;
    LinearLayout bar1;
    LinearLayout bar2;
    private GridLayout boardLayout;
    private RelativeLayout buttonLayout;

    Handler handler = new Handler();
    private DrawerLayout drawerLayout;
    private int advantageWin = 1500;
    //    private int advantageWin = 200;
    private boolean deleteSquares = true;

    //backend staff
    private User gameUser1, gameUser2, currentUser, rivalUser;
    private String gameUser1Key, gameUser2Key;
    private Game gameObject;
    private DAOUser daoUser;
    private GameRequest gameRequest, grPopup;

    private boolean isPlayer1 = true;
    private int pause_secs = 30;
    private ImageView playersignal, rivalsignal;
    private DatabaseReference ref;
    private GameRecord gameRecord;
    private boolean lookingForGame = false;
    private int signal = 1;
    private TextView missingPlayerMsg;
    private boolean makingCall = false;
    private boolean internetReturning = false;
    private boolean userIdle = false;
    private boolean rivalIdle = false;
    private boolean rivalAbandoned = false;
    private boolean background_loop_running = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // game staff
        bar1 = (LinearLayout) findViewById(R.id.player1bar);
        bar2 = (LinearLayout) findViewById(R.id.player2bar);
        bar2.setAlpha((float) 0.3);
        missingPlayerMsg = findViewById(R.id.missing_rival_msg);
        findViewById(R.id.btn_setting).setVisibility(View.INVISIBLE);

        boardLayout = (GridLayout) findViewById(R.id.board);
        buttonLayout = (RelativeLayout) findViewById(R.id.button_bar_layout);
        boardLayout.setBackgroundResource(R.color.blue);
        buttonLayout.setBackgroundResource(R.color.blue);

        // backend staff
        daoUser = new DAOUser();
        if (getIntent().getExtras() != null) {
            currentUser = (User) getIntent().getExtras().getSerializable("CurrentUser");


            setupGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        userIdle = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        userIdle = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        Toast.makeText(this,"deleted callbacks _888",Toast.LENGTH_SHORT).show();
    }

    private void setupGame() {
        DaoGameBackend.gRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                boolean check = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getValue(Game.class).getGameUser1().equals(currentUser) ||
                            data.getValue(Game.class).getGameUser2().equals(currentUser)) {
                        gameObject = data.getValue(Game.class);
                        ref = DaoGameBackend.gRef.child(gameObject.getGameKey());
                        ref.keepSynced(true);
                        boardUnsolved = gameObject.convertToMatrices(gameObject.getBoard());
                        boardSolved = gameObject.convertToMatrices(gameObject.getSolvedBoard());
                        gameUser1 = gameObject.getGameUser1();
                        gameUser2 = gameObject.getGameUser2();
                        createBoardList();
                        check = true;


                    }
                }
                if (!check) {
                    handler.postDelayed(OnlineGameActivity.this::setupGame,500);
                    Toast.makeText(OnlineGameActivity.this,"waiting here _112",Toast.LENGTH_SHORT).show();
                } else {
                    continueSetupGame();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void continueSetupGame() {
        if (gameObject.getGameUser2().equals(currentUser)) {
            isPlayer1 = false;

            rivalUser = gameUser1;
            playersignal = findViewById(R.id.player2signal);
            rivalsignal = findViewById(R.id.player1signal);
            playerTime = findViewById(R.id.player2time);
            playerScore = findViewById(R.id.player2score);
            rivalTime = findViewById(R.id.player1time);
            rivalScore = findViewById(R.id.player1score);
            String newText = String.format("%d:%02d",
                    ((int) (time_limit) / 60),
                    ((int) (time_limit) % 60));
            playerTime.setText(newText);


        } else {
            rivalUser = gameUser2;
            playersignal = findViewById(R.id.player1signal);
            rivalsignal = findViewById(R.id.player2signal);
            playerTime = findViewById(R.id.player1time);
            playerScore = findViewById(R.id.player1score);
            rivalTime = findViewById(R.id.player2time);
            rivalScore = findViewById(R.id.player2score);

            gameObject.setUser1Score(0);
            gameObject.setUser1Time((time_limit));
            gameObject.setUser2Score(0);
            gameObject.setUser2Time((time_limit));


            DaoGameBackend.updateGame(gameObject);

        }


        TextView txtLabelUser1 = findViewById(R.id.player1name);
        txtLabelUser1.setText(gameUser1.getUserName() + "  (" + String.valueOf(gameUser1.getUserMMR()) + ")");
        TextView txtLabelUser2 = findViewById(R.id.player2name);
        txtLabelUser2.setText(gameUser2.getUserName() + "  (" + String.valueOf(gameUser2.getUserMMR()) + ")");

        if (isPlayer1) handler.postDelayed(this::gameLoop,1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pause = !isPlayer1;
                runTimer();
                DaoGameBackend.removeGarbage(currentUser);

                if (!isPlayer1) gameLoop();
            }
        },3000);
    }

    private void gameLoop() {
//        System.out.println("game loop call");
        int oldLevel = signal;
        signal = getSignalLevel();
        updateSignalIcon(signal,playersignal);
        if (signal == 0 || userIdle) {
            pause = true;
            bar1.setAlpha((float) 0.3);
            bar2.setAlpha((float) 0.3);


//            System.out.println("call denied by internet");

            int now = (int) new Timestamp(System.currentTimeMillis()).getTime();
            int timeToReturn = 30 - ((now - Math.max(gameObject.getUser1lastUpdate(),gameObject.getUser2lastUpdate())) / 1000);
            if (timeToReturn < 1) {
                findGameRecord(30);
                return;
            }
            String text = "auto abort in " + timeToReturn + " secs";
            missingPlayerMsg.setText(text);
            missingPlayerMsg.setVisibility(View.VISIBLE);
//            System.out.println(text);
            handler.postDelayed(OnlineGameActivity.this::gameLoop,500);
            return;
        } else if (oldLevel == 0) {
            // player internet returned hardcode
            internetReturning = true;
            handler.postDelayed(() -> {
                internetReturning = false;
            },30000);
        }
        makingCall = true;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (!makingCall) {
//                    System.out.println("call denied by overlapse");
                    return;
                }
                makingCall = false;

                if (snapshot == null) {
                    findGameRecord(30);
                    return;
                } else if (snapshot.getValue(Game.class) == null) {
                    findGameRecord(30);
                    return;
                } else if (!snapshot.getValue(Game.class).exists()) {
                    findGameRecord(30);
                    return;
                } else if (snapshot.getValue(Game.class).isGameOver()) {
                    findGameRecord(30);
                    return;
                }

                int now = (int) new Timestamp(System.currentTimeMillis()).getTime();

                gameObject = snapshot.getValue(Game.class);
//                System.out.println("internet call, updateDiff(1-2):" + ((gameObject.getUser1lastUpdate() - gameObject.getUser2lastUpdate()) / 1000)
//                        + ", diffFromNow: " + ((now - Math.min(gameObject.getUser1lastUpdate(),gameObject.getUser2lastUpdate())) / 1000));

                if (internetReturning) {
                    if ((Math.abs(gameObject.getUser1lastUpdate() - gameObject.getUser2lastUpdate()) / 1000) > 4 ||
                            ((now - Math.min(gameObject.getUser1lastUpdate(),gameObject.getUser2lastUpdate())) / 1000) > 4) {
                        pause = true;
                        bar1.setAlpha((float) 0.3);
                        bar2.setAlpha((float) 0.3);

                        int timeToReturn = 30 - ((now - Math.min(gameObject.getUser1lastUpdate(),gameObject.getUser2lastUpdate())) / 1000);
                        if (timeToReturn < 1) {
                            findGameRecord(30);
                            return;
                        }
                        String text = "auto abort in " + timeToReturn + " secs";
                        missingPlayerMsg.setText(text);
                        missingPlayerMsg.setVisibility(View.VISIBLE);

                        if (isPlayer1) {
                            gameObject.setUser1lastUpdate(now);
                            ref.updateChildren(Map.of(
                                    "user1lastUpdate",gameObject.getUser1lastUpdate()
                            ));
                        } else {
                            gameObject.setUser2lastUpdate(now);
                            ref.updateChildren(Map.of(
                                    "user2lastUpdate",gameObject.getUser2lastUpdate()
                            ));
                        }
                        return;
                    } else {
                        missingPlayerMsg.setVisibility(View.GONE);
                    }
                }

                if (isPlayer1) { // PLAYER 1

                    if (((now - gameObject.getUser2lastUpdate()) / 1000) > 7 && !internetReturning) {
                        rivalIdle = true;
                        gameObject.setUser2signal(0);
                        int timeToReturn = 30 - Math.abs((now - gameObject.getUser2lastUpdate()) / 1000);
                        if (timeToReturn < 1 || gameObject.getUser2discLeft() < 2) {
                            rivalAbandoned = true;
                            createGameRecord();
                            return;
                        }
                        String text = "rival auto abort in " + timeToReturn + " secs";
                        missingPlayerMsg.setText(text);
                        missingPlayerMsg.setVisibility(View.VISIBLE);
//                        System.out.println(text);
                    } else {
                        if (rivalIdle)  showDiscLeft();
                        rivalIdle = false;
                    }

                    gameObject.setUser1lastUpdate(now);
                    gameObject.setUser1signal(signal);
                    ref.updateChildren(Map.of(
                            "user1lastUpdate",gameObject.getUser1lastUpdate(),
                            "user1signal",gameObject.getUser1signal()
                    ));
                    updateSignalIcon(gameObject.getUser2signal(),rivalsignal);

                    if (gameObject.getChange() == -200 && gameObject.getTurn().equals("gameUser2") &&
                            gameObject.getChangeOfPlayer().equals("gameUser2")) {
                        addScoreAnimationForRival(-200,null);
                        gameObject.setChange(0);
                        ref.updateChildren(Map.of(
                                "change",gameObject.getChange()
                        ));
                    }

                    int progress = gameObject.getUser2Time();
                    String newText = String.format("%d:%02d",(progress / 60),(progress % 60));
                    rivalTime.setText(newText);
                    rivalScore.setText(String.valueOf(gameObject.getUser2Score()));

                    if (bar1.getAlpha() < 1 && gameObject.getTurn().equals("gameUser1")) {
                        pressDeleteButton(); //changing to  1
                        bar1.setAlpha(1);
                        bar2.setAlpha((float) 0.3);
                        boardLayout.setBackgroundResource(R.color.blue);
                        buttonLayout.setBackgroundResource(R.color.blue);
                        popupAnimation(bar1);
                        pause = false;
                    }
                    if (bar2.getAlpha() < 1 && gameObject.getTurn().equals("gameUser2")) {
                        pressDeleteButton(); //changing to 2
                        bar2.setAlpha(1);
                        bar1.setAlpha((float) 0.3);
                        boardLayout.setBackgroundResource(R.color.yellow);
                        buttonLayout.setBackgroundResource(R.color.yellow);
                        popupAnimation(bar2);
                    }
                } else { // PLAYER 2

                    if (((now - gameObject.getUser1lastUpdate()) / 1000) > 7 && !internetReturning) {
                        rivalIdle = true;
                        gameObject.setUser1signal(0);
                        int timeToReturn = 30 - Math.abs((now - gameObject.getUser1lastUpdate()) / 1000);
                        if (timeToReturn < 1 || gameObject.getUser1discLeft() < 2) {
                            rivalAbandoned = true;
                            createGameRecord();
                            return;
                        }
                        String text = "rival auto resign in " + timeToReturn + " secs";
                        missingPlayerMsg.setText(text);
                        missingPlayerMsg.setVisibility(View.VISIBLE);
//                        System.out.println(text);
                    } else {
                        if (rivalIdle)  showDiscLeft();
                        rivalIdle = false;
                    }
                    gameObject.setUser2lastUpdate(now);
                    gameObject.setUser2signal(signal);
                    ref.updateChildren(Map.of(
                            "user2lastUpdate",gameObject.getUser2lastUpdate(),
                            "user2signal",gameObject.getUser2signal()
                    ));
                    updateSignalIcon(gameObject.getUser1signal(),rivalsignal);

                    if (gameObject.getChange() == -200 && gameObject.getTurn().equals("gameUser1") &&
                            gameObject.getChangeOfPlayer().equals("gameUser1")) {
                        addScoreAnimationForRival(-200,null);
                        gameObject.setChange(0);
                        ref.updateChildren(Map.of(
                                "change",gameObject.getChange()
                        ));
                    }

                    int progress = gameObject.getUser1Time();
                    String newText = String.format("%d:%02d",
                            ((int) progress / 60),
                            ((int) progress % 60));
                    rivalTime.setText(newText);
                    rivalScore.setText(String.valueOf(gameObject.getUser1Score()));

                    if (bar2.getAlpha() < 1 && gameObject.getTurn().equals("gameUser2")) {
                        pressDeleteButton();
                        bar2.setAlpha(1);
                        bar1.setAlpha((float) 0.3);
                        boardLayout.setBackgroundResource(R.color.yellow);
                        buttonLayout.setBackgroundResource(R.color.yellow);
                        popupAnimation(bar2);
                        pause = false;
                    }
                    if (bar1.getAlpha() < 1 && gameObject.getTurn().equals("gameUser1")) {
                        pressDeleteButton();
                        bar1.setAlpha(1);
                        bar2.setAlpha((float) 0.3);
                        boardLayout.setBackgroundResource(R.color.blue);
                        buttonLayout.setBackgroundResource(R.color.blue);
                        popupAnimation(bar1);
                    }
                }

                if (!gameObject.getBoard().isEmpty()) {
                    boardUnsolved = gameObject.convertToMatrices(gameObject.getBoard());
                    boardSolved = gameObject.convertToMatrices(gameObject.getSolvedBoard());
                    if ((gameObject.getTurn().equals("gameUser2") && isPlayer1 && pause) ||
                            gameObject.getTurn().equals("gameUser1") && !isPlayer1 && pause) {
                        updateBoardList();
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(OnlineGameActivity.this,"listener cancelled",Toast.LENGTH_SHORT).show();
            }
        });


        if (!gameObject.isGameOver()) {
            handler.postDelayed(OnlineGameActivity.this::gameLoop,500);
        }
    }

    private void showDiscLeft() {
        missingPlayerMsg.setVisibility(View.VISIBLE);
        if (!isPlayer1) {
            gameObject.setUser1discLeft(gameObject.getUser1discLeft()-1);
            ref.updateChildren(Map.of(
                    "user1discLeft",gameObject.getUser1discLeft()
            ));
            missingPlayerMsg.setText("rival have " + gameObject.getUser1discLeft() + " disconnects left");
        } else {
            gameObject.setUser2discLeft(gameObject.getUser2discLeft()-1);
            ref.updateChildren(Map.of(
                    "user2discLeft",gameObject.getUser2discLeft()
            ));
            missingPlayerMsg.setText("rival have " + gameObject.getUser2discLeft() + " disconnects left");
        }
        handler.postDelayed(()->{
            missingPlayerMsg.setVisibility(View.GONE);
        },2000);
    }

    private void createBoardList() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int index = (i * 9) + j;
                boardButtonMat[i][j] = (Button) boardLayout.getChildAt(index);
                boardButtonMat[i][j].setTextColor(Color.parseColor("#262626"));
                if (boardUnsolved[i][j] != 0) {
                    boardButtonMat[i][j].setText(String.valueOf(boardUnsolved[i][j]));
                    boardButtonMat[i][j].setEnabled(false);
                } else {
                    boardButtonMat[i][j].setText("");

                    boardButtonMat[i][j].setEnabled(true);
                }
            }
        }
    }

    private void syncBoardList() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (boardButtonMat[i][j].getText().toString() != "") {
                    boardUnsolved[i][j] = Integer.parseInt(boardButtonMat[i][j].getText().toString());
                } else {
                    boardUnsolved[i][j] = 0;
                }
            }
        }
        gameObject.setBoard(gameObject.convertToList(boardUnsolved));
        if (isPlayer1) gameObject.setChangeOfPlayer("gameUser1");
        else gameObject.setChangeOfPlayer("gameUser2");
        ref.updateChildren(Map.of(
                "board",gameObject.getBoard(),
                "change",gameObject.getChange(),
                "changeOfPlayer",gameObject.getChangeOfPlayer()
        ));
    }

    private void updateBoardList() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {

                if (boardUnsolved[i][j] != 0) {

                    if (boardUnsolved[i][j] != boardSolved[i][j]) {

                        if (!boardButtonMat[i][j].getText().toString().equals(String.valueOf(boardUnsolved[i][j]))) {

                            addScoreAnimationForRival(gameObject.getChange(),boardButtonMat[i][j]);
                            boardButtonMat[i][j].setTextColor(Color.parseColor("#CC2936"));
                            boardButtonMat[i][j].setEnabled(true);
                        }

                    } else {
                        if (!boardButtonMat[i][j].getText().toString().equals(String.valueOf(boardSolved[i][j]))) {
                            addScoreAnimationForRival(gameObject.getChange(),boardButtonMat[i][j]);
                            boardButtonMat[i][j].setTextColor(Color.parseColor("#698F3F"));
                            boardButtonMat[i][j].setEnabled(false);

                        }
                    }
                    boardButtonMat[i][j].setText(String.valueOf(boardUnsolved[i][j]));
                } else {
                    boardButtonMat[i][j].setText("");
                    boardButtonMat[i][j].setTextColor(Color.parseColor("#262626"));
                }
            }
        }
    }

    private void resetBoardList() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardButtonMat[i][j].setTextColor(Color.parseColor("#262626"));
                boardButtonMat[i][j].setText("");
                boardButtonMat[i][j].setEnabled(true);
            }
        }
        boardButtonMat = new Button[9][9];

    }

    public void selectSquare(View view) {
        if (!pause && (secs < time_limit) && (secs > 0)) {
            Button myButton = (Button) view;
            if (selectedButton != null) {
                selectedSquare = myButton;
                enterValue();

                selectedButton.setTextColor(Color.parseColor("#FFFFFF"));
                selectedButton.setBackgroundResource(R.drawable.bg_rounded_black);
                selectedButton = null;
                selectedSquare = null;
            } else {
                if (selectedSquare != null) {
                    selectedSquare.setBackgroundResource(R.color.white);
                    if (selectedSquare.getTextColors().getDefaultColor() == Color.parseColor("#FFFFFD")) {
                        selectedSquare.setTextColor(Color.parseColor("#CC2936"));
                    } else {
                        selectedSquare.setTextColor(Color.parseColor("#262626"));
                    }

                }
                if (selectedSquare == myButton) {
                    selectedSquare = null;
                } else {
                    selectedSquare = myButton;
                    if (selectedSquare.getTextColors().getDefaultColor() == Color.parseColor("#CC2936")) {
                        selectedSquare.setTextColor(Color.parseColor("#FFFFFD"));
                    } else {
                        selectedSquare.setTextColor(Color.WHITE);
                    }
                    selectedSquare.setBackgroundResource(R.color.grey);
                }
            }
        }
    }

    public void selectButton(View view) {
        if (!pause && (secs < time_limit) && (secs > 0)) {
            Button mybutton = (Button) view;
            if (mybutton.getText().equals("")) {
                if (selectedSquare != null) {
                    if (selectedSquare.getTextColors().getDefaultColor() == Color.parseColor("#FFFFFD")) {
                        selectedSquare.setTextColor(Color.parseColor("#CC2936"));
                    } else {
                        selectedSquare.setTextColor(Color.parseColor("#262626"));
                    }
                    selectedSquare.setBackgroundResource(R.color.white);
                    selectedSquare = null;
                }
                if (selectedButton != null) {
                    selectedButton.setTextColor(Color.parseColor("#FFFFFF"));
                    selectedButton.setBackgroundResource(R.drawable.bg_rounded_black);
                    selectedButton = null;
                }
                popupAnimation(mybutton);
            } else {
                if (selectedSquare != null) {

                    selectedSquare.setTextColor(Color.parseColor("#262626"));
                    selectedSquare.setBackgroundResource(R.color.white);
                    selectedButton = (Button) view;
                    enterValue();
                    popupAnimation(view);

                    selectedSquare = null;
                    selectedButton = null;
                } else {
                    if (selectedButton != null) {
                        selectedButton.setTextColor(Color.parseColor("#FFFFFF"));
                        selectedButton.setBackgroundResource(R.drawable.bg_rounded_black);
                    }
                    selectedButton = mybutton;
                    selectedButton.setTextColor(Color.parseColor("#262626"));
                    selectedButton.setBackgroundResource(R.drawable.bg_rounded_white);
                    popupAnimation(selectedButton);
                }
            }
        }
    }

    public void enterValue() {
        selectedSquare.setText(selectedButton.getText());
        if (red) {
            findRedSquares();
            red = false;
        }
        if (findButtonReturnSolvedValue() == Integer.parseInt((String) selectedButton.getText())) {
//            Toast.makeText(this,"did it right!",Toast.LENGTH_SHORT).show();
            selectedSquare.setTextColor(Color.parseColor("#698F3F"));
            selectedSquare.setEnabled(false);
            if (time_limit > time_limit_setting) {
                int change = Math.max(((time_limit - secs) / 2 * 5) + 20,5);
                gameObject.setChange(change);
                addScoreAnimation(change);
            } else {
                int change = Math.max(((time_limit_setting - secs) / 2 * 5) + 20,5);
                gameObject.setChange(change);
                addScoreAnimation(change);
            }

        } else {
            selectedSquare.setTextColor(Color.parseColor("#CC2936"));
            int change = Math.min(secs / 2 * -5,-5);
            gameObject.setChange(change);
            addScoreAnimation(change);
            red = true;
            pause = true;
        }
        syncBoardList();
    }

    public void addScoreAnimation(int change) {

        TextView scoreRef = playerScore;
        int[] location = new int[2];

        RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.myLayout);
        TextView textView = new TextView(this);

        if (change > 0) {
            textView.setText("+" + String.valueOf(change));
            textView.setTextColor(Color.parseColor("#698F3F"));
            time_limit = secs + 6;
            combo = true;
        } else {
//            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            textView.setText(String.valueOf(change));
            textView.setTextColor(Color.parseColor("#CC2936"));
            combo = false;
            pause = true;
        }
        textView.setTextSize(15);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setAlpha(0);
        if (change == -200) {
            scoreRef.getLocationInWindow(location);
            int x = location[0] + (int) (scoreRef.getWidth() * 0.75);
            int y = location[1] - (int) (scoreRef.getHeight() * 0.75);
            textView.setX(x);
            textView.setY(y);
        } else {
            selectedSquare.getLocationInWindow(location);
            textView.setX(location[0] + (int) (selectedSquare.getWidth() * 0.3));
            textView.setY(location[1] - (int) (selectedSquare.getHeight() * 0));
        }
        myLayout.addView(textView);

        textView.animate().
                scaleX(1.3f).
                scaleY(1.3f).
                alpha(1).
                setDuration(300).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        playerScore.getLocationInWindow(location);
                        int x = location[0] + (int) (scoreRef.getWidth() * 0.65); //bigger means more right(of the multiplier)
                        int y = location[1] - (int) (scoreRef.getHeight() * 0.8); //bigger means more down(of the multiplier)
                        textView.animate().
                                x(x).
                                y(y).
                                setDuration(700).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.animate().
                                                scaleX(0.8f).
                                                scaleY(0.8f).
                                                alpha(0).
                                                setDuration(500).
                                                withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        myLayout.removeView(textView);
                                                        changeScore(change);
                                                        if (change < 0) {
                                                            changeTurn(null);
                                                            pressDeleteButton();
                                                        }

                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    public void addScoreAnimationForRival(int change,Button square) {
        TextView scoreRef = rivalScore;
        int[] location = new int[2];

        RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.myLayout);
        TextView textView = new TextView(this);

        if (change > 0) {
            textView.setText("+" + String.valueOf(change));
            textView.setTextColor(Color.parseColor("#698F3F"));
        } else {
            textView.setText(String.valueOf(change));
            textView.setTextColor(Color.parseColor("#CC2936"));
        }
        textView.setTextSize(15);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setAlpha(0);
        if (change == -200) {
            scoreRef.getLocationInWindow(location);
            int x = location[0] + (int) (scoreRef.getWidth() * 0.75);
            int y = location[1] - (int) (scoreRef.getHeight() * 0.75);
            textView.setX(x);
            textView.setY(y);
        } else {
            square.getLocationInWindow(location);
            textView.setX(location[0] + (int) (square.getWidth() * 0.3));
            textView.setY(location[1] - (int) (square.getHeight() * 0));
        }
        myLayout.addView(textView);

        textView.animate().
                scaleX(1.3f).
                scaleY(1.3f).
                alpha(1).
                setDuration(300).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        rivalScore.getLocationInWindow(location);
                        int x = location[0] + (int) (scoreRef.getWidth() * 0.65); //bigger means more right(of the multiplier)
                        int y = location[1] - (int) (scoreRef.getHeight() * 0.8); //bigger means more down(of the multiplier)
                        textView.animate().
                                x(x).
                                y(y).
                                setDuration(700).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.animate().
                                                scaleX(0.8f).
                                                scaleY(0.8f).
                                                alpha(0).
                                                setDuration(500).
                                                withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        myLayout.removeView(textView);
                                                        popupAnimation(rivalScore);

                                                    }
                                                });
                                    }
                                });
                    }
                });
    }


    public void changeTurn(View v) {
        //backend
        int score = Integer.parseInt((String) playerScore.getText());
        gameObject.setBoard(gameObject.convertToList(boardUnsolved));
        if (isPlayer1) {
            gameObject.setTurn("gameUser2");
            gameObject.setUser1Score(score);
        } else {
            gameObject.setTurn("gameUser1");
            gameObject.setUser2Score(score);
        }
        ref.updateChildren(Map.of(
                "turn",gameObject.getTurn(),
                "user1Score",gameObject.getUser1Score(),
                "user2Score",gameObject.getUser2Score(),
                "board",gameObject.getBoard()));

        pause = true;
        secs = -1;
        time_limit = time_limit_setting;
        combo = false;
        pressDeleteButton();

//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

    }


    public void runTimer() {
        if (!pause) {
            secs += 1;
            if (secs > time_limit) secs = time_limit;

            String newText = String.format("%d:%02d",
                    ((int) (time_limit - secs) / 60),
                    ((int) (time_limit - secs) % 60));
            playerTime.setText(newText);


            //backend
            if (isPlayer1) {
                gameObject.setUser1Time((time_limit - secs));
                ref.updateChildren(Map.of(
                        "user1Time",gameObject.getUser1Time()));
            } else {
                gameObject.setUser2Time((time_limit - secs));
                ref.updateChildren(Map.of(
                        "user2Time",gameObject.getUser2Time()));
            }

            if (secs > time_limit - 6 && secs < time_limit) {
                popupAnimation(playerTime);
            } else {
                if (secs == time_limit) {
                    pause = true;
//                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    pressDeleteButton();
                    if (combo) {
                        changeTurn(null);
                    } else {
                        addScoreAnimation(-200);
                        gameObject.setChange(-200);
                        if (isPlayer1) gameObject.setChangeOfPlayer("gameUser1");
                        else gameObject.setChangeOfPlayer("gameUser2");
                        ref.updateChildren(Map.of(
                                "change",gameObject.getChange(),
                                "changeOfPlayer",gameObject.getChangeOfPlayer()
                        ));
                        combo = false;
                    }
                }
            }
        }
        if (isOver()) {
            pause = true;
            handler.postDelayed(()-> {
                if ((gameObject.getUser1Score() > gameObject.getUser2Score() && isPlayer1) ||
                        (gameObject.getUser1Score() < gameObject.getUser2Score() && !isPlayer1)) {
                    createGameRecord();
                } else {
                    findGameRecord(30);
                }
            }, 2500);

            return;
        } else {
            if (secs == time_limit) {
                handler.postDelayed(this::runTimer,2000);

            } else {
                handler.postDelayed(this::runTimer,1000);
            }
        }
    }

    public void changeScore(int change) {
        int score = Integer.parseInt((String) playerScore.getText());
        String text = String.valueOf(score + change);
        playerScore.setText(text);
        popupAnimation(playerScore);

        //backend
        if (change > 0) {
            gameObject.setBoard(gameObject.convertToList(boardUnsolved));
            if (isPlayer1) {
                gameObject.setUser1Score(score + change);
                gameObject.setChangeOfPlayer("gameUser1");
            } else {
                gameObject.setUser2Score(score + change);
                gameObject.setChangeOfPlayer("gameUser2");
            }
            gameObject.setChange(change);

            ref.updateChildren(Map.of(
                    "board",gameObject.getBoard(),
                    "user1Score",gameObject.getUser1Score(),
                    "user2Score",gameObject.getUser2Score(),
                    "change",gameObject.getChange(),
                    "changeOfPlayer",gameObject.getChangeOfPlayer()
            ));
        }
    }

    public int findButtonReturnSolvedValue() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (boardButtonMat[i][j] == selectedSquare) {
                    return boardSolved[i][j];
                }
            }
        }
        return 0;
    }

    public void findRedSquares() {
        if (deleteSquares) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (Color.parseColor("#CC2936") ==
                            boardButtonMat[i][j].getTextColors().getDefaultColor()) {
                        boardButtonMat[i][j].setTextColor(Color.parseColor("#262626"));
                        boardButtonMat[i][j].setText("");
                    }
                }
            }
        }

    }

    public void pressDeleteButton() {
        if (selectedSquare != null) {
            if (selectedSquare.getTextColors().getDefaultColor() == Color.parseColor("#FFFFFD")) {
                selectedSquare.setTextColor(Color.parseColor("#CC2936"));
            } else {
                selectedSquare.setTextColor(Color.parseColor("#262626"));
            }
            selectedSquare.setBackgroundResource(R.color.white);
            selectedSquare = null;
        }
        if (selectedButton != null) {
            selectedButton.setTextColor(Color.parseColor("#FFFFFF"));
            selectedButton.setBackgroundResource(R.drawable.bg_rounded_black);
            selectedButton = null;
        }
    }

    public void popupAnimation(View view) {
        view.animate().scaleX(1.1f).scaleY(1.1f).
                setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.animate().setStartDelay(50).scaleX(1f).scaleY(1f).setDuration(150);
            }
        });
    }

    public int getSignalLevel() {
        int level = 0;

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int numberOfLevels = 5;
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),numberOfLevels);
                } else {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        level = telephonyManager.getSignalStrength().getLevel();
                    } else {
                        level = 1;
                    }
                }
            }
        } catch (Exception e) {
//            System.out.println(e.getMessage());
        }

        return level;
    }

    public void updateSignalIcon(int signal,ImageView imageView) {
        switch (signal) {
            case 0:
                imageView.setImageResource(R.drawable.wifi_0);
                break;
            case 1:
                imageView.setImageResource(R.drawable.wifi_1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.wifi_2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.wifi_3);
                break;
            default:
                imageView.setImageResource(R.drawable.wifi_4);
                break;
        }
    }


    public void exitClick(View view) {
        if (gameObject == null || gameObject.isGameOver()) {
            if (lookingForGame) {
                rematchClick(findViewById(R.id.egm_btn_rematch));
                handler.postDelayed(() -> {
                    exitClick(view);
                },1000);
                return;
            }
            background_loop_running = false;
            finish();
            DaoGameBackend.removeGarbage(currentUser);
            DaoGameBackend.removeGames(currentUser);
        } else {
            if (isPlayer1) {
                gameObject.setUser1lastUpdate(gameObject.getUser1lastUpdate() - 30000);
                ref.updateChildren(Map.of(
                        "user1lastUpdate",gameObject.getUser1lastUpdate()
                ));
            } else {
                gameObject.setUser2lastUpdate(gameObject.getUser2lastUpdate() - 30000);
                ref.updateChildren(Map.of(
                        "user1lastUpdate",gameObject.getUser2lastUpdate()
                ));
            }
            background_loop_running = false;
            finish();
            DaoGameBackend.removeGarbage(currentUser);
        }
    }

    public void settingClick(View view) {
        buttonRight_onClick(null);
    }

    private void createGameRecord() {
        handler.removeCallbacksAndMessages(null);
        String whoWon, reason;
        if (isPlayer1) {
            whoWon = "user1";
            gameObject.setOverallUser1Score(gameObject.getOverallUser1Score() + 1);
            gameUser1.setUserMMR(gameUser1.getUserMMR() + 9);
            gameUser2.setUserMMR(gameUser2.getUserMMR() - 9);
        } else {
            whoWon = "user2";
            gameObject.setOverallUser2Score(gameObject.getOverallUser2Score() + 1);
            gameUser1.setUserMMR(gameUser1.getUserMMR() - 9);
            gameUser2.setUserMMR(gameUser2.getUserMMR() + 9);
        }

        if (rivalAbandoned) {
            reason = "left";
        } else if (Math.abs(gameObject.getUser1Score() - gameObject.getUser2Score()) >= advantageWin) {
            reason = "advantage";
        } else {
            reason = "points";
        }

        gameObject.setGameUser1(gameUser1);
        gameObject.setGameUser2(gameUser2);
        gameObject.setGameOver(true);
        ref.updateChildren(Map.of(
                "gameOver",gameObject.isGameOver()
        ));
        gameRecord = new GameRecord(gameObject,whoWon,reason);
        DaoGameBackend.myRef.child("User").child(gameUser1.getUserKey()).updateChildren(Map.of(
                "lastGameRecord",gameRecord,
                "userMMR",gameUser1.getUserMMR()
        ));
        DaoGameBackend.myRef.child("User").child(gameUser2.getUserKey()).updateChildren(Map.of(
                "lastGameRecord",gameRecord,
                "userMMR",gameUser2.getUserMMR()
        ));

        handler.postDelayed(() -> {
            DaoGameBackend.removeGarbage(currentUser);
        },5000);

        showEndGameMessage();

    }

    private void findGameRecord(int ticks) {
        gameObject.setGameOver(true);
        handler.removeCallbacksAndMessages(null);
        if (ticks < 0) {
            Toast.makeText(this,"Error accured",Toast.LENGTH_SHORT).show();
            exitClick(null);
            return;
        }
        DaoGameBackend.myRef.child("User").child(currentUser.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getLastGameRecord() != null) {
                    if (user.getLastGameRecord().getGameKey().equals(gameObject.getGameKey())) {
                        gameRecord = user.getLastGameRecord();
                        DaoGameBackend.myRef.child("User").child(currentUser.getUserKey()).child("lastGameRecord").child("gameKey").setValue("")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        DaoGameBackend.removeGames(currentUser);
                                        DaoGameBackend.removeGarbage(currentUser);
                                        showEndGameMessage();
                                    }
                                });
                        return;
                    }
                }
                findGameRecord(ticks - 1);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showEndGameMessage() {
//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        boolean isWon = (gameRecord.getWhoWon().equals("user1") && isPlayer1) ||
                (gameRecord.getWhoWon().equals("user2") && !isPlayer1);

        View message = findViewById(R.id.end_game_message);
        message.setVisibility(View.VISIBLE);
        popupAnimation(message);
        TextView player1name = (TextView) findViewById(R.id.egm_user1name);
        TextView player2name = (TextView) findViewById(R.id.egm_user2name);
        RelativeLayout player1card = (RelativeLayout) findViewById(R.id.egm_user1card);
        RelativeLayout player2card = (RelativeLayout) findViewById(R.id.egm_user2card);
        TextView overallScore = (TextView) findViewById(R.id.egm_overall_score);
        TextView player_message_won = (TextView) findViewById(R.id.egm_player_message_won);
        TextView reason_message_won = (TextView) findViewById(R.id.egm_reason_message_won);
        TextView player_rating = (TextView) findViewById(R.id.egm_player_rating);
        TextView rating_change = (TextView) findViewById(R.id.egm_rating_change);

        player1name.setText(gameUser1.getUserName());
        player2name.setText(gameUser2.getUserName());
        overallScore.setText(String.valueOf(gameRecord.getUser1overallScore()) +
                " - " + String.valueOf(gameRecord.getUser2overallScore()));

        if (gameRecord.getReason().equals("left")) {
            reason_message_won.setText("by abandonment");
        } else if (gameRecord.getReason().equals("advantage")) {
            reason_message_won.setText("by significant " + String.valueOf(advantageWin) + " points advantage");
        } else {
            reason_message_won.setText("by points advantage");
        }

        if (gameRecord.getWhoWon().equals("user1")) {
            player1card.setBackgroundResource(R.drawable.bg_rounded_green);
            player2card.setBackgroundResource(R.drawable.bg_rounded_white);

        } else if (gameRecord.getWhoWon().equals("user2")) {
            player2card.setBackgroundResource(R.drawable.bg_rounded_green);
            player1card.setBackgroundResource(R.drawable.bg_rounded_white);

        }
        if (isWon) {
            player_message_won.setText("You won!");
            player_message_won.setTextColor(Color.parseColor("#698F3F"));
            rating_change.setText("+" + String.valueOf(gameRecord.getMmrChange()));
            rating_change.setTextColor(Color.parseColor("#698F3F"));
        } else {
            player_message_won.setText("You lost");
            player_message_won.setTextColor(Color.parseColor("#CC2936"));
            rating_change.setText(String.valueOf(gameRecord.getMmrChange() * -1));
            rating_change.setTextColor(Color.parseColor("#CC2936"));
        }
        if (isPlayer1) {
            player_rating.setText(String.valueOf(gameRecord.getUser1MMR()));
        } else {
            player_rating.setText(String.valueOf(gameRecord.getUser2MMR()));
        }

        background_loop();
    }

    public void cancelClick_waiting(View view) {
        Button btn = (Button) findViewById(R.id.egm_btn_rematch);
        if (lookingForGame) {
            lookingForGame = false;
            btn.setEnabled(false);

            handler.postDelayed(() -> {
                btn.setEnabled(true);
                if (!btn.getText().toString().equals("searching")) {
                    lookingForGame = false;
                } else {
                    lookingForGame = true;
                }
            },500);
        }
    }

    public void rematchClick(View view) {
        Button btn = (Button) view;

        if (!lookingForGame) {
            findViewById(R.id.waiting_popup).setVisibility(View.VISIBLE);
            btn.setText("searching");
            btn.setEnabled(false);

            handler.postDelayed(() -> {
                createGameRequest();
                btn.setEnabled(true);
            },500);
        } else {
            lookingForGame = false;
            btn.setEnabled(false);

            handler.postDelayed(() -> {
                btn.setEnabled(true);
                if (!btn.getText().toString().equals("searching")) {
                    lookingForGame = false;
                } else {
                    lookingForGame = true;
                }
            },500);
        }
    }

    private void createGameRequest() {
        lookingForGame = true;

        if (isPlayer1) {
            gameRequest = new GameRequest(gameUser1,gameUser2);
        } else {
            gameRequest = new GameRequest(gameUser2,gameUser1);
        }

        DaoGameBackend.grRef.child(gameRequest.getKey()).setValue(gameRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        waitForAccept(gameRequest);
                    }
                });

    }

    private void waitForAccept(GameRequest gameRequest) {
        System.out.println("waiting for accept");
        DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                boolean check = true, grExists = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    GameRequest temp = data.getValue(GameRequest.class);
                    if (temp.getKey().equals(gameRequest.getKey())) {
                        if (temp.isAccepted()) {
                            check = false;
                        }
                        grExists = true;
                    } else if (temp.getUserReceived().equals(currentUser) &&
                    temp.getUserSender().equals(rivalUser)) {
                        DaoGameBackend.grRef.child(temp.getKey()).child("accepted").setValue(true);
                        resetGame();
                        return;
                    }
                }
                if (check) {
                    if (!lookingForGame || !grExists) {
                        DaoGameBackend.removeGarbage(currentUser);
                        Button btn = findViewById(R.id.egm_btn_rematch);
                        btn.setText("rematch");
                        findViewById(R.id.waiting_popup).setVisibility(View.GONE);
                        return;
                    } else {
                        waitForAccept(gameRequest);
                    }
                } else {
                    resetGame();
                }

            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {


            }
        });
    }

    private void resetGame() {
        Toast.makeText(this,"reset game",Toast.LENGTH_SHORT).show();

        handler.removeCallbacksAndMessages(null);

        DaoGameBackend.gRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Game game = data.getValue(Game.class);
                    if (gameObject.getGameKey().equals(game.getGameKey())) {
                        continueResetGame();
                        return;
                    }
                }
                isPlayer1 = false;
                continueResetGame();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void continueResetGame(){
        if (!isPlayer1) {
            Game gameObjectTemp = new Game(gameUser2,gameUser1);
            gameObjectTemp.setOverallUser1Score(gameRecord.getUser2overallScore());
            gameObjectTemp.setOverallUser2Score(gameRecord.getUser1overallScore());
            gameObjectTemp.setGameKey(gameObject.getGameKey());

            Object[] obj = DaoGameBackend.sudokoGenerator();
            gameObjectTemp.setBoard(gameObjectTemp.convertToList((int[][]) obj[0]));
            gameObjectTemp.setSolvedBoard(gameObjectTemp.convertToList((int[][]) obj[1]));

            DaoGameBackend.updateGame(gameObjectTemp);
        }

        Intent intent = getIntent();
        background_loop_running = false;
        finish();
        startActivity(intent);
    }


    public boolean isOver() {

        TextView player1score = (TextView) findViewById(R.id.player1score);
        TextView player2score = (TextView) findViewById(R.id.player2score);

        if (gameObject.getUser1Score() - gameObject.getUser2Score() > advantageWin ||
                gameObject.getUser1Score() - gameObject.getUser2Score() < (-1 * advantageWin)) {
            return true;
        }

        GridLayout boardLayout = findViewById(R.id.board);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int index = (i * 9) + j;
                boardButtonMat[i][j] = (Button) boardLayout.getChildAt(index);
                if (!boardButtonMat[i][j].getText().toString().equals("")) {
                    if (Integer.parseInt(boardButtonMat[i][j].getText().toString()) != boardSolved[i][j]) {
                        return false;
                    }
                } else return false;

            }
        }
        return true;
    }

    @SuppressLint("WrongConstant")
    public void buttonRight_onClick(View view) {
////        if (!pause) {
//        if (!drawerLayout.isDrawerOpen(Gravity.END)) {
//            drawerLayout.closeDrawers();
//            drawerLayout.openDrawer(Gravity.END);
//        } else {
//            drawerLayout.closeDrawers();
//            }
////        }
//
    }

    public void backClick(View view) {
        View verify_exit = findViewById(R.id.verify_exit_layout);
        verify_exit.setVisibility(View.VISIBLE);
    }

    public void cancelClick(View view) {
        View verify_exit = findViewById(R.id.verify_exit_layout);
        verify_exit.setVisibility(View.GONE);
    }

    public void addFriendClick(View view) {
        DaoGameBackend.uRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user.getUserKey().equals(currentUser.getUserKey())) {
                        currentUser = user;
                    } else if (user.getUserKey().equals(rivalUser.getUserKey())) {
                        rivalUser = user;
                    }
                }

                for (Friend f : currentUser.getFriends()) {
                    if (f.getFriendKey().equals(rivalUser.getUserKey())) {
                        if (f.getFriendStatus().equals("accepted")){
                            Toast.makeText(OnlineGameActivity.this,"already friends",Toast.LENGTH_SHORT).show();
                        }
                        else if (f.getFriendStatus().equals("sent")) {
                            Toast.makeText(OnlineGameActivity.this,"friend request already sent",Toast.LENGTH_SHORT).show();
                        } else {
                            int i = currentUser.getFriends().indexOf(f);
                            f.setFriendStatus("accepted");
                            currentUser.getFriends().set(i,f);
                            DaoGameBackend.uRef.child(currentUser.getUserKey()).child("friends").setValue(currentUser.getFriends());

                            for (Friend g : rivalUser.getFriends()) {
                                if (g.getFriendKey().equals(currentUser.getUserKey())) {
                                    int j = rivalUser.getFriends().indexOf(g);
                                    g.setFriendStatus("accepted");
                                    rivalUser.getFriends().set(j,g);
                                    DaoGameBackend.uRef.child(rivalUser.getUserKey()).child("friends").setValue(rivalUser.getFriends());
                                }
                            }
                            Toast.makeText(OnlineGameActivity.this,"friend added",Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                }
                currentUser.addFriend(new Friend(rivalUser,"sent"));
                DaoGameBackend.uRef.child(currentUser.getUserKey()).child("friends").setValue(currentUser.getFriends());

                rivalUser.addFriend(new Friend(currentUser,"pending"));
                DaoGameBackend.uRef.child(rivalUser.getUserKey()).child("friends").setValue(rivalUser.getFriends());


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void background_loop() {
        if (!background_loop_running) return;



        DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    GameRequest temp = data.getValue(GameRequest.class);
                    if (temp.getUserReceived().equals(currentUser)) {
//                        DaoGameBackend.grRef.child(temp.getKey()).child("accepted").setValue(true);
//                        resetGame();
                        showGameRequest(temp);
                        return;
                    }
                }
                background_loop();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showGameRequest(GameRequest temp) {
        Toast.makeText(this,
                temp.getUserSender().getUserName() + " (" +
                temp.getUserSender().getUserMMR() + ") wants a game"
                ,Toast.LENGTH_SHORT).show();
//        View view = findViewById(R.id.gr);
//        TextView grPopUp_txt = (TextView) findViewById(R.id.grPopUp_txt);
//        view.setVisibility(View.VISIBLE);
//        grPopUp_txt.setText(temp.getUserSender().getUserName() + " (" +
//                temp.getUserSender().getUserMMR() + ") wants a game");
//        grPopup = temp;
    }

    @Override
    public void grPopUp_reject_onClick(View view) {
        View gr_view = findViewById(R.id.gr);
        gr_view.setVisibility(View.GONE);
        DaoGameBackend.grRef.child(grPopup.getKey()).removeValue();
        background_loop();
    }

    @Override
    public void grPopUp_confirm_onClick(View view) {
        View gr_view = findViewById(R.id.gr);
        gr_view.setVisibility(View.GONE);
        DaoGameBackend.grRef.child(grPopup.getKey()).updateChildren(Map.of(
                "accepted", true ));
        resetGame();
    }




    //dont touch below
}