package douex.dou;

import com.google.gson.Gson;
import douex.dou.cfg.Config;
import douex.dou.cfg.Configurator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

import static java.util.concurrent.ThreadLocalRandom.current;

/**
 * Main class used to extract data from DOU.UA
 */
@Slf4j
public class Dou {
    
    private static Dou instance;
    
    private Config cfg;
    
    private String dataUrl;
    
    private boolean isCompanies;
    
    private final Set<String> companies = new HashSet<>();
    
    private Map<String, String> cookies;
    
    private String csrfMiddlewareToken;
    
    private final Queue<Company> data = new LinkedList<>();
    
    private Thread thread;
    
    private boolean paused;
    
    private Dou() {
        setCfg(Configurator.defaultCfg());
    }
    
    public static Dou getInstance() {
        return instance != null ? instance : (instance = new Dou());
    }
    
    /**
     * Set configuration and perform connection to DOU.UA
     *
     * @param cfg {@link Config}
     */
    private void setCfg(@NonNull Config cfg) {
        
        this.cfg = cfg;
        cookies = null;
        csrfMiddlewareToken = null;
    }
    
    /**
     * Start (or resumed if interrupted) the data collecting process
     */
    public void start() {
        if (thread == null) {
            if (status()) {
                thread = new Thread(this::processData);
                thread.start();
            } else {
                LOG.warn("Cannot start to collect data - 'status' is false!");
            }
        } else {
            if (paused) {
                paused = false;
                LOG.info("Collecting data is resumed...");
            }
        }
    }
    
    /**
     * Interrupt collecting data process
     * <p>To resume process - call {@link Dou#start()} again
     */
    public void interrupt() {
        if (thread != null) {
            paused = true;
            LOG.info("Collecting data is paused...");
        } else {
            LOG.info("Cannot interrupt - collecting data process is is not started!");
        }
    }
    
    public Queue<Company> data() {
        return data;
    }
    
    /**
     * The same method as {@link Dou#connect(String, String)}.
     * <p>Used it if it's necessary to fetch all data without any city or category.
     *
     * @return 'true' if connection is successful, 'false' - otherwise.
     */
    public boolean connect() {
        return connect(null, null);
    }
    
    /**
     * Initial connection to dou.ua
     * <p>Calling this method is necessary to get cookies and csrf middleware token from the site,
     * prior to start fetching data with method {@link Dou#start()}.
     *
     * @param city     {@link String} with city name - set if it's necessary to fetch data by city, otherwise - set 'null'.
     * @param category {@link String} with category name (for example 'Java') - set if it's necessary to fetch data by category, otherwise - set 'null'.
     * @return 'true' if connection is successful, 'false' - otherwise.
     */
    public boolean connect(String category, String city) {
    
        String baseUrl;
        
        if (category == null && city == null) {
            baseUrl = cfg.getCompaniesUrl();
            isCompanies = true;
        } else {

            baseUrl = cfg.getVacanciesUrl();

            dataUrl = baseUrl + cfg.getDataUrl()
                    + (category != null ? "category=" + category + "&" : "")
                    + (city != null ? "city=" + city : "");

            isCompanies = false;
        }
        
        try {
            Connection.Response response = getConnection(baseUrl).execute();
            
            int statusCode = response.statusCode();
            
            if (statusCode != 200) {
                LOG.error(String.format("Bad connection. Status is %d", statusCode));
                return false;
            }
            
            cookies = response.cookies();
            Document document = response.parse();
            String tokenPattern = cfg.getCsrfTokenPattern();
            Elements tokenElements = document.select(tokenPattern);
            
            if (tokenElements.isEmpty()) {
                LOG.error(String.format("Element '%s' not found on the page %s", tokenPattern, baseUrl));
                return false;
            }
            
            csrfMiddlewareToken = tokenElements.first().val();
            
            LOG.info("Successfully connected to " + baseUrl);
            
        } catch (IOException e) {
            LOG.error(String.format("Connecting or getting data is failed! Cause: %s", e.getMessage()));
            return false;
        }
        
        return true;
    }
    
    /**
     * Main loop of data collection
     */
    private void processData() {
        int i = 0;
        try {
            while (true) {
                if (!paused) {
                    if (!collectData(i)) break;
                    i += 20;
                }
            }
            LOG.info("Done.");
        } catch (IOException e) {
            LOG.error(String.format("Getting companies data is failed at position: %d! Cause: %s", i, e.getMessage()));
        }
    }
    
    /**
     * Collect portion of companies data to inner queue
     *
     * @param dataPosition address of companies data multiple of 20: 0, 20, 40... etc.
     *                     <p>0 - get data of first 20 companies</p>
     *                     <p>20 - get data of next 20 companies from, etc</p>
     * @return 'false' - if end of companies data reached (or if {@link Dou#status()} does not allow to fetch data), 'true' - otherwise.
     * @throws IOException if we have a problem during the connection.
     */
    private boolean collectData(int dataPosition) throws IOException {
        
        if (!status()) {
            LOG.error("Cannot get data - csrfMiddlewareToken or cookies are empty!");
            return false;
        }
        
        Connection.Response response = getConnection(dataUrl)
                .cookies(cookies)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Referer", cfg.getCompaniesUrl())
                .data("csrfmiddlewaretoken", csrfMiddlewareToken)
                .data("count", String.valueOf(dataPosition))
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();
        
        String json = response.body();
        
        HtmlContainer htmlContainer = new Gson().fromJson(json, HtmlContainer.class);
        Document document = Jsoup.parse(htmlContainer.html);
        
        if(isCompanies) {
            processCompanies(document.select("div[class='company']"));
        } else {
            processVacancies(document.select("div[class='vacancy']"));
        }
        
        if (htmlContainer.last) {
            this.data.add(null);
        }
        
        return !htmlContainer.last;
    }
    
