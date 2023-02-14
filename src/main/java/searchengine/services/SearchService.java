package searchengine.services;

import searchengine.config.SitesList;
import searchengine.model.*;

import java.util.*;

public class SearchService {
    SiteRepository siteRepository;
    PageRepository pageRepository;
    LemmaRepository lemmaRepository;
    IndexRepository indexRepository;
    Site targetSite;
    List<Page> targetPages = new ArrayList<>();
    List<Lemma> targetLemmas = new ArrayList<>();
    float rank = 0;
    HashMap<Page, Float> pagesRanks = new HashMap<>();

    public ArrayList<SearchResult> search(String query, String url) {
        String[] words = query.split("/s");
        if (url.length() != 0) {
            return searchOneSite(words, url);
        } else {
            return searchAllSites(words);
        }
    }

    private ArrayList<SearchResult> searchOneSite(String[] words, String url) {
        Iterable<Site> siteIterable = siteRepository.findAll();
        siteIterable.forEach(el -> {
            if (el.getUrl().equals(url)) {
                targetSite = el;
            }
        });
        targetPages = targetSite.getPages();

        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (String word : words) {
            lemmaIterable.forEach(lem -> {
                if (lem.getLemma().equals(word)) {
                    targetLemmas.add(lem);
                }
            });
        }

        for (Page page : targetPages) {
            for (Lemma lemma : targetLemmas) {
                rank += checkIndex(page, lemma);
            }
            pagesRanks.put(page, rank);
            rank = 0;
        }
        return generateSearchResults(pagesRanks, url);
    }

    private float checkIndex(Page page, Lemma lemma) {
        float rank = 0;
        Iterable<Index> indexIterable = indexRepository.findAll();
        for (Index index : indexIterable) {
            if (index.getPage().equals(page) && index.getLemma().equals(lemma)) {
                rank = index.getRank();
            }
        }
        return rank;
    }

    private ArrayList<SearchResult> generateSearchResults(HashMap<Page, Float> pagesRanks, String url) {
        ArrayList<SearchResult> searchResults = new ArrayList<>();
        pagesRanks.entrySet().forEach(el -> {
            searchResults.add(new SearchResult(url,
                    url.substring(8),
                    el.getKey().getPath(),
                    getTitle(el.getKey().getContent()),
                    el.getKey().getContent(),
                    el.getValue() / getTotalRank(pagesRanks)));
        });
        Collections.sort(searchResults, new SearchResultsComparator());
        return searchResults;
    }

    private String getTitle(String content) {
        String[] words1 = content.split("<title>");
        String[] words2 = words1[1].split("\s");
        return words2[0];
    }

    private float getTotalRank(HashMap<Page, Float> pagesRanks) {
        float totalRank = 0f;
        Collection<Float> ranks = pagesRanks.values();
        for (Float rank : ranks) {
            totalRank += rank;
        }
        return totalRank;
    }

    private ArrayList<SearchResult> searchAllSites(String[] words) {
        ArrayList<SearchResult> result = new ArrayList<>();
        SitesList sitesList = new SitesList();
        sitesList.getSites().forEach(site -> {
            result.addAll(searchOneSite(words, site.getUrl()));
        });
        Collections.sort(result, new SearchResultsComparator());
        return result;
    }

}
