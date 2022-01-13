package it.unipd.dei.se;

import it.unipd.dei.se.index.DebateIndexer;
import it.unipd.dei.se.parse.DebateParser;
import it.unipd.dei.se.parse.ToucheParser;
import it.unipd.dei.se.search.DebateSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

/**
 * Runs the best found searching task based on tests.
 */
public class Ryusei {

    private static final String RUN_DESCRIPTOR = "dirichlet-lucenetoken-lucenestop-nostem";
    private static final String INDEX_PATH = "experiment/indexes/index-" + RUN_DESCRIPTOR;
    private static final String RUN_ID = "goemon2021-" + RUN_DESCRIPTOR;
    private static final int MAX_DOCS_RETRIEVED = 1000;

    public static void main(String[] args) throws Exception {

        if(args.length != 2)
            throw new IllegalArgumentException("USAGE: inputDataset, outputDir");

        String corpusTopicsPath = args[0];
        String runPath = args[1];

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();

        Similarity similarity = new LMDirichletSimilarity();

        System.out.println("\n--------------- INDEXING ---------------\n");
        DebateIndexer indexer = new DebateIndexer(analyzer, similarity, INDEX_PATH, corpusTopicsPath, DebateParser.class);
        indexer.index(true);

        System.out.println("--------------- SEARCHING ---------------\n");
        DebateSearcher searcher = new DebateSearcher(analyzer, similarity, INDEX_PATH, corpusTopicsPath, RUN_ID, runPath,
                MAX_DOCS_RETRIEVED, ToucheParser.class);
        searcher.search(true);
    }
}
