package leifer.example.speedsoduko;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import leifer.example.speedsoduko.dao.DaoGameBackend;
import leifer.example.speedsoduko.objects.User;

public class OfflineGameActivity extends AppCompatActivity {

    Button selectedButton;
    Button selectedSquare;
    Button boardButtonMat[][] = new Button[9][9];
    int boardUnsolved[][];
    int boardSolved[][];
    int secs = 0;
    boolean pause = false;
    int time_limit = 30;
    boolean red = true;
    boolean combo = false;
    TextView playerTime = null;
    TextView playerScore = null;
    Handler handler = new Handler();
    private DrawerLayout drawerLayout;
    private int advantageWin = 1500;
    private boolean changeOrientation = false;
    private boolean deleteSquares = true;
    private int time_limit_setting = 30;
    private int multiplayer = 5;
    private GridLayout boardLayout;
    private RelativeLayout buttonLayout;
    private int player1globalScore = 0;
    private int player2globalScore = 0;
    private User gameUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_game);

        if (getIntent().getExtras() != null) {
            gameUser = (User) getIntent().getExtras().getSerializable("CurrentUser");
            TextView playerName = (TextView) findViewById(R.id.player1name);
            playerName.setText(gameUser.getUserName() + " (" + String.valueOf(gameUser.getUserMMR()) + ")");
        }




        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                pause = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                View pauseGameMessage = findViewById(R.id.pause_game_message);
                if (pauseGameMessage.getVisibility() == View.GONE) {
                    pause = false;
                }
            }
        });




        Object[] obj = DaoGameBackend.sudokoGenerator();
        boardUnsolved = (int[][]) obj[0];
        boardSolved = (int[][]) obj[1];

        findViewById(R.id.player1signal).setVisibility(View.INVISIBLE);
        findViewById(R.id.player2signal).setVisibility(View.INVISIBLE);

        LinearLayout bar2 = (LinearLayout) findViewById(R.id.player2bar);
        boardLayout = (GridLayout) findViewById(R.id.board);
        buttonLayout = (RelativeLayout) findViewById(R.id.button_bar_layout);
        boardLayout.setBackgroundResource(R.color.blue);
        buttonLayout.setBackgroundResource(R.color.blue);
        playerTime = findViewById(R.id.player1time);
        playerScore = findViewById(R.id.player1score);
        bar2.setAlpha((float) 0.3);

//        if (boardLayout.getWidth() > boardLayout.getWidth()) {
//            boardLayout.
//        }

        setupSettingLayout();

        createBoardList();
        runTimer(null);
