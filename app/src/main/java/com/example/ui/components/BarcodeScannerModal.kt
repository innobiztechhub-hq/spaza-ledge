package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ProductEntity
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.CloudySky
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.OceanGradient
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Production-ready CameraX + ML Kit Barcode & QR Code Scanner Modal.
 *
 * Automatically requests camera runtime permissions on mobile phones,
 * runs real-time hardware camera image analysis via Google ML Kit,
 * emits detected barcodes with haptic feedback, and provides flash torch,
 * lens switching, manual typing, and quick catalog presets.
 */
@Composable
fun BarcodeScannerModal(
    availableProducts: List<ProductEntity>,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Camera runtime permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        permissionRequestedOnce = true
    }

    // Auto-request permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Scanner UI states
    var manualInput by remember { mutableStateOf("") }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }
    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }

    // Viewfinder laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPosition"
    )

    // Handle a detected barcode from ML Kit or tap
    fun handleCodeScanned(code: String) {
        val now = System.currentTimeMillis()
        // Prevent duplicate trigger within 1.2s for same code
        if (code == lastScannedCode && (now - lastScannedTimestamp) < 1200) {
            return
        }

        lastScannedCode = code
        lastScannedTimestamp = now

        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
            // Ignored if haptic unsupported
        }

        coroutineScope.launch {
            delay(350)
            onBarcodeScanned(code)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("barcode_scanner_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ImmersivePrimaryContainer)
                                .border(1.dp, ImmersiveBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scanner",
                                tint = ImmersivePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Live Barcode Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "QR, UPC, EAN, Code 128 supported",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("barcode_scanner_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close scanner",
                            tint = ImmersiveTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Camera Scanner Viewport or Permission Rationale
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF040608))
                            .border(1.5.dp, ImmersiveBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Live Camera Preview via CameraX
                        CameraPreviewView(
                            lensFacing = lensFacing,
                            isTorchOn = isTorchOn,
                            onCameraBound = { cam ->
                                cameraRef = cam
                            },
                            onBarcodeDetected = { scannedRaw ->
                                handleCodeScanned(scannedRaw)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Visual scanning HUD overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val boxSize = minOf(w * 0.72f, h * 0.72f)
                            val left = (w - boxSize) / 2f
                            val top = (h - boxSize) / 2f
                            val right = left + boxSize
                            val bottom = top + boxSize

                            val cornerLen = 28.dp.toPx()
                            val strokeW = 3.5.dp.toPx()
                            val bracketColor = if (lastScannedCode != null) AccentEmerald else ImmersivePrimary

                            // Top-Left corner
                            drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                            drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                            // Top-Right corner
                            drawLine(bracketColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
                            drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)
                            // Bottom-Left corner
                            drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
                            drawLine(bracketColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)
                            // Bottom-Right corner
                            drawLine(bracketColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
                            drawLine(bracketColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

                            // Animated Laser Line within the square
                            if (lastScannedCode == null) {
                                val currentLaserY = top + (boxSize * laserY)
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            ImmersiveSecondary.copy(alpha = 0.8f),
                                            ImmersivePrimary,
                                            ImmersiveSecondary.copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    ),
                                    start = Offset(left + 8.dp.toPx(), currentLaserY),
                                    end = Offset(right - 8.dp.toPx(), currentLaserY),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }

                        // Success feedback badge when code is captured
                        if (lastScannedCode != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF064E3B).copy(alpha = 0.92f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentEmerald),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = lastScannedCode ?: "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Top Controls Bar (Torch & Camera Flip)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Flashlight Toggle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B0D12).copy(alpha = 0.7f))
                                    .border(1.dp, ImmersiveBorder, CircleShape)
                                    .clickable {
                                        isTorchOn = !isTorchOn
                                        cameraRef?.cameraControl?.enableTorch(isTorchOn)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Toggle Torch",
                                    tint = if (isTorchOn) ImmersivePrimary else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Lens Flip Toggle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B0D12).copy(alpha = 0.7f))
                                    .border(1.dp, ImmersiveBorder, CircleShape)
                                    .clickable {
                                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                            CameraSelector.LENS_FACING_FRONT
                                        } else {
                                            CameraSelector.LENS_FACING_BACK
                                        }
                                        isTorchOn = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Switch Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Bottom Viewfinder Hint
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF0B0D12).copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (lastScannedCode != null) "Detected!" else "Align barcode or QR code inside frame",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (lastScannedCode != null) AccentEmerald else ImmersiveTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // Camera Permission Request Rationale Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ImmersivePrimaryContainer)
                                    .border(1.dp, ImmersiveBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = ImmersivePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Camera Access Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "To scan physical barcodes and QR codes with your device's camera, please grant camera permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ImmersiveTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ImmersivePrimary,
                                        contentColor = ImmersivePrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                                }

                                if (permissionRequestedOnce) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("App Settings", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Tap Product Presets (handy for catalog testing)
                val barcodedProducts = availableProducts.filter { it.barcode.isNotBlank() && !it.isNonBarcoded }
                if (barcodedProducts.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Catalog Sample Barcodes:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersiveTextSecondary
                        )
                        Text(
                            text = "${barcodedProducts.size} available",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ImmersiveSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(barcodedProducts) { product ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                                modifier = Modifier.clickable {
                                    handleCodeScanned(product.barcode)
                                }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = product.name.take(18) + if (product.name.length > 18) "…" else "",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ImmersiveTextPrimary
                                    )
                                    Text(
                                        text = product.barcode,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ImmersiveSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual barcode typing fallback
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Or manually type barcode number") },
                    placeholder = { Text("e.g. 79361100234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                val isManualValid = manualInput.isNotBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isManualValid) OceanGradient
                            else androidx.compose.ui.graphics.Brush.linearGradient(listOf(CloudySky, CloudySky))
                        )
                        .clickable(enabled = isManualValid) {
                            if (manualInput.isNotBlank()) {
                                handleCodeScanned(manualInput.trim())
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Confirm Barcode",
                        fontWeight = FontWeight.Bold,
                        color = if (isManualValid) Color.White else Color(0xFF2872A1).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * CameraX live preview composable wrapped with ML Kit Barcode Analyzer.
 */
@Composable
private fun CameraPreviewView(
    lensFacing: Int,
    isTorchOn: Boolean,
    onCameraBound: (Camera) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Configure ML Kit barcode scanner for all barcode/QR formats
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Exception) {
            }
            try {
                barcodeScanner.close()
            } catch (e: Exception) {
                Log.e("CameraPreviewView", "Error closing scanner", e)
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(
                        barcodeScanner = barcodeScanner,
                        imageProxy = imageProxy,
                        onBarcodeDetected = onBarcodeDetected
                    )
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera.cameraControl.enableTorch(isTorchOn)
                    onCameraBound(camera)
                } catch (exc: Exception) {
                    Log.e("CameraPreviewView", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = { previewView ->
            // Rebind when lens facing or torch changes
            val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(
                            barcodeScanner = barcodeScanner,
                            imageProxy = imageProxy,
                            onBarcodeDetected = onBarcodeDetected
                        )
                    }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera.cameraControl.enableTorch(isTorchOn)
                    onCameraBound(camera)
                } catch (e: Exception) {
                    Log.e("CameraPreviewView", "Update camera failed", e)
                }
            }, ContextCompat.getMainExecutor(previewView.context))
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue ?: barcode.displayValue
                    if (!rawValue.isNullOrBlank()) {
                        onBarcodeDetected(rawValue)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("processImageProxy", "Barcode processing error", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
