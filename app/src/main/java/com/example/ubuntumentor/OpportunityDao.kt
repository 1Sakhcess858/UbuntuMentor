package com.example.ubuntumentor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities")
    suspend fun getAllOpportunities(): List<OpportunityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunities(opportunities: List<OpportunityEntity>)

    @Query("DELETE FROM opportunities")
    suspend fun clearAll()
}