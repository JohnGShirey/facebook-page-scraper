package common.comment;

import common.Config;
import common.post.Post;
import common.Util;
import db.DbManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CommentsCollector
{
    private String username;
    private String postId;
    private String commentId;
    public JSONArray comments = new JSONArray();
    public static final String fields = "id,message,created_time,from,like_count,comment_count";
    private String accessToken;

    public CommentsCollector(String username, String postId)
    {
        init(username, postId, null);
    }

    public CommentsCollector(String username, String postId, String commentId)
    {
        init(username, postId, commentId);
    }

    private void init(String username, String postId, String commentId)
    {
        this.username = username;
        this.postId = postId;
        this.commentId = commentId;
        this.accessToken = Config.getAccessToken();
    }

    public void collect()
    {
        String url;
        if(null == commentId)
        {
            url = Config.baseUrl + "/" + postId + "/comments";
        }
        else
        {
            url = Config.baseUrl + "/" + commentId + "/comments";
        }
        url += "?access_token=" + accessToken;
        url += "&fields=" + fields;
        while (url != null)
        {
            JSONObject commentsJson = Util.getJson(url);
            if(null != commentsJson)
            {
                JSONArray commentsData = (JSONArray) commentsJson.get("data");
                comments.addAll(commentsData);
                url = null;
                JSONObject paging = (JSONObject) commentsJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading comments failed for url: " + url);
            }
        }

        if(!comments.isEmpty())
        {
            JSONObject obj = new JSONObject();
            obj.put("data", comments);
            writeCommentsJson(obj);
        }
    }

    private void writeCommentsJson(JSONObject commentsJson)
    {
        String dir = Util.buildPath("download", Util.getCurDateDirUtc());
        String path;
        if(null == commentId)
        {
            path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + postId + "_post_comments.json";
        }
        else
        {
            path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + commentId + "_comment_replies.json";
        }
        try
        {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            commentsJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println(Util.getDbDateTimeEst() + " failed to write json file " + path);
        }
    }

    public List<Comment> getComments()
    {
        List<Comment> tempComments = new ArrayList<Comment>();
        Iterator itr = comments.iterator();
        while (itr.hasNext())
        {
            JSONObject comment = (JSONObject) itr.next();
            tempComments.add(new Comment(comment, postId, commentId));
        }
        return tempComments;
    }

    private boolean isFetchRequired()
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
