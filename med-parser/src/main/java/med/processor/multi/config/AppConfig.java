package med.processor.multi.config;

import java.util.Map;

public class AppConfig {
    private String baseDir;
    private Map<String, CountryConfig> countries;

    public AppConfig() {
    }
    
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public Map<String, CountryConfig> getCountries() {
        return countries;
    }

    public void setCountries(Map<String, CountryConfig> countries) {
        this.countries = countries;
    }
}
