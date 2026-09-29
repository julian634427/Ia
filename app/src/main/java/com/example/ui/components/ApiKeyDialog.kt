package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiSparkleBlue
import com.example.ui.theme.RainbowMagenta
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.SuccessGreen

@Composable
fun ApiKeyDialog(
    currentKey: String,
    isUsingBuildConfig: Boolean,
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = GeminiSparkleBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Configuración de Clave API",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentKey.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isUsingBuildConfig) "Conectado por AI Studio" else "Clave personalizada activa",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = StudioTextPrimary
                                )
                                Text(
                                    text = if (isUsingBuildConfig) "Inyectado vía BuildConfig.GEMINI_API_KEY" else "Guardado localmente en la app",
                                    fontSize = 11.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RainbowMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Sin clave configurada",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = StudioTextPrimary
                                )
                                Text(
                                    text = "Ingresa tu clave de Google AI Studio abajo.",
                                    fontSize = 11.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Puedes ingresar una clave directa de Gemini API (AI Studio) para comunicación en tiempo real:",
                    fontSize = 13.sp,
                    color = StudioTextSecondary
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Clave API de Gemini") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Ocultar" else "Mostrar",
                                tint = StudioTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiSparkleBlue,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        focusedLabelColor = GeminiSparkleBlue,
                        unfocusedLabelColor = StudioTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveKey(keyInput.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeminiSparkleBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_api_key_button")
            ) {
                Text("Guardar", color = StudioBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_api_key_button")
            ) {
                Text("Cancelar", color = StudioTextSecondary)
            }
        }
    )
}
