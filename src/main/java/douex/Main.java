package douex;

import douex.cfg.Configurator;
import douex.dou.Dou;

/**
 * @author Cepro, 2017-04-24
 */
public class Main {

    public static void main(String... args) throws Exception {

        Dou dou = Dou.getInstance();
       
        if (dou.setCfg(Configurator.defaultCfg())) {
            dou.post(0).forEach(System.out::println);
            System.out.println("-----------------");
            dou.post(20).forEach(System.out::println);
            System.out.println("-----------------");
            dou.post(40).forEach(System.out::println);
        }
    }
}
