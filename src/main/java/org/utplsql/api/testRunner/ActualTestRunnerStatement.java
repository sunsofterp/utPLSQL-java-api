package org.utplsql.api.testRunner;

import org.utplsql.api.TestRunnerOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Provides the call to run tests for the most actual Framework version.
 * Includes fail on error
 *
 * @author pesse
 */
class ActualTestRunnerStatement extends AbstractTestRunnerStatement {

    public ActualTestRunnerStatement(TestRunnerOptions options, Connection connection) throws SQLException {
        super(options, connection);
    }

    @Override
    public String getSql() {
        // Workaround because Oracle JDBC doesn't support passing boolean to stored procedures.
        String colorConsoleStr = Boolean.toString(options.colorConsole);
        String failOnErrors = Boolean.toString(options.failOnErrors);
        String randomExecutionOrder = Boolean.toString(options.randomTestOrder);

        return
                "BEGIN " +
                        "ut_runner.run(" +
                        "a_paths                  => ?, " +
                        "a_reporters              => ?, " +
                        "a_color_console          => " + colorConsoleStr + ", " +
                        "a_coverage_schemes       => ?, " +
                        "a_source_file_mappings   => ?, " +
                        "a_test_file_mappings     => ?, " +
                        "a_include_objects        => ?, " +
                        "a_exclude_objects        => ?, " +
                        "a_fail_on_errors         => " + failOnErrors + ", " +
                        "a_client_character_set   => ?, " +
                        "a_random_test_order      => " + randomExecutionOrder + ", " +
                        "a_random_test_order_seed => ?, "+
                        "a_tags => ?"+
                        "); " +
                        "END;";
    }

    @Override
    protected int createStatement() throws SQLException {
        int curParamIdx = super.createStatement();

        callableStatement.setString(++curParamIdx, options.clientCharacterSet);
        if ( options.randomTestOrderSeed == null ) {
            callableStatement.setNull(++curParamIdx, Types.INTEGER);
        } else {
            callableStatement.setInt(++curParamIdx, options.randomTestOrderSeed);
        }

        if ( options.tags.size() == 0 ) {
            callableStatement.setNull(++curParamIdx, Types.VARCHAR);
        } else {
            callableStatement.setString(++curParamIdx, options.getTagsAsString());
        }

        return curParamIdx;
    }
}
