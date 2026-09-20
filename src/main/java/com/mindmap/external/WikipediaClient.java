package com.mindmap.external;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WikipediaClient {

    private final HttpClient httpClient;

    public WikipediaClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    // Allows injecting a custom client for testing
    public WikipediaClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String sendGetRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "MindMapApp/1.0 (https://github.com/mindmap) Java-http-client")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new ExternalApiException("Wikipedia API returned HTTP " + response.statusCode());
            }
        } catch (IOException e) {
            throw new ExternalApiException("Network error connecting to Wikipedia", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Request to Wikipedia was interrupted", e);
        }
    }
}
