package com.github.theapache64.primescreams

import com.github.theapache64.primescreams.model.RulesItem
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

private val rules: List<RulesItem> by lazy {
    Json.decodeFromString(PrimeScreamsListener::class.java.getResource("/rules.json").readText().also {
        println("Content: $it")
    })
}

fun invokeLaterOnEDT(block: () -> Unit) =
    ApplicationManager.getApplication().invokeAndWait(block, ModalityState.NON_MODAL)

fun show(
    message: String,
    title: String = "",
    notificationType: NotificationType = NotificationType.INFORMATION,
    groupDisplayId: String = "",
    notificationListener: NotificationListener? = null
) {
    invokeLaterOnEDT {
        val notification = Notification(groupDisplayId, title,
            // this is because Notification doesn't accept empty messages
            message.takeUnless { it.isBlank() } ?: "[ empty ]", notificationType, notificationListener)
        ApplicationManager.getApplication().messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
    }
}

class PrimeScreamsListener : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        super.documentChanged(event)
        if (event.isWholeTextReplaced) return
        val file = FileDocumentManager.getInstance().getFile(event.document)
        val rule = rules.find { it.extensions.contains(file?.extension) } ?: return
        val words = event.getWordSet().also { println("Words: $it") }
        val matchingRule = rule.rules.find { x -> x.words.any { y -> words.contains(y) } } ?: return
        println("Matching rule: ${matchingRule.audio}")
        playSound(matchingRule.audio)
    }

    private fun DocumentEvent.getWordSet(): Set<String> {
        val words = mutableSetOf<String>()
        var x = offset
        while (true) {
            if (x < 0) break
            val word = document.getText(TextRange(x, offset + 1))
            if (word.isBlank() || word.contains("\n") || word.trim().contains(" ")) break
            words.add(word.trim())
            x--
        }
        return words
    }

    @Synchronized
    fun playSound(url: String) = Thread {
        try {
            val clip = AudioSystem.getClip()
            val resourceStream = javaClass.getResourceAsStream("/sounds/$url")
            resourceStream?.let {
                val bufferedInputStream = BufferedInputStream(resourceStream)
                val inputStream: AudioInputStream = AudioSystem.getAudioInputStream(
                    bufferedInputStream
                )
                clip.open(inputStream)
                clip.start()
            }
        } catch (e: Exception) {
            show(e.message.toString(), notificationType = NotificationType.ERROR)
        }
    }.start()
}



