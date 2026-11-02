package com.example.study_mentor.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.study_mentor.ui.theme.BackgroundOffWhite
import com.example.study_mentor.ui.theme.PrimaryBlue
import java.io.File
import java.io.FileOutputStream

// ─── Drag Handle Identifier for Resizing ─────────────────────────────────────

enum class ViewfinderHandle {
    NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, BODY
}

/**
 * Live CameraX Preview Composable using AndroidView to inflate PreviewView.
 * Binds CameraX Preview and ImageCapture use cases to the local lifecycle owner
 * with the default back-facing camera.
 */
@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onCameraBound: ((Camera) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    onCameraBound?.invoke(camera)
                } catch (exc: Exception) {
                    Log.e("CameraScannerScreen", "CameraX binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * Interactive Resizable Crop Frame Overlay.
 * Dims the screen outside the selection box with Color.Black.copy(alpha = 0.6f)
 * while punching a 100% transparent cutout in the center with BlendMode.Clear.
 * Allows dragging 4 corner handles/dots to freely resize the viewfinder frame.
 */
@Composable
fun InteractiveViewfinderOverlay(
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = 80.dp,
    minHeight: Dp = 50.dp,
    dimColor: Color = Color.Black.copy(alpha = 0.6f),
    strokeColor: Color = Color.White
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val totalW = constraints.maxWidth.toFloat()
        val totalH = constraints.maxHeight.toFloat()

        val minWidthPx = with(density) { minWidth.toPx() }
        val minHeightPx = with(density) { minHeight.toPx() }
        val handleTouchRadius = with(density) { 38.dp.toPx() }

        val boundMinX = with(density) { 16.dp.toPx() }
        val boundMaxX = totalW - with(density) { 16.dp.toPx() }
        val boundMinY = with(density) { 70.dp.toPx() }
        val boundMaxY = totalH - with(density) { 150.dp.toPx() }

        var activeHandle by remember { mutableStateOf(ViewfinderHandle.NONE) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(cropRect, totalW, totalH) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val distTL = (startOffset - Offset(cropRect.left, cropRect.top)).getDistance()
                            val distTR = (startOffset - Offset(cropRect.right, cropRect.top)).getDistance()
                            val distBL = (startOffset - Offset(cropRect.left, cropRect.bottom)).getDistance()
                            val distBR = (startOffset - Offset(cropRect.right, cropRect.bottom)).getDistance()

                            activeHandle = when {
                                distTL <= handleTouchRadius -> ViewfinderHandle.TOP_LEFT
                                distTR <= handleTouchRadius -> ViewfinderHandle.TOP_RIGHT
                                distBL <= handleTouchRadius -> ViewfinderHandle.BOTTOM_LEFT
                                distBR <= handleTouchRadius -> ViewfinderHandle.BOTTOM_RIGHT
                                cropRect.contains(startOffset) -> ViewfinderHandle.BODY
                                else -> ViewfinderHandle.NONE
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (activeHandle != ViewfinderHandle.NONE) {
                                change.consume()
                                when (activeHandle) {
                                    ViewfinderHandle.TOP_LEFT -> {
                                        val newL = (cropRect.left + dragAmount.x).coerceIn(boundMinX, cropRect.right - minWidthPx)
                                        val newT = (cropRect.top + dragAmount.y).coerceIn(boundMinY, cropRect.bottom - minHeightPx)
                                        onCropRectChange(Rect(newL, newT, cropRect.right, cropRect.bottom))
                                    }
                                    ViewfinderHandle.TOP_RIGHT -> {
                                        val newR = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minWidthPx, boundMaxX)
                                        val newT = (cropRect.top + dragAmount.y).coerceIn(boundMinY, cropRect.bottom - minHeightPx)
                                        onCropRectChange(Rect(cropRect.left, newT, newR, cropRect.bottom))
                                    }
                                    ViewfinderHandle.BOTTOM_LEFT -> {
                                        val newL = (cropRect.left + dragAmount.x).coerceIn(boundMinX, cropRect.right - minWidthPx)
                                        val newB = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minHeightPx, boundMaxY)
                                        onCropRectChange(Rect(newL, cropRect.top, cropRect.right, newB))
                                    }
                                    ViewfinderHandle.BOTTOM_RIGHT -> {
                                        val newR = (cropRect.right + dragAmount.x).coerceIn(cropRect.left + minWidthPx, boundMaxX)
                                        val newB = (cropRect.bottom + dragAmount.y).coerceIn(cropRect.top + minHeightPx, boundMaxY)
                                        onCropRectChange(Rect(cropRect.left, cropRect.top, newR, newB))
                                    }
                                    ViewfinderHandle.BODY -> {
                                        val dx = dragAmount.x.coerceIn(boundMinX - cropRect.left, boundMaxX - cropRect.right)
                                        val dy = dragAmount.y.coerceIn(boundMinY - cropRect.top, boundMaxY - cropRect.bottom)
                                        onCropRectChange(cropRect.translate(dx, dy))
                                    }
                                    ViewfinderHandle.NONE -> {}
                                }
                            }
                        },
                        onDragEnd = { activeHandle = ViewfinderHandle.NONE },
                        onDragCancel = { activeHandle = ViewfinderHandle.NONE }
                    )
                }
        ) {
            // 1. Semi-transparent dark overlay covering entire camera area
            drawRect(color = dimColor)

            val cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())

            // 2. Punch clear transparent cutout
            drawRoundRect(
                color = Color.Transparent,
                topLeft = cropRect.topLeft,
                size = cropRect.size,
                cornerRadius = cornerRadius,
                blendMode = BlendMode.Clear
            )

            // 3. Crisp white bounding border
            drawRoundRect(
                color = strokeColor.copy(alpha = 0.85f),
                topLeft = cropRect.topLeft,
                size = cropRect.size,
                cornerRadius = cornerRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Photomath-style L-bracket corner guides
            val cornerLen = 22.dp.toPx()
            val cornerStroke = 3.5.dp.toPx()

            // Top-Left
            drawLine(Color.White, Offset(cropRect.left, cropRect.top), Offset(cropRect.left + cornerLen, cropRect.top), cornerStroke, StrokeCap.Round)
            drawLine(Color.White, Offset(cropRect.left, cropRect.top), Offset(cropRect.left, cropRect.top + cornerLen), cornerStroke, StrokeCap.Round)

            // Top-Right
            drawLine(Color.White, Offset(cropRect.right, cropRect.top), Offset(cropRect.right - cornerLen, cropRect.top), cornerStroke, StrokeCap.Round)
            drawLine(Color.White, Offset(cropRect.right, cropRect.top), Offset(cropRect.right, cropRect.top + cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Left
            drawLine(Color.White, Offset(cropRect.left, cropRect.bottom), Offset(cropRect.left + cornerLen, cropRect.bottom), cornerStroke, StrokeCap.Round)
            drawLine(Color.White, Offset(cropRect.left, cropRect.bottom), Offset(cropRect.left, cropRect.bottom - cornerLen), cornerStroke, StrokeCap.Round)

            // Bottom-Right
            drawLine(Color.White, Offset(cropRect.right, cropRect.bottom), Offset(cropRect.right - cornerLen, cropRect.bottom), cornerStroke, StrokeCap.Round)
            drawLine(Color.White, Offset(cropRect.right, cropRect.bottom), Offset(cropRect.right, cropRect.bottom - cornerLen), cornerStroke, StrokeCap.Round)

            // 5. Visual Corner Dots/Handles
            val dotRadius = 4.5.dp.toPx()
            val dotBorderRadius = 7.dp.toPx()
            val cornerPoints = listOf(
                Offset(cropRect.left, cropRect.top),
                Offset(cropRect.right, cropRect.top),
                Offset(cropRect.left, cropRect.bottom),
                Offset(cropRect.right, cropRect.bottom)
            )
            cornerPoints.forEach { pt ->
                drawCircle(Color.White, radius = dotBorderRadius, center = pt)
                drawCircle(PrimaryBlue, radius = dotRadius, center = pt)
            }
        }

        // Floating guidance pill above the resizable frame
        val hintY = (cropRect.top - with(density) { 34.dp.toPx() }).toInt().coerceAtLeast(with(density) { 72.dp.toPx() }.toInt())
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, hintY) }
        ) {
            Text(
                text = "Drag corners to fit math problem",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

/**
 * Accurately crops a captured JPEG photo to the user's viewfinder selection rectangle.
 * Reads EXIF orientation, rotates the bitmap to match portrait orientation, computes the
 * PreviewView.ScaleType.FILL_CENTER aspect ratio mapping, crops the ROI, and saves to cache.
 */
fun cropAndSaveImage(
    context: Context,
    photoFile: File,
    cropRect: Rect,
    previewWidth: Int,
    previewHeight: Int
): Uri? {
    if (!photoFile.exists()) return null

    // 1. Read EXIF Orientation
    val exif = ExifInterface(photoFile.absolutePath)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val rotationDegrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    // 2. Decode full raw bitmap
    val rawBitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return null

    // 3. Rotate bitmap if needed to match screen orientation
    val orientedBitmap = if (rotationDegrees != 0f) {
        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        if (rotated != rawBitmap) rawBitmap.recycle()
        rotated
    } else {
        rawBitmap
    }

    val bWidth = orientedBitmap.width
    val bHeight = orientedBitmap.height

    // 4. Calculate aspect ratio scaling with PreviewView.ScaleType.FILL_CENTER
    val scale = maxOf(
        previewWidth.toFloat() / bWidth.toFloat(),
        previewHeight.toFloat() / bHeight.toFloat()
    )
    val scaledW = bWidth * scale
    val scaledH = bHeight * scale
    val offsetX = (scaledW - previewWidth) / 2f
    val offsetY = (scaledH - previewHeight) / 2f

    // 5. Map screen cropRect coordinates to the oriented bitmap
    val startX = ((cropRect.left + offsetX) / scale).toInt().coerceIn(0, bWidth - 1)
    val startY = ((cropRect.top + offsetY) / scale).toInt().coerceIn(0, bHeight - 1)
    val cropW = (cropRect.width / scale).toInt().coerceIn(1, bWidth - startX)
    val cropH = (cropRect.height / scale).toInt().coerceIn(1, bHeight - startY)

    // 6. Crop bitmap to the user's viewfinder selection
    val croppedBitmap = Bitmap.createBitmap(orientedBitmap, startX, startY, cropW, cropH)
    if (croppedBitmap != orientedBitmap) orientedBitmap.recycle()

    // 7. Save cropped bitmap to temporary cache file
    val croppedFile = File.createTempFile("CROP_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    FileOutputStream(croppedFile).use { out ->
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    croppedBitmap.recycle()

    return FileProvider.getUriForFile(
        context,
        "com.example.study_mentor.fileprovider",
        croppedFile
    )
}

/**
 * Triggers CameraX image capture, processes and crops the resulting image on a background thread,
 * and passes the cropped file URI to the callback.
 */
fun takePhotoAndCrop(
    context: Context,
    imageCapture: ImageCapture,
    cropRect: Rect,
    previewWidth: Int,
    previewHeight: Int,
    onSuccess: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val tempFile = try {
        File.createTempFile("RAW_SCAN_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
    } catch (e: Exception) {
        onError(e)
        return
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Background thread for rotation, aspect ratio mapping, and cropping
                Thread {
                    try {
                        val croppedUri = cropAndSaveImage(
                            context = context,
                            photoFile = tempFile,
                            cropRect = cropRect,
                            previewWidth = previewWidth,
                            previewHeight = previewHeight
                        )
                        // Delete full-size uncropped temporary file
                        try { tempFile.delete() } catch (_: Exception) {}

                        ContextCompat.getMainExecutor(context).execute {
                            if (croppedUri != null) {
                                onSuccess(croppedUri)
                            } else {
                                onError(Exception("Failed to crop captured bitmap"))
                            }
                        }
                    } catch (e: Exception) {
                        try { tempFile.delete() } catch (_: Exception) {}
                        ContextCompat.getMainExecutor(context).execute {
                            onError(e)
                        }
                    }
                }.start()
            }

            override fun onError(exception: ImageCaptureException) {
                try { tempFile.delete() } catch (_: Exception) {}
                onError(exception)
            }
        }
    )
}

/**
 * Polite rationale UI displayed when camera permission is not yet granted.
 */
@Composable
fun CameraPermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.15f),
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Study Mentor requires camera access to scan and solve math problems in real-time. Please grant permission to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Grant Permission",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        ) {
            Text(
                text = "Open App Settings",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─── Main Screen Composable ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Permission state & launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // CameraX use case & controls
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    // Clear active chat session when pressing the system back button
    BackHandler {
        chatViewModel.startNewChat()
        onBackClick()
    }

    // Photo picker launcher for selecting images from gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        chatViewModel.onImageSelected(uri, context)
    }

    // Auto-scroll to the bottom whenever a new message arrives
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .navigationBarsPadding()
            .imePadding()
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // Dynamic, resizable crop frame state
        var cropRect by remember(totalWidth, totalHeight) {
            val initW = totalWidth * 0.82f
            val initH = with(density) { 200.dp.toPx() }
            val initL = (totalWidth - initW) / 2f
            val initT = (totalHeight - initH) / 2f - with(density) { 50.dp.toPx() }
            mutableStateOf(Rect(initL, initT, initL + initW, initT + initH))
        }

        // Shared function: captures photo, crops it to the viewfinder frame, evaluates prompt, and dispatches to AI
        fun performCaptureAndSend() {
            if (isCapturing || uiState.isLoading) return
            isCapturing = true

            takePhotoAndCrop(
                context = context,
                imageCapture = imageCapture,
                cropRect = cropRect,
                previewWidth = totalWidth.toInt(),
                previewHeight = totalHeight.toInt(),
                onSuccess = { croppedUri ->
                    isCapturing = false
                    // "Lazy User" prompt logic: if text input is blank, assign default prompt string
                    val promptToSend = if (uiState.inputText.isNotBlank()) {
                        uiState.inputText.trim()
                    } else {
                        "Solve this math problem step-by-step and explain the concepts."
                    }
                    // Immediately send cropped image and prompt to AI (no second tap needed)
                    chatViewModel.sendImagePrompt(context, croppedUri, promptToSend)
                },
                onError = { exc ->
                    isCapturing = false
                    Log.e("CameraScannerScreen", "Capture or crop error", exc)
                }
            )
        }

        // Listen for capture requests from external triggers (e.g. MainScreen center Send FAB)
        LaunchedEffect(Unit) {
            chatViewModel.captureTrigger.collect {
                if (hasCameraPermission && !isCapturing && !uiState.isLoading) {
                    performCaptureAndSend()
                }
            }
        }

        // ── 1. Live Camera Preview or Permission Rationale ─────────────────
        if (hasCameraPermission) {
            CameraPreview(
                imageCapture = imageCapture,
                modifier = Modifier.fillMaxSize(),
                onCameraBound = { camera -> cameraControl = camera.cameraControl }
            )

            // ── 2. Interactive Resizable Viewfinder Overlay ─────────────────
            if (uiState.messages.isEmpty()) {
                InteractiveViewfinderOverlay(
                    cropRect = cropRect,
                    onCropRectChange = { cropRect = it },
                    modifier = Modifier.fillMaxSize(),
                    dimColor = Color.Black.copy(alpha = 0.6f)
                )
            }
        } else {
            CameraPermissionRationale(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Top Bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    chatViewModel.startNewChat()
                    onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Ask AI", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))

            if (hasCameraPermission && cameraControl != null) {
                IconButton(
                    onClick = {
                        val nextTorchState = !isTorchOn
                        cameraControl?.enableTorch(nextTorchState)
                        isTorchOn = nextTorchState
                    }
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isTorchOn) Color(0xFFFFD700) else Color.White
                    )
                }
            }

            IconButton(onClick = { chatViewModel.startNewChat() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "New Chat", tint = Color.White)
            }
        }

        // ── Persistent Bottom Sheet ────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Chat Message List ──────────────────────────────────────
                if (uiState.messages.isNotEmpty() || uiState.isLoading) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 0.dp, max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatBubble(message = message)
                        }

                        if (uiState.isLoading) {
                            item { AiThinkingBubble() }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Error Banner ───────────────────────────────────────────
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // ── Section Label (shown only before first message) ────────
                if (uiState.messages.isEmpty()) {
                    Text(
                        text = "OR TYPE YOUR QUESTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                // ── Selected Image Thumbnail Preview ──────────────────────
                if (uiState.selectedImageUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(70.dp)) {
                            AsyncImage(
                                model = uiState.selectedImageUri,
                                contentDescription = "Selected image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            )

                            IconButton(
                                onClick = { chatViewModel.clearSelectedImage() },
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear selected image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                val canSend = (uiState.inputText.isNotBlank() || uiState.selectedImageUri != null) && !uiState.isLoading

                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { chatViewModel.onInputTextChanged(it) },
                    placeholder = { Text("Ask something...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                chatViewModel.sendMessage()
                            } else if (hasCameraPermission && uiState.messages.isEmpty()) {
                                performCaptureAndSend()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = BackgroundOffWhite,
                        unfocusedContainerColor = BackgroundOffWhite
                    ),
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            // Gallery picker
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Attach image from gallery",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Camera capture button: triggers one-tap capture & solve
                            IconButton(
                                onClick = {
                                    if (!hasCameraPermission) {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    } else {
                                        performCaptureAndSend()
                                    }
                                },
                                enabled = !isCapturing && !uiState.isLoading,
                                modifier = Modifier.size(36.dp)
                            ) {
                                if (isCapturing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PrimaryBlue,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Capture photo and solve",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    trailingIcon = {
                        val isSendActive = canSend || (hasCameraPermission && uiState.messages.isEmpty())
                        IconButton(
                            onClick = {
                                if (canSend) {
                                    chatViewModel.sendMessage()
                                } else if (hasCameraPermission && uiState.messages.isEmpty()) {
                                    performCaptureAndSend()
                                }
                            },
                            enabled = !uiState.isLoading && !isCapturing,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .background(
                                    if (isSendActive) PrimaryBlue else Color.LightGray,
                                    CircleShape
                                )
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )

                // ── Action Pills (hidden when a conversation has started) ──
                if (uiState.messages.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionPill("Solve step-by-step") {
                            val prompt = if (uiState.inputText.isNotBlank()) {
                                "Solve step-by-step: ${uiState.inputText}"
                            } else {
                                "Solve this math problem step-by-step and explain the concepts."
                            }
                            if (hasCameraPermission && uiState.selectedImageUri == null) {
                                chatViewModel.onInputTextChanged(prompt)
                                performCaptureAndSend()
                            } else {
                                chatViewModel.sendMessage(prompt)
                            }
                        }
                        ActionPill("Explain concept") {
                            val prompt = if (uiState.inputText.isNotBlank()) {
                                "Explain this concept simply: ${uiState.inputText}"
                            } else {
                                "Explain this concept simply"
                            }
                            if (hasCameraPermission && uiState.selectedImageUri == null) {
                                chatViewModel.onInputTextChanged(prompt)
                                performCaptureAndSend()
                            } else {
                                chatViewModel.sendMessage(prompt)
                            }
                        }
                        ActionPill("Check my work") {
                            val prompt = if (uiState.inputText.isNotBlank()) {
                                "Check my work and correct any mistakes: ${uiState.inputText}"
                            } else {
                                "Check my work and correct any mistakes"
                            }
                            if (hasCameraPermission && uiState.selectedImageUri == null) {
                                chatViewModel.onInputTextChanged(prompt)
                                performCaptureAndSend()
                            } else {
                                chatViewModel.sendMessage(prompt)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─── Chat Bubble Composable ─────────────────────────────────────────────────

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryBlue else Color(0xFFF1F5F9),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color(0xFF1E293B),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

// ─── AI Thinking Indicator ──────────────────────────────────────────────────

@Composable
fun AiThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = Color(0xFFF1F5F9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = PrimaryBlue,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "AI is thinking...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ─── Action Pill Composable ─────────────────────────────────────────────────

@Composable
fun ActionPill(text: String, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFDBEAFE),
        modifier = Modifier.padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
