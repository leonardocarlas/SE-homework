package it.unipd.dei.se.index;

import it.unipd.dei.se.parse.DebateFields;
import it.unipd.dei.se.parse.DocumentParser;
import it.unipd.dei.se.parse.ParsedDocument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Indexes debate documents processing a whole directory tree.
 */
public class DebateIndexer {

    private static final String DEBATE_FILES_EXTENSION = ".json";
    private static final int RAM_BUFFER_SIZE = 256;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final IndexWriter writer;
    private final Class<? extends DocumentParser> dpCls;
    private final Path corpusDir;
    private int indexedDocumentsCount;

    public DebateIndexer(Analyzer analyzer, Similarity similarity, String indexPath, String corpusPath, Class<? extends DocumentParser> dpCls) {
        if (dpCls == null) throw new IllegalArgumentException("Document parser class cannot be null.");
        if (analyzer == null) throw new IllegalArgumentException("Analyzer cannot be null.");
        if (similarity == null) throw new IllegalArgumentException("Similarity cannot be null.");
        if (indexPath == null) throw new IllegalArgumentException("Index path cannot be null.");
        if (corpusPath == null) throw new IllegalArgumentException("Corpus path cannot be null.");
        if (indexPath.isEmpty()) throw new IllegalArgumentException("Index path cannot be empty.");
        if (corpusPath.isEmpty()) throw new IllegalArgumentException("Corpus path cannot be empty.");

        Path indexDir = Paths.get(indexPath);
        Path docsDir = Paths.get(corpusPath);

        if (Files.notExists(indexDir)) {
            try {
                Files.createDirectories(indexDir);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to create directory \"" + indexDir.toAbsolutePath() + "\".", e);
            }
        }

        if (!Files.isWritable(indexDir))
            throw new IllegalArgumentException("Index directory \"" + indexDir.toAbsolutePath() + "\" cannot be written.");

        if (!Files.isDirectory(indexDir))
            throw new IllegalArgumentException("\"" + indexDir.toAbsolutePath() + "\" expected to be a directory where to write the index.");

        if (!Files.isReadable(docsDir))
            throw new IllegalArgumentException("Documents directory \"" + docsDir.toAbsolutePath() + "\" cannot be read.");

        if (!Files.isDirectory(docsDir))
            throw new IllegalArgumentException("\"" + docsDir.toAbsolutePath() + "\" expected to be a directory of documents.");

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setRAMBufferSizeMB(RAM_BUFFER_SIZE);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setCommitOnClose(true);

        try {
            writer = new IndexWriter(FSDirectory.open(indexDir), iwc);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create the index writer in directory \"" + indexDir.toAbsolutePath() + "\".", e);
        }

        this.dpCls = dpCls;
        this.corpusDir = docsDir;
    }

    public void index(boolean verbose) throws IOException {
        Files.walkFileTree(corpusDir, new SimpleFileVisitor<>() {
            private long startTime;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                startTime = System.currentTimeMillis();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(DEBATE_FILES_EXTENSION)) {
                    if (verbose) System.out.println("Indexing file: " + file.getFileName());
                    DocumentParser dp = DocumentParser.create(dpCls, Files.newBufferedReader(file, CHARSET));
                    Document doc;
                    Set<String> ids = new HashSet<>();
                    int count = 0;
                    for (ParsedDocument d : dp) {
                        if (!ids.contains(d.getId())) {
                            doc = new Document();
                            doc.add(new StringField(DebateFields.ID, d.getId(), Field.Store.YES));
                            doc.add(new TextField(d.getText()));
                            doc.add(new ConclusionField(d.getConclusion()));
                            doc.add(new StanceField(d.getStance()));
                            ids.add(d.getId());
                            writer.addDocument(doc);
                            count++;
                        }
                        if (verbose && count % 10000 == 0)
                            System.out.println("Indexed documents: " + count + " (partial)");
                    }
                    if (verbose)
                        System.out.println("Indexed documents: " + count + "\n");
                    indexedDocumentsCount += count;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                if (verbose) {
                    String timeFormat = ((elapsedTime / 1000) / 60 < 1 ? "" : "m'min' ") + (elapsedTime / 1000 < 1 ? "" : "s'sec' ") + "S'ms'";
                    String duration = DurationFormatUtils.formatDuration(endTime - startTime, timeFormat);
                    System.out.println("Indexing completed in: " + duration);
                    System.out.println("Total indexed documents: " + indexedDocumentsCount + "\n");
                }
                return super.postVisitDirectory(dir, exc);
            }
        });
        writer.commit();
        writer.close();
    }

    public int getIndexedDocumentsCount() {
        return indexedDocumentsCount;
    }
}
