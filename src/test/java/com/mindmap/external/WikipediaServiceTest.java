package com.mindmap.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.export.JsonUtil;
import com.mindmap.external.dto.WikipediaPageSummary;
import com.mindmap.external.dto.WikipediaSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WikipediaServiceTest {

    private WikipediaService service;
    private MockWikipediaClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = new MockWikipediaClient();
        service = new WikipediaService(mockClient, JsonUtil.getMapper());
    }

    @Test
    void testSearch_ValidResponse() {
        mockClient.setResponse("{\"query\":{\"search\":[{\"title\":\"Test Title\",\"snippet\":\"Test snippet HTML <b>bold</b>\"}]}}");
        
        List<WikipediaSearchResult> results = service.search("Test");
        
        assertEquals(1, results.size());
        assertEquals("Test Title", results.get(0).getTitle());
        // Verify HTML tags are stripped
        assertEquals("Test snippet HTML bold", results.get(0).getSnippet());
        assertEquals("https://en.wikipedia.org/wiki/Test_Title", results.get(0).getPageUrl());
    }

    @Test
    void testSearch_EmptyResult() {
        mockClient.setResponse("{\"query\":{\"search\":[]}}");
        
        List<WikipediaSearchResult> results = service.search("Nothing");
        
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearch_MalformedJson() {
        mockClient.setResponse("{invalid json");
        
        assertThrows(ExternalApiException.class, () -> {
            service.search("Test");
        });
    }

    @Test
    void testSummary_ValidResponse() {
        mockClient.setResponse("{\"title\":\"Machine Learning\",\"extract\":\"Extract summary\",\"content_urls\":{\"desktop\":{\"page\":\"https://url\"}},\"thumbnail\":{\"source\":\"https://thumb\"}}");
        
        WikipediaPageSummary summary = service.getSummary("Machine Learning");
        
        assertNotNull(summary);
        assertEquals("Machine Learning", summary.getTitle());
        assertEquals("Extract summary", summary.getExtract());
        assertEquals("https://url", summary.getPageUrl());
        assertEquals("https://thumb", summary.getThumbnailUrl());
    }

    @Test
    void testSummary_MissingThumbnail() {
        mockClient.setResponse("{\"title\":\"Topic\",\"extract\":\"Info\",\"content_urls\":{\"desktop\":{\"page\":\"https://url\"}}}");
        
        WikipediaPageSummary summary = service.getSummary("Topic");
        
        assertNotNull(summary);
        assertEquals("Topic", summary.getTitle());
        assertNull(summary.getThumbnailUrl());
    }

    @Test
    void testNetworkError_ThrowsExternalApiException() {
        mockClient.setShouldThrow(true);
        
        assertThrows(ExternalApiException.class, () -> {
            service.search("Test");
        });
    }

    @Test
    void testEmptySearchQueryReturnsEmptyList() {
        List<WikipediaSearchResult> results1 = service.search("");
        List<WikipediaSearchResult> results2 = service.search(null);
        
        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
    }

    // A simple mock for testing to avoid importing Mockito
    private static class MockWikipediaClient extends WikipediaClient {
        private String response;
        private boolean shouldThrow = false;

        public void setResponse(String response) {
            this.response = response;
        }

        public void setShouldThrow(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public String sendGetRequest(String url) {
            if (shouldThrow) {
                throw new ExternalApiException("Network error");
            }
            return response;
        }
    }
}
