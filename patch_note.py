import re

with open('src/main/java/com/mindmap/model/Note.java', 'r') as f:
    content = f.read()

# Add fields
fields = """    private LocalDateTime lastViewedAt;
    private boolean isPrivate;
    private String pin;"""
content = content.replace('    private LocalDateTime lastViewedAt;', fields)

# Add getters and setters
getters = """    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }"""
content = content.replace('    public LocalDateTime getLastViewedAt() {\n        return lastViewedAt;\n    }', getters)

with open('src/main/java/com/mindmap/model/Note.java', 'w') as f:
    f.write(content)
