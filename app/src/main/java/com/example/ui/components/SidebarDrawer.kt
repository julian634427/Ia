package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.ui.theme.GeminiSparkleBlue
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
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SidebarDrawerContent(
    sessions: List<ChatSessionEntity>,
    currentSessionId: Long?,
    selectedModel: String,
    systemPrompt: String,
    hasApiKey: Boolean,
    isUsingBuildConfig: Boolean,
    onSelectSession: (Long) -> Unit,
    onNewChat: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onSelectModel: (String) -> Unit,
    onSelectPersona: (String) -> Unit,
    onOpenApiKeySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var personaMenuExpanded by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-3.5-flash" to "Gemini 3.5 Flash (Rápido y Preciso)",
        "gemini-3.8-flash" to "Gemini 3.8 Flash (Última Generación)",
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Razonamiento Complejo)",
        "gemini-2.5-flash" to "Gemini 2.5 Flash (Ultra Ligero)"
    )

    val personas = listOf(
        "Asistente Experto" to "Eres Gemini Studio, un asistente de IA avanzado de Google. Eres experto en desarrollo de software, Kotlin, Jetpack Compose, análisis técnico y productividad en tiempo real.",
        "Ingeniero Android Senior" to "Eres un Ingeniero Principal de Android especializado en Jetpack Compose, Arquitectura MVVM, Room, Coroutines y mejores prácticas de Google.",
        "Revisor de Código & Debugger" to "Eres un revisor de código implacable y meticuloso. Encuentras bugs, mejoras de rendimiento y sugieres soluciones limpias y testeadas.",
        "Diseñador UI/UX Creativo" to "Eres un diseñador UI/UX creativo enfocado en interfaces futuristas, gradientes arcoíris, micro-interacciones y Material Design 3 de alto nivel."
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            // Rounded corners on the end (right side)
            .clip(RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)),
        color = StudioSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            // User & Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Iridescent Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rainbowBorder(strokeWidth = 2.dp, shape = CircleShape)
                        .background(StudioSurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini Logo",
                        tint = RainbowCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Gemini Studio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GeminiSparkleBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiSparkleBlue
                            )
                        }
                    }
                    Text(
                        text = "julian63927@gmail.com",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StudioTextSecondary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // "+ Nueva conversación" Button with Rainbow Gradient Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rainbowBorder(strokeWidth = 2.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioSurfaceVariant)
                    .clickable { onNewChat() }
                    .padding(horizontal = 16.dp, vertical = 13.dp)
                    .testTag("new_chat_drawer_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo Chat",
                        tint = RainbowCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nueva conversación",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Model & Persona quick selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Model Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { modelMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceHighlight)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text("MODELO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RainbowCyan)
                            Text(
                                text = selectedModel.removePrefix("gemini-").removeSuffix("-preview"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier.background(StudioSurfaceVariant)
                    ) {
                        models.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(id, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                                        Text(label, fontSize = 11.sp, color = StudioTextSecondary)
                                    }
                                },
                                onClick = {
                                    onSelectModel(id)
                                    modelMenuExpanded = false
                                },
                                leadingIcon = {
                                    if (selectedModel == id) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = RainbowCyan)
                                    }
                                }
                            )
                        }
                    }
                }

                // Persona Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { personaMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceHighlight)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Text("ROL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RainbowMagenta)
                            Text(
                                text = "Personalizado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = personaMenuExpanded,
                        onDismissRequest = { personaMenuExpanded = false },
                        modifier = Modifier.background(StudioSurfaceVariant)
                    ) {
                        personas.forEach { (name, prompt) ->
                            DropdownMenuItem(
                                text = {
                                    Text(name, fontWeight = FontWeight.Medium, color = StudioTextPrimary)
                                },
                                onClick = {
                                    onSelectPersona(prompt)
                                    personaMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conversations List Label
            Text(
                text = "HISTORIAL DE CONVERSACIONES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = StudioTextTertiary,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // Sessions List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (sessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin conversaciones recientes",
                                style = MaterialTheme.typography.bodySmall.copy(color = StudioTextTertiary)
                            )
                        }
                    }
                } else {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        val bgColor by animateColorAsState(
                            targetValue = if (isSelected) StudioSurfaceHighlight else Color.Transparent,
                            label = "session_bg"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable { onSelectSession(session.id) }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) RainbowCyan else StudioTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) StudioTextPrimary else StudioTextSecondary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                        .format(Date(session.updatedAt))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StudioTextTertiary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar chat",
                                    tint = StudioTextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = StudioDivider, modifier = Modifier.padding(vertical = 12.dp))

            // Footer: API Key & Secrets Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenApiKeySettings() }
                    .testTag("api_key_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (hasApiKey) SuccessGreen.copy(alpha = 0.15f) else RainbowMagenta.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (hasApiKey) SuccessGreen else RainbowMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Gemini API Key",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                )
                            )
                            Text(
                                text = if (hasApiKey) {
                                    if (isUsingBuildConfig) "Conectado vía Secretos" else "Clave local activa"
                                } else "No configurada",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (hasApiKey) SuccessGreen else RainbowMagenta
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
