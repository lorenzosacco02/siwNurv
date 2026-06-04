package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramService {

    private final TelegramBotListener botListener;

    // Inietto il listener per usare il suo metodo di invio
    public TelegramService(TelegramBotListener botListener) {
        this.botListener = botListener;
    }

    @Async
    public void inviaAlertCritico(String targetChatId, String tipoAnomalia, String severita, String dettagli, String videoSorgente) {
        if (targetChatId == null || targetChatId.isEmpty()) return;

        String testo = String.format(
                "🚨 *ALERT SISTEMA NURV* 🚨\n\n" +
                        "📌 *Anomalia:* %s\n" +
                        "⚠️ *Severità:* %s\n" +
                        "🎬 *Video:* %s\n" +
                        "📝 *Dettagli:* %s\n\n" +
                        "👉 _Accedere alla Central Control Station per validare il report._",
                tipoAnomalia.replace("_", " "), severita, videoSorgente, dettagli
        );

        // Usiamo il listener per eseguire l'invio fisico
        botListener.inviaRisposta(targetChatId, testo);
        System.out.println("✅ [Telegram] Alert inviato con successo!");
    }
}