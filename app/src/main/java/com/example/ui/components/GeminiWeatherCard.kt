package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiWeatherCard(
    aiBriefing: String?,
    isGeneratingBriefing: Boolean,
    aiBriefingError: String?,
    aiQuestion: String,
    aiAnswer: String?,
    isAskingQuestion: Boolean,
    aiQuestionError: String?,
    cityName: String,
    onGenerateBriefing: () -> Unit,
    onAskQuestion: (String) -> Unit,
    onClearQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val quickQuestions = listOf(
        "Faut-il un parapluie ?",
        "Comment m'habiller ?",
        "Bon moment pour courir ?"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(
                shape = RoundedCornerShape(26.dp),
                baseAlpha = 0.20f,
                borderAlpha = 0.45f,
                elevation = 8.dp
            )
            .padding(18.dp)
            .testTag("gemini_weather_card")
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF818CF8).copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CONSEILS IA • GEMINI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.95f),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Analyse & recommandations pour $cityName",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onGenerateBriefing,
                    modifier = Modifier.testTag("refresh_ai_briefing_button")
                ) {
                    if (isGeneratingBriefing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Régénérer le bulletin",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Briefing Content Area
            if (aiBriefing.isNullOrEmpty() && !isGeneratingBriefing && aiBriefingError == null) {
                // Empty invitation state
                Surface(
                    onClick = onGenerateBriefing,
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Générer le bulletin intelligent du jour par Gemini",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            } else if (isGeneratingBriefing && aiBriefing.isNullOrEmpty()) {
                // Loading spinner & hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFDE047),
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Gemini analyse les conditions pour $cityName...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            } else if (!aiBriefing.isNullOrEmpty()) {
                // Formatted Briefing Text with streaming indicator if active
                Text(
                    text = aiBriefing,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                )
                if (isGeneratingBriefing) {
                    Text(
                        text = " ▋",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFDE047))
                    )
                }
            } else if (aiBriefingError != null) {
                // Error state with retry
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = aiBriefingError,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFCA5A5))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Appuyez sur actualiser pour réessayer.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { onGenerateBriefing() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Questions FlowRow
            Text(
                text = "QUESTIONS RAPIDES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.75f),
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable {
                                inputQuery = question
                                onAskQuestion(question)
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Question Input
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_question_input"),
                placeholder = {
                    Text(
                        "Poser une question météo à Gemini...",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 13.sp
                    )
                },
                trailingIcon = {
                    if (isAskingQuestion) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (inputQuery.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val q = inputQuery.trim()
                                onAskQuestion(q)
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.testTag("send_gemini_question_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Envoyer",
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    val q = inputQuery.trim()
                    if (q.isNotEmpty()) {
                        onAskQuestion(q)
                        focusManager.clearFocus()
                    }
                }),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                )
            )

            // Q&A Answer Display
            AnimatedVisibility(
                visible = !aiAnswer.isNullOrEmpty() || isAskingQuestion || aiQuestionError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    if (aiQuestion.isNotEmpty()) {
                        Text(
                            text = "“$aiQuestion”",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (!aiAnswer.isNullOrEmpty()) {
                        Text(
                            text = aiAnswer,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        )
                    } else if (isAskingQuestion) {
                        Text(
                            text = "Gemini réfléchit...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    } else if (aiQuestionError != null) {
                        Text(
                            text = aiQuestionError,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFCA5A5)
                            )
                        )
                    }
                }
            }
        }
    }
}
