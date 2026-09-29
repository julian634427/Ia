package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockBorder
import com.example.ui.theme.RainbowCyan
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Parses assistant responses into formatted text and syntax-styled code blocks.
 */
@Composable
fun FormattedAssistantMessage(
    text: String,
    isStreaming: Boolean = false,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMessageBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Code -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }
                is ContentBlock.Text -> {
                    RenderFormattedText(content = block.content)
                }
            }
        }

        if (isStreaming) {
            StreamingBlinker()
        }
    }
}

@Composable
fun StreamingBlinker() {
    val infiniteTransition = rememberInfiniteTransition(label = "blinker")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = Modifier
            .size(width = 8.dp, height = 18.dp)
            .background(RainbowCyan.copy(alpha = alpha), RoundedCornerShape(2.dp))
    )
}

@Composable
fun CodeBlockView(
    language: String,
    code: String
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CodeBlockBorder, RoundedCornerShape(12.dp)),
        color = CodeBlockBackground
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language.isNotBlank()) language.uppercase() else "CODE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = RainbowCyan,
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code snippet", code)
                        clipboard.setPrimaryClip(clip)
                        copied = true
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copiar código",
                        tint = if (copied) RainbowCyan else StudioTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = StudioTextPrimary
                )
            }
        }
    }
}

@Composable
fun RenderFormattedText(content: String) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lines = content.split("\n")

        lines.forEachIndexed { lineIdx, line ->
            var l = line
            // Check bullet points
            val isBullet = l.trimStart().startsWith("- ") || l.trimStart().startsWith("* ")
            if (isBullet) {
                append(" • ")
                l = l.trimStart().removePrefix("- ").removePrefix("* ")
            }

            // Parse bold **text**
            val parts = l.split("**")
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                        append(part)
                    }
                } else {
                    // Check inline code `code`
                    val subParts = part.split("`")
                    subParts.forEachIndexed { subIdx, subPart ->
                        if (subIdx % 2 == 1) {
                            withStyle(
                                SpanStyle(
                                    fontFamily = FontFamily.Monospace,
                                    color = RainbowCyan,
                                    background = StudioSurfaceVariant
                                )
                            ) {
                                append(" $subPart ")
                            }
                        } else {
                            withStyle(SpanStyle(color = StudioTextPrimary)) {
                                append(subPart)
                            }
                        }
                    }
                }
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = 15.sp,
        lineHeight = 23.sp
    )
}

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Code(val language: String, val code: String) : ContentBlock()
}

fun parseMessageBlocks(raw: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val codeFence = "```"
    var remaining = raw

    while (remaining.isNotEmpty()) {
        val startIndex = remaining.indexOf(codeFence)
        if (startIndex == -1) {
            blocks.add(ContentBlock.Text(remaining))
            break
        }

        if (startIndex > 0) {
            val textBefore = remaining.substring(0, startIndex)
            if (textBefore.isNotEmpty()) {
                blocks.add(ContentBlock.Text(textBefore))
            }
        }

        val afterStart = remaining.substring(startIndex + codeFence.length)
        val lineEndIndex = afterStart.indexOf('\n')
        val language: String
        val codeSearchStart: Int

        if (lineEndIndex != -1) {
            language = afterStart.substring(0, lineEndIndex).trim()
            codeSearchStart = lineEndIndex + 1
        } else {
            language = ""
            codeSearchStart = 0
        }

        val endIndex = afterStart.indexOf(codeFence, codeSearchStart)
        if (endIndex != -1) {
            val code = afterStart.substring(codeSearchStart, endIndex).trimEnd()
            blocks.add(ContentBlock.Code(language = language, code = code))
            remaining = afterStart.substring(endIndex + codeFence.length)
        } else {
            // Unclosed code block (e.g. while streaming)
            val code = afterStart.substring(codeSearchStart).trimEnd()
            blocks.add(ContentBlock.Code(language = language, code = code))
            break
        }
    }

    return blocks
}
