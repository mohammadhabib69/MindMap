1. **Database Schema**:
   - `quiz_sessions`: id, title, mode, question_count, time_limit_seconds, started_at, completed_at, score, correct_count, incorrect_count, unanswered_count.
   - `quiz_questions`: id, quiz_session_id, source_note_id, question_text, question_type, options_json, correct_answer_json, user_answer_json, question_order, is_correct.
   - Update `DatabaseInitializer.java`.
   - Models: `QuizSession`, `QuizQuestion`.
   - Repositories: `QuizSessionRepository`, `QuizQuestionRepository`.

2. **QuizGenerationService**:
   - Filter notes.
   - Extract sentences.
   - Generate MCQ (Title -> Description).
   - Generate T/F.
   - Generate Multi-Select.

3. **Controllers**:
   - `QuizController.java`: Setup, Recent History.
   - `QuizSessionController.java`: Exam running, Timer, Question navigation, Submission.
   - `QuizResultController.java`: Score, Answers review.

4. **UI**:
   - `quiz.fxml`: Setup card, History list.
   - `quiz_session.fxml`: Timer, Progress, Question, Options, Prev/Next, Navigator.
   - `quiz_result.fxml`: Score summary, Review list.

5. **Integration**:
   - Sidebar in `main.fxml`.
