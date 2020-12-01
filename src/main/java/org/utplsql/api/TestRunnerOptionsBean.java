package org.utplsql.api;

import org.utplsql.api.reporter.Reporter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds the various possible options of TestRunner
 *
 * @author pesse
 */
public class TestRunnerOptionsBean implements TestRunnerOptions {
    private final List<String> pathList = new ArrayList<>();
    private final List<Reporter> reporterList = new ArrayList<>();
    private final List<String> coverageSchemes = new ArrayList<>();
    private final List<String> sourceFiles = new ArrayList<>();
    private final List<String> testFiles = new ArrayList<>();
    private final List<String> includeObjects = new ArrayList<>();
    private final List<String> excludeObjects = new ArrayList<>();
    private boolean colorConsole = false;
    private FileMapperOptions sourceMappingOptions;
    private FileMapperOptions testMappingOptions;
    private boolean failOnErrors = false;
    private boolean skipCompatibilityCheck = false;
    private String clientCharacterSet = Charset.defaultCharset().toString();
    private boolean randomTestOrder = false;
    private Integer randomTestOrderSeed;
    private final Set<String> tags = new LinkedHashSet<>();

    public List<String> getPathList() {
        return pathList;
    }

    public List<Reporter> getReporterList() {
        return reporterList;
    }

    public List<String> getCoverageSchemes() {
        return coverageSchemes;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public List<String> getTestFiles() {
        return testFiles;
    }

    public List<String> getIncludeObjects() {
        return includeObjects;
    }

    public List<String> getExcludeObjects() {
        return excludeObjects;
    }

    public boolean isColorConsole() {
        return colorConsole;
    }

    public void setColorConsole(boolean colorConsole) {
        this.colorConsole = colorConsole;
    }

    public FileMapperOptions getSourceMappingOptions() {
        return sourceMappingOptions;
    }

    public void setSourceMappingOptions(FileMapperOptions sourceMappingOptions) {
        this.sourceMappingOptions = sourceMappingOptions;
    }

    public FileMapperOptions getTestMappingOptions() {
        return testMappingOptions;
    }

    public void setTestMappingOptions(FileMapperOptions testMappingOptions) {
        this.testMappingOptions = testMappingOptions;
    }

    public boolean isFailOnErrors() {
        return failOnErrors;
    }

    public void setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors = failOnErrors;
    }

    public boolean getSkipCompatibilityCheck() {
        return skipCompatibilityCheck;
    }

    public void setSkipCompatibilityCheck(boolean skipCompatibilityCheck) {
        this.skipCompatibilityCheck = skipCompatibilityCheck;
    }

    public String getClientCharacterSet() {
        return clientCharacterSet;
    }

    public void setClientCharacterSet(String clientCharacterSet) {
        this.clientCharacterSet = clientCharacterSet;
    }

    public boolean isRandomTestOrder() {
        return randomTestOrder;
    }

    public void setRandomTestOrder(boolean randomTestOrder) {
        this.randomTestOrder = randomTestOrder;
    }

    public Integer getRandomTestOrderSeed() {
        return randomTestOrderSeed;
    }

    public void setRandomTestOrderSeed(Integer randomTestOrderSeed) {
        this.randomTestOrderSeed = randomTestOrderSeed;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public String tagsAsCommaDelimitedString() {
        return String.join(",", tags);
    }
}
