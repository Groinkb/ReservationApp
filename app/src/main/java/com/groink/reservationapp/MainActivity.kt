package com.groink.reservationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.groink.reservationapp.ui.theme.ReservationAppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Locale

// Modèles de données
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String
)

data class Reservation(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val terrainType: String
)

data class TimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAvailable: Boolean = true
)

// Gestionnaire de données (simulation d'une base de données)
object DataManager {
    private val users = mutableListOf<User>()
    private val reservations = mutableListOf<Reservation>()

    val terrainTypes = listOf("Football", "Tennis", "Basketball", "Volleyball")

    fun addUser(user: User) {
        users.add(user)
    }

    fun getUsers(): List<User> = users.toList()

    fun addReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    fun getReservations(): List<Reservation> = reservations.toList()

    fun getReservationsForDate(date: LocalDate): List<Reservation> {
        return reservations.filter { it.date == date }
    }

    fun isTimeSlotAvailable(date: LocalDate, startTime: LocalTime, endTime: LocalTime): Boolean {
        val dayReservations = getReservationsForDate(date)
        return dayReservations.none { reservation ->
            // Vérifier s'il y a un conflit d'horaire
            !(endTime <= reservation.startTime || startTime >= reservation.endTime)
        }
    }

    fun getAvailableTimeSlots(date: LocalDate): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        val dayReservations = getReservationsForDate(date)

        // Créer des créneaux d'une heure de 8h à 22h
        for (hour in 8..21) {
            val startTime = LocalTime.of(hour, 0)
            val endTime = LocalTime.of(hour + 1, 0)

            val isAvailable = dayReservations.none { reservation ->
                !(endTime <= reservation.startTime || startTime >= reservation.endTime)
            }

            timeSlots.add(TimeSlot(startTime, endTime, isAvailable))
        }

        return timeSlots
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReservationAppTheme {
                TerrainReservationApp()
            }
        }
    }
}

