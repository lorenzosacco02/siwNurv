package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    /*
    * Invia una notifica formattata al gruppo Telegram in modo asincrono.
     */

    @Async
    public void inviaAlertCritico(String tipoAnomalia, String severita, String dettagli, String videoSorgente){
        /*
        System.out.println("Debug Telegram - Token letto: ["+ botToken +"]");
        System.out.println("Debug Telegram - Chat ID letto: ["+ chatId +"]");

        //Controllo di sicurezza per evitare chiamate se non configurato
        if(botToken == null || botToken.isEmpty() || botToken.contains("8973547285:AAGP7uJKYQ_V8Y3folBAaRhBkW-0igZx1EY")){
            System.out.println("[Telegram] Bot non configurato in application.properties. Salto l'invio.");
            return;
        }



        String url = "https://api.telegram.org/Bot" + botToken + "/sendMessage";
        */

        //1. Pulizia drastica da spazi o caratteri a capo invisibili
        String cleanToken = botToken != null ? botToken.trim() : "";
        String cleanChatId = chatId != null ? chatId.trim() : "";

        //2. Uso java.net.URI per impedire a java di modificare i ":" del token in "%3A"
        String urlString = "https://api.telegram.org/bot" + cleanToken + "/sendMessage";
        URI uri = URI.create(urlString);

        //Costruiamo il testo del messaggio sfruttando il Markdown di Telegram
        String testoMessaggio = String.format(
                "🚨 *ALERT SISTEMA NURV* 🚨\n\n" +
                        "📌 *Anomalia:* %s\n" +
                        "⚠️ *Severità:* %s\n" +
                        "🎬 *Video:* %s\n" +
                        "📝 *Dettagli:* %s\n\n" +
                        "👉 _Accedere alla Central Control Station per validare il report._",
                tipoAnomalia, severita, videoSorgente, dettagli
        );

        //Prepariamo il body della richiesta HTTP POST
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", testoMessaggio);
        requestBody.put("parse_mode", "Markdown");

        try{
            restTemplate.postForObject(uri,requestBody,String.class);
            System.out.println("✅ [Telegram] Notifica inviata con successo per anomalia!");
        } catch (Exception e) {
            System.out.println("❌ [Telegram] Errore durante l'invio della notifica: " + e.getMessage());
        }
    }
}
