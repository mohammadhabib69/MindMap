with open('src/main/java/com/mindmap/controller/QuizController.java', 'r') as f:
    text = f.read()

target = "text.setWrapText(true);"
replacement = "text.setWrapText(true);\n                text.prefWidthProperty().bind(listQuestionBank.widthProperty().subtract(60));"

text = text.replace(target, replacement)

with open('src/main/java/com/mindmap/controller/QuizController.java', 'w') as f:
    f.write(text)
