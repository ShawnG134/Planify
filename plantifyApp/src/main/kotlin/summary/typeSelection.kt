package summary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportTypeSelection(selectedSection: String, onSelectedSectionChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                ) {
                    Text(selectedSection, color = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onSelectedSectionChanged("Weekly")
                            expanded = false
                        }) {
                            Text("Weekly")
                        }
                    DropdownMenuItem(
                        onClick = {
                            onSelectedSectionChanged("Monthly")
                            expanded = false
                        }) {
                            Text("Monthly")
                        }
                    DropdownMenuItem(
                        onClick = {
                            onSelectedSectionChanged("Annual")
                            expanded = false
                        }) {
                            Text("Annual")
                        }
                }
            }
        }
    }
}
