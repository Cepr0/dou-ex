package douex.cfg;

import com.esotericsoftware.yamlbeans.YamlReader;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;

/**
 * Utility class used to get {@link Config} from external sources
 */
@Slf4j
public class Configurator {

    private static final String DEFAULT_CFG_PATH = "config/dou.yml";

    /**
     * Load config file from given path
     * @param ymlPath path to config file in YAML format
     * @return {@link Config}
     * @throws Exception if config file cannot be red or transform to {@link Config}
     */
    public static Config fileCfg(String ymlPath) throws Exception {
        YamlReader reader = new YamlReader(new FileReader(ymlPath));
        return reader.read(Config.class);
    }

    /**
     * Try to load config parameters from the file at {@link Configurator#DEFAULT_CFG_PATH}.
     * If it doesn't exist then return new instance of {@link Config} with default params.
     * @return {@link Config}
     */
    public static Config defaultCfg() {
        try {
            return fileCfg(DEFAULT_CFG_PATH);
        } catch (Exception e) {
            LOG.error("File '{}' not found! Using default configuration.", DEFAULT_CFG_PATH);
            return new Config();
        }
    }
}
