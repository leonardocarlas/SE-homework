package it.unipd.dei.se.parse;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents an abstract document parser.
 */
public abstract class DocumentParser implements Iterator<ParsedDocument>, Iterable<ParsedDocument> {

    protected final Reader in;

    protected DocumentParser(Reader in) {
        if (in == null) throw new IllegalArgumentException("Reader cannot be null.");
        this.in = in;
    }

    public static DocumentParser create(Class<? extends DocumentParser> cls, Reader in) {
        if (cls == null) throw new IllegalArgumentException("Document parser class cannot be null.");
        if (in == null) throw new IllegalArgumentException("Reader cannot be null.");

        try {
            return cls.getConstructor(Reader.class).newInstance(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate document parser \"" + cls.getName() + "\".", e);
        }
    }

    @Override
    public Iterator<ParsedDocument> iterator() {
        return this;
    }

    @Override
    public ParsedDocument next() {
        if (hasNext()) return parse();
        try {
            in.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close the reader.", e);
        }
        throw new NoSuchElementException("No more documents to parse.");
    }

    protected abstract ParsedDocument parse();
}
