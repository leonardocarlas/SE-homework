import it.unipd.dei.se.analysis.ToucheAnalyzer;
import it.unipd.dei.se.index.DebateIndexer;
import it.unipd.dei.se.parse.DebateParser;
import it.unipd.dei.se.parse.ToucheParser;
import it.unipd.dei.se.search.DebateSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.opennlp.OpenNLPLemmatizerFilterFactory;
import org.apache.lucene.analysis.opennlp.OpenNLPTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for {@link it.unipd.dei.se.Ryusei}.
 */
public class RyuseiTest {

    private static final String CORPUS_PATH = "corpus";
    private static final String RUN_PATH = "experiment/runs/2020";
    private static final String TOPICS_PATH = "experiment/topics/2020/topics.xml";
    private static final int MAX_DOCS_RETRIEVED = 1000;

    private static final String STOPFILTER_CONFIG_WORDS_KEY = "words";
    private static final String ATIRE_STOPLIST_FILENAME = "atire.txt";
    private static final String SMART_STOPLIST_FILENAME = "smart.txt";
    private static final String TERRIER_STOPLIST_FILENAME = "terrier.txt";
    private static final String OPENNLP_SENTENCE_MODEL_FILENAME = "en-sent.bin";
    private static final String OPENNLP_TOKENIZER_MODEL_FILENAME = "en-token.bin";
    private static final String OPENNLP_DICTIONARY_FILENAME = "en-lemmatizer.dict";
    private static final String OPENNLP_LEMMATIZER_MODEL_FILENAME = "en-lemmatizer.bin";
    private static final Map<String, String> STOPFILTER_ATIRE_CONFIG = new HashMap<>();
    private static final Map<String, String> STOPFILTER_TERRIER_CONFIG = new HashMap<>();
    private static final Map<String, String> STOPFILTER_SMART_CONFIG = new HashMap<>();
    private static final Map<String, String> OPENNLP_TOKENIZER_CONFIG = new HashMap<>();
    private static final Map<String, String> OPENNLP_LEMMATIZER_CONFIG = new HashMap<>();

    private String runDescriptor;
    private Analyzer analyzer;
    private Similarity similarity;

    @BeforeAll
    static void configure() {
        STOPFILTER_ATIRE_CONFIG.put(STOPFILTER_CONFIG_WORDS_KEY, ATIRE_STOPLIST_FILENAME);
        STOPFILTER_TERRIER_CONFIG.put(STOPFILTER_CONFIG_WORDS_KEY, TERRIER_STOPLIST_FILENAME);
        STOPFILTER_SMART_CONFIG.put(STOPFILTER_CONFIG_WORDS_KEY, SMART_STOPLIST_FILENAME);
        OPENNLP_TOKENIZER_CONFIG.put(OpenNLPTokenizerFactory.SENTENCE_MODEL, OPENNLP_SENTENCE_MODEL_FILENAME);
        OPENNLP_TOKENIZER_CONFIG.put(OpenNLPTokenizerFactory.TOKENIZER_MODEL, OPENNLP_TOKENIZER_MODEL_FILENAME);
        OPENNLP_LEMMATIZER_CONFIG.put(OpenNLPLemmatizerFilterFactory.DICTIONARY, OPENNLP_DICTIONARY_FILENAME);
        OPENNLP_LEMMATIZER_CONFIG.put(OpenNLPLemmatizerFilterFactory.LEMMATIZER_MODEL, OPENNLP_LEMMATIZER_MODEL_FILENAME);
    }

    @Test
    @DisplayName("BM25 + Lucene Tokenizer + Lucene Stop + No Stem")
    public void bm25LuceneTokenLuceneStopNoStem() throws IOException {
        runDescriptor = "bm25-lucenetoken-lucenestop-nostem";
        similarity = new BM25Similarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();
    }

