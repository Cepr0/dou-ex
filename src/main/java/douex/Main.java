package douex;

import douex.dou.Company;
import douex.dou.Dou;
import douex.dou.Office;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main class of the application
 * <p>DOUex - Extracting company emails from DOU.UA
 */
@Slf4j
public class Main {
    private static Dou dou;
    private static PrintWriter outFile;
    
    public static void main(String... args) throws Exception {
        
        dou = Dou.getInstance();
        
        boolean status = false;
        String outFilePath = "data/dou-emails";
        
        if (args.length == 0) {
            status = dou.connect();
        }
        
        if (args.length == 1) {
            status = dou.connect(args[0], null);
            outFilePath = outFilePath + "-" + args[0];
        }
        
        if (args.length > 1) {
            status = dou.connect(args[0], args[1]);
            outFilePath = outFilePath + "-" + args[0] + "-" + args[1];
        }
        
        if (status) {
            dou.start();
            
            outFilePath = outFilePath + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".csv";
            outFile = new PrintWriter(outFilePath, "UTF-8");
            
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
                    outFile.printf("%d;%s;%s;%s;%s%n", i, company.getName(), company.getOfficesUrl(), office.getCity(), office.getEmailsInStr());
                    outFile.flush();
                }
                i++;
            }
        }
    }
}
