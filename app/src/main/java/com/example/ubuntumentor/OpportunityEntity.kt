package com.example.ubuntumentor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val provider: String,
    val deadline: String
)