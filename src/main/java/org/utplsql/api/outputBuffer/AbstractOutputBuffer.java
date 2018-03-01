package org.utplsql.api.outputBuffer;

import org.utplsql.api.reporter.Reporter;

import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fetches the lines produced by a reporter.
 *
 * @author vinicius
 * @author pesse
 */
abstract class AbstractOutputBuffer implements OutputBuffer {

    private Reporter reporter;

    /**
     * Creates a new DefaultOutputBuffer.
     * @param reporter the reporter to be used
     */
    AbstractOutputBuffer(Reporter reporter) {

        assert reporter.isInit() : "Reporter is not initialized! You can only create OutputBuffers for initialized Reporters";

        this.reporter = reporter;
    }

    /**
     * Returns the reporter used by this buffer.
     * @return the reporter instance
     */
    public Reporter getReporter() {
        return reporter;
    }

    /**
     * Print the lines as soon as they are produced and write to a PrintStream.
     * @param conn DB connection
     * @param ps the PrintStream to be used, e.g: System.out
     * @throws SQLException any sql errors
     */
    public void printAvailable(Connection conn, PrintStream ps) throws SQLException {
        List<PrintStream> printStreams = new ArrayList<>(1);
        printStreams.add(ps);
        printAvailable(conn, printStreams);
    }

    /**
     * Print the lines as soon as they are produced and write to a list of PrintStreams.
     * @param conn DB connection
     * @param printStreams the PrintStream list to be used, e.g: System.out, new PrintStream(new FileOutputStream)
     * @throws SQLException any sql errors
     */
    public void printAvailable(Connection conn, List<PrintStream> printStreams) throws SQLException {
        fetchAvailable(conn, s -> {
            for (PrintStream ps : printStreams)
                ps.println(s);
        });
    }

    protected abstract PreparedStatement getLinesStatement( Connection conn ) throws SQLException;

    protected abstract CallableStatement getLinesCursorStatement( Connection conn ) throws SQLException;

    /**
     * Print the lines as soon as they are produced and call the callback passing the new line.
     * @param conn DB connection
     * @param onLineFetched the callback to be called
     * @throws SQLException any sql errors
     */
    public void fetchAvailable(Connection conn, Consumer<String> onLineFetched) throws SQLException {

        try (PreparedStatement pstmt = getLinesStatement(conn)) {

            pstmt.setString(1, getReporter().getId());
            try (ResultSet resultSet = pstmt.executeQuery() ) {
                while (resultSet.next())
                    onLineFetched.accept(resultSet.getString(1));
            }
        }
    }

    /**
     * Get all lines from output buffer and return it as a list of strings.
     * @param conn DB connection
     * @return the lines
     * @throws SQLException any sql errors
     */
    public List<String> fetchAll(Connection conn) throws SQLException {

        try (CallableStatement cstmt = getLinesCursorStatement(conn)) {

            cstmt.execute();

            try ( ResultSet resultSet = (ResultSet) cstmt.getObject(1)) {

                List<String> outputLines = new ArrayList<>();
                while (resultSet.next()) {
                    outputLines.add(resultSet.getString("text"));
                }
                return outputLines;
            }
        }
    }



}
