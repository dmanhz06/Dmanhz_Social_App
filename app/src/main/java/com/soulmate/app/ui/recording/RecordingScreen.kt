package com.soulmate.app.ui.recording

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soulmate.app.R
import com.soulmate.app.ui.home.components.RecordingNote
import com.soulmate.app.ui.journal.history.HistoryViewModel
import com.soulmate.app.ui.theme.customGradient
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordingScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var transcribedText by remember { mutableStateOf("") }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    
    val notesList = remember { mutableStateListOf<RecordingNote>() }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    // Trạng thái đồng hồ kỹ thuật số
    val currentTimeString = remember { mutableStateOf("") }
    val currentDateString = remember {
        val sdf = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM 'năm' yyyy", Locale("vi", "VN"))
        sdf.format(Date())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                recordingDuration = System.currentTimeMillis() - startTime
                delay(100)
            }
        } else {
            recordingDuration = 0L
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isRecording = true }
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) transcribedText = data[0]
                isRecording = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) transcribedText = data[0]
            }
            override fun onEndOfSpeech() { isRecording = false }
            override fun onError(error: Int) { 
                isRecording = false
                Log.e("SpeechError", "Mã lỗi: $error")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            transcribedText = ""
            speechRecognizer.startListening(recognizerIntent)
            isRecording = true
        } else {
            Toast.makeText(context, "Vui lòng cấp quyền ghi âm", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Đồng hồ Digital & Ngày tháng
            Surface(
                elevation = 12.dp,
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            "STUDIO GHI ÂM",
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colors.primary)
                        )
                        Box(Modifier.size(48.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = currentTimeString.value,
                        style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colors.primary, letterSpacing = 2.sp)
                    )
                    Text(
                        text = currentDateString,
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Danh sách các bản ghi nháp
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (notesList.isEmpty() && transcribedText.isEmpty() && !isRecording) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.MicNone,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colors.primary.copy(alpha = 0.05f)
                        )
                        Text(
                            "Sẵn sàng lắng nghe tâm sự của bạn...",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(notesList, key = { it.id }) { item ->
                            SwipeableRecordingItem(
                                item = item,
                                onDelete = { notesList.remove(item) },
                                onSend = {
                                    historyViewModel.saveRecordingNote(item) { _ ->
                                        notesList.remove(item)
                                        showSaveSuccess = true
                                    }
                                }
                            )
                        }
                        
                        if (isRecording || transcribedText.isNotEmpty()) {
                            item {
                                ActiveTranscribingCard(text = transcribedText, isRecording = isRecording)
                            }
                        }
                    }
                }
            }

            // Bảng điều khiển ghi âm (Sử dụng Gradient)
            Surface(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp),
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        LiveWaveformVisualizer()
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút Reset
                        IconButton(
                            onClick = { transcribedText = "" },
                            enabled = transcribedText.isNotEmpty() && !isRecording,
                            modifier = Modifier.size(56.dp).background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear", 
                                tint = if (transcribedText.isNotEmpty()) Color.Gray else Color.Transparent)
                        }

                        // Nút Ghi âm chính (Gradient đồng bộ Login)
                        Box(contentAlignment = Alignment.Center) {
                            if (isRecording) { PulseRingEffect() }
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(12.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(if (isRecording) Brush.linearGradient(listOf(Color(0xFFFF4B4B), Color(0xFFFF8E8E))) else customGradient)
                                    .clickable {
                                        if (isRecording) speechRecognizer.stopListening()
                                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        // Nút thêm vào danh sách bản nháp
                        IconButton(
                            onClick = {
                                if (transcribedText.isNotEmpty()) {
                                    val now = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date())
                                    notesList.add(0, RecordingNote(dateTime = now, text = transcribedText))
                                    transcribedText = ""
                                }
                            },
                            enabled = transcribedText.isNotEmpty() && !isRecording,
                            modifier = Modifier.size(56.dp).background(
                                if (transcribedText.isNotEmpty()) MaterialTheme.colors.primary.copy(alpha = 0.15f) 
                                else MaterialTheme.colors.onSurface.copy(alpha = 0.05f), 
                                CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd, 
                                contentDescription = "Add to drafts",
                                tint = if (transcribedText.isNotEmpty()) MaterialTheme.colors.primary else Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (isRecording) "GHI ÂM: ${formatMillisToTime(recordingDuration)}" else "Nhấn để bắt đầu ghi âm",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Black,
                        color = if (isRecording) Color(0xFFFF4B4B) else Color.Gray
                    )
                }
            }
        }

        if (showSaveSuccess) {
            SuccessRecordingOverlay(onAnimationFinish = { showSaveSuccess = false })
        }
    }
}

