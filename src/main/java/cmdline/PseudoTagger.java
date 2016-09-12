package cmdline;

import common.Config;
import common.Util;
import db.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class PseudoTagger
{
    public static void main(String[] args)
    {
        Config.initPseudoTagging();

        while (true)
        {
            List<Integer> codes = new ArrayList<Integer>();
            for(int i: DbManager.getIntValues("SELECT id FROM `Code`"))
            {
                if(!Config.excludeCodes.contains(i))
                {
                    codes.add(i);
                }
            }

            String q = "SELECT id FROM " + Config.tagTable + " WHERE `code` IS NULL LIMIT 10000";

            List<String> ids = DbManager.getStringValues(q);

            Random random = new Random();

            HashMap<String, Integer> idCode = new HashMap<String, Integer>();

            for(String id: ids)
            {
                int rand = random.nextInt(codes.size());
                idCode.put(id, codes.get(rand));
            }

            updateTag(idCode);

            System.out.println(Util.getDbDateTimeEst() + " applied pseudo tagging to " + ids.size() + " entries");

            Util.sleepMillis(Config.delay);
        }
    }

    private static void updateTag(HashMap<String, Integer> idCode)
    {
        final int batchSize = 100;
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "UPDATE " + Config.tagTable + " SET `code`=? WHERE id=?";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Map.Entry<String, Integer> entry: idCode.entrySet())
            {
                statement.setInt(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.executeUpdate();
                if (++count % batchSize == 0)
                {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
