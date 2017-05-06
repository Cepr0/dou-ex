package douex;

import douex.dou.Company;
import douex.dou.Dou;
import douex.dou.Office;
import lombok.extern.slf4j.Slf4j;

/**
 * Main class of the application
 * <p>DOUex - Extracting company emails from DOU.UA
 */
@Slf4j
public class Main {
    private static Dou dou;
    
    public static void main(String... args) throws Exception {
        
        dou = Dou.getInstance();
    
        if (dou.status()) {
            dou.start();
            new Thread(Main::getData).start();
        }
    }
    
    private static void getData() {
        int i = 1;
        while (true) {
            if (!dou.data().isEmpty()) {
                
                Company company = dou.data().poll();
                if (company == null) break;
    
                for (Office office : company.getOffices()) {
                    LOG.info(String.format("%d;%s;%s;%s;%s", i, company.getName(), company.getOfficesUrl(), office.getCity(), office.getEmailsInStr()));
                }
                i++;
            }
        }
    }
}
