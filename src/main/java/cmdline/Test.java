package cmdline;

import common.Config;
import common.Util;
import db.DbManager;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Test
{
    public static void main(String[] args)
    {
        Config.init();
        int i = 0;
        for(String postId: DbManager.getStringValues("SELECT id FROM Post ORDER BY created_at DESC"))
        {
            for(String commentId: DbManager.getStringValues("SELECT id FROM Comment WHERE post_id='" + postId + "' ORDER BY created_at DESC"))
            {
                String url = Config.baseUrl + "/" + commentId + "?access_token=" + Config.accessToken + "&fields=parent";
                JSONObject commentJson = Util.getJson(url);
                if(null != commentJson)
                {
                    JSONObject parent = (JSONObject) commentJson.get("parent");
                    if(null != parent)
                    {
                        String parentId = parent.get("id").toString();
                        String parentMessage = parent.get("message").toString();
                        Connection connection = DbManager.getConnection();
                        String query = "UPDATE Comment SET parent_id=?,parent_message=? WHERE id=?";
                        PreparedStatement statement = null;
                        try
                        {
                            statement = connection.prepareStatement(query);
                            statement.setString(1, parentId);
                            statement.setString(2, parentMessage);
                            statement.setString(3, commentId);
                        }
                        catch (SQLException e)
                        {
                            System.err.println("failed to update parent for comment: " + commentId);
                        }
                        finally
                        {
                            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
                            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
                        }
                        System.out.println("updated parent for comment: " + commentId);
                    }
                }
            }
            if(++i == 100)
            {
                break;
            }
        }
    }
}
