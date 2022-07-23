package leifer.example.speedsoduko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import leifer.example.speedsoduko.dao.DaoGameBackend;
import leifer.example.speedsoduko.objects.User;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        DaoGameBackend.uRef.orderByChild("userMMR").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot == null) return;
                if (!snapshot.exists()) return;
                int i = (int) snapshot.getChildrenCount() - 1;
                TextView username, mmr;
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
//                    int j = (int)snapshot.getChildrenCount() - i;
                    switch (i) {
                        case 0:
                            username= findViewById(R.id.username_1);
                            mmr = findViewById(R.id.mmr_1);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 1:
                            username = findViewById(R.id.username_2);
                            mmr = findViewById(R.id.mmr_2);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            username = findViewById(R.id.username_3);
                            mmr = findViewById(R.id.mmr_3);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 3:
                            username = findViewById(R.id.username_4);
                            mmr = findViewById(R.id.mmr_4);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 4:
                            username = findViewById(R.id.username_5);
                            mmr = findViewById(R.id.mmr_5);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 5:
                            username = findViewById(R.id.username_6);
                            mmr = findViewById(R.id.mmr_6);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 6:
                            username = findViewById(R.id.username_7);
                            mmr = findViewById(R.id.mmr_7);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 7:
                            username = findViewById(R.id.username_8);
                            mmr = findViewById(R.id.mmr_8);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 8:
                            username = findViewById(R.id.username_9);
                            mmr = findViewById(R.id.mmr_9);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        case 9:
                            username = findViewById(R.id.username_10);
                            mmr = findViewById(R.id.mmr_10);
                            username.setText(user.getUserName());
                            mmr.setText(String.valueOf(user.getUserMMR()));
                            username.setVisibility(View.VISIBLE);
                            mmr.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }

                    i--;
                }



            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void onClickBack(View view) {
        finish();
    }
}