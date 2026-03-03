package tn.hounayda.utils.GestionInvestissment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GroqAIService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final String API_KEY = ""; // paste your key here

    public static String suggestDescription(String title, String draft) throws Exception {
        String apiKey = API_KEY;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La clé GROQ_API_KEY n'est pas définie.");
        }

        String prompt = "Tu es un assistant créatif qui aide un artiste francophone à rédiger la description d'un projet artistique pour une plateforme de financement participatif.\n\n"
                + "Titre du projet : \"" + title + "\"\n"
                + "Brouillon actuel : \"" + draft + "\"\n\n"
                + "Réécris une description claire, inspirante et professionnelle en français (10 à 15 lignes maximum), "
                + "mettant en avant : l'univers artistique, les objectifs du projet, l'impact recherché et pourquoi les investisseurs devraient soutenir ce projet. "
                + "N'utilise pas de balises HTML, seulement du texte brut bien structuré avec quelques paragraphes courts.";

        String jsonBody = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"Tu es un assistant qui aide à rédiger des descriptions de projets artistiques en français.\"},"
                + "{\"role\":\"user\",\"content\":" + toJsonString(prompt) + "}"
                + "]"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Erreur API Groq: " + response.statusCode() + " - " + response.body());
        }

        return extractContentFromResponse(response.body());
    }

    private static String toJsonString(String text) {
        if (text == null) text = "";
        String escaped = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "\"" + escaped + "\"";
    }

    // Extraction simple du premier champ "content" dans la réponse JSON
    private static String extractContentFromResponse(String body) {
        String marker = "\"content\":\"";
        int idx = body.indexOf(marker);
        if (idx == -1) return body;
        int start = idx + marker.length();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escaped) {
                if (c == 'n') sb.append('\n');
                else if (c == 'r') sb.append('\r');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }
}
