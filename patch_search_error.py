import re

with open('src/main/java/com/mindmap/controller/SearchController.java', 'r') as f:
    content = f.read()

content = re.sub(r'''if \(reqId == searchRequestId\.get\(\)\) \{\s*LOGGER\.log\(Level\.SEVERE, "Background search error: " \+ throwable\.getMessage\(\), throwable\);\s*\}''',
                 r'''if (reqId == searchRequestId.get()) {
                        LOGGER.log(Level.SEVERE, "Background search error: " + throwable.getMessage(), throwable);
                        UiUtils.showError("Search Error", "An error occurred during search: " + throwable.getMessage());
                    }''', content)

with open('src/main/java/com/mindmap/controller/SearchController.java', 'w') as f:
    f.write(content)
