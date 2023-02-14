package searchengine.services;

import searchengine.model.*;

import java.util.HashMap;

public class IndexPage {
    PageRepository pageRepository;
    LemmaRepository lemmaRepository;
    IndexRepository indexRepository;
    Lemmator lemmator;
    public int start(String url) {
        try {
            Iterable<Page> pageIterable = pageRepository.findAll();
            for (Page page : pageIterable) {
                if (page.getPath().contains(url)) {
                    HashMap<String, Integer> lemmasMap = new HashMap<>();
                    lemmasMap = lemmator.countLemmas(lemmator.getTextFromHtml(page.getContent()));
                    lemmasMap.entrySet().forEach(el -> {
                        boolean isLemmaInDB = checkLemma(el.getKey());
                        if (!isLemmaInDB) {
                            Lemma lemma = new Lemma();
                            lemma.setSite(page.getSite());
                            lemma.setLemma(el.getKey());
                            lemma.setFrequency(1);
                            lemmaRepository.save(lemma);
                            Index index = new Index();
                            index.setPage(page);
                            index.setLemma(lemma);
                            index.setRank(el.getValue());
                            indexRepository.save(index);
                        }
                    });
                }
            }
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean checkLemma(String lemma) {
        boolean res = false;
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (Lemma lem : lemmaIterable) {
            if (lemma.equals(lem)) {
                res = true;
                lem.setFrequency(lem.getFrequency() + 1);
                lemmaRepository.save(lem);
            }
        }
        return res;
    }
}
