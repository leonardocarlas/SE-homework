package it.unipd.dei.se.parse;

import org.apache.lucene.benchmark.quality.QualityQuery;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents an abstract topic parser.
 */
public abstract class TopicParser implements Iterator<QualityQuery>, Iterable<QualityQuery> {

    protected final Reader in;

    protected TopicParser(Reader in) {
        if (in == null) throw new IllegalArgumentException("Reader cannot be null.");
        this.in = in;
    }

    public static TopicParser create(Class<? extends TopicParser> cls, Reader in) {
        if (cls == null) throw new IllegalArgumentException("Topic parser class cannot be null.");
        if (in == null) throw new IllegalArgumentException("Reader cannot be null.");

        try {
            return cls.getConstructor(Reader.class).newInstance(in);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate topic parser \"" + cls.getName() + "\".", e);
        }
    }

    @Override
    public Iterator<QualityQuery> iterator() {
        return this;
    }

    @Override
    public QualityQuery next() {
        if (hasNext()) return parse();
        try {
            in.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close the reader.", e);
        }
        throw new NoSuchElementException("No more topics to parse.");
    }

    protected abstract QualityQuery parse();
}
