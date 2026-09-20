package com.mindmap.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.export.JsonUtil;
import com.mindmap.external.dto.WikipediaPageSummary;
import com.mindmap.external.dto.WikipediaSearchResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WikipediaService {
    private static final Logger LOGGER = Logger.getLogger(WikipediaService.class.getName());
    
    private final WikipediaClient client;
    private final ObjectMapper mapper;

    public WikipediaService() {
        this(new WikipediaClient(), JsonUtil.getMapper());
    }

    public WikipediaService(WikipediaClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<WikipediaSearchResult> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" 
                         + encodedQuery + "&utf8=&format=json";
            
            String jsonResponse = client.sendGetRequest(url);
            JsonNode rootNode = mapper.readTree(jsonResponse);
            
            List<WikipediaSearchResult> results = new ArrayList<>();
            JsonNode searchNode = rootNode.path("query").path("search");
            
            if (searchNode.isArray()) {
                for (JsonNode node : searchNode) {
                    String title = node.path("title").asText("");
                    String snippet = node.path("snippet").asText("").replaceAll("\\<.*?\\>", ""); // strip HTML tags
                    String pageUrl = "https://en.wikipedia.org/wiki/" + URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8);
                    
                    results.add(new WikipediaSearchResult(title, snippet, pageUrl));
                }
            }
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to search Wikipedia", e);
            throw new ExternalApiException("Error searching Wikipedia: " + e.getMessage(), e);
        }
    }

    public WikipediaPageSummary getSummary(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedTitle = URLEncoder.encode(title.trim().replace(" ", "_"), StandardCharsets.UTF_8).replace("+", "%20");
            String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encodedTitle;
            
            String jsonResponse = client.sendGetRequest(url);
            JsonNode rootNode = mapper.readTree(jsonResponse);
            
            WikipediaPageSummary summary = new WikipediaPageSummary();
            summary.setTitle(rootNode.path("title").asText(""));
            summary.setExtract(rootNode.path("extract").asText(""));
            summary.setPageUrl(rootNode.path("content_urls").path("desktop").path("page").asText(""));
            
            JsonNode thumbnailNode = rootNode.path("thumbnail");
            if (!thumbnailNode.isMissingNode() && thumbnailNode.has("source")) {
                summary.setThumbnailUrl(thumbnailNode.path("source").asText(""));
            }
            
            return summary;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get Wikipedia summary", e);
            throw new ExternalApiException("Error retrieving Wikipedia summary: " + e.getMessage(), e);
        }
    }
}
