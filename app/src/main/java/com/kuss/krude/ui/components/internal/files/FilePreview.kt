package com.kuss.krude.ui.components.internal.files

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.kuss.krude.shizuku.bean.BeanFile
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ActivityHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.sqrt


class FilePreviewState {
    var file by mutableStateOf<BeanFile?>(null)
    var previewing by mutableStateOf(false)

    fun show(file: BeanFile) {
        this.file = file
        previewing = true
    }

    fun dismiss() {
        file = null
        previewing = false
    }
}

@Composable
fun rememberFilePreviewState(): FilePreviewState {
    return remember { FilePreviewState() }
}

private fun loadBitmapFromFile(file: File): Bitmap? {
    return FileHelper.openInputStream(file)?.use {
        BitmapFactory.decodeStream(it)
    }
}

fun getUriFromFile(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
}

fun getFileMimeType(context: Context, uri: Uri): String {
    val contentResolver: ContentResolver = context.contentResolver
    val mimeType: String = contentResolver.getType(uri) ?: return "*"
    return mimeType
}

@Composable
fun ImageFilePreview(file: File) {
    val context = LocalContext.current
    val bitmap = remember(file) {
        loadBitmapFromFile(file)
    }
    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = "Preview Image")
    }
}

fun formatDuration(int: Int): String {
    if (int < 0) {
        return "00:00"
    }
    val seconds = int / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

private fun readTextFile(file: File): List<String> {
    val inputStream = FileHelper.openInputStream(file)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val list = mutableListOf<String>()
    reader.use { r ->
        var line = r.readLine()
        while (line != null) {
            list.add(line)
            line = r.readLine()
        }
    }
    return list
}

fun isBinaryFile(file: File): Boolean {
    // Maximum bytes to read to determine if the file is binary
    val maxCheckBytes = 4096

    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(file)
        val buffer = ByteArray(maxCheckBytes)
        val bytesRead = fis.read(buffer, 0, maxCheckBytes)

        if (bytesRead == -1) {
            // File is empty
            return false
        }

        // Analyze bytes to determine if they are binary
        for (i in 0 until bytesRead) {
            val b = buffer[i].toInt() and 0xFF
            if (b == 0) {
                // Null byte found, likely binary
                return true
            } else if (b < 7 || b == 11 || b == 12 || b == 14 || b == 15 || (b >= 16 && b <= 31)) {
                // Control characters found, likely binary
                return true
            } else if (b == 127) {
                // DEL character found, likely binary
                return true
            }
        }

        // No binary indicators found
        return false

    } catch (e: IOException) {
        e.printStackTrace()
        return true // Treat IOException as binary (conservative approach)
    } finally {
        fis?.close()
    }
}

@Composable
fun AudioFilePreview(file: File) {
    val context = LocalContext.current
    var mediaPlayer by remember {
        mutableStateOf<MediaPlayer?>(null)
    }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember {
        mutableIntStateOf(0)
    }
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(key1 = file.path) {
        val uri = getUriFromFile(context, file)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            setOnCompletionListener {
                isPlaying = false
                progress = 0f
            }
        }
    }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                currentPosition = player.currentPosition
                progress = player.currentPosition.toFloat() / player.duration
            }
            delay(100L) // Update every 100ms
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            // Release MediaPlayer resources when composable is disposed
            mediaPlayer?.release()
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            value = progress,
            onValueChange = { newValue ->
                progress = newValue
                mediaPlayer?.let { player ->
                    val newPosition = (newValue * player.duration).toInt()
                    player.seekTo(newPosition)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary
            )
        )
        mediaPlayer?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDuration(currentPosition))
                Text(formatDuration(it.duration))
            }
        }
        if (mediaPlayer != null && mediaPlayer!!.isPlaying.not()) {
            Button(onClick = {
                isPlaying = true
                mediaPlayer?.start()
            }) {
                Text("Play")
            }
        } else if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            Button(onClick = {
                isPlaying = false
                mediaPlayer?.pause()
            }) {
                Text("Pause")
            }
        }
    }
}

@Composable
fun VideoFilePreview(file: File) {
    val context = LocalContext.current
    var mediaPlayerController by remember { mutableStateOf<MediaPlayerController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember {
        mutableIntStateOf(0)
    }
    var progress by remember { mutableFloatStateOf(0f) }
    var videoDuration by remember { mutableIntStateOf(0) }
    val uri = remember {
        getUriFromFile(context, file)
    }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayerController?.let { controller ->
                currentPosition = controller.currentPosition
                progress = controller.currentPosition.toFloat() / videoDuration
            }
            delay(100L) // Update every 100ms
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayerController?.release()
        }
    }

    Column {
        AndroidView(factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(uri)
                setOnPreparedListener { mediaPlayer ->
                    videoDuration = mediaPlayer.duration
                    mediaPlayerController = MediaPlayerController(this, mediaPlayer)
                }
//                setMediaController(MediaController(ctx).apply {
//                    setAnchorView(this)
//                })
                requestFocus()
            }
        }, update = { videoView ->
            videoView.setVideoURI(uri)
            videoView.requestFocus()
        }, modifier = Modifier
            .fillMaxSize()
            .heightIn(max = 512.dp)
        )
        Slider(
            value = progress,
            onValueChange = { newValue ->
                progress = newValue
                mediaPlayerController?.seekTo((newValue * videoDuration).toInt())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary
            )
        )
        mediaPlayerController?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDuration(currentPosition))
                Text(formatDuration(it.duration))
            }
        }
        Spacing(1)
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                if (isPlaying) {
                    mediaPlayerController?.pause()
                } else {
                    mediaPlayerController?.play()
                }
                isPlaying = !isPlaying
            }) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}

