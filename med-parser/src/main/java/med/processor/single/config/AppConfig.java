package med.processor.single.config;

import java.util.Map;

public class AppConfig {
    private String inputFileName;
    private String outputDir;
    private Integer partitionSize;
    private Integer partitionNumber;
    private Map<String, CountryConfig> countries;

    public AppConfig() {
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public Integer getPartitionSize() {
        return partitionSize;
    }

    public void setPartitionSize(Integer partitionSize) {
        this.partitionSize = partitionSize;
    }

    public Integer getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(Integer partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public Map<String, CountryConfig> getCountries() {
        return countries;
    }

    public void setCountries(Map<String, CountryConfig> countries) {
        this.countries = countries;
    }
}
