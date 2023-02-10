package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingService {
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SitesList sites;

    public static boolean isIndexingStopped;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository, SitesList sites) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sites = sites;
    }

    public void startIndexing() {
        List<searchengine.config.Site> siteList = sites.getSites();
        for (searchengine.config.Site site : siteList) {
            deleteSiteDataFromDB(site.getUrl());
            Site siteObj = makeIndexingRecord(site.getUrl(), site.getName());
            Runnable task = () -> {
               indexSitePages(siteObj, siteRepository, pageRepository, site.getUrl());
            };
            Thread thread = new Thread(task);
            thread.start();
            if (isIndexingStopped) { break; }
            setStatusIndexed();
            siteList.remove(site);
        }
        if (siteList.size() != 0) {
            siteList.forEach(site -> {
                Iterable<Site> siteIterable = siteRepository.findAll();
                siteIterable.forEach(record -> {
                    if (site.getUrl().equals(record.getUrl())) {
                        record.setStatus(Status.FAILED);
                        record.setLastError("Индексация остановлена пользователем");
                        siteRepository.save(record);
                    }
                });
            });
        }
    }

    private void deleteSiteDataFromDB(String url) {
        Iterable<Site> siteIterable = siteRepository.findAll();
        ArrayList<Site> siteArrayList = new ArrayList<>();
        siteIterable.forEach(site -> {
            siteArrayList.add(site);
        });
        if (siteArrayList.size() > 0) {
            for (Site site : siteArrayList) {
                if (site.getUrl().equals(url)) {
                    Iterable<Page> pageIterable = pageRepository.findAll();
                    ArrayList<Page> pageArrayList = new ArrayList<>();
                    pageIterable.forEach(page -> {
                        pageArrayList.add(page);
                    });
                    if (pageArrayList.size() > 0) {
                        for (Page page : pageArrayList) {
                            if (page.getSite().equals(site)) {
                                pageRepository.delete(page);
                            }
                        }
                    }
                    siteRepository.delete(site);
                }
            }
        }
    }

    private Site makeIndexingRecord(String url, String name) {
        Site site = new Site();
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        site.setLastError(null);
        site.setUrl(url);
        site.setName(name);
        siteRepository.save(site);
        return site;
    }

    private void indexSitePages(Site site, SiteRepository siteRepository, PageRepository pageRepository, String url) {
        Parser parser = new Parser(site, siteRepository, pageRepository, url);
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(parser);
    }

    private void setStatusIndexed() {
        Iterable<Site> siteIterable = siteRepository.findAll();
        for (Site site : siteIterable) {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.INDEXED);
                site.setStatusTime(new Date());
                siteRepository.save(site);
            }
        }
    }

    public void stopIndexing() {
        isIndexingStopped = true;
    }

}
