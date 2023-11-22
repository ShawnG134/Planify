package note

import DatabaseManager
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import uploadDatabaseToCloud


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
    val id: Int,
    var name: String,
    var content: String,
    var folder: String
)

data class FolderItem(
    val id: Int,
    var name: String,
    var parentFolderName: String
)

@Composable
fun CreateFileDialog(folder: String, onCreate: (FileItem) -> Unit, onClose: () -> Unit) {
    //Database.connect("jdbc:sqlite:chinook.db")
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* dismiss dialog */ },
        title = {
            Text(text = "Create File")
        },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name the File") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onCreate(FileItem(id = 0, name = name, content = "", folder = folder))
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onClose()
                }
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CreateFolderDialog(parentFolder: String, onCreate: (FolderItem) -> Unit, onClose: () -> Unit) {
    //Database.connect("jdbc:sqlite:chinook.db")
    val manager = DatabaseManager()
    val db = manager.setupDatabase()
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* dismiss dialog */ },
        title = {
            Text(text = "Create Folder")
        },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name the Folder") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onCreate(FolderItem(id = 0, name = name, parentFolderName = parentFolder))
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onClose()
                }
            ) {
                Text("Close")
            }
        }
    )
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun NoteList(state: RichTextState): Pair<Boolean, List<String>> {
    val manager = DatabaseManager()
    val db = manager.setupDatabase()
    transaction {
        SchemaUtils.create(Table__File)
        SchemaUtils.create(Folders__Table)
    }
    val idList = remember { mutableStateListOf<String>() }
    val folderList = remember { mutableStateListOf<String>() }
    val folderPath = remember { mutableStateListOf<String>("") }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isFolderDialogOpen by remember { mutableStateOf(false) }
    var isSaveDialogOpen by remember { mutableStateOf(false) }
    var isDelDialogOpen by remember { mutableStateOf(false) }
    var canIdelete by remember { mutableStateOf(false) }
    val selectedNoteIndex = remember { mutableStateOf<Int>(-1) }
    val selectedFolderIndex = remember { mutableStateOf<Int>(-1) }
    val numItemInFolder = remember { mutableStateOf<Int?>(0) }
    var isFile by remember { mutableStateOf(false) }
    val (currentFolder, setCurrentFolder) = remember { mutableStateOf("") }

    print(folderPath.size)

    idList.clear()
    transaction {
        Table__File.selectAll().forEach {
            if (it[Table__File.folderName] == currentFolder) {
                idList.add(it[Table__File.name])
            }
        }
    }

    folderList.clear()
    transaction {
        print(Folders__Table.selectAll().count())
        Folders__Table.selectAll().forEach {
            if (it[Folders__Table.parentFolder] == currentFolder) {
                folderList.add(it[Folders__Table.name])
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        FlowRow(modifier = Modifier.align(Alignment.TopCenter)) {
            // add file button
            FloatingActionButton(
                modifier = Modifier
                    .padding(5.dp),
                onClick = {
                    isFile = true
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
                modifier = Modifier
                    .padding(5.dp),
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
                modifier = Modifier
                    .padding(5.dp),
                onClick = {
                    if (selectedNoteIndex.value != -1) {
                        isSaveDialogOpen = true
                        transaction {
                            Table__File.update({
                                (Table__File.name eq idList[selectedNoteIndex.value]) and
                                        (Table__File.folderName eq currentFolder)
                            }) {
                                it[content] = state.toHtml()
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
                modifier = Modifier
                    .padding(5.dp),
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
                modifier = Modifier
                    .padding(5.dp),
                onClick = {
                    transaction {
                        // delete a file
                        if ((Table__File.selectAll().count().toInt() != 0) and (selectedNoteIndex.value != -1)) {
                            Table__File.deleteWhere {
                                Table__File.name eq idList[selectedNoteIndex.value]
                            }
                            idList.clear()
                            transaction {
                                Table__File.selectAll().forEach {
                                    if (it[Table__File.folderName] == currentFolder) {
                                        idList.add(it[Table__File.name])
                                    }
                                }
                            }
                        }

                        // delete a folder
                        if ((Folders__Table.selectAll().count().toInt() != 0) and (selectedFolderIndex.value != -1)) {
                            Folders__Table.deleteWhere {
                                Folders__Table.name eq folderList[selectedFolderIndex.value]
                            }
                            Table__File.deleteWhere {
                                Table__File.folderName eq folderList[selectedFolderIndex.value]
                            }

                            folderList.clear()
                            transaction {
                                Folders__Table.selectAll().forEach {
                                    if (it[Folders__Table.parentFolder] == currentFolder) {
                                        folderList.add(it[Folders__Table.name])
                                    }
                                }
                            }
                        }
                    }
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
                modifier = Modifier
                    .padding(5.dp)
                    .size(width = 200.dp, height = 55.dp),
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
                    Row(modifier = Modifier
                        .clickable {
                            selectedNoteIndex.value = it
                            selectedFolderIndex.value = -1
                            isFile = true
                            transaction {
                                Table__File.select { Table__File.name eq idList[it] }.forEach {
                                    state.setHtml(it[Table__File.content])
                                }
                            }

                        }
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Filled.Edit, // icon image
                            contentDescription = "A Pen",
                            modifier = Modifier
                                .clickable { }
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 10.dp)
                        )

                        Text(
                            text = idList[it],
                            fontWeight = fontWeight,
                            modifier = Modifier
                                .padding(16.dp),
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
                    Row(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    selectedFolderIndex.value = num
                                    selectedNoteIndex.value = -1
                                    isFile = false
                                },
                                onDoubleClick = {
                                    isFile = false
                                    selectedFolderIndex.value = -1
                                    selectedNoteIndex.value = -1
                                    setCurrentFolder(folderList[num])
                                    folderPath.add(folderList[num])
                                },
                                onLongClick = {}
                            )
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Filled.Menu, // icon image
                            contentDescription = "A Pen",
                            modifier = Modifier
                                .clickable { }
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 10.dp)
                        )

                        Text(
                            text = folderList[num],
                            fontWeight = fontWeight,
                            modifier = Modifier
                                .padding(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (isDialogOpen) {
            CreateFileDialog(
                folder = currentFolder,
                onCreate = { newItem ->
                    isDialogOpen = false
                    transaction {
                        val newId = Table__File.insert {
                            it[name] = newItem.name
                            it[content] = newItem.content
                            it[folderName] = newItem.folder
                            it[folderID] = 0
                            it[marked] = false
                        }
                        print(Table__File.selectAll().count())
                    }
                },
                onClose = { isDialogOpen = false }
            )
        }

        if (isFolderDialogOpen) {
            CreateFolderDialog(
                parentFolder = currentFolder,
                onCreate = { newItem ->
                    isFolderDialogOpen = false
                    transaction {
                        val newId = Folders__Table.insert {
                            it[name] = newItem.name
                            it[parentFolder] = newItem.parentFolderName
                            it[parentID] = 0
                            it[marked] = false
                        }
                        print(Folders__Table.selectAll().count())
                    }
                },
                onClose = { isDialogOpen = false }
            )
        }

        if (isSaveDialogOpen) {
            var result = false
            AlertDialog(
                onDismissRequest = { /* dismiss dialog */ },
                title = {
                    Text(text = "Save Successfully")
                },
                confirmButton = {
                    Button(onClick = { isSaveDialogOpen = false }) {
                        Text("Confirm")
                    }
                }
            )
        }

        if (isDelDialogOpen) {
            var result = false
            AlertDialog(
                onDismissRequest = { /* dismiss dialog */ },
                title = {
                    Text(text = "Save Successfully")
                },
                confirmButton = {
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
    return Pair(isFile, folderPath)
}