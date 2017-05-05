package douex.dou;

import com.google.gson.Gson;
import douex.cfg.Config;
import douex.cfg.Configurator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.concurrent.ThreadLocalRandom.current;

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
        setCfg(Configurator.defaultCfg());
    }
    
    public static Dou getInstance() {
        return instance;
    }
    
    public boolean setCfg(@NonNull Config cfg) {
        
        this.cfg = cfg;
        
        try {
            connect();
            return true;
        } catch (IOException e) {
            LOG.error(String.format("Connecting or getting data is failed! Cause: %s", e.getMessage()));
            return false;
        }
    }

    /**
     * Used to check status of {@link Dou}. If status is 'false' then we cannot get data from {@link Dou}
     * <p>Checking status is necessary after getting instance of {@link Dou}
     * or after calling {@link Dou#setCfg(douex.cfg.Config)} method
     * @return 'true' if we can get data from {@link Dou} and 'false' otherwise.
     */
    public boolean status() {
        return csrfMiddlewareToken != null && cookies != null;
    }
    
    public Stream<String> post(int num) throws Exception {
        
        if (!status()) {
            LOG.error("Cannot get data - csrfMiddlewareToken or cookies are empty!");
            return null;
        }

        delay();

        String url = cfg.getDataUrl();
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
                .header("Referer", cfg.getStartUrl())
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

    /**
     * Perform random delay based on {@link Config#loadingDataDelay}
     */
    private void delay() {
        List<Integer> delayValues = cfg.getDelayValues();
        try {
            Thread.sleep(current().nextInt(delayValues.get(0), delayValues.get(1)));
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Initial connection to dou.ua
     * <p>Calling this method is necessary to get cookies and csrf middleware token from the site
     * @throws IOException if connection fails | it return non 200 status | csrf token not found on the  page.
     */
    private void connect() throws IOException {
        String url = cfg.getStartUrl();
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
        String tokenPattern = cfg.getCsrfTokenPattern();
        Elements tokenElements = document.select(tokenPattern);
        
        if (tokenElements.isEmpty()) {
            throw new IOException(String.format("Element '%s' not found on the page %s", tokenPattern, url));
        }
        
        csrfMiddlewareToken = tokenElements.first().val();
    
        LOG.info("Successfully connected to " + url);
    }
    
    @SuppressWarnings("initialization.fields.uninitialized")
    private static class HtmlContainer {
        String html;
        Boolean last;
        Integer num;
    }
}
