with open('src/main/java/com/mindmap/DataGenerator.java', 'r') as f:
    content = f.read()

content = content.replace('DatabaseInitializer.initialize();', 'try { DatabaseInitializer.initialize(); } catch (Exception e) {}')

with open('src/main/java/com/mindmap/DataGenerator.java', 'w') as f:
    f.write(content)
