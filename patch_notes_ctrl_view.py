import re

with open('src/main/java/com/mindmap/controller/NotesController.java', 'r') as f:
    content = f.read()

# Add cmbViewFilter
content = re.sub(r'(\s*@FXML\s*private ComboBox<String> cmbSubjectFilter;)',
                 r'\1\n    @FXML\n    private ComboBox<String> cmbViewFilter;', content)

# Initialize cmbViewFilter
init_code = """
        if (cmbViewFilter != null) {
            cmbViewFilter.setItems(javafx.collections.FXCollections.observableArrayList(
                "All Notes", "Favorites", "Private Notes", "Recently Viewed"
            ));
            cmbViewFilter.setValue("All Notes");
            cmbViewFilter.setOnAction(e -> handleSearch());
        }
"""
content = content.replace('        setupDifficultyFilter();', '        setupDifficultyFilter();\n' + init_code)

# Add lock icon to title column
col_title_code = """
        colTitle.setCellFactory(tc -> new javafx.scene.control.TableCell<Note, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Note note = getTableView().getItems().get(getIndex());
                    if (note.isPrivate()) {
                        setText("🔒 " + item);
                    } else {
                        setText(item);
                    }
                }
            }
        });
"""
content = content.replace('        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));',
                          '        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));\n' + col_title_code)

# Make search method handle view filter
search_filter_logic = """
        String viewMode = (cmbViewFilter != null && cmbViewFilter.getValue() != null) ? cmbViewFilter.getValue() : "All Notes";
        if ("Favorites".equals(viewMode)) {
            filtered = filtered.stream().filter(Note::isFavorite).toList();
        } else if ("Private Notes".equals(viewMode)) {
            filtered = filtered.stream().filter(Note::isPrivate).toList();
        } else if ("Recently Viewed".equals(viewMode)) {
            filtered = filtered.stream().filter(n -> n.getLastViewedAt() != null)
                .sorted((n1, n2) -> n2.getLastViewedAt().compareTo(n1.getLastViewedAt()))
                .toList();
        }
"""
# Insert search filter logic before setting items
content = re.sub(r'(\s*javafx\.application\.Platform\.runLater\(\(\) -> \{\s*tableResults\.getItems\(\)\.setAll\(filtered\);)',
                 search_filter_logic + r'\1', content)


with open('src/main/java/com/mindmap/controller/NotesController.java', 'w') as f:
    f.write(content)
