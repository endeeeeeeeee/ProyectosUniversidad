package com.simkernel.ai;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class GroqHelper {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public GroqHelper(String apiKey) {
        this.apiKey = apiKey;
    }

    public String generarRespuesta(String prompt) throws IOException {
        // Construir el cuerpo JSON usando ObjectMapper para evitar errores de formato
        ObjectNode root = mapper.createObjectNode();
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        root.set("messages", messages);
        root.put("model", "llama3-8b-8192");
        String json = mapper.writeValueAsString(root);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Error en la respuesta de Groq: " + response);
            }
            String responseBody = response.body().string();
            return mapper.readTree(responseBody)
                    .get("choices").get(0)
                    .get("message").get("content")
                    .asText()
                    .trim();
        }
    }
}