@Composable
fun TerrainReservationApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("users") { UsersScreen(navController) }
        composable("calendar") { MainCalendarScreen(navController) }
        composable("booking") { BookingScreen(navController) }
        composable("reservations") { ReservationsScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réservation de Terrain") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenue dans l'App de Réservation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("users") }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("👥 Gestion des Utilisateurs", fontWeight = FontWeight.Bold)
                    Text("Ajouter et voir les utilisateurs")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("calendar") }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("📅 Calendrier", fontWeight = FontWeight.Bold)
                    Text("Voir les créneaux disponibles")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("booking") }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("⚽ Nouvelle Réservation", fontWeight = FontWeight.Bold)
                    Text("Réserver un terrain")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("reservations") }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("📋 Mes Réservations", fontWeight = FontWeight.Bold)
                    Text("Voir toutes les réservations")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(DataManager.getUsers()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Utilisateurs") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }
            ) {
                Text("+", fontSize = 20.sp)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("📧 ${user.email}")
                        Text("📱 ${user.phone}")
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nouvel Utilisateur") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom complet") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank() && email.isNotBlank()) {
                                DataManager.addUser(User(name = name, email = email, phone = phone))
                                users = DataManager.getUsers()
                                name = ""
                                email = ""
                                phone = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Ajouter")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val timeSlots = remember(selectedDate) { DataManager.getAvailableTimeSlots(selectedDate) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier des Disponibilités") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sélecteur de date
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { selectedDate = selectedDate.minusDays(1) }
                    ) {
                        Text("← Précédent")
                    }

                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.FRENCH)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    TextButton(
                        onClick = { selectedDate = selectedDate.plusDays(1) }
                    ) {
                        Text("Suivant →")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Créneaux horaires:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timeSlots) { slot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (slot.isAvailable)
                                Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${slot.startTime} - ${slot.endTime}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                if (slot.isAvailable) "✅ Disponible" else "❌ Réservé",
                                color = if (slot.isAvailable)
                                    Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simplified Main Calendar Screen without external library
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalendarScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val timeSlotsForSelectedDate = remember(selectedDate) { DataManager.getAvailableTimeSlots(selectedDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier Principal") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simple date selector instead of external calendar
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sélectionner une date",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { selectedDate = selectedDate.minusDays(1) }
                        ) {
                            Text("← Précédent")
                        }

                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.FRENCH)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        TextButton(
                            onClick = { selectedDate = selectedDate.plusDays(1) }
                        ) {
                            Text("Suivant →")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick date selection buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { selectedDate = LocalDate.now() }
                        ) {
                            Text("Aujourd'hui")
                        }
                        TextButton(
                            onClick = { selectedDate = LocalDate.now().plusDays(1) }
                        ) {
                            Text("Demain")
                        }
                        TextButton(
                            onClick = { selectedDate = LocalDate.now().plusDays(7) }
                        ) {
                            Text("Dans 7j")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Disponibilités pour le ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH))}:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (timeSlotsForSelectedDate.isEmpty()) {
                Text("Aucun créneau disponible pour cette date.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeSlotsForSelectedDate) { slot ->
                        TimeSlotItem(slot = slot, onClick = { /* Optionally navigate to booking or show details */ })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(navController: NavController) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var selectedEndTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var selectedTerrain by remember { mutableStateOf("Football") }
    var showUserDialog by remember { mutableStateOf(false) }

    val users = DataManager.getUsers()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle Réservation") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sélection utilisateur
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (users.isNotEmpty()) showUserDialog = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("👤 Utilisateur", fontWeight = FontWeight.Bold)
                    if (users.isEmpty()) {
                        Text("Aucun utilisateur - Ajoutez d'abord un utilisateur", color = Color.Red)
                    } else {
                        Text(selectedUser?.name ?: "Sélectionner un utilisateur")
                    }
                }
            }

            // Sélection date
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("📅 Date", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { selectedDate = selectedDate.minusDays(1) }
                        ) {
                            Text("← Précédent")
                        }

                        Text(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))

                        TextButton(
                            onClick = { selectedDate = selectedDate.plusDays(1) }
                        ) {
                            Text("Suivant →")
                        }
                    }
                }
            }

            // Sélection horaires
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🕘 Heure début", fontWeight = FontWeight.Bold)
                        Text("$selectedStartTime", fontSize = 18.sp)
                        Row {
                            TextButton(onClick = {
                                if (selectedStartTime.hour > 8) {
                                    selectedStartTime = selectedStartTime.minusHours(1)
                                    if (selectedEndTime <= selectedStartTime) {
                                        selectedEndTime = selectedStartTime.plusHours(1)
                                    }
                                }
                            }) {
                                Text("−", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                if (selectedStartTime.hour < 21) {
                                    selectedStartTime = selectedStartTime.plusHours(1)
                                    if (selectedEndTime <= selectedStartTime) {
                                        selectedEndTime = selectedStartTime.plusHours(1)
                                    }
                                }
                            }) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🕘 Heure fin", fontWeight = FontWeight.Bold)
                        Text("$selectedEndTime", fontSize = 18.sp)
                        Row {
                            TextButton(onClick = {
                                if (selectedEndTime.hour > selectedStartTime.hour + 1) {
                                    selectedEndTime = selectedEndTime.minusHours(1)
                                }
                            }) {
                                Text("−", fontSize = 20.sp)
                            }
                            TextButton(onClick = {
                                if (selectedEndTime.hour < 22) {
                                    selectedEndTime = selectedEndTime.plusHours(1)
                                }
                            }) {
                                Text("+", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // Sélection type de terrain
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("🏟️ Type de terrain", fontWeight = FontWeight.Bold)
                    DataManager.terrainTypes.forEach { terrain ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTerrain == terrain,
                                onClick = { selectedTerrain = terrain }
                            )
                            Text(terrain)
                        }
                    }
                }
            }

            // Vérification de disponibilité
            if (selectedUser != null) {
                val isAvailable = DataManager.isTimeSlotAvailable(selectedDate, selectedStartTime, selectedEndTime)
                if (!isAvailable) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            "❌ Créneau non disponible - Conflit avec une autre réservation",
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Bouton de réservation
            Button(
                onClick = {
                    selectedUser?.let { user ->
                        if (DataManager.isTimeSlotAvailable(selectedDate, selectedStartTime, selectedEndTime)) {
                            val reservation = Reservation(
                                userId = user.id,
                                userName = user.name,
                                date = selectedDate,
                                startTime = selectedStartTime,
                                endTime = selectedEndTime,
                                terrainType = selectedTerrain
                            )
                            DataManager.addReservation(reservation)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedUser != null &&
                        DataManager.isTimeSlotAvailable(selectedDate, selectedStartTime, selectedEndTime)
            ) {
                Text("✅ Confirmer la Réservation", fontSize = 16.sp)
            }
        }

        if (showUserDialog) {
            AlertDialog(
                onDismissRequest = { showUserDialog = false },
                title = { Text("Sélectionner un utilisateur") },
                text = {
                    LazyColumn {
                        items(users) { user ->
                            TextButton(
                                onClick = {
                                    selectedUser = user
                                    showUserDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold)
                                    Text(user.email, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showUserDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotItem(slot: TimeSlot, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = slot.isAvailable, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isAvailable)
                Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${slot.startTime} - ${slot.endTime}",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                if (slot.isAvailable) "✅ Disponible" else "❌ Réservé",
                color = if (slot.isAvailable)
                    Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(navController: NavController) {
    val reservations = DataManager.getReservations()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toutes les Réservations") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (reservations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📋 Aucune réservation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Créez votre première réservation !")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reservations.sortedBy { it.date }) { reservation ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "👤 ${reservation.userName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("🏟️ Terrain: ${reservation.terrainType}")
                            Text("📅 Date: ${reservation.date.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.FRENCH))}")
                            Text("🕘 Horaire: ${reservation.startTime} - ${reservation.endTime}")
                        }
                    }
                }
            }
        }
    }
}