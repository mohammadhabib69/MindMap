with open('src/main/java/com/mindmap/controller/QuizController.java', 'r') as f:
    text = f.read()

import re
text = re.sub(r'text\.prefWidthProperty\(\)\.bind\(.*?\);\n', '', text)
text = text.replace("text.setWrapText(true);", "text.setWrapText(true);\n                text.prefWidthProperty().bind(listQuestionBank.widthProperty().subtract(80));")

with open('src/main/java/com/mindmap/controller/QuizController.java', 'w') as f:
    f.write(text)
