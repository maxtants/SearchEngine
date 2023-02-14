package searchengine.services;

import java.util.Comparator;

public class SearchResultsComparator implements Comparator<SearchResult> {

    @Override
    public int compare(SearchResult o1, SearchResult o2) {
        return (int) o1.relevance - (int) o2.relevance;
    }
}
