package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;

public class Parser extends RecursiveAction {
    public Site site;
    SiteRepository siteRepository;
    PageRepository pageRepository;
    public String url;

    public Parser(Site site, SiteRepository siteRepository, PageRepository pageRepository, String url) {
        this.site = site;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.url = url;
    }

    @Override
    protected void compute() {
        if (IndexingService.isIndexingStopped) {
            return;
        }
        String mainPageUrl = site.getUrl();
        List<Parser> tasks = new ArrayList<>();
        boolean urlIsInDB = checkUrl(url.substring(mainPageUrl.length()));
        if (urlIsInDB) { return; }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            Elements links = doc.select("a[href]");
            Thread.sleep(5000);

            Page page = new Page();
            page.setSite(site);
            page.setPath(url.equals(mainPageUrl) ? "/" : url.substring(mainPageUrl.length()));
            page.setContent(doc.html());
            page.setCode(doc.connection().response().statusCode());
            pageRepository.save(page);
            site.setStatusTime(new Date());
            siteRepository.save(site);

            if(!links.isEmpty()) {
                Set<String> linksSet = new HashSet<>();
                for (Element link : links) {
                    String linkUrl = link.attr("href");
                    if (linkUrl.equals("") || (linkUrl.charAt(0) == '/' && linkUrl.length() == 1)
                            || linkUrl.equals(mainPageUrl)
                            || (!linkUrl.contains(mainPageUrl) && linkUrl.charAt(0) != '/')) {
                        continue;
                    } else if ((linkUrl.charAt(0) == '/' && linkUrl.length() > 1)) {
                        linkUrl = mainPageUrl + linkUrl;
                    }
                    linksSet.add(linkUrl);
                }
                linksSet.forEach(linkUrl -> {
                    Parser task = new Parser(site, siteRepository, pageRepository, linkUrl);
                    task.fork();
                    tasks.add(task);
                });
            }

            if(tasks.size() > 0) {
                tasks.forEach(task -> {
                    task.join();
                });
            }
        } catch (Exception ex) {
            site.setStatus(Status.FAILED);
            site.setLastError(ex.getMessage());
            site.setStatusTime(new Date());
            siteRepository.save(site);
        }

    }

//Checking if the url is in the database
    private boolean checkUrl(String path) {
        boolean result = false;
        synchronized (pageRepository) {
            Iterable<Page> pageIterable = pageRepository.findAll();
            for(Page page : pageIterable) {
                if(page.getPath().equals(path)) {
                    result = true;
                }
            }
        }
        return result;
    }

}
