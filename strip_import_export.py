import re

with open('src/main/java/com/mindmap/service/ImportExportService.java', 'r') as f:
    text = f.read()

pattern = r'\s*try \{\s*if \(rs\.getInt\("is_private"\) == 1\) \{\s*continue; // Skip private notes to prevent data leaks\s*\}\s*\} catch \(Exception ignored\) \{\}'
text = re.sub(pattern, '', text)

with open('src/main/java/com/mindmap/service/ImportExportService.java', 'w') as f:
    f.write(text)
