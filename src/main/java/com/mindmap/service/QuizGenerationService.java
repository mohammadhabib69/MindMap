package com.mindmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.model.Note;
import com.mindmap.model.QuizConfiguration;
import com.mindmap.model.QuizQuestion;

import java.util.*;
import java.util.stream.Collectors;

public class QuizGenerationService {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<QuizQuestion> generateQuiz(List<Note> allNotes, QuizConfiguration config) {
        List<Note> filtered = filterNotes(allNotes, config);
        
        int requestedCount = config.getQuestionCount();
        List<QuizQuestion> questions = new ArrayList<>();
        
        if (filtered.isEmpty()) {
            return questions; // Return empty, controller handles UI
        }
        
        Random random = new Random();
        Set<Integer> usedNoteIds = new HashSet<>();
        
        // Ensure we try to hit different notes if possible
        Collections.shuffle(filtered, random);
        
        int index = 0;
        int attempts = 0;
        while (questions.size() < requestedCount && attempts < requestedCount * 3) {
            Note note = filtered.get(index % filtered.size());
            
            // Randomly select type if mixed
            String type = config.getType();
            if ("Mixed".equalsIgnoreCase(type) || "All Types".equalsIgnoreCase(type) || type == null) {
                String[] types = {"SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE"};
                type = types[random.nextInt(3)];
            }
            
            QuizQuestion q = generateQuestionFromNote(note, filtered, type, random);
            if (q != null && isUnique(q, questions)) {
                q.setQuestionOrder(questions.size() + 1);
                questions.add(q);
                usedNoteIds.add(note.getId());
            }
            
            index++;
            attempts++;
        }
        
        return questions;
    }
    
    private boolean isUnique(QuizQuestion q, List<QuizQuestion> existing) {
        for (QuizQuestion e : existing) {
            if (e.getQuestionText().equals(q.getQuestionText())) {
                return false;
            }
        }
        return true;
    }
    
    private List<Note> filterNotes(List<Note> allNotes, QuizConfiguration config) {
        return allNotes.stream().filter(n -> {
            boolean subjectMatch = config.getSubject() == null || config.getSubject().equalsIgnoreCase("All Subjects") 
                                   || n.getSubject().equalsIgnoreCase(config.getSubject());
            boolean diffMatch = config.getDifficulty() == null || config.getDifficulty().equalsIgnoreCase("All Difficulties")
                                || n.getDifficulty().equalsIgnoreCase(config.getDifficulty());
            // Topic filtering would require joining tags, but we'll approximate with content/title for now if tags are complex,
            // or assume topic is handled externally. For MVP offline, subject + diff is great.
            return subjectMatch && diffMatch;
        }).collect(Collectors.toList());
    }
    
    private QuizQuestion generateQuestionFromNote(Note note, List<Note> allNotes, String type, Random random) {
        try {
            if ("SINGLE_CHOICE".equalsIgnoreCase(type) || "Single Choice".equalsIgnoreCase(type)) {
                return generateSingleChoice(note, allNotes, random);
            } else if ("MULTIPLE_CHOICE".equalsIgnoreCase(type) || "Multiple Choice".equalsIgnoreCase(type)) {
                return generateMultipleChoice(note, allNotes, random);
            } else {
                return generateTrueFalse(note, allNotes, random);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private QuizQuestion generateSingleChoice(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        String firstSentence = getFirstSentence(note.getContent());
        if (firstSentence == null || firstSentence.length() < 10) return null;
        
        QuizQuestion q = new QuizQuestion();
        q.setSourceNoteId(note.getId());
        q.setSourceNote(note);
        q.setQuestionType("SINGLE_CHOICE");
        q.setQuestionText("What is the primary definition or concept of: " + note.getTitle() + "?");
        
        List<String> options = new ArrayList<>();
        options.add(firstSentence); // Correct
        
        List<String> distractors = getDistractors(allNotes, note.getId(), 3, random);
        while (distractors.size() < 3) {
            distractors.add("It is a concept related to " + (random.nextInt(100) + 1));
        }
        options.addAll(distractors);
        
        List<String> correct = List.of(firstSentence);
        Collections.shuffle(options, random);
        
        q.setOptionsJson(mapper.writeValueAsString(options));
        q.setCorrectAnswerJson(mapper.writeValueAsString(correct));
        return q;
    }
    
    private QuizQuestion generateMultipleChoice(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        List<String> sentences = getSentences(note.getContent());
        if (sentences.size() < 2) return null;
        
        Collections.shuffle(sentences, random);
        List<String> correctOpts = sentences.subList(0, Math.min(2, sentences.size()));
        
        QuizQuestion q = new QuizQuestion();
        q.setSourceNoteId(note.getId());
        q.setSourceNote(note);
        q.setQuestionType("MULTIPLE_CHOICE");
        q.setQuestionText("Which of the following statements apply to " + note.getTitle() + "? (Select all that apply)");
        
        List<String> options = new ArrayList<>(correctOpts);
        List<String> distractors = getDistractors(allNotes, note.getId(), 2, random);
        while (distractors.size() < 2) {
            distractors.add("It does not involve any significant structure.");
        }
        options.addAll(distractors);
        Collections.shuffle(options, random);
        
        q.setOptionsJson(mapper.writeValueAsString(options));
        q.setCorrectAnswerJson(mapper.writeValueAsString(correctOpts));
        return q;
    }
    
    private QuizQuestion generateTrueFalse(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        boolean isTrue = random.nextBoolean();
        String statement;
        
        if (isTrue) {
            statement = getFirstSentence(note.getContent());
        } else {
            List<String> distractors = getDistractors(allNotes, note.getId(), 1, random);
            if (!distractors.isEmpty()) {
                statement = distractors.get(0);
            } else {
                statement = "It has no relationship to any other subject.";
            }
        }
        
        if (statement == null || statement.length() < 10) return null;
        
        QuizQuestion q = new QuizQuestion();
        q.setSourceNoteId(note.getId());
        q.setSourceNote(note);
        q.setQuestionType("TRUE_FALSE");
        q.setQuestionText("True or False regarding " + note.getTitle() + ":\n\n\"" + statement + "\"");
        
        List<String> options = List.of("True", "False");
        List<String> correct = List.of(isTrue ? "True" : "False");
        
        q.setOptionsJson(mapper.writeValueAsString(options));
        q.setCorrectAnswerJson(mapper.writeValueAsString(correct));
        return q;
    }
    
    private String getFirstSentence(String content) {
        List<String> sentences = getSentences(content);
        return sentences.isEmpty() ? null : sentences.get(0);
    }
    
    private List<String> getSentences(String content) {
        if (content == null || content.trim().isEmpty()) return new ArrayList<>();
        // Simple sentence split
        String[] parts = content.split("(?<=\\.)\\s+");
        List<String> valid = new ArrayList<>();
        for (String p : parts) {
            String clean = p.replaceAll("\\n", " ").trim();
            if (clean.length() > 15) {
                valid.add(clean);
            }
        }
        return valid;
    }
    
    private List<String> getDistractors(List<Note> allNotes, int excludeNoteId, int count, Random random) {
        List<String> res = new ArrayList<>();
        List<Note> others = allNotes.stream().filter(n -> n.getId() != excludeNoteId).collect(Collectors.toList());
        Collections.shuffle(others, random);
        
        for (Note o : others) {
            String s = getFirstSentence(o.getContent());
            if (s != null && s.length() > 15) {
                res.add(s);
                if (res.size() >= count) break;
            }
        }
        return res;
    }
}
