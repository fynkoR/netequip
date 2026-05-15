package ru.ssau.netequip.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {

    private final String secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public TokenService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 characters long");
        }
        this.secret = secret;
    }

    /**
     * Генерация токена: base64Url(payload).base64Url(signature)
     */
    public String generateToken(Map<String, Object> payload) {
        try {
            // Шаг 1: Преобразование payload в JSON
            String json = mapper.writeValueAsString(payload);

            // Шаг 2: Кодирование payload в Base64 URL
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes());

            // Шаг 3: Создание подписи HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);
            byte[] signatureBytes = mac.doFinal(encodedPayload.getBytes());

            // Шаг 4: Кодирование подписи в Base64 URL
            String encodedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureBytes);

            // Шаг 5: Сборка токена
            return encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    /**
     * Проверка токена: валидация подписи и срока действия.
     * Возвращает payload если токен валиден, иначе null.
     */
    public Map<String, Object> validateToken(String token) {
        try {
            // Проверка структуры — должно быть ровно 2 части
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            String encodedPayload = parts[0];
            String encodedSignature = parts[1];

            // Проверка подписи
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);
            byte[] expectedSignatureBytes = mac.doFinal(encodedPayload.getBytes());
            String expectedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(expectedSignatureBytes);

            if (!expectedSignature.equals(encodedSignature)) {
                return null; // подпись неверна
            }

            // Декодирование payload
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            Map<String, Object> payload = mapper.readValue(decoded,
                    new TypeReference<Map<String, Object>>() {});

            // Проверка срока действия
            Object expObj = payload.get("exp");
            if (expObj == null) {
                return null;
            }
            long exp = ((Number) expObj).longValue();
            long now = System.currentTimeMillis() / 1000;
            if (now > exp) {
                return null; // токен истёк
            }

            return payload;
        } catch (Exception e) {
            return null;
        }
    }
}
