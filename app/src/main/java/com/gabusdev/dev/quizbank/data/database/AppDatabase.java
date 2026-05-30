package com.gabusdev.dev.quizbank.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gabusdev.dev.quizbank.data.database.daos.DocenteDao;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;

@Database(entities = {DocenteEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DocenteDao docenteDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "quizbank_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