class MediaPlayerController(
    private val videoView: VideoView, private val mediaPlayer: MediaPlayer
) {
    val currentPosition: Int
        get() = mediaPlayer.currentPosition

    val duration: Int
        get() = mediaPlayer.duration

    fun play() {
        videoView.start()
    }

    fun pause() {
        videoView.pause()
    }

    fun seekTo(position: Int) {
        videoView.seekTo(position)
    }

    fun release() {
        mediaPlayer.release()
    }
}

@Composable
fun TextFilePreview(file: File) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var textLines by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(file) {
        scope.launch {
            withContext(IO) {
                textLines = readTextFile(file)
            }
        }
    }

    val listState = rememberLazyListState()

    if (textLines == null) {
        CircularProgressIndicator()
    }

    textLines?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(max = 512.dp)
        ) {
            LazyColumnScrollbar(
                listState, settings = ScrollbarSettings(
                    thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
                    thumbSelectedColor = MaterialTheme.colorScheme.primary
                )
            ) {
                LazyColumn(state = listState) {
                    items(it) { line ->
                        BasicText(
                            text = line,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                                .copy(color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                }
            }
        }
    }
}

// thanks: https://stackoverflow.com/a/69946154
@Composable
fun PdfPreview(
    modifier: Modifier = Modifier,
    file: File,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
) {
    val context = LocalContext.current
    val uri = remember {
        getUriFromFile(context, file)
    }
    val rendererScope = rememberCoroutineScope()
    val mutex = remember { Mutex() }
    val renderer by produceState<PdfRenderer?>(null, file) {
        rendererScope.launch(IO) {
            val input = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            value = PdfRenderer(input)
        }
        awaitDispose {
            val currentRenderer = value
            rendererScope.launch(IO) {
                mutex.withLock {
                    currentRenderer?.close()
                }
            }
        }
    }
    val imageLoader = LocalContext.current.imageLoader
    val imageLoadingScope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 768.dp)
    ) {
        val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
        val height = (width * sqrt(2f)).toInt()
        val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
        LazyColumn(
            verticalArrangement = verticalArrangement
        ) {
            items(
                count = pageCount,
                key = { index -> "$uri-$index" }
            ) { index ->
                val cacheKey = MemoryCache.Key("$uri-$index")
                imageLoader.memoryCache?.let { memoryCache ->
                    var bitmap by remember { mutableStateOf(memoryCache[cacheKey]?.bitmap) }
                    if (bitmap == null) {
                        DisposableEffect(uri, index) {
                            val job = imageLoadingScope.launch(IO) {
                                val destinationBitmap =
                                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                mutex.withLock {
                                    Timber.d("Loading PDF $uri - page $index/$pageCount")
                                    if (!coroutineContext.isActive) return@launch
                                    try {
                                        renderer?.let {
                                            it.openPage(index).use { page ->
                                                page.render(
                                                    destinationBitmap,
                                                    null,
                                                    null,
                                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        //Just catch and return in case the renderer is being closed
                                        return@launch
                                    }
                                }
                                bitmap = destinationBitmap
                            }
                            onDispose {
                                job.cancel()
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White)
                                .aspectRatio(1f / sqrt(2f))
                                .fillMaxWidth()
                        )
                    } else {
                        val request = ImageRequest.Builder(context)
                            .size(width, height)
                            .memoryCacheKey(cacheKey)
                            .data(bitmap)
                            .build()

                        Image(
                            modifier = Modifier
                                .background(Color.White)
                                .aspectRatio(1f / sqrt(2f))
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            painter = rememberAsyncImagePainter(request),
                            contentDescription = "Page ${index + 1} of $pageCount"
                        )
                    }
                }
            }
        }
    }
}

const val MAX_READABLE_SIZE = 1024 * 1024 * 50;

fun openFile(context: Context, file: File) {
    val uri = getUriFromFile(context, file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, getFileMimeType(context, uri))
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        ActivityHelper.startIntentWithTransition(context, intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e("OpenFile", "No activity found to handle file type for $uri")
    }
}

@Composable
fun Unsupported(file: File) {
    val context = LocalContext.current
    Button(onClick = {
        openFile(context, file)
    }) {
        Text(text = "Open with")
    }
}

@Composable
fun FilePreview(file: File) {
    val context = LocalContext.current
    val mimeType = remember(file) {
        getFileMimeType(context, getUriFromFile(context, file))
    }
    LaunchedEffect(key1 = file) {
        Timber.d("FilePreview mimeType: $mimeType")
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .heightIn(min = 128.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = file.name, style = MaterialTheme.typography.titleMedium
            )
        }
        Spacing(0.5f)
        if (mimeType.startsWith("image")) {
            ImageFilePreview(file = file)
        } else if (mimeType.startsWith("audio")) {
            AudioFilePreview(file = file)
        } else if (mimeType.startsWith("video")) {
            VideoFilePreview(file)
        } else if (mimeType.startsWith("application/pdf")) {
            PdfPreview(file = file)
        } else if (mimeType.startsWith("application/octet-stream")) {
            TextFilePreview(file = file)
        } else if (mimeType.startsWith("text")) {
            TextFilePreview(file = file)
        } else {
            val isBinary = remember {
                isBinaryFile(file)
            }
            if (file.length() < MAX_READABLE_SIZE && !isBinary) {
                TextFilePreview(file = file)
            } else {
                Unsupported(file = file)
            }
        }
    }
}

