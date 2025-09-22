package com.bajajfinserv.qualifier.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class StartupService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public StartupService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Application started. Initiating webhook generation...");
        
        try {
            // Step 1: Generate webhook
            String webhookUrl = generateWebhook();
            
            if (webhookUrl != null) {
                logger.info("Webhook generated successfully. Processing SQL solution...");
                
                // Step 2: Solve SQL problem (Question 2 for even regNo)
                String sqlSolution = solveSqlProblem();
                
                // Step 3: Submit solution
                submitSolution(webhookUrl, sqlSolution);
            }
        } catch (Exception e) {
            logger.error("Error during execution: ", e);
        }
    }
    
    private String generateWebhook() {
        try {
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");
            requestBody.put("regNo", "0101CS221094");
            requestBody.put("email", "john@example.com");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String webhook = jsonNode.get("webhook").asText();
                String accessToken = jsonNode.get("accessToken").asText();
                
                logger.info("Webhook URL: {}", webhook);
                logger.info("Access Token received");
                
                // Store access token for later use
                System.setProperty("accessToken", accessToken);
                
                return webhook;
            } else {
                logger.error("Failed to generate webhook. Status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error generating webhook: ", e);
            return null;
        }
    }
    
    private String solveSqlProblem() {
        // Question 2: Calculate number of employees younger than each employee in their department
        
        logger.info("Solving SQL Problem for Question 2 (even regNo)");
        
        String sqlQuery = """
            SELECT 
                e1.EMP_ID,
                e1.FIRST_NAME,
                e1.LAST_NAME,
                d.DEPARTMENT_NAME,
                COUNT(e2.EMP_ID) as YOUNGER_EMPLOYEES_COUNT
            FROM EMPLOYEE e1
            JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
            LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB
            GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
            ORDER BY e1.EMP_ID DESC;
            """;
        
        logger.info("SQL Solution generated: {}", sqlQuery);
        return sqlQuery.trim();
    }
    
    private void submitSolution(String webhookUrl, String sqlQuery) {
        try {
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
            String accessToken = System.getProperty("accessToken");
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("finalQuery", sqlQuery);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", accessToken);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Solution submitted successfully!");
                logger.info("Response: {}", response.getBody());
            } else {
                logger.error("Failed to submit solution. Status: {}", response.getStatusCode());
                logger.error("Response: {}", response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error submitting solution: ", e);
        }
    }
}