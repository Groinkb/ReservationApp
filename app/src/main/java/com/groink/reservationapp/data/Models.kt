package com.groink.reservationapp.data

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Terrain(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val surface: String = "",
    val coating: String = "",
    val lighting: Boolean = false,
    val pricePerHour: Double = 0.0,
    val imageUrl: String = ""
)

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val terrainId: String = "",
    val date: String = "", // Format: "yyyy-MM-dd"
    val timeSlot: String = "", // Format: "HH:mm"
    val userName: String = "",
    val userEmail: String = "",
    val status: String = "confirmed", // confirmed, cancelled
    val createdAt: Timestamp = Timestamp.now(),
    val totalPrice: Double = 0.0
)

data class TimeSlotAvailability(
    val terrainId: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val isAvailable: Boolean = true,
    val reservationId: String? = null
)