    @Test
    @DisplayName("BM25 + Lucene Tokenizer + Atire Stop + No Stem")
    public void bm25LuceneTokenTerrierStopNoStem() throws IOException {
        runDescriptor = "bm25-lucenetoken-atirestop-nostem";
        similarity = new BM25Similarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_ATIRE_CONFIG)
                .build();
    }

    @Test
    @DisplayName("BM25 + OpenNLP Tokenizer + Atire Stop + OpenNLP Lemmatizer")
    public void bm25OpenNLPTokenAtireStopOpenNLPLemma() throws IOException {
        runDescriptor = "bm25-opennlptoken-atirestop-opennlplemma";
        similarity = new BM25Similarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(OpenNLPTokenizerFactory.class, OPENNLP_TOKENIZER_CONFIG)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_ATIRE_CONFIG)
                .addTokenFilter(OpenNLPLemmatizerFilterFactory.class, OPENNLP_LEMMATIZER_CONFIG)
                .build();
    }

    @Test
    @DisplayName("BM25 + Lucene Tokenizer + Lucene Stop + No Stem + Query Expansion")
    public void bm25LuceneTokenLuceneStopNoStemQueryExp() {
        runDescriptor = "bm25-lucenetoken-lucenestop-nostem-queryexp";
        similarity = new BM25Similarity();
        analyzer = new ToucheAnalyzer();
    }

    @Test
    @DisplayName("BM25 + Lucene Tokenizer + Terrier Stop + No Stem + Query Expansion")
    public void bm25LuceneTokenTerrierStopNoStemQueryExp() {
        runDescriptor = "bm25-lucenetoken-terrierstop-nostem-queryexp";
        similarity = new BM25Similarity();
        analyzer = new ToucheAnalyzer(TERRIER_STOPLIST_FILENAME);
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Lucene Stop + No Stem")
    public void dirichletLuceneTokenLuceneStopNoStem() throws IOException {
        runDescriptor = "dirichlet-lucenetoken-lucenestop-nostem";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Atire Stop + No Stem")
    public void dirichletLuceneTokenAtireStopNoStem() throws IOException {
        runDescriptor = "dirichlet-lucenetoken-atirestop-nostem";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_ATIRE_CONFIG)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Smart Stop + No Stem")
    public void dirichletLuceneTokenSmartStopNoStem() throws IOException {
        runDescriptor = "dirichlet-lucenetoken-smartstop-nostem";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_SMART_CONFIG)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Terrier Stop + No Stem")
    public void dirichletLuceneTokenTerrierStopNoStem() throws IOException {
        runDescriptor = "dirichlet-lucenetoken-terrierstop-nostem";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_TERRIER_CONFIG)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + OpenNLP Tokenizer + Terrier Stop + No Stem")
    public void dirichletOpenNLPTokenTerrierStopNoStem() throws IOException {
        runDescriptor = "dirichlet-opennlptoken-terrierstop-nostem";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(OpenNLPTokenizerFactory.class, OPENNLP_TOKENIZER_CONFIG)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_TERRIER_CONFIG)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + OpenNLP Tokenizer + Atire Stop + OpenNLP Lemmatizer")
    public void dirichletOpenNLPTokenAtireStopOpenNLPLemma() throws IOException {
        runDescriptor = "dirichlet-opennlptoken-atirestop-opennlplemma";
        similarity = new LMDirichletSimilarity();
        analyzer = CustomAnalyzer.builder()
                .withTokenizer(OpenNLPTokenizerFactory.class, OPENNLP_TOKENIZER_CONFIG)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class, STOPFILTER_ATIRE_CONFIG)
                .addTokenFilter(OpenNLPLemmatizerFilterFactory.class, OPENNLP_LEMMATIZER_CONFIG)
                .build();
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Lucene Stop + No Stem + Query Expansion")
    public void dirichletLuceneTokenLuceneStopNoStemQueryExp() {
        runDescriptor = "dirichlet-lucenetoken-lucenestop-nostem-queryexp";
        similarity = new LMDirichletSimilarity();
        analyzer = new ToucheAnalyzer();
    }

    @Test
    @DisplayName("DirichletLM + Lucene Tokenizer + Terrier Stop + No Stem + Query Expansion")
    public void dirichletLuceneTokenTerrierStopNoStemQueryExp() {
        runDescriptor = "dirichlet-lucenetoken-terrierstop-nostem-queryexp";
        similarity = new LMDirichletSimilarity();
        analyzer = new ToucheAnalyzer(TERRIER_STOPLIST_FILENAME);
    }

    @AfterEach
    public void indexAndSearch() throws IOException, ParseException {
        String indexPath = "experiment/indexes/index-" + runDescriptor;
        String runId = "goemon2020-" + runDescriptor;
        String runFileName = runId + ".txt";

        System.out.println("\n--------------- INDEXING ---------------\n");
        DebateIndexer indexer = new DebateIndexer(analyzer, similarity, indexPath, CORPUS_PATH, DebateParser.class);
        indexer.index(true);

        System.out.println("--------------- SEARCHING ---------------\n");
        if (analyzer instanceof ToucheAnalyzer) ((ToucheAnalyzer) analyzer).setIndexing(false);
        DebateSearcher searcher = new DebateSearcher(analyzer, similarity, indexPath, TOPICS_PATH, runId, RUN_PATH,
                MAX_DOCS_RETRIEVED, ToucheParser.class, runFileName);
        searcher.search(true);
    }
}
