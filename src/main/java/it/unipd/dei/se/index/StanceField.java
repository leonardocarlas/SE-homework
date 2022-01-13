package it.unipd.dei.se.index;

import it.unipd.dei.se.parse.DebateFields;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * Represents a {@link Field} for containing the stance of a debate document.
 * <p>
 * It is a tokenized field, not stored, keeping only document ids and term frequencies (see {@link
 * IndexOptions#DOCS_AND_FREQS} in order to minimize the space occupation.
 */
public class StanceField extends Field {

    private static final FieldType STANCE_TYPE = new FieldType();

    static {
        STANCE_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        STANCE_TYPE.setTokenized(true);
        STANCE_TYPE.setStored(false);
    }

    public StanceField(String value) {
        super(DebateFields.STANCE, value, STANCE_TYPE);
    }
}
