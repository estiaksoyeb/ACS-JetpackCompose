package com.example.composestarter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import kotlin.math.roundToInt
import android.graphics.Path as AndroidPath
import android.graphics.RectF as AndroidRectF

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make system status/navigation bars transparent to blend with the full-screen layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            GlassExperimentApp()
        }
    }
}

// Custom Glass Surface Modifier that renders true directional inner shadows and clipped outer shadows
fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.00f),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    borderWidth: Dp = 1.dp,
    
    // Outer shadow
    outerShadowColor: Color = Color.Black.copy(alpha = 0.00f),
    outerShadowBlur: Dp = 0.dp,
    outerShadowOffsetX: Dp = 0.dp,
    outerShadowOffsetY: Dp = 0.dp,
    
    // Inner Highlight (Top-left)
    innerHighlightColor: Color = Color.White.copy(alpha = 0.35f),
    innerHighlightBlur: Dp = 5.dp,
    innerHighlightOffsetX: Dp = (-2).dp,
    innerHighlightOffsetY: Dp = (-2).dp,
    
    // Inner Shadow (Bottom-right)
    innerShadowColor: Color = Color.Black.copy(alpha = 0.35f),
    innerShadowBlur: Dp = 5.dp,
    innerShadowOffsetX: Dp = 2.dp,
    innerShadowOffsetY: Dp = 2.dp
) = this.drawWithContent {
    val density = this
    val outerShadowBlurPx = with(density) { outerShadowBlur.toPx() }
    val outerShadowOffsetXPx = with(density) { outerShadowOffsetX.toPx() }
    val outerShadowOffsetYPx = with(density) { outerShadowOffsetY.toPx() }
    
    val cornerRadiusPx = with(density) { shape.topStart.toPx(size, density) }
    
    drawIntoCanvas { canvas ->
        // 1. Draw outer shadow (clipped so it doesn't bleed inside the transparent body)
        if (outerShadowBlurPx > 0f && outerShadowColor.alpha > 0f) {
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                )
            }
            canvas.save()
            canvas.clipPath(path, clipOp = ClipOp.Difference)
            
            val shadowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    outerShadowBlurPx,
                    outerShadowOffsetXPx,
                    outerShadowOffsetYPx,
                    outerShadowColor.toArgb()
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                cornerRadiusPx, cornerRadiusPx,
                shadowPaint
            )
            canvas.restore()
        }
    }
    
    // 2. Draw semi-transparent background color
    drawRoundRect(
        color = backgroundColor,
        cornerRadius = CornerRadius(cornerRadiusPx)
    )
    
    // 3. Draw inner shadows (Highlight & Shadow)
    val innerShadowBlurPx = with(density) { innerShadowBlur.toPx() }
    val innerShadowOffsetXPx = with(density) { innerShadowOffsetX.toPx() }
    val innerShadowOffsetYPx = with(density) { innerShadowOffsetY.toPx() }
    
    val innerHighlightBlurPx = with(density) { innerHighlightBlur.toPx() }
    val innerHighlightOffsetXPx = with(density) { innerHighlightOffsetX.toPx() }
    val innerHighlightOffsetYPx = with(density) { innerHighlightOffsetY.toPx() }
    
    drawIntoCanvas { canvas ->
        val rectPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            )
        }
        
        canvas.save()
        canvas.clipPath(rectPath)
        
        // A. Inner Highlight (Top-left)
        if (innerHighlightBlurPx > 0f && innerHighlightColor.alpha > 0f) {
            val highlightPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    innerHighlightBlurPx,
                    innerHighlightOffsetXPx,
                    innerHighlightOffsetYPx,
                    innerHighlightColor.toArgb()
                )
                style = android.graphics.Paint.Style.FILL
            }
            
            val invertedPath = AndroidPath().apply {
                addRect(
                    -innerHighlightBlurPx - 20f,
                    -innerHighlightBlurPx - 20f,
                    size.width + innerHighlightBlurPx + 20f,
                    size.height + innerHighlightBlurPx + 20f,
                    AndroidPath.Direction.CW
                )
                val buttonPath = AndroidPath().apply {
                    val rectF = AndroidRectF(0f, 0f, size.width, size.height)
                    addRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, AndroidPath.Direction.CW)
                }
                op(buttonPath, AndroidPath.Op.DIFFERENCE)
            }
            canvas.nativeCanvas.drawPath(invertedPath, highlightPaint)
        }
        
        // B. Inner Shadow (Bottom-right)
        if (innerShadowBlurPx > 0f && innerShadowColor.alpha > 0f) {
            val shadowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    innerShadowBlurPx,
                    innerShadowOffsetXPx,
                    innerShadowOffsetYPx,
                    innerShadowColor.toArgb()
                )
                style = android.graphics.Paint.Style.FILL
            }
            
            val invertedShadowPath = AndroidPath().apply {
                addRect(
                    -innerShadowBlurPx - 20f,
                    -innerShadowBlurPx - 20f,
                    size.width + innerShadowBlurPx + 20f,
                    size.height + innerShadowBlurPx + 20f,
                    AndroidPath.Direction.CW
                )
                val buttonPath = AndroidPath().apply {
                    val rectF = AndroidRectF(0f, 0f, size.width, size.height)
                    addRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, AndroidPath.Direction.CW)
                }
                op(buttonPath, AndroidPath.Op.DIFFERENCE)
            }
            canvas.nativeCanvas.drawPath(invertedShadowPath, shadowPaint)
        }
        
        canvas.restore()
    }
    
    // 4. Draw Content
    drawContent()
    
    // 5. Draw Border on top
    if (borderWidth > 0.dp && borderColor.alpha > 0f) {
        val borderWidthPx = with(density) { borderWidth.toPx() }
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = borderWidthPx)
        )
    }
}

