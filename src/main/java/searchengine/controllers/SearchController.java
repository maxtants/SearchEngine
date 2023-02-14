package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.SearchResult;
import searchengine.services.SearchService;

import java.util.ArrayList;

@RestController
public class SearchController {
    @GetMapping("/api/search")
    public ResponseEntity search(String query, String site, int offset, int limit) {
        try {
            ArrayList<SearchResult> searchResults = new ArrayList<>();
            SearchService ss = new SearchService();
            searchResults = ss.search(query, site);
            int count = searchResults.size();
            String data = "";
            for (int i = offset; i <= offset + limit - 1; i++) {
                data += searchResults.get(i).toString();
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body("'result': true\n" +
                            "'count': " + count + ", \n" +
                            "'data': " + data);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("'result': false,\n" +
                            "'error': \"Задан пустой поисковый запрос\"");
        }
    }
}
