package com.mindmap.model;

import java.util.Objects;

/**
 * Represents a directional semantic relationship (edge) between two notes in the knowledge graph.
 * <p>
 * Note: Distinct from {@code java.sql.Connection}. In database and repository code,
 * {@code java.sql.Connection} must be fully qualified to avoid ambiguity.
 */
public class Connection {

    private int id;
    private int fromNoteId;
    private int toNoteId;
    private String relation;

    public Connection() {
    }

    public Connection(int fromNoteId, int toNoteId, String relation) {
        this.fromNoteId = fromNoteId;
        this.toNoteId = toNoteId;
        this.relation = relation;
    }

    public Connection(int id, int fromNoteId, int toNoteId, String relation) {
        this.id = id;
        this.fromNoteId = fromNoteId;
        this.toNoteId = toNoteId;
        this.relation = relation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromNoteId() {
        return fromNoteId;
    }

    public void setFromNoteId(int fromNoteId) {
        this.fromNoteId = fromNoteId;
    }

    public int getToNoteId() {
        return toNoteId;
    }

    public void setToNoteId(int toNoteId) {
        this.toNoteId = toNoteId;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id == that.id && fromNoteId == that.fromNoteId && toNoteId == that.toNoteId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromNoteId, toNoteId);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "id=" + id +
                ", fromNoteId=" + fromNoteId +
                ", toNoteId=" + toNoteId +
                ", relation='" + relation + '\'' +
                '}';
    }
}
