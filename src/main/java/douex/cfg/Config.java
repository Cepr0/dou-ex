package douex.cfg;

import lombok.Data;

/**
 * @author Cepro, 2017-05-04
 */
@Data
public class Config {
    private String url = "https://jobs.dou.ua/companies/";
    private String csrfmiddlewaretoken = "input[name='csrfmiddlewaretoken']";
    private String post = "https://jobs.dou.ua/companies/xhr-load/?";
    private Boolean useProxy = false;
    private String proxyHost = "localhost";
    private Integer proxyPort = 3128;
    private Integer timeout = 2000;
}
