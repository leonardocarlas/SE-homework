package it.unipd.dei.se.search;

import it.unipd.dei.se.parse.DebateFields;
import it.unipd.dei.se.parse.TopicParser;
import it.unipd.dei.se.parse.ToucheFields;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * Searches a debate documents collection.
 */
public class DebateSearcher {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final String runId;
    private final String runFileName;
    private final PrintWriter writer;
    private final IndexReader reader;
    private final IndexSearcher searcher;
    private final QueryParser queryParser;
    private final int maxDocsRetrieved;
    private final Class<? extends TopicParser> tpCls;
    private String topicsPath;

    public DebateSearcher(Analyzer analyzer, Similarity similarity, String indexPath, String topicsPath, String runId,
                          String runPath, int maxDocsRetrieved, Class<? extends TopicParser> tpCls) {
        this(analyzer, similarity, indexPath, topicsPath, runId, runPath, maxDocsRetrieved, tpCls, "run.txt");
    }

    public DebateSearcher(Analyzer analyzer, Similarity similarity, String indexPath, String topicsPath, String runId,
                          String runPath, int maxDocsRetrieved, Class<? extends TopicParser> tpCls, String runFileName) {

        if (analyzer == null) throw new IllegalArgumentException("Analyzer cannot be null.");
        if (similarity == null) throw new IllegalArgumentException("Similarity cannot be null.");
        if (indexPath == null) throw new IllegalArgumentException("Index path cannot be null.");
        if (topicsPath == null) throw new IllegalArgumentException("Topics path cannot be null.");
        if (runId == null) throw new IllegalArgumentException("Run identifier cannot be null.");
        if (runPath == null) throw new IllegalArgumentException("Run path cannot be null.");
        if (tpCls == null) throw new IllegalArgumentException("Topic parser class cannot be null.");
        if (runFileName == null) throw new IllegalArgumentException("Run file name cannot be null.");
        if (indexPath.isEmpty()) throw new IllegalArgumentException("Index path cannot be empty.");
        if (topicsPath.isEmpty()) throw new IllegalArgumentException("Topics path cannot be empty.");
        if (runId.isEmpty()) throw new IllegalArgumentException("Run identifier cannot be empty.");
        if (runPath.isEmpty()) throw new IllegalArgumentException("Run path cannot be empty.");
        if (runFileName.isEmpty()) throw new IllegalArgumentException("Run file name cannot be empty.");
        if (maxDocsRetrieved <= 0)
            throw new IllegalArgumentException("The maximum number of documents to be retrieved cannot be less than or equal to zero.");

        Path indexDir = Paths.get(indexPath);
        Path runDir = Paths.get(runPath);
        Path runFile = runDir.resolve(runFileName);

        if (!Files.isReadable(indexDir))
            throw new IllegalArgumentException("Index directory \"" + indexDir.toAbsolutePath() + "\" cannot be read.");

        if (!Files.isDirectory(indexDir))
            throw new IllegalArgumentException("\"" + indexDir.toAbsolutePath() + "\" expected to be a directory where to search the index.");

        if (!Files.isWritable(runDir))
            throw new IllegalArgumentException("Run directory \"" + runDir.toAbsolutePath() + "\" cannot be written.");

        if (!Files.isDirectory(runDir))
            throw new IllegalArgumentException("\"" + runDir.toAbsolutePath() + "\" expected to be a directory where to write the run.");

        try {
            this.reader = DirectoryReader.open(FSDirectory.open(indexDir));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create the index reader for directory \"" + indexDir.toAbsolutePath() + "\"");
        }

        try {
            this.writer = new PrintWriter(Files.newBufferedWriter(runFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to open run file \"" + runFile.toAbsolutePath() + "\".");
        }

        this.searcher = new IndexSearcher(reader);
        this.searcher.setSimilarity(similarity);
        this.queryParser = new QueryParser(DebateFields.TEXT, analyzer);
        this.runId = runId;
        this.maxDocsRetrieved = maxDocsRetrieved;
        this.tpCls = tpCls;
        this.topicsPath = topicsPath;
        this.runFileName = runFileName;
    }

    public void search(boolean verbose) throws IOException, ParseException {
        long startTime = System.currentTimeMillis();
        if (!topicsPath.endsWith(".xml")) topicsPath += topicsPath.endsWith("/") ? "topics.xml" : "/topics.xml";
        TopicParser tp = TopicParser.create(tpCls, Files.newBufferedReader(Paths.get(topicsPath), CHARSET));
        int searchedTopicsCount = 0;
        for (QualityQuery t : tp) {
            if (verbose) System.out.println("Searching topic: " + t.getValue(ToucheFields.TITLE));
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            builder.add(queryParser.parse(QueryParserBase.escape(t.getValue(ToucheFields.TITLE))), BooleanClause.Occur.SHOULD);
            Query query = builder.build();
            TopDocs topDocs = searcher.search(query, maxDocsRetrieved);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (int i = 0; i < scoreDocs.length; i++) {
                Set<String> idField = Collections.singleton(DebateFields.ID);
                String docId = reader.document(scoreDocs[i].doc, idField).get(DebateFields.ID);
                writer.printf(Locale.ENGLISH, "%s Q0 %s %d %.6f %s%n", t.getQueryID(), docId, i, scoreDocs[i].score, runId);
            }
            writer.flush();
            searchedTopicsCount++;
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        if (verbose) {
            String timeFormat = ((elapsedTime / 1000) / 60 < 1 ? "" : "m'min' ") + (elapsedTime / 1000 < 1 ? "" : "s'sec' ") + "S'ms'";
            String duration = DurationFormatUtils.formatDuration(endTime - startTime, timeFormat);
            System.out.println("\nSearching completed in: " + duration);
            System.out.println("Total searched topics: " + searchedTopicsCount);
            System.out.println("Created run file: \"" + runFileName + "\"");
        }
        writer.close();
        reader.close();
    }
}
