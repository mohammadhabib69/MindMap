package com.mindmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmap.model.BankQuestion;
import com.mindmap.model.Note;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class QuizGenerationService {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<BankQuestion> generateQuestions(List<Note> sourceNotes, List<Note> allNotes, int count, String targetType) {
        List<BankQuestion> questions = new ArrayList<>();
        if (sourceNotes.isEmpty()) return questions;

        Random random = new Random();
        List<Note> shuffledSources = new ArrayList<>(sourceNotes);
        Collections.shuffle(shuffledSources, random);

        int index = 0;
        int attempts = 0;
        
        while (questions.size() < count && attempts < count * 3) {
            Note note = shuffledSources.get(index % shuffledSources.size());
            
            String typeToGenerate = targetType;
            if (typeToGenerate == null || typeToGenerate.equals("Mixed") || typeToGenerate.equals("All Types")) {
                String[] types = {"SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE"};
                typeToGenerate = types[random.nextInt(3)];
            }
            
            BankQuestion q = generateQuestionFromNote(note, allNotes, typeToGenerate, random);
            
            if (q != null && isUnique(q, questions)) {
                questions.add(q);
            }
            
            index++;
            attempts++;
        }
        
        return questions;
    }
    
    private boolean isUnique(BankQuestion q, List<BankQuestion> existing) {
        for (BankQuestion e : existing) {
            if (e.getQuestionText().equals(q.getQuestionText())) {
                return false;
            }
        }
        return true;
    }

    private BankQuestion generateQuestionFromNote(Note note, List<Note> allNotes, String type, Random random) {
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
    
    private BankQuestion createBaseQuestion(Note note, String type) {
        BankQuestion q = new BankQuestion();
        q.setSourceNoteId(note.getId());
        q.setSourceNote(note);
        q.setQuestionType(type);
        q.setSubject(note.getSubject() == null ? "Uncategorized" : note.getSubject());
        q.setTopic("Generated"); // Could extract tags if needed
        q.setDifficulty(note.getDifficulty() == null ? "EASY" : note.getDifficulty());
        q.setCreatedAt(LocalDateTime.now().toString());
        return q;
    }

    private BankQuestion generateSingleChoice(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        String firstSentence = getFirstSentence(note.getContent());
        if (firstSentence == null || firstSentence.length() < 10) return null;
        
        BankQuestion q = createBaseQuestion(note, "SINGLE_CHOICE");
        q.setQuestionText("What is the primary definition or concept of: " + note.getTitle() + "?");
        q.setExplanation("The core concept of " + note.getTitle() + " is defined as: " + firstSentence);
        
        List<String> options = new ArrayList<>();
        options.add(firstSentence);
        
        List<String> distractors = getDistractors(allNotes, note.getId(), 3, random);
        while (distractors.size() < 3) {
            distractors.add("It relates to a general concept #" + random.nextInt(1000));
        }
        options.addAll(distractors);
        
        List<String> correct = List.of(firstSentence);
        Collections.shuffle(options, random);
        
        q.setOptionsJson(mapper.writeValueAsString(options));
        q.setCorrectAnswerJson(mapper.writeValueAsString(correct));
        return q;
    }

    private BankQuestion generateMultipleChoice(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        List<String> sentences = getSentences(note.getContent());
        if (sentences.size() < 2) return null;
        
        Collections.shuffle(sentences, random);
        List<String> correctOpts = sentences.subList(0, Math.min(2, sentences.size()));
        
        BankQuestion q = createBaseQuestion(note, "MULTIPLE_CHOICE");
        q.setQuestionText("Which of the following statements apply to " + note.getTitle() + "? (Select all that apply)");
        q.setExplanation("According to the source note, the valid statements are: " + String.join(" ", correctOpts));
        
        List<String> options = new ArrayList<>(correctOpts);
        List<String> distractors = getDistractors(allNotes, note.getId(), 2, random);
        while (distractors.size() < 2) {
            distractors.add("It is not typically associated with standard usage.");
        }
        options.addAll(distractors);
        Collections.shuffle(options, random);
        
        q.setOptionsJson(mapper.writeValueAsString(options));
        q.setCorrectAnswerJson(mapper.writeValueAsString(correctOpts));
        return q;
    }

    private BankQuestion generateTrueFalse(Note note, List<Note> allNotes, Random random) throws JsonProcessingException {
        boolean isTrue = random.nextBoolean();
        String statement;
        
        if (isTrue) {
            statement = getFirstSentence(note.getContent());
        } else {
            List<String> distractors = getDistractors(allNotes, note.getId(), 1, random);
            statement = !distractors.isEmpty() ? distractors.get(0) : "It functions independently of all other variables.";
        }
        
        if (statement == null || statement.length() < 10) return null;
        
        BankQuestion q = createBaseQuestion(note, "TRUE_FALSE");
        q.setQuestionText("True or False regarding " + note.getTitle() + ":\n\n\"" + statement + "\"");
        
        if (isTrue) {
            q.setExplanation("True. This statement accurately reflects the content of " + note.getTitle() + ".");
        } else {
            q.setExplanation("False. This statement does not apply to " + note.getTitle() + ".");
        }
        
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
        String[] parts = content.split("(?<=\\.)\\s+");
        List<String> valid = new ArrayList<>();
        for (String p : parts) {
            String clean = p.replaceAll("\\n", " ").trim();
            if (clean.length() > 15) valid.add(clean);
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
