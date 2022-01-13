package it.unipd.dei.se.index;

import it.unipd.dei.se.parse.DebateFields;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * Represents a {@link Field} for containing the conclusion of a debate document.
 * <p>
 * It is a tokenized field, not stored, keeping only document ids and term frequencies (see {@link
 * IndexOptions#DOCS_AND_FREQS} in order to minimize the space occupation.
 */
public class ConclusionField extends Field {

    private static final FieldType CONCLUSION_TYPE = new FieldType();

    static {
        CONCLUSION_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        CONCLUSION_TYPE.setTokenized(true);
        CONCLUSION_TYPE.setStored(false);
    }

    public ConclusionField(String value) {
        super(DebateFields.CONCLUSION, value, CONCLUSION_TYPE);
    }
}
