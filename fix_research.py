import re

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    content = f.read()

content = content.replace('"Unable to connect to Wikipedia.\nPlease check your internet connection and try again."', '"Unable to connect to Wikipedia.\\nPlease check your internet connection and try again."')
content = content.replace('"Wikipedia returned an error.\nPlease try again later.\nDetails: "', '"Wikipedia returned an error.\\nPlease try again later.\\nDetails: "')

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(content)
