package douex.cfg;

import douex.dou.Dou;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Config parameters container (with default values) used to setup {@link Dou}
 */
@Data
public class Config {
    private String startUrl = "https://jobs.dou.ua/companies/";
    private String csrfTokenPattern = "input[name='csrfmiddlewaretoken']";
    private String dataUrl = "https://jobs.dou.ua/companies/xhr-load/?";
    private Boolean useProxy = false;
    private String proxyHost = "localhost";
    private Integer proxyPort = 3128;
    private Integer timeout = 2000;
    private List<String> loadingDataDelay = asList("3000", "5000");

    public List<Integer> getDelays() {
        return loadingDataDelay.stream().map(Integer::valueOf).collect(Collectors.toList());
    }
}
