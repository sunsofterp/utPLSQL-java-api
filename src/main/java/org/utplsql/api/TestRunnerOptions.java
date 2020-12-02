package org.utplsql.api;

import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.Reporter;

import java.util.List;
import java.util.Set;

public interface TestRunnerOptions {
    List<String> getPathList();

    List<Reporter> getReporterList();

    CompatibilityProxy getCompatibilityProxy();

    List<String> getCoverageSchemes();

    List<String> getSourceFiles();

    List<String> getTestFiles();

    List<String> getIncludeObjects();

    List<String> getExcludeObjects();

    boolean isColorConsole();

    FileMapperOptions getSourceMappingOptions();

    FileMapperOptions getTestMappingOptions();

    boolean isFailOnErrors();

    boolean getSkipCompatibilityCheck();

    String getClientCharacterSet();

    boolean isRandomTestOrder();

    Integer getRandomTestOrderSeed();

    Set<String> getTags();

    String tagsAsCommaDelimitedString();
}
