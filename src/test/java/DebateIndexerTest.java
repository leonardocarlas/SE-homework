import it.unipd.dei.se.index.DebateIndexer;
import it.unipd.dei.se.parse.DebateParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link DebateIndexer}.
 */
public class DebateIndexerTest {

    private static final String CORPUS_PATH = "corpus";
    private static final String INDEX_PATH = "experiment/indexes/index-test";
    private static final int EXPECTED_INDEXED_DOCUMENTS_COUNT = 385831;

    @Test
    @DisplayName("Simple indexing")
    public void simpleIndexTest() throws IOException {
        DebateIndexer indexer = new DebateIndexer(new StandardAnalyzer(), new ClassicSimilarity(), INDEX_PATH,
                CORPUS_PATH, DebateParser.class);
        indexer.index(true);
        assertEquals(EXPECTED_INDEXED_DOCUMENTS_COUNT, indexer.getIndexedDocumentsCount(),
                "Indexed documents count should be " + EXPECTED_INDEXED_DOCUMENTS_COUNT);
    }
}
