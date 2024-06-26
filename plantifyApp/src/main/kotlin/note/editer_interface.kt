package note

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import java.awt.FileDialog
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.codebot.models.*
import org.jetbrains.exposed.sql.Database

@Composable
fun Notes() {
    NotesEditor()
}

@Composable
fun EditorInterface(state: RichTextState, selectedFile: FileItem, currentFolder: String) {
    Database.connect("jdbc:sqlite:chinook.db")
    var isSaveDialogOpen by remember { mutableStateOf(false) }
    if (isSaveDialogOpen) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Save Successfully") },
            confirmButton = { Button(onClick = { isSaveDialogOpen = false }) { Text("Confirm") } })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)) {
            controlBar(state = state, fileName = selectedFile.name)

            RichTextEditor(
                state = state,
                modifier =
                    Modifier.fillMaxSize().onKeyEvent {
                        // Reference: This implementation use the API for rich text formatting
                        // in [Compose Rich Text Editor]'s documentation. I use hotkey to
                        // trigger the rich state format changing
                        if (it.type == KeyEventType.KeyDown) {
                            if (it.isMetaPressed) {
                                when (it.key) {
                                    Key.L -> {
                                        state.addParagraphStyle(
                                            ParagraphStyle(textAlign = TextAlign.Left))
                                        true
                                    }
                                    Key.E -> {
                                        state.addParagraphStyle(
                                            ParagraphStyle(textAlign = TextAlign.Center))
                                        true
                                    }
                                    Key.R -> {
                                        state.addParagraphStyle(
                                            ParagraphStyle(textAlign = TextAlign.Right))
                                        true
                                    }
                                    Key.B -> {
                                        state.toggleSpanStyle(
                                            SpanStyle(fontWeight = FontWeight.Bold))
                                        true
                                    }
                                    Key.I -> {
                                        state.toggleSpanStyle(
                                            SpanStyle(fontStyle = FontStyle.Italic))
                                        true
                                    }
                                    Key.U -> {
                                        state.toggleSpanStyle(
                                            SpanStyle(textDecoration = TextDecoration.Underline))
                                        true
                                    }
                                    Key.Minus -> {
                                        state.toggleSpanStyle(
                                            SpanStyle(textDecoration = TextDecoration.LineThrough))
                                        true
                                    }
                                    Key.T -> {
                                        state.toggleSpanStyle(SpanStyle(fontSize = 28.sp))
                                        true
                                    }
                                    Key.D -> {
                                        print("DD")
                                        state.toggleSpanStyle(SpanStyle(color = Color.Red))
                                        true
                                    }
                                    Key.Y -> {
                                        print("YYYY")
                                        state.toggleSpanStyle(SpanStyle(background = Color.Yellow))
                                        true
                                    }
                                    Key.S -> {
                                        isSaveDialogOpen = true

                                        runBlocking {
                                            launch {
                                                val updateRequest =
                                                    updateTodoItem(
                                                        selectedFile.id,
                                                        FileItem(
                                                            id = selectedFile.id,
                                                            content = state.toHtml(),
                                                            folder = "",
                                                            marked = false,
                                                            name = "a"))
                                            }
                                        }

                                        true
                                    }
                                    Key.P -> {
                                        val fileDialog =
                                            FileDialog(
                                                ComposeWindow(), "Save File", FileDialog.SAVE)
                                        fileDialog.file = "${selectedFile.name}.html"
                                        fileDialog.isVisible = true

                                        val file =
                                            fileDialog.file?.let { File(fileDialog.directory, it) }

                                        file?.let { it.writeText(state.toHtml()) }
                                        true
                                    }
                                    Key.O -> {
                                        val fileDialog =
                                            FileDialog(
                                                ComposeWindow(), "Select a File", FileDialog.LOAD)
                                        fileDialog.isVisible = true

                                        val file =
                                            fileDialog.file?.let { File(fileDialog.directory, it) }

                                        file?.let {
                                            val content = it.readText()
                                            if (file.name.endsWith("html")) {
                                                state.setHtml(content)
                                            }
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        } else false
                    })
        }
}
