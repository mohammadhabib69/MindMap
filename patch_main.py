import re

with open('src/main/java/com/mindmap/controller/MainController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.INFORMATION\);\s*alert\.setTitle\("About MindMap"\);\s*alert\.setHeaderText\("MindMap: Personal Knowledge Base & Study Organizer"\);\s*alert\.setContentText\("""([^"]+)"""\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showInfo("About MindMap", "MindMap: Personal Knowledge Base & Study Organizer\\n\\n"""\1""");', content)


with open('src/main/java/com/mindmap/controller/MainController.java', 'w') as f:
    f.write(content)
