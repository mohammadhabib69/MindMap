import os
import re

directory = 'src/main/java/com/mindmap/controller/'

for filename in os.listdir(directory):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r') as f:
        content = f.read()
    
    # 1. Single line: if (!com.mindmap.util.SecurityHelper.verifyPin(note)) return;
    content = re.sub(r'[ \t]*if[ \t]*\(\!com\.mindmap\.util\.SecurityHelper\.verifyPin\([^)]*\)\)[ \t]*return;[ \t]*\n?', '', content)
    
    # 2. Block: if (!com.mindmap.util.SecurityHelper.verifyPin(noteToView)) { return; }
    content = re.sub(r'[ \t]*if[ \t]*\(\!com\.mindmap\.util\.SecurityHelper\.verifyPin\([^)]*\)\)[ \t]*\{[ \t\n]*return;[ \t\n]*\}[ \t]*\n?', '', content)
    
    # 3. Mode edit check: if (mode == NoteEditorMode.EDIT && note != null && !com.mindmap.util.SecurityHelper.verifyPin(note)) { return; }
    content = re.sub(r'[ \t]*if[ \t]*\(mode == NoteEditorMode\.EDIT && note != null && !com\.mindmap\.util\.SecurityHelper\.verifyPin\([^)]*\)\)[ \t]*\{[ \t\n]*return;[ \t\n]*\}[ \t]*\n?', '', content)

    with open(filepath, 'w') as f:
        f.write(content)