    /**
     * Companies data (from vacancy blocks) processing and adding to data queue.
     *
     * @param vacancies collection of {@link Elements} with vacancy data.
     * @throws IOException if something happens during connection.
     */
    private void  processVacancies(Elements vacancies) throws IOException {
    
        if (vacancies.isEmpty()) {
            LOG.error("List of 'vacancies' blocks is empty!");
            return;
        }
    
        for (Element vacancy : vacancies) {
    
            Element company = vacancy.select("a[class='company']").first();
            if(company != null) {
                String name = company.text().replace("\u00A0","");
    
                if (companies.contains(name)) {
                    continue;
                }
                
                companies.add(name);
                
                String url = company.attr("href");
                url = url.substring(0, url.indexOf("vacancies"));
                
                String officesUrl = url + "offices/";
                
                Company douCompany = new Company(name, url, "", officesUrl);
                
                douCompany.getOffices().addAll(getOffices(officesUrl));
                
                this.data.add(douCompany);
            } else {
                LOG.warn("Block 'company' not found, skipping company...");
            }
        }
    }
    
    /**
     * Companies data processing and adding to data queue.
     *
     * @param companies collection of {@link Elements} with company data.
     * @throws IOException if we have a problem during the connection.
     */
    private void processCompanies(Elements companies) throws IOException {
        if (companies.isEmpty()) {
            LOG.error("List of 'company' blocks is empty!");
            return;
        }
        
        for (Element company : companies) {
            
            Element cnA = company.select("a[class='cn-a']").first();
            if (cnA != null) {
                String name = cnA.text();
                String url = cnA.attributes().get("href");
                
                Element descr = company.select("div[class='descr']").first();
                String description = descr != null ? descr.text() : "";
                
                String officesUrl = url + "offices/";
                
                Company douCompany = new Company(name, url, description, officesUrl);
                douCompany.getOffices().addAll(getOffices(officesUrl));
                // this.companies.add(douCompany);
                this.data.add(douCompany);
            } else {
                LOG.warn("Block 'cn-a' not found, skipping company...");
            }
        }
    }
    
    /**
     * Get {@link List} of {@link Office}s processing the data from the url of company offices.
     *
     * @param officesUrl url to the company offices.
     * @return {@link List} of {@link Office}s.
     * @throws IOException if we have a problem during the connection.
     */
    private List<Office> getOffices(String officesUrl) throws IOException {
        
        List<Office> result = new ArrayList<>();
        
        Document document = getConnection(officesUrl).get();
        Elements offices = document.select("div[class='city']");
        for (Element office : offices) {
            Element h4 = office.select("h4").first();
            if (h4 != null) {
                String city = h4.text();
                
                Office companyOffice = new Office(city);
                
                Elements contacts = office.select("div[class='contacts']");
                for (Element contact : contacts) {
                    Element mail = contact.select("div[class='mail'] a").first();
                    if (mail != null) {
                        String email = mail.text();
                        companyOffice.getEmails().add(email);
                    } else {
                        LOG.warn("Block 'mail' not found, skipping contact...");
                    }
                }
                result.add(companyOffice);
            } else {
                LOG.warn("Block 'h4' not found, skipping office...");
            }
        }
        return result;
    }
    
    /**
     * Used to check status of {@link Dou}. If status is 'false' then we cannot get data from {@link Dou}
     * <p>Checking status is necessary after getting instance of {@link Dou}
     * or after calling {@link Dou#setCfg(douex.dou.cfg.Config)} method
     *
     * @return 'true' if we can get data from {@link Dou} and 'false' otherwise.
     */
    public boolean status() {
        return csrfMiddlewareToken != null && cookies != null;
    }
    
    /**
     * Perform random delay based on {@link Config#loadingDataDelay}
     */
    private void delay() {
        List<Integer> delayValues = cfg.getLoadingDataDelay();
        try {
            Thread.sleep(current().nextInt(delayValues.get(0), delayValues.get(1)));
        } catch (InterruptedException ignored) {
        }
    }
    
    /**
     * Get connection to given url (through proxy or not - depend on {@link Dou#cfg})
     *
     * @param url The destination.
     * @return {@link Connection} to the given url.
     * @throws IOException if something happens during connection.
     */
    private Connection getConnection(String url) throws IOException {
        LOG.info("Connecting to {}...", url);
        delay();
        
        Connection connect = Jsoup.connect(url);
        
        if (cfg.getUseProxy()) {
            connect.proxy(cfg.getProxyHost(), cfg.getProxyPort());
        }
        
        return connect
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
                .timeout(cfg.getTimeout());
    }
    
    @SuppressWarnings("initialization.fields.uninitialized")
    private static class HtmlContainer {
        String html;
        Boolean last;
        Integer num;
    }
}
