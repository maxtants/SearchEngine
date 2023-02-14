package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.IndexPage;
import searchengine.services.IndexingService;


@RestController
public class IndexingController {
    private IndexingService indexingService;

    public IndexingController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @GetMapping("/api/startIndexing")
    public ResponseEntity startIndexing() {
        try {
            indexingService.startIndexing();
            return ResponseEntity.status(HttpStatus.OK).body("'result': true");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("'result': false, 'error': \"Индексация уже запущена\"");
        }
    }

    @GetMapping("/api/stopIndexing")
    public ResponseEntity stopIndexing() {
        try {
            indexingService.stopIndexing();
            return ResponseEntity.status(HttpStatus.OK).body("'result': true");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("'result': false, 'error': \"Индексация не запущена\"");
        }
    }

    @PostMapping(" /api/indexPage")
    public ResponseEntity indexPage(String url) {
        IndexPage indexPage = new IndexPage();
        int res = indexPage.start(url);
        if (res == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("'result': false,\n" +
                            "'error': \"Данная страница находится за пределами сайтов,\n" +
                            "указанных в конфигурационном файле\"");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("'result': true");
        }
    }
}
