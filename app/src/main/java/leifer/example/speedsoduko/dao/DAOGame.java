package leifer.example.speedsoduko.dao;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import leifer.example.speedsoduko.objects.Game;

public class DAOGame {
    private DatabaseReference databaseReference;
    private Game tempGame;

    public DAOGame() {
        FirebaseDatabase db  = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Game.class.getSimpleName());
    }
    public void add(Game game) {
        databaseReference.push().setValue(game);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getValue(Game.class).getGameUser1().equals(game.getGameUser1())
                    && data.getValue(Game.class).getGameUser2().equals(game.getGameUser2())) {
                        game.setGameKey(data.getKey());
                        update(game);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public Task<Void> update(String key,HashMap<String,Object> hashMap) {
        return databaseReference.child(key).updateChildren(hashMap);
    }
    public Task<Void> update(Game game) {
        return databaseReference.child(game.getGameKey())
                .updateChildren(parameters(game));
    }
    public Task<Void> remove(String key) {

        return databaseReference.child(key).removeValue();
    }

    public void removeByUser(String key) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getValue(Game.class).getGameUser1().equals(key) ||
                            data.getValue(Game.class).getGameUser2().equals(key)) {
                        databaseReference.child(data.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public Game getGame(String key) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getValue(Game.class).getGameUser1().equals(key) ||
                            data.getValue(Game.class).getGameUser2().equals(key) ||
                            data.getValue(Game.class).getGameKey().equals(key)){
                        tempGame = data.getValue(Game.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return tempGame;
    }

    public Query get() {
        return databaseReference;
    }

    public static HashMap<String, Object> parameters(Object obj) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }



}