@Composable
fun GlassExperimentApp() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Interactive states
    var usePremiumGlass by remember { mutableStateOf(true) }
    var dragModeByButton by remember { mutableStateOf(false) } // true = drag button, false = drag background
    var isSettingsExpanded by remember { mutableStateOf(true) }
    var showCodeDialog by remember { mutableStateOf(false) }
    
    // Position/Transform states
    var imageOffset by remember { mutableStateOf(Offset.Zero) }
    var buttonDragOffset by remember { mutableStateOf(Offset.Zero) }
    var buttonPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var panelPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    
    // Customization states
    var bgAlpha by remember { mutableFloatStateOf(0.00f) }
    var borderAlpha by remember { mutableFloatStateOf(0.12f) }
    var backdropBlur by remember { mutableFloatStateOf(24f) }
    
    var highlightAlpha by remember { mutableFloatStateOf(0.35f) }
    var highlightBlur by remember { mutableFloatStateOf(5f) }
    var highlightOffset by remember { mutableFloatStateOf(-2f) }
    
    var shadowAlpha by remember { mutableFloatStateOf(0.35f) }
    var shadowBlur by remember { mutableFloatStateOf(5f) }
    var shadowOffset by remember { mutableFloatStateOf(2f) }
    
    var outerShadowAlpha by remember { mutableFloatStateOf(0.00f) }
    var outerShadowBlur by remember { mutableFloatStateOf(0f) }
    var outerShadowOffset by remember { mutableFloatStateOf(0f) }
    
    var selectedTintName by remember { mutableStateOf("Frost White") }
    val tintColor = when (selectedTintName) {
        "Sunset Gold" -> Color(0xFFFFD54F)
        "Ocean Blue" -> Color(0xFF4FC3F7)
        "Emerald Green" -> Color(0xFF81C784)
        "Rose Pink" -> Color(0xFFF06292)
        else -> Color.White // Frost White
    }
    
    val generatedCode = remember(bgAlpha, borderAlpha, highlightAlpha, highlightBlur, highlightOffset, shadowAlpha, shadowBlur, shadowOffset, outerShadowAlpha, outerShadowBlur, outerShadowOffset, selectedTintName) {
        generateGlassCode(bgAlpha, borderAlpha, highlightAlpha, highlightBlur, highlightOffset, shadowAlpha, shadowBlur, shadowOffset, outerShadowAlpha, outerShadowBlur, outerShadowOffset, selectedTintName)
    }
    
    val helperCode = remember { getHelperFunctionCode() }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark mode backdrop
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val density = LocalDensity.current
        val screenWidthPx = with(density) { screenWidth.toPx() }
        val screenHeightPx = with(density) { screenHeight.toPx() }
        
        // --- 1. Background Layer (Moveable Image) ---
        Image(
            painter = painterResource(id = context.resources.getIdentifier("glass_background", "drawable", context.packageName)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    if (!dragModeByButton) {
                        IntOffset(imageOffset.x.roundToInt(), imageOffset.y.roundToInt())
                    } else {
                        IntOffset.Zero
                    }
                }
                .pointerInput(dragModeByButton) {
                    if (!dragModeByButton) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            imageOffset += dragAmount
                        }
                    }
                }
        )
        
        // --- 2. Floating Glass Button (Interactive Target) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
                .offset {
                    IntOffset(
                        buttonDragOffset.x.roundToInt(),
                        buttonDragOffset.y.roundToInt()
                    )
                }
                .onGloballyPositioned {
                    buttonPositionInRoot = it.positionInRoot()
                }
                .pointerInput(dragModeByButton) {
                    if (dragModeByButton) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            buttonDragOffset += dragAmount
                        }
                    }
                }
                .width(260.dp)
                .height(84.dp)
        ) {
            // BACKDROP BLUR SIMULATION (Only in Premium Mode)
            if (usePremiumGlass && backdropBlur > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = context.resources.getIdentifier("glass_background", "drawable", context.packageName)),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val relOffset = if (dragModeByButton) {
                                    -buttonPositionInRoot
                                } else {
                                    imageOffset - buttonPositionInRoot
                                }
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = screenWidthPx / size.width
                                scaleY = screenHeightPx / size.height
                                translationX = relOffset.x
                                translationY = relOffset.y
                            }
                            .blur(backdropBlur.dp)
                    )
                }
            }

            // GLASS STYLING OVERLAY
            if (usePremiumGlass) {
                // Premium glassSurface Modifier
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassSurface(
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = tintColor.copy(alpha = bgAlpha),
                            borderColor = tintColor.copy(alpha = borderAlpha),
                            borderWidth = 1.dp,
                            outerShadowColor = Color.Black.copy(alpha = outerShadowAlpha),
                            outerShadowBlur = outerShadowBlur.dp,
                            outerShadowOffsetX = 0.dp,
                            outerShadowOffsetY = outerShadowOffset.dp,
                            innerHighlightColor = Color.White.copy(alpha = highlightAlpha),
                            innerHighlightBlur = highlightBlur.dp,
                            innerHighlightOffsetX = highlightOffset.dp,
                            innerHighlightOffsetY = highlightOffset.dp,
                            innerShadowColor = Color.Black.copy(alpha = shadowAlpha),
                            innerShadowBlur = shadowBlur.dp,
                            innerShadowOffsetX = shadowOffset.dp,
                            innerShadowOffsetY = shadowOffset.dp
                        )
                        .clickable {
                            Toast.makeText(context, "Premium Glass Clicked!", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ButtonContent(title = "Premium Glass", subtitle = if (dragModeByButton) "Drag button directly" else "Drag background image")
                }
            } else {
                // Simple ChatGPT implementation (Gradients only, no inner shadows)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            tintColor.copy(alpha = bgAlpha)
                        )
                        .border(
                            1.dp,
                            tintColor.copy(alpha = borderAlpha),
                            RoundedCornerShape(24.dp)
                        )
                        .drawWithContent {
                            drawContent()

                            // Top-left inner highlight
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = highlightAlpha),
                                        Color.Transparent
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                                )
                            )

                            // Bottom-right inner shadow
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = shadowAlpha)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                                ),
                                blendMode = BlendMode.Multiply
                            )
                        }
                        .clickable {
                            Toast.makeText(context, "ChatGPT Glass Clicked!", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ButtonContent(title = "Basic Gradient", subtitle = "Notice flat edge lighting")
                }
            }
        }
        
        // --- 3. Floating Reset Button ---
        FloatingActionButton(
            onClick = {
                imageOffset = Offset.Zero
                buttonDragOffset = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp),
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Positions")
        }

        // --- 4. Bottom Control Panel Dashboard (Also a glass card!) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
                .onGloballyPositioned {
                    panelPositionInRoot = it.positionInRoot()
                }
        ) {
            // Panel background blur simulation
            if (backdropBlur > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Image(
                        painter = painterResource(id = context.resources.getIdentifier("glass_background", "drawable", context.packageName)),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val relOffset = if (dragModeByButton) {
                                    -panelPositionInRoot
                                } else {
                                    imageOffset - panelPositionInRoot
                                }
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = screenWidthPx / size.width
                                scaleY = screenHeightPx / size.height
                                translationX = relOffset.x
                                translationY = relOffset.y
                            }
                            .blur(backdropBlur.dp)
                    )
                }
            }

            // Glass Dashboard Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = Color.Black.copy(alpha = 0.65f), // Darker glass for readability
                        borderColor = Color.White.copy(alpha = 0.15f),
                        borderWidth = 1.dp,
                        outerShadowColor = Color.Black.copy(alpha = 0.3f),
                        outerShadowBlur = 15.dp,
                        outerShadowOffsetY = 5.dp
                    )
                    .padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Glass UI Sandbox",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showCodeDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4FC3F7))
                        ) {
                            Text("Copy Code", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        
                        IconButton(onClick = { isSettingsExpanded = !isSettingsExpanded }) {
                            Icon(
                                imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Expand/Collapse",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Copy Code Dialog
                if (showCodeDialog) {
                    var selectedTab by remember { mutableStateOf(0) } // 0 = Modifier config, 1 = Helper function
                    AlertDialog(
                        onDismissRequest = { showCodeDialog = false },
                        title = { Text("Copy Glass Code", color = Color.White) },
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White,
                        textContentColor = Color.White.copy(alpha = 0.8f),
                        text = {
                            Column {
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF4FC3F7)
                                ) {
                                    Tab(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        text = { Text("Modifier Config", fontSize = 12.sp, color = if (selectedTab == 0) Color(0xFF4FC3F7) else Color.White.copy(alpha = 0.6f)) }
                                    )
                                    Tab(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        text = { Text("Helper Function", fontSize = 12.sp, color = if (selectedTab == 1) Color(0xFF4FC3F7) else Color.White.copy(alpha = 0.6f)) }
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val codeText = if (selectedTab == 0) generatedCode else helperCode
                                val explanation = if (selectedTab == 0) {
                                    "This contains the exact values you configured using the sliders. Apply this modifier directly on your Box/Button."
                                } else {
                                    "This is the Modifier.glassSurface extension function. Copy and paste it once in your codebase so the modifier is available."
                                }
                                
                                Text(explanation, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                ) {
                                    Box(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = codeText,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFF81C784),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 220.dp)
                                                .verticalScroll(rememberScrollState())
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val codeText = if (selectedTab == 0) generatedCode else helperCode
                                    clipboardManager.setText(AnnotatedString(codeText))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7), contentColor = Color.Black)
                            ) {
                                Text("Copy Text", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showCodeDialog = false },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Done")
                            }
                        }
                    )
                }
                
                if (isSettingsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Toggle: Premium vs Basic
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Use Custom glassSurface()", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = usePremiumGlass,
                                onCheckedChange = { usePremiumGlass = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4FC3F7),
                                    checkedTrackColor = Color(0xFF0288D1)
                                )
                            )
                        }

                        // Toggle: Drag Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Drag Button directly", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = dragModeByButton,
                                onCheckedChange = { dragModeByButton = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFFD54F),
                                    checkedTrackColor = Color(0xFFF57F17)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Tint selector
                        Text("Glass Color Tint", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val tints = listOf("Frost White", "Ocean Blue", "Rose Pink", "Sunset Gold", "Emerald Green")
                            tints.forEach { tintName ->
                                val tintColorCircle = when (tintName) {
                                    "Sunset Gold" -> Color(0xFFFFD54F)
                                    "Ocean Blue" -> Color(0xFF4FC3F7)
                                    "Emerald Green" -> Color(0xFF81C784)
                                    "Rose Pink" -> Color(0xFFF06292)
                                    else -> Color.White
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tintColorCircle.copy(alpha = 0.4f))
                                        .border(
                                            width = if (selectedTintName == tintName) 2.dp else 1.dp,
                                            color = if (selectedTintName == tintName) Color.White else Color.White.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedTintName = tintName }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sliders: Common Parameters
                        SliderSetting("Glass Opacity", bgAlpha, 0f..0.6f) { bgAlpha = it }
                        SliderSetting("Border Opacity", borderAlpha, 0f..0.8f) { borderAlpha = it }
                        SliderSetting("Backdrop Blur (dp)", backdropBlur, 0f..40f) { backdropBlur = it }
                        
                        if (usePremiumGlass) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Premium Shadow Parameters", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            SliderSetting("Inner Highlight Alpha", highlightAlpha, 0f..0.6f) { highlightAlpha = it }
                            SliderSetting("Inner Highlight Blur", highlightBlur, 0f..20f) { highlightBlur = it }
                            SliderSetting("Inner Highlight Offset", highlightOffset, -15f..0f) { highlightOffset = it }
                            
                            SliderSetting("Inner Shadow Alpha", shadowAlpha, 0f..0.6f) { shadowAlpha = it }
                            SliderSetting("Inner Shadow Blur", shadowBlur, 0f..20f) { shadowBlur = it }
                            SliderSetting("Inner Shadow Offset", shadowOffset, 0f..15f) { shadowOffset = it }
                            
                            SliderSetting("Outer Shadow Alpha", outerShadowAlpha, 0f..0.3f) { outerShadowAlpha = it }
                            SliderSetting("Outer Shadow Blur", outerShadowBlur, 0f..45f) { outerShadowBlur = it }
                            SliderSetting("Outer Shadow Offset", outerShadowOffset, 0f..20f) { outerShadowOffset = it }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Basic Gradient Parameters", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            SliderSetting("Top-Left Gradient Alpha", highlightAlpha, 0f..0.8f) { highlightAlpha = it }
                            SliderSetting("Bottom-Right Gradient Alpha", shadowAlpha, 0f..0.8f) { shadowAlpha = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonContent(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(String.format("%.2f", value), color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            valueRange = range,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.8f),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GlassExperimentApp()
}

fun generateGlassCode(
    bgAlpha: Float,
    borderAlpha: Float,
    highlightAlpha: Float,
    highlightBlur: Float,
    highlightOffset: Float,
    shadowAlpha: Float,
    shadowBlur: Float,
    shadowOffset: Float,
    outerShadowAlpha: Float,
    outerShadowBlur: Float,
    outerShadowOffset: Float,
    selectedTintName: String
): String {
    val tintColorCode = when (selectedTintName) {
        "Sunset Gold" -> "Color(0xFFFFD54F)"
        "Ocean Blue" -> "Color(0xFF4FC3F7)"
        "Emerald Green" -> "Color(0xFF81C784)"
        "Rose Pink" -> "Color(0xFFF06292)"
        else -> "Color.White"
    }
    
    return """
Modifier.glassSurface(
    shape = RoundedCornerShape(24.dp),
    backgroundColor = $tintColorCode.copy(alpha = ${String.format("%.2ff", bgAlpha)}),
    borderColor = $tintColorCode.copy(alpha = ${String.format("%.2ff", borderAlpha)}),
    borderWidth = 1.dp,
    outerShadowColor = Color.Black.copy(alpha = ${String.format("%.2ff", outerShadowAlpha)}),
    outerShadowBlur = ${String.format("%.1ff", outerShadowBlur)}.dp,
    outerShadowOffsetX = 0.dp,
    outerShadowOffsetY = ${String.format("%.1ff", outerShadowOffset)}.dp,
    innerHighlightColor = Color.White.copy(alpha = ${String.format("%.2ff", highlightAlpha)}),
    innerHighlightBlur = ${String.format("%.1ff", highlightBlur)}.dp,
    innerHighlightOffsetX = ${String.format("%.1ff", highlightOffset)}.dp,
    innerHighlightOffsetY = ${String.format("%.1ff", highlightOffset)}.dp,
    innerShadowColor = Color.Black.copy(alpha = ${String.format("%.2ff", shadowAlpha)}),
    innerShadowBlur = ${String.format("%.1ff", shadowBlur)}.dp,
    innerShadowOffsetX = ${String.format("%.1ff", shadowOffset)}.dp,
    innerShadowOffsetY = ${String.format("%.1ff", shadowOffset)}.dp
)
    """.trimIndent()
}

fun getHelperFunctionCode(): String {
    return """
// Custom Glass Surface Modifier that renders true directional inner shadows and clipped outer shadows
fun Modifier.glassSurface(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.00f),
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    borderWidth: Dp = 1.dp,
    
    // Outer shadow
    outerShadowColor: Color = Color.Black.copy(alpha = 0.00f),
    outerShadowBlur: Dp = 0.dp,
    outerShadowOffsetX: Dp = 0.dp,
    outerShadowOffsetY: Dp = 0.dp,
    
    // Inner Highlight (Top-left)
    innerHighlightColor: Color = Color.White.copy(alpha = 0.35f),
    innerHighlightBlur: Dp = 5.dp,
    innerHighlightOffsetX: Dp = (-2).dp,
    innerHighlightOffsetY: Dp = (-2).dp,
    
    // Inner Shadow (Bottom-right)
    innerShadowColor: Color = Color.Black.copy(alpha = 0.35f),
    innerShadowBlur: Dp = 5.dp,
    innerShadowOffsetX: Dp = 2.dp,
    innerShadowOffsetY: Dp = 2.dp
) = this.drawWithContent {
    val density = this
    val outerShadowBlurPx = with(density) { outerShadowBlur.toPx() }
    val outerShadowOffsetXPx = with(density) { outerShadowOffsetX.toPx() }
    val outerShadowOffsetYPx = with(density) { outerShadowOffsetY.toPx() }
    
    val cornerRadiusPx = with(density) { shape.topStart.toPx(size, density) }
    
    drawIntoCanvas { canvas ->
        if (outerShadowBlurPx > 0f && outerShadowColor.alpha > 0f) {
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                )
            }
            canvas.save()
            canvas.clipPath(path, clipOp = ClipOp.Difference)
            
            val shadowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    outerShadowBlurPx,
                    outerShadowOffsetXPx,
                    outerShadowOffsetYPx,
                    outerShadowColor.toArgb()
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                cornerRadiusPx, cornerRadiusPx,
                shadowPaint
            )
            canvas.restore()
        }
    }
    
    drawRoundRect(
        color = backgroundColor,
        cornerRadius = CornerRadius(cornerRadiusPx)
    )
    
    val innerShadowBlurPx = with(density) { innerShadowBlur.toPx() }
    val innerShadowOffsetXPx = with(density) { innerShadowOffsetX.toPx() }
    val innerShadowOffsetYPx = with(density) { innerShadowOffsetY.toPx() }
    
    val innerHighlightBlurPx = with(density) { innerHighlightBlur.toPx() }
    val innerHighlightOffsetXPx = with(density) { innerHighlightOffsetX.toPx() }
    val innerHighlightOffsetYPx = with(density) { innerHighlightOffsetY.toPx() }
    
    drawIntoCanvas { canvas ->
        val rectPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            )
        }
        
        canvas.save()
        canvas.clipPath(rectPath)
        
        if (innerHighlightBlurPx > 0f && innerHighlightColor.alpha > 0f) {
            val highlightPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    innerHighlightBlurPx,
                    innerHighlightOffsetXPx,
                    innerHighlightOffsetYPx,
                    innerHighlightColor.toArgb()
                )
                style = android.graphics.Paint.Style.FILL
            }
            
            val invertedPath = android.graphics.Path().apply {
                addRect(
                    -innerHighlightBlurPx - 20f,
                    -innerHighlightBlurPx - 20f,
                    size.width + innerHighlightBlurPx + 20f,
                    size.height + innerHighlightBlurPx + 20f,
                    android.graphics.Path.Direction.CW
                )
                val buttonPath = android.graphics.Path().apply {
                    val rectF = android.graphics.RectF(0f, 0f, size.width, size.height)
                    addRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, android.graphics.Path.Direction.CW)
                }
                op(buttonPath, android.graphics.Path.Op.DIFFERENCE)
            }
            canvas.nativeCanvas.drawPath(invertedPath, highlightPaint)
        }
        
        if (innerShadowBlurPx > 0f && innerShadowColor.alpha > 0f) {
            val shadowPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    innerShadowBlurPx,
                    innerShadowOffsetXPx,
                    innerShadowOffsetYPx,
                    innerShadowColor.toArgb()
                )
                style = android.graphics.Paint.Style.FILL
            }
            
            val invertedShadowPath = android.graphics.Path().apply {
                addRect(
                    -innerShadowBlurPx - 20f,
                    -innerShadowBlurPx - 20f,
                    size.width + innerShadowBlurPx + 20f,
                    size.height + innerShadowBlurPx + 20f,
                    android.graphics.Path.Direction.CW
                )
                val buttonPath = android.graphics.Path().apply {
                    val rectF = android.graphics.RectF(0f, 0f, size.width, size.height)
                    addRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, android.graphics.Path.Direction.CW)
                }
                op(buttonPath, android.graphics.Path.Op.DIFFERENCE)
            }
            canvas.nativeCanvas.drawPath(invertedShadowPath, shadowPaint)
        }
        
        canvas.restore()
    }
    
    drawContent()
    
    if (borderWidth > 0.dp && borderColor.alpha > 0f) {
        val borderWidthPx = with(density) { borderWidth.toPx() }
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = borderWidthPx)
        )
    }
}
    """.trimIndent()
}