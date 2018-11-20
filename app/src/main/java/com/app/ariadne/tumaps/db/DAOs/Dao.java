package com.app.ariadne.tumaps.db.DAOs;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.app.ariadne.tumaps.db.models.TargetPointTagged;

import java.util.ArrayList;

public interface Dao {
    @Query("SELECT * FROM targetpointtagged")
    ArrayList<TargetPointTagged> getAllTargetPointsTagged();

    @Query("SELECT * FROM targetpointtagged WHERE building_id IS :buildingId")
    ArrayList<TargetPointTagged> loadAllTargetPointsTaggedForBuildingId(String buildingId);

    @Insert
    void insertAll(ArrayList<TargetPointTagged> targetPointsTagged);

    @Delete
    void delete(TargetPointTagged user);

}
