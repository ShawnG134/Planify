package note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import io.ktor.client.request.*
import io.ktor.http.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.exposed.sql.*
import io.ktor.client.engine.cio.*
import io.ktor.client.HttpClient
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table


object Table__File : Table() {
    val id = integer("id").autoIncrement()
    var name = varchar("name", 255)
    var content = text("content")
    val folderName = varchar("folderName", 255)
    val folderID = integer("folderID")
    val marked = bool("isStared")
    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}

object Folders__Table : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val parentID = integer("parentID")
    val parentFolder = varchar("parentName", 255)
    val marked = bool("isStared")
    override val primaryKey = PrimaryKey(id, name = "PK_User_ID")
}

data class FileItem(
    val id: Int, var name: String, var content: String, var folder: String, var marked: Boolean
)


@Serializable
data class FileItemJson(
    val id: Int, var name: String, var content: String, var folder: String, var marked: Boolean
)


data class FolderItem(
    val id: Int, var name: String, var parentFolderName: String, var marked: Boolean
)

@Serializable
data class FolderItemJson(
    val id: Int, var name: String, var parentFolderName: String, var marked: Boolean
)


fun FileItem.ToFileJson(): FileItemJson {
    return FileItemJson(
        id = this.id, name = this.name, content = this.content, folder = this.folder, marked = this.marked
    )
}

fun FolderItem.ToFolderJson(): FolderItemJson {
    return FolderItemJson(
        id = this.id, name = this.name, parentFolderName = this.parentFolderName, marked = this.marked
    )
}

@Serializable
data class FileNamePara(val name: String, val folderName: String, val content: String)


@OptIn(InternalAPI::class)
suspend fun create(notes: FileItem) {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.post("http://localhost:8080/notes") {
        contentType(ContentType.Application.Json)
        println(Json.encodeToString(notes.ToFileJson()))
        body = Json.encodeToString(notes.ToFileJson())
    }
}

@OptIn(InternalAPI::class)
suspend fun create(notes: FolderItem) {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.post("http://localhost:8080/Folder") {
        contentType(ContentType.Application.Json)
        println(Json.encodeToString(notes.ToFolderJson()))
        body = Json.encodeToString(notes.ToFolderJson())
    }
}


@OptIn(InternalAPI::class)
suspend fun updateFileContent(updateRequest: FileNamePara) {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.post("http://localhost:8080/updateFileContent") {
        contentType(ContentType.Application.Json)
        body = Json.encodeToString(updateRequest)
    }
}

@OptIn(InternalAPI::class)
suspend fun deleteFolder(todoId: String) {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.delete("http://localhost:8080/folder/$todoId") {
        contentType(ContentType.Application.Json)
    }
    client.close()
}

@OptIn(InternalAPI::class)
suspend fun deleteNotes(todoId: String) {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.delete("http://localhost:8080/notes/$todoId") {
        contentType(ContentType.Application.Json)
    }
    client.close()
}

@OptIn(InternalAPI::class)
suspend fun fetchnotes(): List<FileItemJson> {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("http://localhost:8080/notes_name")
    val jsonString = response.bodyAsText()
    client.close()
    return Json.decodeFromString(jsonString)
}

@OptIn(InternalAPI::class)
suspend fun fetchFolder(): List<FolderItemJson> {
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("http://localhost:8080/folder_name")
    val jsonString = response.bodyAsText()
    client.close()
    return Json.decodeFromString(jsonString)
}


@Composable
fun CreateFileDialog(folder: String, onCreate: (FileItem) -> Unit, onClose: () -> Unit) {
    //Database.connect("jdbc:sqlite:chinook.db")
    var name by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = { /* dismiss dialog */ }, title = {
        Text(text = "Create File")
    }, text = {
        Column {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name the File") })
        }
    }, confirmButton = {
        Button(onClick = {
            if (name.isNotEmpty()) {
                onCreate(FileItem(id = 0, name = name, content = "", folder = folder, marked = false))
            }
        }) {
            Text("Create")
        }
    }, dismissButton = {
        Button(onClick = {
            onClose()
        }) {
            Text("Close")
        }
    })
}

@Composable
fun CreateFolderDialog(parentFolder: String, onCreate: (FolderItem) -> Unit, onClose: () -> Unit) {
    //Database.connect("jdbc:sqlite:chinook.db")
    var name by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = { /* dismiss dialog */ }, title = {
        Text(text = "Create Folder")
    }, text = {
        Column {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name the Folder") })
        }
    }, confirmButton = {
        Button(onClick = {
            if (name.isNotEmpty()) {
                onCreate(FolderItem(id = 0, name = name, parentFolderName = parentFolder, marked = false))
            }
        }) {
            Text("Create")
        }
    }, dismissButton = {
        Button(onClick = {
            onClose()
        }) {
            Text("Close")
        }
    })
}

