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
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var userName by remember { mutableStateOf("Utilisateur") }

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
                            else -> "Terrain"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = {
                        currentScreen = if (currentScreen == "profile") "calendar" else "profile"
                    }) {
                        Icon(
                            if (currentScreen == "profile") Icons.Default.DateRange else Icons.Default.Person,
                            contentDescription = if (currentScreen == "profile") "Calendrier" else "Profil"
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
                    onTimeSlotSelected = { selectedTimeSlot = it }
                )
            }
            "profile" -> {
                ProfileScreen(
                    modifier = Modifier.padding(paddingValues),
                    profileImageUri = profileImageUri,
                    userName = userName,
                    onUserNameChange = { userName = it },
                    onImagePickerClick = { imagePickerLauncher.launch("image/*") }
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
    onTimeSlotSelected: (String?) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Terrain
        item {
            TerrainInfoCard()
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

        // Bouton de réservation
        item {
            if (selectedTimeSlot != null) {
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
                        text = "Réserver le ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} à $selectedTimeSlot",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TerrainInfoCard() {
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
                    text = "Terrain Polyvalent",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Surface : 400m²\n• Revêtement : Gazon synthétique\n• Éclairage : Disponible\n• Tarif : 25€/heure",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileImageUri: Uri?,
    userName: String,
    onUserNameChange: (String) -> Unit,
    onImagePickerClick: () -> Unit
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

@Preview(showBackground = true)
@Composable
fun ReservationAppPreview() {
    ReservationAppTheme {
        ReservationApp()
    }
}