@Composable
fun LiveWaveformVisualizer() {
    val infiniteTransition = rememberInfiniteTransition()
    Row(
        modifier = Modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(15) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 12f,
                targetValue = 48f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300 + (index * 30), easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFF4B4B), Color(0xFFFFB0B0))
                        )
                    )
            )
        }
    }
}

@Composable
fun PulseRingEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.8f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Restart)
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Restart)
    )
    Box(
        modifier = Modifier.size(96.dp).scale(scale).background(Color(0xFFFF4B4B).copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun ActiveTranscribingCard(text: String, isRecording: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isRecording) Color.Red else Color.Gray))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isRecording) "ĐANG GHI NHẬN..." else "BẢN NHÁP TẠM THỜI",
                    style = MaterialTheme.typography.caption.copy(letterSpacing = 1.5.sp),
                    fontWeight = FontWeight.Black,
                    color = if (isRecording) Color.Red else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (text.isEmpty() && isRecording) "Hãy nói gì đó, tôi đang lắng nghe..." else text,
                style = MaterialTheme.typography.body1.copy(lineHeight = 28.sp, fontSize = 17.sp),
                color = if (text.isEmpty()) Color.LightGray else MaterialTheme.colors.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableRecordingItem(item: RecordingNote, onDelete: () -> Unit, onSend: () -> Unit) {
    val density = LocalDensity.current
    val swipeLimit = with(density) { 150.dp.toPx() }
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(0f to 0, -swipeLimit to 1, swipeLimit to 2)

    LaunchedEffect(swipeableState.currentValue) {
        when (swipeableState.currentValue) {
            1 -> { onDelete(); swipeableState.snapTo(0) }
            2 -> { onSend(); swipeableState.snapTo(0) }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().swipeable(
            state = swipeableState, anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Horizontal
        )
    ) {
        // Nền khi vuốt phải (Lưu vào Nhật ký)
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)).background(Color(0xFF22C55E)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.padding(start = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("LƯU NHẬT KÝ", color = Color.White, fontWeight = FontWeight.Black)
            }
        }

        // Nền khi vuốt trái (Xóa)
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)).background(Color(0xFFEF4444)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(modifier = Modifier.padding(end = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("XOÁ BỎ", color = Color.White, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
            }
        }

        Box(modifier = Modifier.offset { IntOffset(swipeableState.offset.value.toInt(), 0) }.fillMaxWidth()) {
            RecordingDetailCard(item)
        }
    }
}

@Composable
fun RecordingDetailCard(item: RecordingNote) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = item.avatarRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = item.userName, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(text = item.dateTime, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.text, 
                fontSize = 16.sp, 
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f), 
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colors.primary.copy(alpha = 0.08f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.SwipeRight, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colors.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Vuốt phải để gửi vào Journal", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colors.primary)
            }
        }
    }
}

private fun formatMillisToTime(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / 60000) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
}

@Composable
fun SuccessRecordingOverlay(onAnimationFinish: () -> Unit) {
    var startTickAnimation by remember { mutableStateOf(false) }
    val sweepAngle = animateFloatAsState(targetValue = if (startTickAnimation) 360f else 0f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    val tickScale = animateFloatAsState(targetValue = if (sweepAngle.value >= 360f) 1.2f else 0f, animationSpec = spring(0.5f))

    LaunchedEffect(Unit) {
        startTickAnimation = true
        delay(2200)
        onAnimationFinish()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(40.dp), color = MaterialTheme.colors.surface, elevation = 16.dp, modifier = Modifier.size(220.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        drawArc(color = Color(0xFFF1F5F9), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(color = Color(0xFF22C55E), startAngle = -90f, sweepAngle = sweepAngle.value, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                    }
                    if (sweepAngle.value >= 360f) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(56.dp).scale(tickScale.value))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("ĐÃ GỬI VÀO NHẬT KÝ!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF22C55E))
            }
        }
    }
}
