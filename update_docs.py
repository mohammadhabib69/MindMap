import os

docs = {
    'README.md': "## Quiz & Exam System\nGenerate quizzes directly from your notes using offline deterministic generation. Practice without time limits or challenge yourself in a timed Exam mode. Includes detailed post-exam review and scoring.\n\n",
    'docs/FEATURES.md': "### Quiz & Exam System\n- **Offline Generation**: Deterministically generates True/False, Multiple Choice, and Single Choice questions from your notes without any external API.\n- **Practice Mode**: Test your knowledge at your own pace.\n- **Exam Mode**: Timed exam conditions with auto-submit.\n- **Review**: Review missed questions and instantly jump back to the source note.\n\n",
    'docs/ARCHITECTURE.md': "### Quiz Module\n- **QuizGenerationService**: A deterministic, rule-based text processor that extracts sentences from `Note` content to construct valid MCQs, True/False, and Multi-select questions. Does NOT use LLMs or APIs, ensuring fast, offline privacy.\n- **QuizControllers**: Manages timer states (via `Timeline`) and dynamic `ToggleGroup` or `CheckBox` rendering for options.\n\n",
    'docs/DATABASE.md': "### Quiz Schema\n- `quiz_sessions`: Tracks attempt metadata, mode (PRACTICE/EXAM), timer limit, and scores.\n- `quiz_questions`: Links directly to `notes.id` (source_note_id) and stores randomized distractors, correct answers, and the user's selected answers in JSON arrays.\n\n",
    'docs/VIVA_PREPARATION.md': "### Q: How does the Quiz system generate questions without an API?\n**A:** It uses deterministic extraction. For example, it identifies the first sentence of a note as a core definition and pulls sentences from other random notes of the same subject as distractors. It also generates True/False statements by randomly deciding whether to present a true sentence from the current note or a false sentence from another note.\n\n",
    'docs/PRESENTATION_GUIDE.md': "### Slide: Quiz & Exam Engine\n- Emphasize **Offline capability**: No API keys required.\n- Show **Exam Mode**: Demonstrate the real-time countdown timer and the automatic submission feature.\n- Show **Source Tracking**: Show how reviewing a wrong answer lets you immediately open the source note to re-study.\n\n"
}

for file_path, append_text in docs.items():
    if os.path.exists(file_path):
        with open(file_path, 'a') as f:
            f.write(append_text)
    else:
        with open(file_path, 'w') as f:
            f.write("# " + os.path.basename(file_path).replace('.md', '') + "\n\n" + append_text)
            
print("Docs updated")
