package douex.dou;

import com.google.gson.Gson;
import douex.cfg.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Cepro, 2017-05-03
 */
@Slf4j
public class Dou {
    
    private static Dou instance = new Dou();
    
    private Config cfg;
    
    private Map<String, String> cookies;
    
    private String csrfMiddlewareToken;
    
    private Dou() {
    }
    
    public static Dou getInstance() {
        return instance;
    }
    
    public boolean setCfg(@NonNull Config cfg) {
        
        this.cfg = cfg;
        
        try {
            get();
            return true;
        } catch (IOException e) {
            LOG.error(String.format("Connecting or getting data is failed! Cause: %s", e.getMessage()));
            return false;
        }
    }
    
    public Stream<String> post(int num) throws IOException {
        
        if (cookies == null || csrfMiddlewareToken == null) {
            return null;
        }

        String url = cfg.getPost();
        LOG.info("Connecting to {}...", url);
   
        Connection connect = Jsoup.connect(url);
    
        if (cfg.getUseProxy()) {
            connect.proxy(cfg.getProxyHost(), cfg.getProxyPort());
        }
    
        Connection.Response response = connect
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
                .cookies(cookies)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Referer", cfg.getUrl())
                .data("csrfmiddlewaretoken", csrfMiddlewareToken)
                .data("count", String.valueOf(num))
                .timeout(cfg.getTimeout())
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
        
        String json = response.body();
    
        HtmlContainer htmlContainer = new Gson().fromJson(json, HtmlContainer.class);
        String html = htmlContainer.html;
        Boolean last = htmlContainer.last;
        Integer num1 = htmlContainer.num;
        Document document = Jsoup.parse(html);
        Elements companies = document.select("div[class='company']");
        return companies.stream()
                        .map(el -> el.select("div[class='h2'] a[class='cn-a']").first().text());
    }
    
    private void get() throws IOException {
        String url = cfg.getUrl();
        LOG.info("Connecting to {}...", url);
        
        Connection connect = Jsoup.connect(url);
    
        if (cfg.getUseProxy()) {
            connect.proxy(cfg.getProxyHost(), cfg.getProxyPort());
        }
    
        Connection.Response response = connect
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
                .timeout(cfg.getTimeout())
                .execute();
    
        int statusCode = response.statusCode();
    
        if (statusCode != 200) {
            throw new IOException(String.format("Status is %d", statusCode));
        }
    
        cookies = response.cookies();
        Document document = response.parse();
        Elements tokenElements = document.select(cfg.getCsrfmiddlewaretoken());
        
        if (tokenElements.isEmpty()) {
            throw new IOException(String.format("'csrfmiddlewaretoken' noy found on the page %s", url));
        }
        
        csrfMiddlewareToken = tokenElements.val();
    
        LOG.info("Successfully connected to " + url);
    }
    
    @SuppressWarnings("initialization.fields.uninitialized")
    private static class HtmlContainer {
        String html;
        Boolean last;
        Integer num;
    }
}