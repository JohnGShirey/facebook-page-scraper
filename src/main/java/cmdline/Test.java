package cmdline;

import common.comment.CommentsCollector;
import common.Config;
import common.Util;
import db.DbManager;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Test
{
    public static void main(String[] args) throws IOException
    {
        Config.initCollector();
        String postId = "153080620724_10156892090815725";
        String url = Config.baseUrl + "/" + postId + "/comments";
        url += "?access_token=" + Config.getAccessToken();
        url += "&fields=id,message,from,comments.summary(true)&limit=1";
        JSONObject commentsJson = Util.getJson(url);
        FileWriter writer = new FileWriter("C:/z/comments.v2.json");
        commentsJson.writeJSONString(writer);
        writer.close();
        //print();
    }

    public static void print()
    {
        System.out.println(DbManager.getStringValues("select id from Post where comments > 1000 order by created_at desc limit 1"));
    }

    public static void updateComment(String commentId, JSONObject commentJson)
    {
        JSONObject parent = (JSONObject) commentJson.get("parent");
        if(null != parent)
        {
            String parentId = parent.get("id").toString();
            String parentMessage = parent.get("message").toString();
            System.out.println("parentId: " + parentId);
            /*Connection connection = DbManager.getConnection();
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
            }*/
            System.out.println("updated parent for comment: " + commentId);
        }
    }
}
