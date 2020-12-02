package org.utplsql.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.db.DatabaseInformation;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.OracleCreateStatmenetStuckException;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.reporter.DocumentationReporter;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.testRunner.TestRunnerStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Vinicius Avellar on 12/04/2017.
 *
 * @author Vinicius Avellar
 * @author pesse
 */
public class TestRunnerOptionsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TestRunnerOptionsBuilder.class);

    private final TestRunnerOptionsBean options = new TestRunnerOptionsBean();
    private final List<String> reporterNames = new ArrayList<>();
    private CompatibilityProxy compatibilityProxy;
    private ReporterFactory reporterFactory;

    public TestRunnerOptionsBuilder addPath(String path) {
        options.getPathList().add(path);
        return this;
    }

    public TestRunnerOptionsBuilder addPathList(List<String> paths) {
        options.getPathList().addAll(paths);
        return this;
    }

    public TestRunnerOptionsBuilder addReporter(Reporter reporter) {
        options.getReporterList().add(reporter);
        return this;
    }

    public TestRunnerOptionsBuilder addReporter(String reporterName) {
        if (reporterFactory != null) {
            options.getReporterList().add(reporterFactory.createReporter(reporterName));
        } else {
            reporterNames.add(reporterName);
        }
        return this;
    }

    public TestRunnerOptionsBuilder colorConsole(boolean colorConsole) {
        options.setColorConsole(colorConsole);
        return this;
    }

    public TestRunnerOptionsBuilder addReporterList(List<Reporter> reporterList) {
        options.getReporterList().addAll(reporterList);
        return this;
    }

    public TestRunnerOptionsBuilder addCoverageScheme(String coverageScheme) {
        options.getCoverageSchemes().add(coverageScheme);
        return this;
    }

    public TestRunnerOptionsBuilder addCoverageSchemes(Collection<String> schemaNames) {
        this.options.getCoverageSchemes().addAll(schemaNames);
        return this;
    }

    public TestRunnerOptionsBuilder includeObject(String obj) {
        options.getIncludeObjects().add(obj);
        return this;
    }

    public TestRunnerOptionsBuilder excludeObject(String obj) {
        options.getExcludeObjects().add(obj);
        return this;
    }

    public TestRunnerOptionsBuilder includeObjects(List<String> obj) {
        options.getIncludeObjects().addAll(obj);
        return this;
    }

    public TestRunnerOptionsBuilder excludeObjects(List<String> obj) {
        options.getExcludeObjects().addAll(obj);
        return this;
    }

    public TestRunnerOptionsBuilder sourceMappingOptions(FileMapperOptions mapperOptions) {
        options.setSourceMappingOptions(mapperOptions);
        return this;
    }

    public TestRunnerOptionsBuilder testMappingOptions(FileMapperOptions mapperOptions) {
        options.setTestMappingOptions(mapperOptions);
        return this;
    }

    public TestRunnerOptionsBuilder failOnErrors(boolean failOnErrors) {
        options.setFailOnErrors(failOnErrors);
        return this;
    }

    public TestRunnerOptionsBuilder skipCompatibilityCheck(boolean skipCompatibilityCheck) {
        options.setSkipCompatibilityCheck(skipCompatibilityCheck);
        return this;
    }

    public TestRunnerOptionsBuilder setReporterFactory(ReporterFactory reporterFactory) {
        this.reporterFactory = reporterFactory;
        return this;
    }

    public TestRunnerOptionsBuilder randomTestOrder(boolean randomTestOrder ) {
        this.options.setRandomTestOrder(randomTestOrder);
        return this;
    }

    public TestRunnerOptionsBuilder randomTestOrderSeed(Integer seed) {
        this.options.setRandomTestOrderSeed(seed);
        this.options.setRandomTestOrder(true);
        return this;
    }

    public TestRunnerOptionsBuilder addTag(String tag ) {
        this.options.getTags().add(tag);
        return this;
    }

    public TestRunnerOptionsBuilder addTags(Collection<String> tags) {
        this.options.getTags().addAll(tags);
        return this;
    }

    public TestRunnerOptions build(final Connection conn) throws SQLException {
        DatabaseInformation databaseInformation = new DefaultDatabaseInformation();

        if ( options.getSkipCompatibilityCheck() ) {
            compatibilityProxy = new CompatibilityProxy(conn, Version.LATEST, databaseInformation);
        } else {
            compatibilityProxy = new CompatibilityProxy(conn, databaseInformation);
        }
        logger.info("Running on utPLSQL {}", compatibilityProxy.getVersionDescription());

        if (reporterFactory == null) {
            reporterFactory = ReporterFactory.createDefault(compatibilityProxy);
        }

        delayedAddReporters();

        // First of all check version compatibility
        compatibilityProxy.failOnNotCompatible();

        logger.info("Initializing reporters");
        for (Reporter r : options.getReporterList()) {
            validateReporter(conn, r);
        }

        if (options.getPathList().isEmpty()) {
            options.getPathList().add(databaseInformation.getCurrentSchema(conn));
        }

        if (options.getReporterList().isEmpty()) {
            logger.info("No reporter given so choosing ut_documentation_reporter");
            options.getReporterList().add(new DocumentationReporter().init(conn));
        }

        return this.options;
    }

    private void delayedAddReporters() {
        if (reporterFactory != null) {
            reporterNames.forEach(
                    reporter -> this.options.getReporterList().add(this.reporterFactory.createReporter(reporter))
            );
        } else {
            throw new IllegalStateException("ReporterFactory must be set to add delayed Reporters!");
        }
    }

    /**
     * Check if the reporter was initialized, if not call reporter.init.
     *
     * @param conn     the database connection
     * @param reporter the reporter
     * @throws SQLException any sql exception
     */
    private void validateReporter(Connection conn, Reporter reporter) throws SQLException {
        if (!reporter.isInit() || reporter.getId() == null || reporter.getId().isEmpty()) {
            reporter.init(conn, compatibilityProxy, reporterFactory);
        }
    }
}
