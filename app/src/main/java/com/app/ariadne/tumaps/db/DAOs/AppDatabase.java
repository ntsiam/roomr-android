package com.app.ariadne.tumaps.db.DAOs;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.app.ariadne.tumaps.db.models.TargetPointTagged;

@Database(entities = {TargetPointTagged.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract Dao dao();
}
