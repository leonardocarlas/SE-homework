package it.unipd.dei.se.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;

import java.io.Reader;
import java.util.Objects;

import static it.unipd.dei.se.analysis.AnalyzerUtil.loadStopList;

/**
 * Represents a custom {@link Analyzer} for analyzing documents and topics form the Touche Task. This analyzer
 * implements the query expansion technique.
 */
public class ToucheAnalyzer extends Analyzer {

    private boolean indexing;
    private final String stopListPath;

    public ToucheAnalyzer() {
        this(null);
    }

    public ToucheAnalyzer(String stopListPath) {
        this(stopListPath, true);
    }

    public ToucheAnalyzer(String stopListPath, boolean indexing) {
        super();
        if (stopListPath != null && stopListPath.isEmpty())
            throw new IllegalArgumentException("StopList path cannot be empty.");
        this.indexing = indexing;
        this.stopListPath = stopListPath;
    }

    public void setIndexing(boolean indexing) {
        this.indexing = indexing;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream tokens = new LowerCaseFilter(source);
        if (stopListPath == null) tokens = new StopFilter(tokens, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        else tokens = new StopFilter(tokens, loadStopList(stopListPath));
        tokens = new SynonymGraphFilter(tokens, Objects.requireNonNull(AnalyzerUtil.buildSynonymMap()), true);

        if (indexing) tokens = new FlattenGraphFilter(tokens);

        return new TokenStreamComponents(source, tokens);
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        return super.initReader(fieldName, reader);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
