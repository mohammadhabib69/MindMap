import re

with open('src/main/java/com/mindmap/controller/ResearchController.java', 'r') as f:
    content = f.read()

# Imports
if 'com.mindmap.util.UiUtils' not in content:
    content = content.replace('import java.util.logging.Logger;', 'import java.util.logging.Logger;\nimport com.mindmap.util.UiUtils;')


content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setHeaderText\("Wikipedia Integration Error"\);\s*if \(error instanceof ExternalApiException\) \{\s*if \(error\.getMessage\(\)\.contains\("interrupted"\) \|\| error\.getMessage\(\)\.contains\("Network error"\) \|\| error\.getMessage\(\)\.contains\("timed out"\)\) \{\s*alert\.setTitle\("Connection Error"\);\s*alert\.setContentText\("Unable to connect to Wikipedia\.\\nPlease check your internet connection and try again\."\);\s*\} else \{\s*alert\.setTitle\("API Error"\);\s*alert\.setContentText\("Wikipedia returned an error\.\\nPlease try again later\.\\nDetails: " \+ error\.getMessage\(\)\);\s*\}\s*\} else \{\s*alert\.setTitle\("Unexpected Error"\);\s*alert\.setContentText\("An unexpected error occurred: " \+ error\.getMessage\(\)\);\s*\}\s*alert\.show\(\);''',
                 r'''
            if (error instanceof ExternalApiException) {
                if (error.getMessage().contains("interrupted") || error.getMessage().contains("Network error") || error.getMessage().contains("timed out")) {
                    UiUtils.showError("Connection Error", "Unable to connect to Wikipedia.\nPlease check your internet connection and try again.");
                } else {
                    UiUtils.showError("API Error", "Wikipedia returned an error.\nPlease try again later.\nDetails: " + error.getMessage());
                }
            } else {
                UiUtils.showError("Unexpected Error", "An unexpected error occurred: " + error.getMessage());
            }
''', content)

content = re.sub(r'''Alert alert = new Alert\(Alert\.AlertType\.ERROR\);\s*alert\.setTitle\("Error"\);\s*alert\.setHeaderText\(null\);\s*alert\.setContentText\("Could not open note editor: " \+ e\.getMessage\(\)\);\s*alert\.showAndWait\(\);''',
                 r'UiUtils.showError("Error", "Could not open note editor: " + e.getMessage());', content)


with open('src/main/java/com/mindmap/controller/ResearchController.java', 'w') as f:
    f.write(content)
