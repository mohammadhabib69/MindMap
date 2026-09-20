import re

with open('src/main/java/com/mindmap/service/NoteService.java', 'r') as f:
    content = f.read()

# Make sanitizeTagNames use a case-insensitive unique filter
new_sanitize = '''    private List<String> sanitizeTagNames(List<String> rawTagNames) {
        if (rawTagNames == null || rawTagNames.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> lowerCaseNames = new java.util.HashSet<>();
        return rawTagNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(String::trim)
                .filter(name -> lowerCaseNames.add(name.toLowerCase())) // Case-insensitive distinct
                .toList();
    }'''

content = re.sub(r'''\s*private List<String> sanitizeTagNames\(List<String> rawTagNames\) \{.*?(?=\s*/\*\*\s*\* Validates note fields)''', '\n' + new_sanitize + '\n\n', content, flags=re.DOTALL)

with open('src/main/java/com/mindmap/service/NoteService.java', 'w') as f:
    f.write(content)