data class Result(
    val firstValue: Boolean, val secondValue: List<String>, val thirdValue: String, val fourthValue: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun NoteList(state: RichTextState): Result {
    Database.connect("jdbc:sqlite:chinook.db")
    var idList = remember { mutableStateListOf<String>() }
    val folderList = remember { mutableStateListOf<String>() }
    val folderPath = remember { mutableStateListOf<String>("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isFolderDialogOpen by remember { mutableStateOf(false) }
    var isSaveDialogOpen by remember { mutableStateOf(false) }
    var isDupDialogOpen by remember { mutableStateOf(false) }
    var isDelDialogOpen by remember { mutableStateOf(false) }
    var canIdelete by remember { mutableStateOf(false) }
    val selectedNoteIndex = remember { mutableStateOf<Int>(-1) }
    val selectedFolderIndex = remember { mutableStateOf<Int>(-1) }
    val numItemInFolder = remember { mutableStateOf<Int?>(0) }
    var isFile by remember { mutableStateOf(false) }
    val (currentFolder, setCurrentFolder) = remember { mutableStateOf("") }
    val (selectedFile, setSelectedFile) = remember { mutableStateOf("") }

    print(folderPath.size)

    idList.clear()
    runBlocking {
        var result: List<FileItemJson>
        launch {
            result = fetchnotes()
            result.forEach { jsonItem ->
                if (jsonItem.folder == currentFolder) {
                    idList.add(jsonItem.name)
                }
            }
        }
    }
    folderList.clear()
    runBlocking {
        var result: List<FolderItemJson>
        launch {
            result = fetchFolder()
            result.forEach { jsonItem ->
                if (jsonItem.parentFolderName == currentFolder) {
                    folderList.add(jsonItem.name)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        FlowRow(modifier = Modifier.align(Alignment.TopCenter)) {
            // add file button
            FloatingActionButton(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    isFile = if (selectedNoteIndex.value != -1) {
                        true
                    } else {
                        false
                    }
                    isDialogOpen = true
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                Icon(Icons.Filled.Edit, "Localized description")
            }

            // add folder button
            FloatingActionButton(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    isFile = false
                    isFolderDialogOpen = true
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }

            // add save button
            FloatingActionButton(
                modifier = Modifier.padding(5.dp),
                onClick = {

                    if (selectedNoteIndex.value != -1) {
                        isSaveDialogOpen = true
                        runBlocking {
                            launch {
                                val updateRequest = updateFileContent(
                                    FileNamePara(
                                        idList[selectedNoteIndex.value], currentFolder, state.toHtml()
                                    )
                                )

                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                Text("SAVE")
            }

            // 4. click to back
            FloatingActionButton(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    selectedFolderIndex.value = -1
                    selectedNoteIndex.value = -1
                    isFile = false

                    if (folderPath.size > 1) {
                        folderPath.removeLast()
                    }
                    setCurrentFolder(folderPath.last())
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                Text("Back", modifier = Modifier.padding(2.dp))
            }

            // add delete button
            FloatingActionButton(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    var result_notes: List<FileItemJson>
                    var result_folder: List<FolderItemJson>
                    runBlocking {
//                        result_notes = fetchnotes()
//                        result_folder = fetchFolder()
//                        if (( result_notes.count() != 0 ) and (selectedNoteIndex.value != -1)) {
//                            deleteNotes(idList[selectedNoteIndex.value])
//                            println(idList[selectedNoteIndex.value])
//                            idList.remove(idList[selectedNoteIndex.value])
//                        }
//                        if ((result_folder.count() != 0) and (selectedFolderIndex.value != -1)) {
//                            deleteFolder(folderList[selectedFolderIndex.value])
//                            deleteNotes(folderList[selectedFolderIndex.value])
//                            println("Running Delete")
//                            println(folderList[selectedFolderIndex.value])
//                            println(selectedFolderIndex.value)
//                            folderList.remove(folderList[selectedFolderIndex.value])
//                        }
                    }
                    selectedNoteIndex.value = -1
                    selectedFolderIndex.value = -1
                    isFile = false
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                Text("DEL")
            }
            // 6. show current folder
            FloatingActionButton(
                modifier = Modifier.padding(5.dp).size(width = 200.dp, height = 55.dp),
                onClick = {

                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 10.dp
                )
            ) {
                var str = ""
                if (currentFolder == "") {
                    str = "Root"
                } else {
                    str = currentFolder
                }
                Text("Folder: " + str)
            }

        }

        Divider(modifier = Modifier.padding(vertical = 138.dp), thickness = 2.dp)

        LazyColumn(modifier = Modifier.padding(top = 150.dp)) {
            // list files
            items(idList.size) {
                val backgroundColor =
                    if (it == selectedNoteIndex.value) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                val fontWeight = if (it == selectedNoteIndex.value) FontWeight.Bold else FontWeight.Normal

                ElevatedCard(
                    // shadow
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(modifier = Modifier.clickable {
                            selectedNoteIndex.value = it
                            setSelectedFile(idList[it])
                            selectedFolderIndex.value = -1
                            isFile = true

                            runBlocking {
                                var result: List<FileItemJson>
                                launch {
                                    result = fetchnotes()
                                    result.forEach { JsonItem ->
                                        if (JsonItem.name == idList[it]) {
                                            state.setHtml(JsonItem.content)
                                            println(JsonItem.content)
                                        }
                                    }
                                }
                            }
                        }.fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                        Icon(Icons.Filled.Edit, // icon image
                            contentDescription = "A Pen",
                            modifier = Modifier.clickable { }.align(Alignment.CenterVertically)
                                .padding(horizontal = 10.dp))

                        Text(
                            text = idList[it],
                            fontWeight = fontWeight,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item { Divider(modifier = Modifier.padding(vertical = 10.dp), thickness = 2.dp) }

            // list folder
            items(folderList.size) { num ->
                val backgroundColor =
                    if (num == selectedFolderIndex.value) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                val fontWeight = if (num == selectedFolderIndex.value) FontWeight.Bold else FontWeight.Normal

                ElevatedCard(
                    // shadow
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    var isHovered by remember { mutableStateOf(false) }
                    val TransparentLightGray = Color(0xFFCCCCCC).copy(alpha = 0.3f)
                    val bc = if (isHovered) TransparentLightGray else Color.Transparent
                    Row(modifier = Modifier.combinedClickable(onClick = {
                            selectedFolderIndex.value = num
                            selectedNoteIndex.value = -1
                            isFile = false
                        }, onDoubleClick = {
                            isFile = false
                            selectedFolderIndex.value = -1
                            selectedNoteIndex.value = -1
                            setCurrentFolder(folderList[num])
                            folderPath.add(folderList[num])
                        }, onLongClick = {}).fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                        Icon(Icons.Filled.Menu, // icon image
                            contentDescription = "A Pen",
                            modifier = Modifier.clickable { }.align(Alignment.CenterVertically)
                                .padding(horizontal = 10.dp))

                        Text(
                            text = folderList[num],
                            fontWeight = fontWeight,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (isDialogOpen) {
            CreateFileDialog(folder = currentFolder, onCreate = { newItem ->
                isDialogOpen = false
                print("newItem.name in idList:")
                print(newItem.name in idList)

                if (newItem.name in idList) {
                    isDupDialogOpen = true
                } else {
                    runBlocking {
                        launch {
                            create(
                                FileItem(
                                    name = newItem.name,
                                    content = newItem.content,
                                    folder = newItem.folder,
                                    id = 0,
                                    marked = false
                                )
                            )
                        }
                    }
//

                }
            }, onClose = { isDialogOpen = false })
        }

        if (isFolderDialogOpen) {
            CreateFolderDialog(parentFolder = currentFolder, onCreate = { newItem ->
                isFolderDialogOpen = false

                if (newItem.name in folderList) {
                    isDupDialogOpen = true
                } else {
                    runBlocking {
                        launch {
                            create(FolderItem(
                                name = newItem.name,
                                parentFolderName = newItem.parentFolderName,
                                marked = false,
                                id = 0
                            )
                            )
                        }
                    }
                }

            }, onClose = { isFolderDialogOpen = false })
        }

        if (isSaveDialogOpen) {
            var result = false
            AlertDialog(onDismissRequest = { /* dismiss dialog */ }, title = {
                Text(text = "Save Successfully")
            }, confirmButton = {
                Button(onClick = { isSaveDialogOpen = false }) {
                    Text("Confirm")
                }
            })
        }

        if (isDupDialogOpen) {
            var result = false
            AlertDialog(onDismissRequest = { /* dismiss dialog */ }, title = {
                Text(text = "Duplicated Name")
            }, confirmButton = {
                Button(onClick = { isDupDialogOpen = false }) {
                    Text("Confirm")
                }
            })
        }

        if (isDelDialogOpen) {
            var result = false
            AlertDialog(onDismissRequest = { /* dismiss dialog */ }, title = {
                Text(text = "Save Successfully")
            }, confirmButton = {
                Button(onClick = {
                    isDelDialogOpen = false
                    canIdelete = true
                }) {
                    Text("Delete")
                }
                Button(onClick = {
                    isDelDialogOpen = false
                    canIdelete = false
                }) {
                    Text("Cancel")
                }
            }
            )
        }

    }
    return Result(isFile, folderPath, selectedFile, currentFolder)
}


