package com.github.theapache64.primescreams.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RulesItem(
    @SerialName("extensions")
    val extensions: List<String>,
    @SerialName("rules")
    val rules: List<Rule>
)

@Serializable
data class Rule(
    @SerialName("words")
    val words: List<String>,
    @SerialName("audio")
    val audio: String // prime_screams.wave
)