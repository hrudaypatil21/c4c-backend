package com.tisd.c4change.Service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ch.qos.logback.core.subst.Tokenizer;
import com.tisd.c4change.DTO.ProjectDTO.ProjectMatch;
import com.tisd.c4change.Entity.Project;
import com.tisd.c4change.FirebaseAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.server.Encoding;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.LongBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillMatchingService {
    private static final Logger logger = LoggerFactory.getLogger(SkillMatchingService.class);
    private final OrtEnvironment env;
    private final OrtSession session;
    private final Map<String, Integer> tokenizerVocab;
    private final Map<Integer, String> tokenizerIds;
    private Set<String> commonSkills;

    public SkillMatchingService() throws Exception {
        // Initialize ONNX Runtime environment
        this.env = OrtEnvironment.getEnvironment();

        // Load the pre-trained model
        try (InputStream modelStream = getClass().getResourceAsStream("/model.onnx")) {
            byte[] modelBytes = modelStream.readAllBytes();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            this.session = env.createSession(modelBytes, options);
        }

        // Initialize a simple tokenizer (for demonstration)
        // In production, you should use a proper tokenizer library
        this.tokenizerVocab = new HashMap<>();
        this.tokenizerIds = new HashMap<>();
        this.commonSkills = new HashSet<>();
        initializeEnhancedTokenizer();
    }

    private void initializeEnhancedTokenizer() {
        // Add common technical skills
        String[] technicalSkills = {
                "programming", "coding", "development", "software", "web", "mobile",
                "frontend", "backend", "fullstack", "database", "sql", "nosql",
                "java", "python", "javascript", "typescript", "c++", "c#", "php",
                "html", "css", "react", "angular", "vue", "node", "spring",
                "docker", "kubernetes", "aws", "azure", "gcp", "devops",
                "machine learning", "ai", "data science", "analytics", "big data"
        };

        // Add common non-technical skills
        String[] softSkills = {
                "communication", "leadership", "teamwork", "problem solving",
                "time management", "adaptability", "creativity", "critical thinking",
                "collaboration", "public speaking", "writing", "presentation",
                "project management", "organization", "research", "teaching",
                "mentoring", "coaching", "customer service", "negotiation"
        };

        // Add domain-specific skills
        String[] domainSkills = {
                "healthcare", "medical", "nursing", "first aid", "cpr",
                "education", "teaching", "tutoring", "curriculum",
                "environment", "sustainability", "conservation", "recycling",
                "construction", "carpentry", "electrical", "plumbing",
                "graphic design", "ui/ux", "illustration", "photography",
                "marketing", "social media", "seo", "content creation"
        };

        // Combine all skills
        Set<String> allSkills = new HashSet<>();
        allSkills.addAll(Arrays.asList(technicalSkills));
        allSkills.addAll(Arrays.asList(softSkills));
        allSkills.addAll(Arrays.asList(domainSkills));
        this.commonSkills = allSkills;

        // Build vocabulary with meaningful IDs
        int id = 5; // Start after special tokens
        for (String skill : allSkills) {
            // Add both full skill and individual words
            String[] words = skill.split("\\s+");
            for (String word : words) {
                if (!tokenizerVocab.containsKey(word)) {
                    tokenizerVocab.put(word, id);
                    tokenizerIds.put(id, word);
                    id++;
                }
            }

            // Add multi-word phrases
            if (words.length > 1) {
                tokenizerVocab.put(skill, id);
                tokenizerIds.put(id, skill);
                id++;
            }
        }

        // Add special tokens
        tokenizerVocab.put("[PAD]", 0);
        tokenizerVocab.put("[UNK]", 1);
        tokenizerVocab.put("[CLS]", 2);
        tokenizerVocab.put("[SEP]", 3);
        tokenizerVocab.put("[MASK]", 4);
    }

    @CacheEvict(value = "recommendations", allEntries = true)
    public List<ProjectMatch> matchVolunteerToProjects(List<String> volunteerSkills, List<Project> allProjects) {
        if (volunteerSkills == null || volunteerSkills.isEmpty() || allProjects == null || allProjects.isEmpty()) {
            return Collections.emptyList();
        }
        logger.info("Matching projects for skills: {}", volunteerSkills);
        logger.info("Total projects to match against: {}", allProjects.size());

        try {
            // Encode volunteer skills
            float[] volunteerEmbedding;
            try {
                volunteerEmbedding = getEmbedding(String.join(", ", volunteerSkills));
            } catch (Exception e) {
                logger.error("Failed to get volunteer embedding", e);
                return Collections.emptyList();
            }

            List<ProjectMatch> matches = new ArrayList<>();

            for (Project project : allProjects) {
                if (project.getSkills() == null || project.getSkills().isEmpty()) {
                    continue;
                }

                try {
                    // Encode project required skills
                    float[] projectEmbedding = getEmbedding(String.join(", ", project.getSkills()));

                    // Calculate cosine similarity
                    double similarity = cosineSimilarity(volunteerEmbedding, projectEmbedding);
                    matches.add(new ProjectMatch(project, similarity));
                } catch (Exception e) {
                    logger.error("Failed to process project: " + project.getId(), e);
                    continue;
                }
            }

            // Sort by similarity score (descending)
            matches.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

            logger.info("Generated {} matches with scores: {}",
                    matches.size(),
                    matches.stream().map(m -> m.getSimilarityScore()).collect(Collectors.toList()));

            return matches;
        } catch (Exception e) {
            logger.error("Error in skill matching", e);
            return Collections.emptyList();
        }
    }

    private float[] getEmbedding(String text) throws Exception {
        // Simple tokenization - replace with proper tokenizer in production
        String processedText = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s/]", "") // Remove special chars
                .replaceAll("\\s+", "");

                        // Tokenize with attention to multi-word skills
                        // Tokenize with attention to multi-word skill
                        List<String> tokens = new ArrayList<>();

        // First try to match multi-word skills from our vocabulary
        for (String skill : commonSkills) {
            if (processedText.contains(skill)) {
                tokens.add(skill);
                processedText = processedText.replace(skill, ""); // Remove matched skill
            }
        }

        // Then add remaining individual words
        String[] words = processedText.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                tokens.add(word);
            }
        }

        // Convert tokens to IDs
        long[] inputIds = new long[tokens.size()];
        long[] attentionMask = new long[tokens.size()];
        long[] tokenTypeIds = new long[tokens.size()];

        for (int i = 0; i < tokens.size(); i++) {
            inputIds[i] = tokenizerVocab.getOrDefault(tokens.get(i), 1); // Default to UNK
            attentionMask[i] = 1;
            tokenTypeIds[i] = 0;
        }

        // Create input tensors
        long[] shape = new long[]{1, inputIds.length};
        OnnxTensor idsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape);
        OnnxTensor maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape);
        OnnxTensor typeTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape);

        // Run inference
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input_ids", idsTensor);
        inputs.put("attention_mask", maskTensor);
        inputs.put("token_type_ids", typeTensor);

        try (OrtSession.Result results = session.run(inputs)) {
            // Handle the 3D output properly
            float[][][] output = (float[][][]) results.get(0).getValue();

            // Extract the embeddings - assuming shape [1, sequence_length, embedding_dim]
            float[][] embeddings = output[0];

            // Average pooling across tokens to get sentence embedding
            float[] sentenceEmbedding = new float[embeddings[0].length];
            for (float[] tokenEmbedding : embeddings) {
                for (int i = 0; i < tokenEmbedding.length; i++) {
                    sentenceEmbedding[i] += tokenEmbedding[i];
                }
            }

            // Normalize
            for (int i = 0; i < sentenceEmbedding.length; i++) {
                sentenceEmbedding[i] /= embeddings.length;
            }

            return sentenceEmbedding;
        }
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        return normA == 0 || normB == 0 ? 0.0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
