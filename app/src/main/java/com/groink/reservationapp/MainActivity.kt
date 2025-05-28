package com.groink.reservationapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.groink.reservationapp.ui.theme.ReservationAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class TimeSlot(
    val hour: String,
    val isAvailable: Boolean,
    val isSelected: Boolean = false
)

data class TerrainInfo(
    val name: String = "Terrain Polyvalent",
    val surface: String? = null,
    val revetement: String? = null,
    val eclairage: Boolean? = null,
    val tarif: String? = null,
    val description: String? = null
)

data class Reservation(
    val startTime: String,
    val duration: Int // en minutes (30, 60, 90, 120, etc.)
)

data class DayInfo(
    val date: LocalDate,
    val dayName: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservationAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReservationApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationApp() {
    var currentScreen by remember { mutableStateOf("calendar") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTimeSlot by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf(60) } // durée en minutes
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var userName by remember { mutableStateOf("Utilisateur") }
    var isAdmin by remember { mutableStateOf(false) } // Rôle admin
    var terrainInfo by remember { mutableStateOf(TerrainInfo()) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when(currentScreen) {
                            "calendar" -> "Réservation Terrain"
                            "profile" -> "Mon Profil"
                            "admin" -> "Administration"
                            else -> "Terrain"
                        }
                    )
                },
                actions = {
                    if (isAdmin && currentScreen != "admin") {
                        IconButton(onClick = { currentScreen = "admin" }) {
                            Icon(Icons.Default.Settings, contentDescription = "Administration")
                        }
                    }
                    IconButton(onClick = {
                        currentScreen = when(currentScreen) {
                            "profile" -> "calendar"
                            "admin" -> "calendar"
                            else -> "profile"
                        }
                    }) {
                        Icon(
                            when(currentScreen) {
                                "profile", "admin" -> Icons.Default.DateRange
                                else -> Icons.Default.Person
                            },
                            contentDescription = when(currentScreen) {
                                "profile", "admin" -> "Calendrier"
                                else -> "Profil"
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (currentScreen) {
            "calendar" -> {
                CalendarScreen(
                    modifier = Modifier.padding(paddingValues),
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    selectedTimeSlot = selectedTimeSlot,
                    onTimeSlotSelected = { selectedTimeSlot = it },
                    selectedDuration = selectedDuration,
                    onDurationSelected = { selectedDuration = it },
                    terrainInfo = terrainInfo
                )
            }
            "profile" -> {
                ProfileScreen(
                    modifier = Modifier.padding(paddingValues),
                    profileImageUri = profileImageUri,
                    userName = userName,
                    onUserNameChange = { userName = it },
                    onImagePickerClick = { imagePickerLauncher.launch("image/*") },
                    isAdmin = isAdmin,
                    onAdminToggle = { isAdmin = it }
                )
            }
            "admin" -> {
                AdminScreen(
                    modifier = Modifier.padding(paddingValues),
                    terrainInfo = terrainInfo,
                    onTerrainInfoChange = { terrainInfo = it }
                )
            }
        }
    }
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    selectedTimeSlot: String?,
    onTimeSlotSelected: (String?) -> Unit,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    terrainInfo: TerrainInfo
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Terrain
        item {
            TerrainInfoCard(terrainInfo)
        }

        // Section Calendrier
        item {
            Text(
                text = "Choisir une date",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            WeekCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }

        // Section Créneaux horaires
        item {
            Text(
                text = "Créneaux disponibles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            TimeSlotGrid(
                selectedTimeSlot = selectedTimeSlot,
                onTimeSlotSelected = onTimeSlotSelected
            )
        }

        // Section Durée
        if (selectedTimeSlot != null) {
            item {
                Text(
                    text = "Durée de réservation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DurationSelector(
                    selectedDuration = selectedDuration,
                    onDurationSelected = onDurationSelected
                )
            }
        }

        // Bouton de réservation
        item {
            if (selectedTimeSlot != null) {
                val endTime = calculateEndTime(selectedTimeSlot, selectedDuration)
                Button(
                    onClick = { /* Logique de réservation */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Réserver le ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} de $selectedTimeSlot à $endTime",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TerrainInfoCard(terrainInfo: TerrainInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = terrainInfo.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (terrainInfo.surface != null || terrainInfo.revetement != null ||
                terrainInfo.eclairage != null || terrainInfo.tarif != null ||
                terrainInfo.description != null) {

                Spacer(modifier = Modifier.height(8.dp))

                val details = buildList {
                    terrainInfo.surface?.let { add("Surface : $it") }
                    terrainInfo.revetement?.let { add("Revêtement : $it") }
                    terrainInfo.eclairage?.let { if (it) add("Éclairage : Disponible") }
                    terrainInfo.tarif?.let { add("Tarif : $it") }
                    terrainInfo.description?.let { add(it) }
                }

                Text(
                    text = details.joinToString("\n") { "• $it" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val weekDays = (0..6).map { today.plusDays(it.toLong()) }

    val days = weekDays.map { date ->
        DayInfo(
            date = date,
            dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH),
            dayNumber = date.dayOfMonth.toString(),
            isSelected = date == selectedDate
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            DayItem(
                day = day,
                onClick = { onDateSelected(day.date) }
            )
        }
    }
}

@Composable
fun DayItem(
    day: DayInfo,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                if (day.isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .padding(12.dp)
    ) {
        Text(
            text = day.dayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (day.isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day.dayNumber,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (day.isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TimeSlotGrid(
    selectedTimeSlot: String?,
    onTimeSlotSelected: (String?) -> Unit
) {
    val timeSlots = listOf(
        TimeSlot("09:00", true),
        TimeSlot("10:00", true),
        TimeSlot("11:00", false),
        TimeSlot("14:00", true),
        TimeSlot("15:00", true),
        TimeSlot("16:00", true),
        TimeSlot("17:00", false),
        TimeSlot("18:00", true),
        TimeSlot("19:00", true),
        TimeSlot("20:00", true)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(timeSlots) { timeSlot ->
            TimeSlotItem(
                timeSlot = timeSlot,
                isSelected = selectedTimeSlot == timeSlot.hour,
                onClick = {
                    if (timeSlot.isAvailable) {
                        onTimeSlotSelected(
                            if (selectedTimeSlot == timeSlot.hour) null else timeSlot.hour
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TimeSlotItem(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = timeSlot.isAvailable) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                timeSlot.isAvailable -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Text(
            text = timeSlot.hour,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = when {
                isSelected -> Color.White
                timeSlot.isAvailable -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    val durations = listOf(30, 60, 90, 120, 150, 180) // en minutes

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(durations) { duration ->
            DurationItem(
                duration = duration,
                isSelected = selectedDuration == duration,
                onClick = { onDurationSelected(duration) }
            )
        }
    }
}

@Composable
fun DurationItem(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Text(
            text = formatDuration(duration),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}min"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h${minutes % 60}min"
    }
}

fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    val parts = startTime.split(":")
    val hours = parts[0].toInt()
    val minutes = parts[1].toInt()

    val totalMinutes = hours * 60 + minutes + durationMinutes
    val endHours = (totalMinutes / 60) % 24
    val endMinutes = totalMinutes % 60

    return String.format("%02d:%02d", endHours, endMinutes)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileImageUri: Uri?,
    userName: String,
    onUserNameChange: (String) -> Unit,
    onImagePickerClick: () -> Unit,
    isAdmin: Boolean,
    onAdminToggle: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Photo de profil
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImagePickerClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ajouter photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Nom d'utilisateur
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            }
        )

        // Informations du profil
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Mes informations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = "utilisateur@exemple.com"
                )

                ProfileInfoItem(
                    icon = Icons.Default.Phone,
                    label = "Téléphone",
                    value = "+33 6 12 34 56 78"
                )

                ProfileInfoItem(
                    icon = Icons.Default.DateRange,
                    label = "Membre depuis",
                    value = "Janvier 2024"
                )

                // Toggle admin pour le développement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mode Administrateur",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isAdmin,
                        onCheckedChange = onAdminToggle
                    )
                }
            }
        }

        // Boutons d'action
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Sauvegarder les modifications */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sauvegarder les modifications")
            }

            OutlinedButton(
                onClick = { /* Déconnexion */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se déconnecter")
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    modifier: Modifier = Modifier,
    terrainInfo: TerrainInfo,
    onTerrainInfoChange: (TerrainInfo) -> Unit
) {
    var nom by remember { mutableStateOf(terrainInfo.name) }
    var surface by remember { mutableStateOf(terrainInfo.surface ?: "") }
    var revetement by remember { mutableStateOf(terrainInfo.revetement ?: "") }
    var eclairage by remember { mutableStateOf(terrainInfo.eclairage ?: false) }
    var tarif by remember { mutableStateOf(terrainInfo.tarif ?: "") }
    var description by remember { mutableStateOf(terrainInfo.description ?: "") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configuration du terrain",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom du terrain") },
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = surface,
                onValueChange = { surface = it },
                label = { Text("Surface (optionnel)") },
                placeholder = { Text("ex: 400m²") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = revetement,
                onValueChange = { revetement = it },
                label = { Text("Revêtement (optionnel)") },
                placeholder = { Text("ex: Gazon synthétique") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Éclairage disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Le terrain dispose d'un éclairage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = eclairage,
                        onCheckedChange = { eclairage = it }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = tarif,
                onValueChange = { tarif = it },
                label = { Text("Tarif (optionnel)") },
                placeholder = { Text("ex: 25€/heure") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description supplémentaire (optionnel)") },
                placeholder = { Text("Informations complémentaires...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Button(
                onClick = {
                    onTerrainInfoChange(
                        TerrainInfo(
                            name = nom,
                            surface = if (surface.isBlank()) null else surface,
                            revetement = if (revetement.isBlank()) null else revetement,
                            eclairage = if (eclairage) true else null,
                            tarif = if (tarif.isBlank()) null else tarif,
                            description = if (description.isBlank()) null else description
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sauvegarder les modifications")
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aperçu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TerrainInfoCard(
                        TerrainInfo(
                            name = nom,
                            surface = if (surface.isBlank()) null else surface,
                            revetement = if (revetement.isBlank()) null else revetement,
                            eclairage = if (eclairage) true else null,
                            tarif = if (tarif.isBlank()) null else tarif,
                            description = if (description.isBlank()) null else description
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationAppPreview() {
    ReservationAppTheme {
        ReservationApp()
    }
}