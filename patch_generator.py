with open('src/main/java/com/mindmap/DataGenerator.java', 'r') as f:
    content = f.read()

content = content.replace('connectionService.addConnection(from.getId(), to.getId(), type);', 'connectionService.createConnection(from.getId(), to.getId(), type);')
content = content.replace('revisionService.recordReview', '// revisionService.recordReview')

with open('src/main/java/com/mindmap/DataGenerator.java', 'w') as f:
    f.write(content)
