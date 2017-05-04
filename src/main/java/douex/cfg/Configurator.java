package douex.cfg;

import com.esotericsoftware.yamlbeans.YamlReader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;

/**
 *
 */
@Slf4j
public class Configurator {

    private static final String DEFAULT_CFG_PATH = "config/dou.yml";

    public static Config fileCfg(String ymlPath) throws Exception {
        YamlReader reader = new YamlReader(new FileReader(ymlPath));
        return reader.read(Config.class);
    }
    
    public static Config defaultCfg() {
        try {
            return fileCfg(DEFAULT_CFG_PATH);
        } catch (Exception e) {
            LOG.error("File '{}' not found! Using default configuration.", DEFAULT_CFG_PATH);
            return new Config();
        }
    }
}
