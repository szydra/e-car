CREATE ALIAS VALIDATE_TIME AS $$
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
@CODE
    boolean isValid(final Connection connection, Integer id, String start, String end) throws SQLException {
        if (start == null || end == null) {
            return true;
        }
        String sql;
        if (start.compareTo(end) > 0) {
            sql = String.format("select count(*) as total from price where " +
                    "(start_hour<end_hour and ('%s'<end_hour or start_hour<'%s') and id!=%s) " +
                    "or (end_hour<start_hour and id!=%s);", start, end, id, id);
        } else {
            sql = String.format("select count(*) as total from price where " +
                    "(start_hour<end_hour and '%s'<end_hour and start_hour<'%s' and id!=%s) " +
                    "or (end_hour<start_hour and ('%s'<end_hour or start_hour<'%s') and id!=%s);", start, end, id, start, end, id);
        }
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery(sql);
        set.next();
        return set.getInt("total") == 0;
    }
$$;

alter table price add constraint test_overlap check(VALIDATE_TIME(id, start_hour, end_hour));
