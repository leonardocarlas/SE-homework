package it.unipd.dei.se.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * Helper class to load stop lists and build {@link SynonymMap}.
 */
public class AnalyzerUtil {

    private static final ClassLoader CL = AnalyzerUtil.class.getClassLoader();

    public static SynonymMap buildSynonymMap() {
        SynonymMap.Builder synonymMapBuilder = new SynonymMap.Builder();
        CharsRefBuilder charsRefBuilder = new CharsRefBuilder();

        try {
            BufferedReader wordnetProlog = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(CL.getResourceAsStream("wn_s.pl")))
            );

            Map<String, ArrayList<String>> termsById = new HashMap<>();

            String line;
            while ((line = wordnetProlog.readLine()) != null) {
                String[] arguments = line.substring(2, line.length() - 1).split(",");
                ArrayList<String> terms = termsById.get(arguments[0]);
                if (terms == null) terms = new ArrayList<>();
                terms.add(arguments[2].substring(1, arguments[2].length() - 1));
                termsById.put(arguments[0], terms);
            }

            Collection<ArrayList<String>> allGroupsSynonyms = termsById.values();
            for (ArrayList<String> singleGroupSynonyms : allGroupsSynonyms) {
                for (String word : singleGroupSynonyms) {
                    ArrayList<String> synonyms = new ArrayList<>(singleGroupSynonyms);
                    synonyms.remove(word);
                    if (synonyms.size() != 0) {
                        String[] synonymsArray = synonyms.toArray(new String[0]);
                        CharsRef input = SynonymMap.Builder.join(new String[]{word}, charsRefBuilder);
                        CharsRef output = SynonymMap.Builder.join(synonymsArray, charsRefBuilder);
                        synonymMapBuilder.add(input, output, true);
                    }
                }
            }

            return synonymMapBuilder.build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to build the synonym map.", e);
        }
    }

    public static CharArraySet loadStopList(String stopListPath) {
        if (stopListPath == null) throw new NullPointerException("Stop list file name cannot be null.");
        if (stopListPath.isEmpty()) throw new IllegalArgumentException("Stop list file name cannot be empty.");

        CharArraySet stopList;

        try {
            Reader in = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(CL.getResourceAsStream(stopListPath)))
            );
            stopList = WordlistLoader.getWordSet(in);
            in.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load the stop list \"" + stopListPath + "\".", e);
        }

        return stopList;
    }
}
