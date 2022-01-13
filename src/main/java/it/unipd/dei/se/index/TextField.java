package it.unipd.dei.se.index;

import it.unipd.dei.se.parse.DebateFields;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * Represents a {@link Field} for containing the text of a debate document.
 * <p>
 * It is a tokenized field, not stored, keeping only document ids and term frequencies (see {@link
 * IndexOptions#DOCS_AND_FREQS} in order to minimize the space occupation.
 */
public class TextField extends Field {

    private static final FieldType PREMISES_TEXT_TYPE = new FieldType();

    static {
        PREMISES_TEXT_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        PREMISES_TEXT_TYPE.setTokenized(true);
        PREMISES_TEXT_TYPE.setStored(false);
    }

    public TextField(String value) {
        super(DebateFields.TEXT, value, PREMISES_TEXT_TYPE);
    }
}
