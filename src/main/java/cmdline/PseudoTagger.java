package cmdline;

import common.Config;
import common.Util;
import db.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PseudoTagger
{
    public static void main(String[] args)
    {
        Config.initPseudoTaggingConfig();

        if(!Config.isDbConfigValid())
        {
            System.err.println("Exiting. Check Configuration.");
            System.exit(0);
        }

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

            String q = "SELECT id FROM Post WHERE `code` IS NULL LIMIT 1000";

            List<String> postIds = DbManager.getStringValues(q);

            Random random = new Random();

            for(String postId: postIds)
            {
                int rand = random.nextInt(codes.size());
                updateTag(postId, codes.get(rand));
            }

            System.out.println(Util.getDbDateTimeEst() + " applied pseudo tagging to " + postIds.size() + " posts");

            Util.sleep(300);
        }
    }

    public static void updateTag(String postId, int code)
    {
        Connection connection = DbManager.getConnection();
        String query = "UPDATE Post SET `code`=? WHERE id=?";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setInt(1, code);
            statement.setString(2, postId);
            statement.executeUpdate();
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
