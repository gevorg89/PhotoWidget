package com.gevorg89.photowidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gevorg89.photowidget.ui.theme.PhotoWidgetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val photoViewModel: PhotoViewModel by viewModels()

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Uri? = result.data?.data
                data?.let {
                    CoroutineScope(Dispatchers.Main.immediate).launch {
                        val file = it.toFileFromUri(
                            this@MainActivity,
                            photoViewModel.selectedWidgetId.toString()
                        )
                        photoViewModel.addPhoto(file)
                        this@MainActivity.updateWidget()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhotoWidgetTheme {
                val photoSize = 160.dp
                Surface(color = MaterialTheme.colors.background) {
                    val state by photoViewModel.state.collectAsState()
                    when (state) {
                        PhotoViewModel.State.Loaded -> {
                            LoadedWidgets(photoSize)
                        }
                        PhotoViewModel.State.Loading -> {
                            LoadingWidgets()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getWidgets()
    }

    private fun getWidgets() {
        val widgetManager = AppWidgetManager.getInstance(this@MainActivity)
            .getAppWidgetIds(ComponentName(this@MainActivity, PhotoWidget::class.java))
        val widgetFiles = widgetManager.map {
            PhotoViewModel.PhotoWidget(
                id = it,
                file = it.toString().file(this@MainActivity)
            )
        }
        photoViewModel.setFiles(widgetFiles)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LoadedWidgets(photoSize: Dp) {
        val files by photoViewModel.photos.collectAsState()
        if (files.isEmpty()) {
            NoWidgets()
        } else {
            LazyVerticalGrid(
                cells = GridCells.Adaptive(photoSize),
                modifier = Modifier.fillMaxSize()
            ) {
                items(files) { photoWidget ->
                    val file = photoWidget.file
                    Column(modifier = Modifier.wrapContentSize()) {
                        if (file.exists()) {
                            FullWidget(file, photoSize, photoWidget)
                        } else {
                            EmptyWidget(photoSize, photoWidget)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingWidgets() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Загружаем данные...", fontSize = 20.sp)
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun NoWidgets() {
        Text(
            text = "Добавьте widget чтобы выбрать изображение",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
    }

    @Composable
    private fun FullWidget(
        file: File,
        photoSize: Dp,
        photoWidget: PhotoViewModel.PhotoWidget
    ) {
        val widgetBitmap = file.getBitmap()
        Image(
            bitmap = widgetBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(photoSize)
        )
        Button(
            onClick = { selectPhoto(photoWidget) },
            modifier = Modifier.width(photoSize)
        ) {
            Text(text = "Изменить")
        }
    }

    @Composable
    private fun EmptyWidget(
        photoSize: Dp,
        photoWidget: PhotoViewModel.PhotoWidget
    ) {
        Box(
            modifier = Modifier
                .size(photoSize)
                .background(color = Color.Black)
                .fillMaxSize()
        )
        Button(
            onClick = { selectPhoto(photoWidget) },
            modifier = Modifier.width(photoSize)
        ) {
            Text(text = "Добавить")
        }
    }

    private fun selectPhoto(photoWidget: PhotoViewModel.PhotoWidget) {
        photoViewModel.setSelectedWidgetId(photoWidget.id)
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "image/*"
        resultLauncher.launch(galleryIntent)
    }
}
