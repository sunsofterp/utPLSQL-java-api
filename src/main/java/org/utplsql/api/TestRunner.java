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
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Vinicius Avellar on 12/04/2017.
 *
 * @author Vinicius Avellar
 * @author pesse
 */
public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private final TestRunnerOptions options;

    public TestRunner() {
        this(new TestRunnerOptionsBean());
    }

    public TestRunner(TestRunnerOptions options) {
        this.options = options;
    }

    private void handleException(Throwable e) throws SQLException {
        // Just pass exceptions already categorized
        if ( e instanceof UtPLSQLNotInstalledException ) throw (UtPLSQLNotInstalledException)e;
        else if ( e instanceof SomeTestsFailedException ) throw (SomeTestsFailedException)e;
        else if ( e instanceof OracleCreateStatmenetStuckException ) throw (OracleCreateStatmenetStuckException)e;
        // Categorize exceptions
        else if (e instanceof SQLException) {
            SQLException sqlException = (SQLException) e;
            if (sqlException.getErrorCode() == SomeTestsFailedException.ERROR_CODE) {
                throw new SomeTestsFailedException(sqlException.getMessage(), e);
            } else if (((SQLException) e).getErrorCode() == UtPLSQLNotInstalledException.ERROR_CODE) {
                throw new UtPLSQLNotInstalledException(sqlException);
            } else {
                throw sqlException;
            }
        } else {
            throw new SQLException("Unknown exception, wrapping: " + e.getMessage(), e);
        }
    }

    public void run(Connection conn) throws SQLException {
        logger.info("TestRunner initialized");
        TestRunnerStatement testRunnerStatement = null;
        try {
            testRunnerStatement = initStatementWithTimeout(conn);
            logger.info("Running tests");
            testRunnerStatement.execute();
            logger.info("Running tests finished.");
            testRunnerStatement.close();
        } catch (OracleCreateStatmenetStuckException e) {
            // Don't close statement in this case for it will be stuck, too
            throw e;
        } catch (SQLException e) {
            if (testRunnerStatement != null) testRunnerStatement.close();
            handleException(e);
        }
    }

    private TestRunnerStatement initStatementWithTimeout( Connection conn ) throws OracleCreateStatmenetStuckException, SQLException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestRunnerStatement> future = executor.submit(
            () -> options.getCompatibilityProxy().getTestRunnerStatement(options, conn)
        );

        // We want to leave the statement open in case of stuck scenario
        TestRunnerStatement testRunnerStatement = null;
        try {
            testRunnerStatement = future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Detected Oracle driver stuck during Statement initialization");
            executor.shutdownNow();
            throw new OracleCreateStatmenetStuckException(e);
        } catch (InterruptedException e) {
            handleException(e);
        } catch (ExecutionException e) {
            handleException(e.getCause());
        }

        return testRunnerStatement;
    }

    /**
     * Returns the databaseVersion the TestRunner was run against
     *
     * @return Version of the database the TestRunner was run against
     */
    public Version getUsedDatabaseVersion() {
        if (options.getCompatibilityProxy() != null) {
            return options.getCompatibilityProxy().getUtPlsqlVersion();
        } else {
            return null;
        }
    }

}
