package it.unipd.dei.se.parse;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a parsed document to be indexed.
 */
public class ParsedDocument {

    private final String id;
    private final String text;
    private final String conclusion;
    private final String stance;

    public ParsedDocument(String id, String text, String conclusion, String stance) {
        if (id == null) throw new IllegalArgumentException("Document id cannot be null.");
        if (text == null) throw new IllegalArgumentException("Document text cannot be null.");
        if (conclusion == null) throw new IllegalArgumentException("Document conclusion cannot be null.");
        if (stance == null) throw new IllegalArgumentException("Document stance cannot be null.");
        if (id.isEmpty()) throw new IllegalArgumentException("Document id cannot be empty.");
        if (text.isEmpty()) throw new IllegalArgumentException("Document text cannot be empty.");

        this.id = id;
        this.text = text;
        this.conclusion = conclusion;
        this.stance = stance;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getConclusion() {
        return conclusion;
    }

    public String getStance() {
        return stance;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", id)
                .append("text", text)
                .append("conclusion", conclusion.isEmpty() ? "<empty>" : conclusion)
                .append("stance", stance.isEmpty() ? "<empty>" : stance);
        return tsb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof ParsedDocument) && id.equals(((ParsedDocument) obj).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
