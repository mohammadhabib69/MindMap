with open('src/main/java/com/mindmap/external/WikipediaService.java', 'r') as f:
    lines = f.readlines()

for i in range(len(lines)):
    if "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" in lines[i]:
        # The next line contains the rest of the URL
        lines[i+1] = '                         + encodedQuery + "&utf8=1&format=json&srlimit=20";\n'

with open('src/main/java/com/mindmap/external/WikipediaService.java', 'w') as f:
    f.writelines(lines)
