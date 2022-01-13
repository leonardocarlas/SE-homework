package it.unipd.dei.se.parse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.io.SerializedString;

import java.io.IOException;
import java.io.Reader;

/**
 * Represents a specific {@code DocumentParser} that parses debates encoded in JSON files.
 */
public class DebateParser extends DocumentParser {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private final JsonParser in;
    private ParsedDocument parsedDocument;
    private boolean documentPending;

    public DebateParser(Reader in) throws IOException {
        super(in);
        this.in = jsonFactory.createJsonParser(in);
    }

    @Override
    protected ParsedDocument parse() {
        if (documentPending) {
            documentPending = false;
            return parsedDocument;
        }
        try {
            while (!in.nextFieldName(new SerializedString(DebateFields.ID)))
                if (in.getCurrentToken() == null) return null;
            String id = in.nextTextValue();

            while (!in.nextFieldName(new SerializedString(DebateFields.CONCLUSION))) ;
            String conclusion = in.nextTextValue();

            while (!in.nextFieldName(new SerializedString(DebateFields.TEXT))) ;
            String text = in.nextTextValue();
            if (text.isEmpty()) return parse();

            while (!in.nextFieldName(new SerializedString(DebateFields.STANCE))) ;
            String stance = in.nextTextValue();

            parsedDocument = new ParsedDocument(id, text, conclusion, stance);
            return parsedDocument;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse the document.", e);
        }
    }

    @Override
    public boolean hasNext() {
        if (documentPending) return true;
        if (parse() == null) return false;
        documentPending = true;
        return true;
    }
}
