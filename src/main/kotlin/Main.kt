import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import java.io.FileInputStream
import java.io.InputStream
import ui.theme.AppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import summary.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.InputSource
import java.io.File
import java.io.IOException
import androidx.compose.ui.window.*
import java.awt.Dimension
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material3.*
import androidx.compose.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.*

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.res.*

//import androidx.compose.ui.input.key.Key.Companion.R

import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
import ui.theme.md_theme_light_background
import note.*
// Sample Composable functions for each section

import note.*

@Composable
fun BoxItem(color: Color, text: String) {
    Box(modifier = Modifier.size(200.dp, 200.dp).background(color)) {
        Text(text)
    }
}

@Composable
fun MagicHome() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        items(4) { index ->
            when (index) {
                0 -> HomeSummary()

                1 -> homeCalendar()
//                2 -> BoxItem(Color.Blue, "Todo section")
//                3 -> BoxItem(Color.Green, "Notes section")
            }
        }
    }
}

@Composable
fun Notes() {
    NotesEditor()
}

@Composable
fun AppLayout() {
    val (selectedSection, setSelectedSection) = remember { mutableStateOf("Section 1") }
    Row() {
        Column(
            verticalArrangement = Arrangement.SpaceBetween, // Controls vertical arrangement
            horizontalAlignment = Alignment.CenterHorizontally, // Controls horizontal alignment
            modifier = Modifier.fillMaxHeight()

        ) {
            val commonButtonModifier = Modifier
                .weight(1f)
                .padding(14.dp)
                .size(width = 150.dp, height = 1000.dp)
            Button(
                onClick = { setSelectedSection("garbage") }, modifier = commonButtonModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource("home.svg"),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home")
                }
            }
            FilledTonalButton(
                onClick = { setSelectedSection("Calendar") }, modifier = commonButtonModifier
            ) {
                Text(
                    text = "Calendar"
                )
            }
            FilledTonalButton(
                onClick = { setSelectedSection("Summary") }, modifier = commonButtonModifier
            ) {
                Text("Summary")
            }
            FilledTonalButton(
                onClick = { setSelectedSection("To-Do-List") }, modifier = commonButtonModifier
            ) {
                Text("To-Do-List")
            }
            FilledTonalButton(
                onClick = { setSelectedSection("Notes") }, modifier = commonButtonModifier
            ) {
                Text("Notes")
            }

        }
        Box(
            modifier = Modifier.weight(3f).fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            when (selectedSection) {
                "Home" -> MagicHome()
                "Calendar" -> Calendar()
                "Summary" -> Summary()
                "To-Do-List" -> ToDoList()
                "Notes" -> Notes()
                else -> MagicHome()
            }
        }
    }
}

fun main() = application {
    val window = Window(
        onCloseRequest = ::exitApplication, title = "My Journal"
    ) {
        window.minimumSize = Dimension(1300, 800)
        AppTheme {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                AppLayout()
            }
        }
    }
}
