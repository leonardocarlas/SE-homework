import it.unipd.dei.se.parse.ToucheParser;
import it.unipd.dei.se.search.DebateSearcher;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test class for {@link DebateSearcher}.
 */
public class DebateSearcherTest {

    private static final String TOPICS_PATH = "experiment/topics/2020/topics.xml";
    private static final String INDEX_PATH = "experiment/indexes/index-test";
    private static final String RUN_ID = "search-test";
    private static final String RUN_PATH = "experiment/runs/2020";
    private static final int MAX_DOCS_RETRIEVED = 1000;

    @Test
    @DisplayName("Simple searching")
    public void simpleSearchTest() throws IOException, ParseException {
        DebateSearcher searcher = new DebateSearcher(new StandardAnalyzer(), new ClassicSimilarity(), INDEX_PATH,
                TOPICS_PATH, RUN_ID, RUN_PATH, MAX_DOCS_RETRIEVED, ToucheParser.class, RUN_ID + ".txt");
        searcher.search(true);
    }
}
