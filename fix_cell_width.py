import re

with open('src/main/java/com/mindmap/controller/QuizController.java', 'r') as f:
    text = f.read()

# find Label text = new Label(item.getQuestionText());
# and add text.prefWidthProperty().bind(listQuestionBank.widthProperty().subtract(60));

replacement = """
                Label text = new Label(item.getQuestionText());
                text.setWrapText(true);
                text.prefWidthProperty().bind(listQuestionBank.widthProperty().subtract(80));
                text.setStyle("-fx-font-size: 15px; -fx-text-fill: #0f172a; -fx-font-weight: 500;");
"""

text = re.sub(
    r'Label\s+text\s*=\s*new\s+Label\(item\.getQuestionText\(\)\);\s*text\.setWrapText\(true\);\s*text\.setStyle\("-fx-font-size:\s*15px;\s*-fx-text-fill:\s*#0f172a;\s*-fx-font-weight:\s*500;"\);',
    replacement,
    text
)

with open('src/main/java/com/mindmap/controller/QuizController.java', 'w') as f:
    f.write(text)
