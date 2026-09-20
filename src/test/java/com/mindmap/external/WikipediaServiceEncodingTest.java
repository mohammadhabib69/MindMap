package com.mindmap.external;

import com.mindmap.export.JsonUtil;
import com.mindmap.external.dto.WikipediaPageSummary;
import com.mindmap.external.dto.WikipediaSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WikipediaServiceEncodingTest {

    private WikipediaService service;
    private MockWikipediaClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = new MockWikipediaClient();
        service = new WikipediaService(mockClient, JsonUtil.getMapper());
    }

    @Test
    void testSearch_MultiWordEncoding() {
        mockClient.setResponse("{\"query\":{\"search\":[]}}");
        service.search("Machine Learning");
        assertTrue(mockClient.getLastUrl().contains("Machine+Learning"));
    }

    @Test
    void testSearch_BengaliEncoding() {
        mockClient.setResponse("{\"query\":{\"search\":[]}}");
        service.search("বাংলাদেশ");
        String expected = URLEncoder.encode("বাংলাদেশ", StandardCharsets.UTF_8);
        assertTrue(mockClient.getLastUrl().contains(expected));
    }

    @Test
    void testSearch_SpecialCharacterEncoding() {
        mockClient.setResponse("{\"query\":{\"search\":[]}}");
        service.search("C++");
        assertTrue(mockClient.getLastUrl().contains("C%2B%2B"));
    }

    @Test
    void testSummary_MultiWordEncodingRestApi() {
        mockClient.setResponse("{\"title\":\"Machine Learning\",\"extract\":\"info\",\"content_urls\":{\"desktop\":{\"page\":\"https://url\"}}}");
        service.getSummary("Machine Learning");
        
        // Ensure space is replaced by underscore or %20 and NOT +
        assertTrue(mockClient.getLastUrl().endsWith("Machine_Learning") || mockClient.getLastUrl().endsWith("Machine%20Learning"));
        assertFalse(mockClient.getLastUrl().endsWith("Machine+Learning"));
    }

    @Test
    void testSummary_SpecialCharacterEncodingRestApi() {
        mockClient.setResponse("{\"title\":\"C++\",\"extract\":\"info\",\"content_urls\":{\"desktop\":{\"page\":\"https://url\"}}}");
        service.getSummary("C++");
        
        assertTrue(mockClient.getLastUrl().endsWith("C%2B%2B"));
    }

    @Test
    void testSummary_BengaliEncodingRestApi() {
        mockClient.setResponse("{\"title\":\"বাংলাদেশ\",\"extract\":\"info\",\"content_urls\":{\"desktop\":{\"page\":\"https://url\"}}}");
        service.getSummary("বাংলাদেশ");
        
        String expected = URLEncoder.encode("বাংলাদেশ", StandardCharsets.UTF_8);
        assertTrue(mockClient.getLastUrl().endsWith(expected));
    }

    private static class MockWikipediaClient extends WikipediaClient {
        private String response;
        private String lastUrl;

        public void setResponse(String response) {
            this.response = response;
        }

        public String getLastUrl() {
            return lastUrl;
        }

        @Override
        public String sendGetRequest(String url) {
            this.lastUrl = url;
            return response;
        }
    }
}
