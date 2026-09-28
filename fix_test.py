with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'r') as f:
    text = f.read()

text = text.replace('public void setup() {', 'public void setup() throws Exception {')
text = text.replace('public void testReviewFlow() {', 'public void testReviewFlow() throws Exception {')

with open('src/test/java/com/mindmap/ReviewFlowTest.java', 'w') as f:
    f.write(text)
