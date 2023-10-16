package summary


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HabitSelection(selectedSection: String, onSelectedSectionChanged: (String) -> Unit) {
    val items = listOf("All", "Habit 1", "Habit 2", "Habit 3", "Habit 4")
    var expanded by remember { mutableStateOf(false) }

    Box {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                ) {
                    Text(selectedSection)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                onSelectedSectionChanged(item)
                                expanded = false
                            }
                        ) {
                            Text(item)
                        }
                    }
                }
            }
        }
    }
}