//        Toast.makeText(this,"created",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSettingLayout() {
        Switch switch_setting_flip, switch_setting_wrong_answers;
        SeekBar seekbar_setting_advantage_win, seekbar_setting_turn_time, seekbar_setting_multiplayer;
        TextView text_label_advantage_win, text_label_flip_state, text_label_turn_time,
                text_label_multiplayer, text_label_wrong_answers_state;


        switch_setting_wrong_answers = findViewById(R.id.switch_setting_wrong_answers);
        text_label_wrong_answers_state = findViewById(R.id.text_label_wrong_answers_state);
        switch_setting_wrong_answers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    text_label_wrong_answers_state.setText("ON");
                    deleteSquares = true;
                } else {
                    text_label_wrong_answers_state.setText("OFF");
                    deleteSquares = false;
                }
            }
        });

        switch_setting_flip = findViewById(R.id.switch_setting_flip);
        text_label_flip_state = findViewById(R.id.text_label_flip_state);
        switch_setting_flip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked) {
                    text_label_flip_state.setText("ON");
                    changeOrientation = true;
                } else {
                    text_label_flip_state.setText("OFF");
                    changeOrientation = false;
                }
            }
        });

        seekbar_setting_advantage_win = findViewById(R.id.seekbar_setting_advantage_win);
        text_label_advantage_win = findViewById(R.id.text_label_advantage_win);
        seekbar_setting_advantage_win.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                progress = progress - (progress % 500);
                seekBar.setProgress(progress);

                if (progress == 4000) {
                    advantageWin = 10000000;
                    text_label_advantage_win.setText("OFF");
                } else {
                    text_label_advantage_win.setText(String.valueOf(progress));
                    advantageWin = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbar_setting_advantage_win.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v,MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle seekbar touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        seekbar_setting_turn_time = findViewById(R.id.seekbar_setting_turn_time);
        text_label_turn_time = findViewById(R.id.text_label_turn_time);
        seekbar_setting_turn_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                progress = progress - (progress % 5);
                seekBar.setProgress(progress);
                String newText = String.format("%d:%02d",(progress / 60),(progress % 60));
                text_label_turn_time.setText(newText);
                time_limit_setting = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbar_setting_turn_time.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v,MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle seekbar touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        seekbar_setting_multiplayer = findViewById(R.id.seekbar_setting_multiplayer);
        text_label_multiplayer = findViewById(R.id.text_label_multiplayer);
        seekbar_setting_multiplayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                text_label_multiplayer.setText(String.valueOf(progress));
                multiplayer = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbar_setting_multiplayer.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v,MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle seekbar touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    private void createBoardList() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int index = (i * 9) + j;
                boardButtonMat[i][j] = (Button) boardLayout.getChildAt(index);
                boardButtonMat[i][j].setTextColor(Color.parseColor("#262626"));
                boardButtonMat[i][j].setBackgroundResource(R.color.white);
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
                    popupAnimation(selectedButton);
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
        if (findButtonReturnSolvedValue() == Integer.parseInt((String) selectedButton.getText())) {
            if (red) {
                findRedSquares();
                red = false;
            }

            selectedSquare.setTextColor(Color.parseColor("#698F3F"));
            selectedSquare.setEnabled(false);
            if (time_limit > time_limit_setting) {
                addScoreAnimation(Math.max(((time_limit - secs) * multiplayer)/2 + (multiplayer * 4),5));
            } else {
                addScoreAnimation(Math.max(((time_limit_setting - secs)/2 * multiplayer) + (multiplayer * 4),5));
            }

        } else {
            if (red) {
                findRedSquares();
                red = false;
            }
            selectedSquare.setTextColor(Color.parseColor("#CC2936"));
            addScoreAnimation(Math.min(secs/2 * -1 * multiplayer, -5));
            red = true;
        }


    }

    public void addScoreAnimation(int change) {

        TextView scoreRef = playerScore;
        int[] location = new int[2];

        RelativeLayout myLayout = (RelativeLayout) findViewById(R.id.myLayout);
        TextView textView = new TextView(this);

        if (change > 0) {
            textView.setText("+" + String.valueOf(change));
            textView.setTextColor(Color.parseColor("#698F3F"));
            time_limit = secs + 5;
            combo = true;
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            textView.setText(String.valueOf(change));
            textView.setTextColor(Color.parseColor("#CC2936"));
            combo = false;
            pause = true;
        }
        textView.setTextSize(15);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setAlpha(0);
        if (change == (time_limit_setting * multiplayer * -1)) {
            scoreRef.getLocationInWindow(location);
            int x = location[0] + (int) (scoreRef.getWidth() * 0.75);
            int y = location[1] - (int) (scoreRef.getHeight() * 0.75);
            textView.setX(x);
            textView.setY(y);
        } else {
            selectedSquare.getLocationInWindow(location);
            textView.setX(location[0] + (int)(selectedSquare.getWidth() * 0.3));
            textView.setY(location[1] - (int)(selectedSquare.getHeight() * 0));
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
                        if (change < 0) {
                            pressDeleteButton();
                        }
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
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    public void changeTurn(View v) {

        if (changeOrientation) {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        secs = -1;
        time_limit = time_limit_setting;
        combo = false;
        pause = false;
        pressDeleteButton();

        LinearLayout bar1 = (LinearLayout) findViewById(R.id.player1bar);
        LinearLayout bar2 = (LinearLayout) findViewById(R.id.player2bar);

        if (bar1.getAlpha() < 1) {
            boardLayout.setBackgroundResource(R.color.blue);
            buttonLayout.setBackgroundResource(R.color.blue);

            bar1.setAlpha(1);
            bar2.setAlpha((float) 0.3);
            popupAnimation(bar1);
            playerTime = (TextView) findViewById(R.id.player1time);
            playerScore = (TextView) findViewById(R.id.player1score);

        } else {
            boardLayout.setBackgroundResource(R.color.yellow);
            buttonLayout.setBackgroundResource(R.color.yellow);

            bar2.setAlpha(1);
            bar1.setAlpha((float) 0.3);
            popupAnimation(bar2);
            playerTime = (TextView) findViewById(R.id.player2time);
            playerScore = (TextView) findViewById(R.id.player2score);

        }

        pause = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pause = false;
                pressDeleteButton();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        },1000);
    }

    private void showEndGameMessage() {
        View message = findViewById(R.id.end_game_message);
        message.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        popupAnimation(message);

        handler.removeCallbacksAndMessages(null);
        pause = true;

        TextView player1score = (TextView) findViewById(R.id.player1score);
        TextView player2score = (TextView) findViewById(R.id.player2score);

        TextView player1name = (TextView) findViewById(R.id.egm_user1name);
        TextView player2name = (TextView) findViewById(R.id.egm_user2name);

        RelativeLayout player1card = (RelativeLayout) findViewById(R.id.egm_user1card);
        RelativeLayout player2card = (RelativeLayout) findViewById(R.id.egm_user2card);

        TextView overallScore = (TextView) findViewById(R.id.egm_overall_score);
        TextView reason_message_won = (TextView) findViewById(R.id.egm_reason_message_won);
        TextView player_message_won = (TextView) findViewById(R.id.egm_player_message_won);

        findViewById(R.id.egm_btn_add_friend).setVisibility(View.INVISIBLE);
        findViewById(R.id.egm_rating_title).setVisibility(View.INVISIBLE);
        findViewById(R.id.egm_player_rating).setVisibility(View.INVISIBLE);
        findViewById(R.id.egm_rating_change).setVisibility(View.INVISIBLE);
        findViewById(R.id.egm_btn_rematch).setVisibility(View.GONE);

        player1name.setText(gameUser.getUserName());

        if (Integer.parseInt(String.valueOf(player1score.getText())) >
                Integer.parseInt(String.valueOf(player2score.getText()))) {
            player_message_won.setText("You won!");
            player1card.setBackgroundResource(R.drawable.bg_rounded_green);
            player2card.setBackgroundResource(R.drawable.bg_rounded_white);
            player1globalScore++;
        } else {
            player_message_won.setText("Guest won!");
            player2card.setBackgroundResource(R.drawable.bg_rounded_green);
            player1card.setBackgroundResource(R.drawable.bg_rounded_white);
            player2globalScore++;
        }

        overallScore.setText(String.valueOf(player1globalScore)+" - "+String.valueOf(player2globalScore));

        if (Integer.parseInt(String.valueOf(player1score.getText())) -
                Integer.parseInt(String.valueOf(player2score.getText())) > advantageWin ||
                Integer.parseInt(String.valueOf(player1score.getText())) -
                        Integer.parseInt(String.valueOf(player2score.getText())) < (-1 * advantageWin)) {
            reason_message_won.setText("Won by a significant " + String.valueOf(advantageWin) + " points gap");
        } else {
            reason_message_won.setText("Won by a points advantage");
        }
    }

    public void runTimer(View view) {
        playerTime = (TextView) findViewById(R.id.player1time);
        playerScore = (TextView) findViewById(R.id.player1score);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pause != true) {
                    secs += 1;
                    String newText = String.format("%d:%02d",((int) (time_limit-secs) / 60),
                            ((int) (time_limit-secs) % 60));
                    playerTime.setText(newText);
                    if (secs > time_limit - 6 && secs < time_limit) {
                        popupAnimation(playerTime);
                    } else {
                        if (secs == time_limit) {
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            pressDeleteButton();
                            if (combo) {
                                changeTurn(null);
                            } else {
                                addScoreAnimation(time_limit_setting * multiplayer * -1);
                                pause = true;
                                combo = false;
                                pressDeleteButton();
                            }
                        }
                    }
                } else {
                    pressDeleteButton();
                }
                if (!isOver()) {
                    handler.postDelayed(this,1000);
                } else {
                    handler.postDelayed(OfflineGameActivity.this::showEndGameMessage,1500);
                }

            }
        },2000);
    }

    public void changeScore(int change) {
        int score = Integer.parseInt((String) playerScore.getText());
        String text = String.valueOf(score + change);
        playerScore.setText(text);
        popupAnimation(playerScore);
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
        Button b = (Button) findViewById(R.id.deleteButton);
        selectButton(b);
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


    public void settingClick(View view) {
        buttonRight_onClick(null);
    }

    public void newGameClick(View view) {
        View message = findViewById(R.id.end_game_message);
        message.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        handler.removeCallbacksAndMessages(null);


        pressDeleteButton();

        TextView player1score = (TextView) findViewById(R.id.player1score);
        player1score.setText("0");
        TextView player2score = (TextView) findViewById(R.id.player2score);
        player2score.setText("0");
        TextView player1time = (TextView) findViewById(R.id.player1time);
        player1time.setText("00:00");
        TextView player2time = (TextView) findViewById(R.id.player2time);
        player2time.setText("00:00");

        secs = 0;
        pause = false;
        time_limit = time_limit_setting;
        red = true;
        combo = false;
        playerTime = null;
        playerScore = null;
        selectedButton = null;
        selectedSquare = null;


        Object[] obj = DaoGameBackend.sudokoGenerator();
        boardUnsolved = (int[][]) obj[0];
        boardSolved = (int[][]) obj[1];



        createBoardList();
        runTimer(null);
        changeTurn(null);
    }

    public boolean isOver() {

        TextView player1score = (TextView) findViewById(R.id.player1score);
        TextView player2score = (TextView) findViewById(R.id.player2score);

        if (Integer.parseInt(String.valueOf(player1score.getText())) -
                Integer.parseInt(String.valueOf(player2score.getText())) > advantageWin ||
                Integer.parseInt(String.valueOf(player1score.getText())) -
                        Integer.parseInt(String.valueOf(player2score.getText())) < (-1 * advantageWin)) {
            return true;
        }

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
        View pauseGameMessage = findViewById(R.id.pause_game_message);
        if (!pause || (pauseGameMessage.getVisibility()==View.VISIBLE)) {
            if (!drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawers();
                drawerLayout.openDrawer(Gravity.END);
            } else {
                drawerLayout.closeDrawers();
            }
        }
    }

    public void pauseClick(View view) {
        ImageButton pauseButton = (ImageButton) view;
        View pauseGameMessage = findViewById(R.id.pause_game_message);
        if (!pause) {
            pause = true;
            pauseButton.setImageResource(R.drawable.play);
            pauseGameMessage.setVisibility(View.VISIBLE);
        } else {
            if (pauseGameMessage.getVisibility() == View.VISIBLE) {
                resumeClick(null);
            } else {
                Toast.makeText(this,"Can't pause between turns",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void resumeClick(View view) {
        View pauseGameMessage = findViewById(R.id.pause_game_message);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.btn_pause);
        pauseButton.setImageResource(R.drawable.pause);
        pauseGameMessage.setVisibility(View.GONE);

        pause = false;
    }

    public void backClick(View view) {
        View verify_exit = findViewById(R.id.verify_exit_layout);
        verify_exit.setVisibility(View.VISIBLE);
    }

    public void cancelClick(View view) {
        View verify_exit = findViewById(R.id.verify_exit_layout);
        verify_exit.setVisibility(View.GONE);
    }

    public void exitClick(View view) {
        pause = true;
        handler.removeCallbacksAndMessages(null);
        finish();
    }

}