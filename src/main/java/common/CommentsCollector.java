package common;

import db.DbManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CommentsCollector extends Thread
{
    private String postId;
    private List<String> postIds;
    public JSONArray comments = new JSONArray();
    public static String fields;

    public CommentsCollector(String postId)
    {
        this.postId = postId;
        if(null == Config.commentFields || Config.commentFields.isEmpty())
        {
            fields = "id,message,created_time,from,like_count,comment_count,message_tags";
        }
        else
        {
            fields = Config.commentFields;
        }
    }

    public CommentsCollector(List<String> postIds)
    {
        this.postIds = postIds;
        if(null == Config.commentFields || Config.commentFields.isEmpty())
        {
            fields = "id,message,created_time,from,like_count,comment_count,message_tags";
        }
        else
        {
            fields = Config.commentFields;
        }
    }

    public void run()
    {
        for(String id: postIds)
        {
            CommentsCollector commentsCollector = new CommentsCollector(id);
            commentsCollector.collect();
            Util.sleepMillis(10 * commentsCollector.comments.size());
        }
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + postId + "/comments";
        url += "?access_token=" + Config.accessToken;
        url += "&fields=" + fields;

        collect(url);

        if(!comments.isEmpty())
        {
            if(Config.collectJson)
            {
                JSONObject obj = new JSONObject();
                obj.put("data", comments);
                writeCommentsJson(obj);
            }

            /*List<Comment> allComments = new ArrayList<Comment>();
            Iterator itr = comments.iterator();
            while (itr.hasNext())
            {
                JSONObject commentJson = (JSONObject) itr.next();
                Comment comment = new Comment(postId, commentJson);
                allComments.add(comment);
            }
            if(Config.updateDb)
            {
                updateDb(allComments);
            }*/
        }
    }

    private void collect(String url)
    {
        JSONObject commentsJson = Util.getJson(url);
        if(null != commentsJson)
        {
            JSONArray commentsData = (JSONArray) commentsJson.get("data");
            comments.addAll(commentsData);
            JSONObject paging = (JSONObject) commentsJson.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading comments failed for url: " + url);
        }
    }

    private void writeCommentsJson(JSONObject commentsJson)
    {
        String dir = Util.buildPath("insert_queue", Util.getCurDateDirUtc());
        String path = dir + "/" + Util.getCurTimeDirUtc() + "_comments_" + postId + ".json";
        try
        {
            FileWriter writer = new FileWriter(path);
            commentsJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean isFetchRequired()
    {
        return getCommentsCount() < new Post(postId).getComments();
    }

    public int getCommentsCount()
    {
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "SELECT COUNT(*) AS Count FROM Comment WHERE post_id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, postId);
            resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                count = resultSet.getInt(1);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != resultSet) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return count;
    }
}
