import it.unipd.dei.se.parse.DebateParser;
import it.unipd.dei.se.parse.DocumentParser;
import it.unipd.dei.se.parse.ParsedDocument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link DebateParser}.
 */
public class DebateParserTest {

    private static final String CORPUS_PATH = "corpus";
    private static final String DEBATE_FILES_EXTENSION = ".json";
    private static final int EXPECTED_PARSED_DOCUMENTS_COUNT = 387658;

    @Test
    @DisplayName("Simple parsing")
    public void simpleParseTest() throws IOException {
        Path corpusPath = Paths.get(CORPUS_PATH);
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<>() {
            private int parsedDocumentsCount = 0;
            private long startTime;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                startTime = System.currentTimeMillis();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(DEBATE_FILES_EXTENSION)) {
                    System.out.println("Parsing file: " + file.getFileName());
                    DocumentParser dp = DebateParser.create(DebateParser.class, Files.newBufferedReader(file));
                    int count = 0;
                    for (ParsedDocument d : dp) {
                        if (count == 0) System.out.println("Example document: \n" + d);
                        count++;
                        if (count % 10000 == 0) System.out.println("Parsed documents: " + count + " (partial)");
                    }
                    System.out.println("Parsed documents: " + count + "\n");
                    parsedDocumentsCount += count;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                String timeFormat = ((elapsedTime / 1000) / 60 < 1 ? "" : "m'min' ") + (elapsedTime / 1000 < 1 ? "" : "s'sec' ") + "S'ms'";
                String duration = DurationFormatUtils.formatDuration(endTime - startTime, timeFormat);
                System.out.println("Parsing completed in: " + duration);
                System.out.print("Total parsed documents: " + parsedDocumentsCount);
                assertEquals(EXPECTED_PARSED_DOCUMENTS_COUNT, parsedDocumentsCount,
                        "Parsed documents count should be " + EXPECTED_PARSED_DOCUMENTS_COUNT);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
