package com.l1khith.calender28.ui.notes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lightweight, zero-dependency Markdown renderer complying strictly with MaterialTheme tokens.
 * Zero hardcoded emojis or custom glyphs.
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    var inCodeBlock = false
    val codeBlockBuilder = StringBuilder()

    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()

            // Code block delimiter
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    CodeBlockView(code = codeBlockBuilder.toString())
                    codeBlockBuilder.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockBuilder.appendLine(line)
                continue
            }

            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                continue
            }

            // Headings
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### "), primaryColor),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## "), primaryColor),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("# "), primaryColor),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    HorizontalDivider(
                        color = dividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    // Blockquote
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(primaryColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> "), primaryColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = secondaryColor,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    // Bullet list item rendered using Canvas circle dot (no Unicode glyphs)
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Canvas(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 10.dp)
                                .size(5.dp)
                        ) {
                            drawCircle(color = primaryColor, radius = size.minDimension / 2f)
                        }
                        Text(
                            text = parseInlineMarkdown(trimmed.drop(2), primaryColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
                trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(". ") -> {
                    // Numbered list item
                    val numPrefix = trimmed.substringBefore(". ") + "."
                    val itemText = trimmed.substringAfter(". ")
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = numPrefix,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(itemText, primaryColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
                else -> {
                    // Regular paragraph text
                    Text(
                        text = parseInlineMarkdown(line, primaryColor),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockBuilder.isNotEmpty()) {
            CodeBlockView(code = codeBlockBuilder.toString())
        }
    }
}

@Composable
private fun CodeBlockView(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
            .padding(12.dp)
    ) {
        Text(
            text = code.trimEnd(),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

/**
 * Parses inline formatting: **bold**, *italic*, and `inline code`.
 */
fun parseInlineMarkdown(text: String, primaryColor: Color) = buildAnnotatedString {
    var i = 0
    val len = text.length

    while (i < len) {
        when {
            // Bold (**text**)
            text.startsWith("**", i) && text.indexOf("**", i + 2) != -1 -> {
                val end = text.indexOf("**", i + 2)
                val boldText = text.substring(i + 2, end)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldText)
                }
                i = end + 2
            }
            // Inline Code (`code`)
            text[i] == '`' && text.indexOf('`', i + 1) != -1 -> {
                val end = text.indexOf('`', i + 1)
                val codeText = text.substring(i + 1, end)
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = primaryColor.copy(alpha = 0.12f),
                        color = primaryColor
                    )
                ) {
                    append(" $codeText ")
                }
                i = end + 1
            }
            // Italic (*text*)
            text[i] == '*' && text.indexOf('*', i + 1) != -1 && !text.startsWith("**", i) -> {
                val end = text.indexOf('*', i + 1)
                val italicText = text.substring(i + 1, end)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicText)
                }
                i = end + 1
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
