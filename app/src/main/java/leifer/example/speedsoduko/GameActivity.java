package leifer.example.speedsoduko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import leifer.example.speedsoduko.dao.DAOGame;
import leifer.example.speedsoduko.dao.DAOUser;
import leifer.example.speedsoduko.objects.Game;
import leifer.example.speedsoduko.objects.User;

public class GameActivity extends AppCompatActivity {
    private User gameUser1, gameUser2, currentUser;
    private String gameUser1Key, gameUser2Key;
    private Game gameObject;
    private TextView txtLabelUser1,txtLabelUser2;
    private Button btnTime;
    private DAOGame daoGame;
    private DAOUser daoUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        txtLabelUser1 = findViewById(R.id.txt_label_gameUser1);
        txtLabelUser2 = findViewById(R.id.txt_label_gameUser2);
        btnTime = findViewById(R.id.btn_timer);
        daoGame = new DAOGame();
        daoUser = new DAOUser();

        if(getIntent().getExtras() != null) {
            gameUser1 = (User) getIntent().getExtras().getSerializable("CurrentUser");
            currentUser = (User) getIntent().getExtras().getSerializable("CurrentUser");
            gameUser2 = (User) getIntent().getExtras().getSerializable("OtherUser");

            setupGame();
        }
    }

    private void setupGame() {
        daoGame.get().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                boolean check = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    if  (data.getValue(Game.class).getGameUser1().equals(gameUser1.getUserKey()) ||
                            data.getValue(Game.class).getGameUser1().equals(gameUser2.getUserKey())) {
                        gameObject = data.getValue(Game.class);
//                        btnTime.setText(String.valueOf(gameObject.getGameTime()));
                        btnTime.setEnabled(true);
                        check = true;
                    }
                }
                if (!check) {
                    gameObject = null;
                    btnTime.setEnabled(false);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {                }
        });

        daoUser.get().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if  (data.getValue(User.class).getUserKey().equals(gameObject.getGameUser1())) {
                        gameUser1 = data.getValue(User.class);
                    }
                    else if (data.getValue(User.class).getUserKey().equals(gameObject.getGameUser2())) {
                        gameUser2 = data.getValue(User.class);
                    }
                }
                txtLabelUser1.setText(gameUser1.getUserName());
                txtLabelUser2.setText(gameUser2.getUserName());
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gameObject != null) {
            daoGame.remove(gameObject.getGameKey());
        }
    }


    public void onClickBack(View view) {
        if (gameObject != null) {
            daoGame.remove(gameObject.getGameKey());
        }

        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);

    }

    public void onClickTimer(View view) {
//        gameObject.setGameTime(gameObject.getGameTime()+1);
        daoGame.update(gameObject);

    }
}