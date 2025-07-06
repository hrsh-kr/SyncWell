package com.example.syncwell.ui.medicines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.R
import com.example.syncwell.data.local.entities.Medicine
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToEditMedicine: (String) -> Unit,
    onNavigateBack: () -> Unit,
    medicineViewModel: MedicineViewModel = hiltViewModel()
) {
    val medicineState by medicineViewModel.medicineState.collectAsState()
    
    // Refresh medicines when screen loads
    LaunchedEffect(Unit) {
        medicineViewModel.refreshMedicines()
    }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = "Medications",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { medicineViewModel.refreshMedicines() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Medicine",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        when (medicineState) {
            is MedicineViewModel.MedicineState.Loading -> {
                FullScreenLoading()
            }
            is MedicineViewModel.MedicineState.Success -> {
                val medicines = (medicineState as MedicineViewModel.MedicineState.Success).medicines
                if (medicines.isEmpty()) {
                    EmptyMedicineList(padding = innerPadding)
                } else {
                    MedicineList(
                        padding = innerPadding,
                        medicines = medicines,
                        onTakeMedicine = { medicineViewModel.takeMedicine(it) },
                        onEditMedicine = { 
                            medicineViewModel.selectMedicine(it)
                            onNavigateToEditMedicine(it.id)
                        },
                        onDeleteMedicine = { medicineViewModel.deleteMedicine(it) }
                    )
                }
            }
            is MedicineViewModel.MedicineState.Error -> {
                ErrorView(
                    message = (medicineState as MedicineViewModel.MedicineState.Error).message,
                    padding = innerPadding,
                    onRetry = { medicineViewModel.refreshMedicines() }
                )
            }
        }
    }
}

@Composable
private fun MedicineList(
    padding: PaddingValues,
    medicines: List<Medicine>,
    onTakeMedicine: (Medicine) -> Unit,
    onEditMedicine: (Medicine) -> Unit,
    onDeleteMedicine: (Medicine) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(medicines) { medicine ->
            MedicineItem(
                medicine = medicine,
                onTakeMedicine = { onTakeMedicine(medicine) },
                onEditMedicine = { onEditMedicine(medicine) },
                onDeleteMedicine = { onDeleteMedicine(medicine) }
            )
        }
    }
}

@Composable
private fun MedicineItem(
    medicine: Medicine,
    onTakeMedicine: () -> Unit,
    onEditMedicine: () -> Unit,
    onDeleteMedicine: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_medication),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = medicine.dosage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Scheduled: ${formatTime(medicine.timeMillis)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (medicine.lastTaken > 0) {
                        Text(
                            text = "Last taken: ${formatDateTime(medicine.lastTaken)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(onClick = onTakeMedicine) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark as Taken",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditMedicine) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDeleteMedicine) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMedicineList(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_medication),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No medications yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the + button to add your first medication",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    padding: PaddingValues,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorMessage(message = message)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IconButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Helper functions for date/time formatting
private fun formatTime(timeMillis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timeMillis))
}

private fun formatDateTime(timeMillis: Long): String {
    return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timeMillis))
} 