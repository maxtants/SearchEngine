package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lemmator {
    public HashMap<String, Integer> countLemmas(String text) throws IOException {
        HashMap<String, Integer> lemmasMap = new HashMap<>();
        ArrayList<String> lemmas = new ArrayList<>();
        String[] words = text.split("\s");
        for (String word : words) {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            wordBaseForms.forEach(wbf -> {
                if (!wbf.contains("|")) {
                    lemmas.add(wbf);
                }
            });
        }
        for (String lemma : lemmas) {
            int count = 0;
            for (String lem : lemmas) {
                if (lemma.equals(lem)) {
                    count++;
                }
            }
            lemmasMap.put(lemma, count);
        }
        return lemmasMap;
    }

    public String getTextFromHtml(String html) {
        String[] words = html.split("<[.]*>");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word + "");
        }
        return sb.toString();
    }
}
