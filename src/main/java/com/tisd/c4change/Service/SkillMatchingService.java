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
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.LongBuffer;
import java.util.*;

@Service
public class SkillMatchingService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private final OrtEnvironment env;
    private final OrtSession session;
    private final Map<String, Integer> tokenizerVocab;
    private final Map<String, Integer> tokenizerIds;

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
        initializeSimpleTokenizer();
    }

    private void initializeSimpleTokenizer() {
        // This is a simplified tokenizer initialization
        // In a real application, you would load the actual tokenizer vocabulary
        tokenizerVocab.put("[PAD]", 0);
        tokenizerVocab.put("[UNK]", 1);
        tokenizerVocab.put("[CLS]", 2);
        tokenizerVocab.put("[SEP]", 3);
        tokenizerVocab.put("[MASK]", 4);

        // Reverse mapping for IDs to tokens
        for (Map.Entry<String, Integer> entry : tokenizerVocab.entrySet()) {
            tokenizerIds.put(entry.getKey(), entry.getValue());
        }
    }

    public List<ProjectMatch> matchVolunteerToProjects(List<String> volunteerSkills, List<Project> allProjects) {
        if (volunteerSkills == null || volunteerSkills.isEmpty() || allProjects == null || allProjects.isEmpty()) {
            return Collections.emptyList();
        }

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

            return matches;
        } catch (Exception e) {
            logger.error("Error in skill matching", e);
            return Collections.emptyList();
        }
    }

    private float[] getEmbedding(String text) throws Exception {
        // Simple tokenization - replace with proper tokenizer in production
        String[] tokens = text.toLowerCase().split("\\s+");
        long[] inputIds = new long[tokens.length];
        long[] attentionMask = new long[tokens.length];
        long[] tokenTypeIds = new long[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            inputIds[i] = tokenizerVocab.getOrDefault(tokens[i], 1); // Default to UNK token
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
