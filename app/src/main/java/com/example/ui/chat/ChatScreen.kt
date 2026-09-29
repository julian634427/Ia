package com.example.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.FormattedAssistantMessage
import com.example.ui.components.SidebarDrawerContent
import com.example.ui.components.rainbowBorder
import com.example.ui.components.rainbowPulse
import com.example.ui.theme.GeminiSparkleBlue
import com.example.ui.theme.RainbowColors
import com.example.ui.theme.RainbowCyan
import com.example.ui.theme.RainbowMagenta
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDivider
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary
import com.example.ui.theme.UserBubbleColor
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Auto-scroll to bottom on new messages or streaming tokens
    LaunchedEffect(uiState.messages.size, uiState.streamingContent) {
        val totalCount = uiState.messages.size + if (uiState.isGenerating && uiState.streamingContent.isNotEmpty()) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                SidebarDrawerContent(
                    sessions = uiState.sessions,
                    currentSessionId = uiState.currentSessionId,
                    selectedModel = uiState.selectedModel,
                    systemPrompt = uiState.systemPrompt,
                    hasApiKey = uiState.hasApiKey,
                    isUsingBuildConfig = uiState.isUsingBuildConfig,
                    onSelectSession = { id ->
                        viewModel.selectSession(id)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.createNewSession()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { id ->
                        viewModel.deleteSession(id)
                    },
                    onSelectModel = { model ->
                        viewModel.selectModel(model)
                    },
                    onSelectPersona = { prompt ->
                        viewModel.selectPersona(prompt)
                    },
                    onOpenApiKeySettings = {
                        viewModel.setApiKeyDialogOpen(true)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(StudioBackground),
            containerColor = StudioBackground,
            topBar = {
                ChatTopBar(
                    title = uiState.currentSessionTitle,
                    modelName = uiState.selectedModel,
                    isGenerating = uiState.isGenerating,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onNewChat = {
                        viewModel.createNewSession()
                    },
                    onOpenApiKeySettings = {
                        viewModel.setApiKeyDialogOpen(true)
                    },
                    onClearChat = {
                        viewModel.clearCurrentMessages()
                    }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    isGenerating = uiState.isGenerating,
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    onStop = {
                        viewModel.stopGeneration()
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                    EmptyChatWelcome(
                        onSelectPrompt = { prompt ->
                            inputText = prompt
                            viewModel.sendMessage(prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatMessageItem(message = message)
                        }

                        // Active streaming bubble
                        if (uiState.isGenerating && uiState.streamingContent.isNotEmpty()) {
                            item {
                                StreamingAssistantBubble(content = uiState.streamingContent)
                            }
                        } else if (uiState.isGenerating && uiState.streamingContent.isEmpty()) {
                            item {
                                ThinkingAssistantBubble()
                            }
                        }
                    }
                }

                // Error Banner if needed
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    uiState.errorMessage?.let { errorMsg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { viewModel.setApiKeyDialogOpen(true) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(RainbowMagenta))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = RainbowMagenta,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Aviso de Conexión",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StudioTextPrimary
                                    )
                                    Text(
                                        text = errorMsg,
                                        fontSize = 12.sp,
                                        color = StudioTextSecondary
                                    )
                                }
                                Text(
                                    text = "Revisar",
                                    color = RainbowCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isApiKeyDialogOpen) {
        ApiKeyDialog(
            currentKey = uiState.currentApiKey,
            isUsingBuildConfig = uiState.isUsingBuildConfig,
            onDismiss = { viewModel.setApiKeyDialogOpen(false) },
            onSaveKey = { key -> viewModel.saveApiKey(key) },
            onClearKey = { viewModel.clearApiKey() }
        )
    }
}

/**
 * Top App Bar with the iconic 3-line hamburger menu button.
 */
@Composable
fun ChatTopBar(
    title: String,
    modelName: String,
    isGenerating: Boolean,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenApiKeySettings: () -> Unit,
    onClearChat: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = StudioBackground
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // The Hamburger Menu Button (3 rayas) requested by user
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menú lateral",
                            tint = StudioTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Title & Sparkle Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .rainbowPulse(enabled = isGenerating)
                                .rainbowBorder(strokeWidth = 1.5.dp, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RainbowCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isGenerating) RainbowMagenta else RainbowCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isGenerating) "Generando..." else modelName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = StudioTextTertiary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Right Actions: New Chat & Overflow Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("new_chat_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva conversación",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(StudioSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Configurar Clave API", color = StudioTextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = GeminiSparkleBlue)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenApiKeySettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Limpiar conversación", color = StudioTextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StudioTextSecondary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearChat()
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioDivider)
            )
        }
    }
}

/**
 * Modern Input bar surrounded by animated continuous Rainbow Border Gradient!
 */
@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isGenerating: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        color = Color.Transparent
    ) {
        // Rainbow Border Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rainbowBorder(
                    strokeWidth = 2.dp,
                    shape = RoundedCornerShape(26.dp),
                    isAnimated = true,
                    durationMillis = if (isGenerating) 1800 else 4500
                )
                .clip(RoundedCornerShape(26.dp))
                .background(StudioSurface)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Field
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("chat_input_field"),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = StudioTextPrimary,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(RainbowCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Pregunta a Gemini Studio...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = StudioTextTertiary,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                // Action button: Send or Stop with fluid rainbow background
                val isSendEnabled = inputText.isNotBlank() || isGenerating
                val rainbowBrush = Brush.linearGradient(RainbowColors)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGenerating) SolidColor(RainbowMagenta)
                            else if (inputText.isNotBlank()) rainbowBrush
                            else SolidColor(StudioSurfaceHighlight)
                        )
                        .clickable(enabled = isSendEnabled) {
                            if (isGenerating) onStop() else onSend()
                        }
                        .testTag(if (isGenerating) "stop_button" else "send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Detener generación",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = if (inputText.isNotBlank()) StudioBackground else StudioTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty Chat Screen with animated iridescent sparkler and categorized quick chips.
 */
@Composable
fun EmptyChatWelcome(
    onSelectPrompt: (String) -> Unit
) {
    val quickPrompts = listOf(
        "🚀" to "Construye una pantalla en Jetpack Compose con animaciones fluidas",
        "⚡" to "Explica la diferencia entre StateFlow y SharedFlow con ejemplos",
        "🎨" to "Crea un gradiente arcoíris animado para un borde en Android",
        "🔍" to "Optimiza una consulta Room con índices y Flow reactivo"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Glowing Sparkle Logo
        Box(
            modifier = Modifier
                .size(76.dp)
                .rainbowPulse(enabled = true)
                .rainbowBorder(strokeWidth = 3.dp, shape = CircleShape)
                .background(StudioSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = RainbowCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bienvenido a Gemini Studio",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Asistente de desarrollo y productividad en tiempo real. Escribe una pregunta o pulsa una sugerencia:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = StudioTextSecondary
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Quick suggestions grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            quickPrompts.forEach { (emoji, prompt) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPrompt(prompt) }
                        .testTag("quick_prompt_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(StudioBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = StudioTextPrimary,
                                fontSize = 13.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chat Message Item for User or Model with formatted markdown & syntax highlight blocks.
 */
@Composable
fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .rainbowBorder(strokeWidth = 1.5.dp, shape = CircleShape)
                    .background(StudioSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RainbowCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Message bubble
        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) UserBubbleColor else StudioSurface,
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .let {
                        if (!isUser) it.border(1.dp, StudioBorder, RoundedCornerShape(18.dp)) else it
                    }
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = StudioTextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    } else {
                        FormattedAssistantMessage(
                            text = message.content,
                            isStreaming = false
                        )
                    }
                }
            }

            // Quick actions for assistant message
            if (!isUser && message.content.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("gemini response", message.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Respuesta copiada", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar respuesta",
                            tint = StudioTextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(StudioSurfaceHighlight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario",
                    tint = StudioTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StreamingAssistantBubble(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .rainbowPulse(enabled = true)
                .rainbowBorder(strokeWidth = 1.5.dp, shape = CircleShape)
                .background(StudioSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = RainbowCyan,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = StudioSurface,
            modifier = Modifier
                .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                FormattedAssistantMessage(
                    text = content,
                    isStreaming = true
                )
            }
        }
    }
}

@Composable
fun ThinkingAssistantBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .rainbowPulse(enabled = true)
                .rainbowBorder(strokeWidth = 1.5.dp, shape = CircleShape)
                .background(StudioSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = RainbowCyan,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = StudioSurface,
            modifier = Modifier.border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pensando",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = StudioTextSecondary,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(RainbowCyan.copy(alpha = dotAlpha1), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(RainbowMagenta.copy(alpha = dotAlpha2), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(GeminiSparkleBlue.copy(alpha = dotAlpha3), CircleShape)
                )
            }
        }
    }
}
