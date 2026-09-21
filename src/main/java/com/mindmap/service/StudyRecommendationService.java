package com.mindmap.service;

import com.mindmap.model.Note;
import com.mindmap.model.Revision;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.RevisionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

public class StudyRecommendationService {

    public static class Recommendation {
        private final Note note;
        private final String reason;
        private final int score;

        public Recommendation(Note note, String reason, int score) {
            this.note = note;
            this.reason = reason;
            this.score = score;
        }

        public Note getNote() { return note; }
        public String getReason() { return reason; }
        public int getScore() { return score; }
    }

    private final NoteRepository noteRepository;
    private final RevisionRepository revisionRepository;

    public StudyRecommendationService() {
        this.noteRepository = new NoteRepository();
        this.revisionRepository = new RevisionRepository();
    }

    public List<Recommendation> getStudyNextRecommendations(int limit) {
        List<Recommendation> recs = new ArrayList<>();
        List<Note> allNotes = noteRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Note note : allNotes) {
            List<Revision> revs = revisionRepository.findByNoteId(note.getId());
            Optional<Revision> optRev = revs.isEmpty() ? Optional.empty() : Optional.of(revs.get(0));
            int score = 0;
            String primaryReason = "";

            if (optRev.isPresent()) {
                Revision rev = optRev.get();
                if (rev.getReviewDate().isBefore(today)) {
                    score += 50;
                    primaryReason = "Review overdue";
                } else if (rev.getReviewDate().isEqual(today)) {
                    score += 30;
                    primaryReason = "Due today";
                }
            } else {
                score += 15;
                primaryReason = "Never reviewed";
            }

            if ("Hard".equalsIgnoreCase(note.getDifficulty())) {
                score += 20;
                if (primaryReason.isEmpty() || primaryReason.equals("Never reviewed")) {
                    primaryReason = "Difficult concept";
                }
            }

            if (note.getConnectionCount() > 0) {
                score += Math.min(note.getConnectionCount() * 2, 20); // Cap at +20
                if (primaryReason.isEmpty()) {
                    primaryReason = "Highly connected concept";
                }
            }

            if (score > 0) {
                if (primaryReason.isEmpty()) {
                    primaryReason = "Recommended for review";
                }
                recs.add(new Recommendation(note, primaryReason, score));
            }
        }

        return recs.stream()
                .sorted(Comparator.comparingInt(Recommendation::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
