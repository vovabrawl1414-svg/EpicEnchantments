package net.vova.epicenchantments.client;

import net.vova.epicenchantments.EpicEnchantments;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {

    public static void sendReport(String playerName, String reportText, Callback callback) {
        new Thread(() -> {
            CloseableHttpClient client = HttpClients.createDefault();
            try {
                String json = String.format("{\"name\":\"%s\", \"text\":\"%s\"}",
                        escapeJson(playerName), escapeJson(reportText));

                HttpPost post = new HttpPost(EpicEnchantments.SERVER_URL + "/reports");
                post.setHeader("Content-Type", "application/json");
                post.setEntity(new StringEntity(json, "UTF-8"));

                org.apache.http.HttpResponse response = client.execute(post);
                try {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        callback.onSuccess("Отправлено!");
                    } else {
                        callback.onFailure("Ошибка сервера: " + statusCode);
                    }
                } finally {
                    // Закрываем response вручную
                    if (response instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) response).close();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }).start();
    }

    public interface Callback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}