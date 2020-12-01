package org.utplsql.api.testRunner;

import oracle.jdbc.OracleConnection;
import org.utplsql.api.CustomTypes;
import org.utplsql.api.TestRunnerOptionsBean;
import org.utplsql.api.Version;
import org.utplsql.api.compatibility.OptionalFeatures;
import org.utplsql.api.db.DynamicParameterList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class DynamicTestRunnerStatement implements TestRunnerStatement {

    private CallableStatement stmt;
    private final OracleConnection oracleConnection;
    private final Version utPlSQlVersion;
    private final TestRunnerOptionsBean options;
    private final DynamicParameterList dynamicParameterList;

    private DynamicTestRunnerStatement(Version utPlSQlVersion, OracleConnection connection, TestRunnerOptionsBean options, CallableStatement statement ) throws SQLException {
        this.utPlSQlVersion = utPlSQlVersion;
        this.oracleConnection = connection;
        this.options = options;
        this.stmt = statement;

        this.dynamicParameterList = initParameterList();

        prepareStatement();
    }

    private DynamicParameterList initParameterList() throws SQLException {

        Object[] sourceMappings = (options.getSourceMappingOptions() != null)
                ?FileMapper.buildFileMappingList(oracleConnection, options.getSourceMappingOptions()).toArray()
                :null;
        Object[] testMappings = (options.getTestMappingOptions() != null)
                ?FileMapper.buildFileMappingList(oracleConnection, options.getTestMappingOptions()).toArray()
                :null;

        DynamicParameterList.DynamicParameterListBuilder builder = DynamicParameterList.builder()
                .addIfNotEmpty("a_paths", options.getPathList().toArray(), CustomTypes.UT_VARCHAR2_LIST, oracleConnection)
                .addIfNotEmpty("a_reporters", options.getReporterList().toArray(), CustomTypes.UT_REPORTERS, oracleConnection)
                .addIfNotEmpty("a_color_console", options.isColorConsole())
                .addIfNotEmpty("a_coverage_schemes", options.getCoverageSchemes().toArray(), CustomTypes.UT_VARCHAR2_LIST, oracleConnection)
                .addIfNotEmpty("a_source_file_mappings", sourceMappings, CustomTypes.UT_FILE_MAPPINGS, oracleConnection)
                .addIfNotEmpty("a_test_file_mappings", testMappings, CustomTypes.UT_FILE_MAPPINGS, oracleConnection)
                .addIfNotEmpty("a_include_objects", options.getIncludeObjects().toArray(), CustomTypes.UT_VARCHAR2_LIST, oracleConnection)
                .addIfNotEmpty("a_exclude_objects", options.getExcludeObjects().toArray(), CustomTypes.UT_VARCHAR2_LIST, oracleConnection);

        if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(utPlSQlVersion)) {
            builder.addIfNotEmpty("a_fail_on_errors", options.isFailOnErrors());
        }
        if (OptionalFeatures.CLIENT_CHARACTER_SET.isAvailableFor(utPlSQlVersion)) {
            builder.addIfNotEmpty("a_client_character_set", options.getClientCharacterSet());
        }
        if (OptionalFeatures.RANDOM_EXECUTION_ORDER.isAvailableFor(utPlSQlVersion)) {
            builder.addIfNotEmpty("a_random_test_order", options.isRandomTestOrder())
                    .addIfNotEmpty("a_random_test_order_seed", options.getRandomTestOrderSeed());
        }
        if (OptionalFeatures.TAGS.isAvailableFor(utPlSQlVersion)) {
            builder.addIfNotEmpty("a_tags", options.tagsAsCommaDelimitedString());
        }

        return builder.build();
    }

    private void prepareStatement() throws SQLException {
        if ( stmt == null ) {
            String sql =  "BEGIN " +
                    "ut_runner.run(" +
                    dynamicParameterList.getSql() +
                    ");" +
                    "END;";
            stmt = oracleConnection.prepareCall(sql);
        }

        dynamicParameterList.setParamsStartWithIndex(stmt, 1);
    }

    @Override
    public void execute() throws SQLException {
        stmt.execute();
    }

    @Override
    public String getSql() {
        return dynamicParameterList.getSql();
    }

    @Override
    public void close() throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    public static DynamicTestRunnerStatement forVersion(Version version, Connection connection, TestRunnerOptionsBean options, CallableStatement statement ) throws SQLException {
        OracleConnection oraConn = connection.unwrap(OracleConnection.class);
        return new DynamicTestRunnerStatement(version, oraConn, options, statement);
    }
}
