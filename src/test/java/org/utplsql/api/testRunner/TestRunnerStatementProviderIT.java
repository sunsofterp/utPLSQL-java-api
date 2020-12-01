package org.utplsql.api.testRunner;

import org.junit.jupiter.api.Test;
import org.utplsql.api.*;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.ReporterFactory;

import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunnerStatementProviderIT extends AbstractDatabaseTest {

    public static TestRunnerOptionsBean getCompletelyFilledOptions() {
        TestRunnerOptionsBean options = new TestRunnerOptionsBean();
        options.getPathList().add("path");
        options.getReporterList().add(ReporterFactory.createEmpty().createReporter(CoreReporters.UT_DOCUMENTATION_REPORTER.name()));
        options.getCoverageSchemes().add("APP");
        options.setSourceMappingOptions(new FileMapperOptions(Arrays.asList("sourcePath")));
        options.setTestMappingOptions(new FileMapperOptions(Arrays.asList("testPath")));
        options.getIncludeObjects().add("include1");
        options.getExcludeObjects().add("exclude1");
        options.setFailOnErrors(true);
        options.setClientCharacterSet("UTF8");
        options.setRandomTestOrder(true);
        options.setRandomTestOrderSeed(123);
        options.getTags().add("WIP");
        options.getTags().add("long_running");
        return options;
    }

    TestRunnerStatement getTestRunnerStatementForVersion( Version version ) throws SQLException {
        return TestRunnerStatementProvider.getCompatibleTestRunnerStatement(version, getCompletelyFilledOptions(), getConnection());
    }

    @Test
    void testGettingPre303Version() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_0_2);
        assertThat(stmt.getSql(), not(containsString("a_fail_on_errors")));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }


    @Test
    void testGettingPre312Version_from_303() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_0_3);
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre312Version_from_311() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_1);
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), not(containsString("a_client_character_set")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre317Version_from_312() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_2);
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingPre317Version_from_316() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.V3_1_6);
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order")));
        assertThat(stmt.getSql(), not(containsString("a_random_test_order_seed")));
        assertThat(stmt.getSql(), not(containsString("a_tags")));
    }

    @Test
    void testGettingActualVersion_from_latest() throws SQLException {
        TestRunnerStatement stmt = getTestRunnerStatementForVersion(Version.LATEST);
        assertThat(stmt.getSql(), containsString("a_fail_on_errors"));
        assertThat(stmt.getSql(), containsString("a_client_character_set"));
        assertThat(stmt.getSql(), containsString("a_random_test_order"));
        assertThat(stmt.getSql(), containsString("a_random_test_order_seed"));
        assertThat(stmt.getSql(), containsString("a_tags"));
    }
}
