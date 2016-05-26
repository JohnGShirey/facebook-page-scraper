package common.like;

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

public class LikesCollector
{
    private String username;
    private String postId;
    public JSONArray likes = new JSONArray();
    public static final String fields = "id,name";
    private String accessToken;

    public LikesCollector(String username, String postId)
    {
        this.username = username;
        this.postId = postId;
        this.accessToken = Config.getAccessToken();
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + postId + "/likes";
        url += "?access_token=" + accessToken;
        url += "&fields=" + fields;
        while (null != url)
        {
            JSONObject likesJson = Util.getJson(url);
            String prevUrl = url;
            url = null;
            if(null != likesJson)
            {
                JSONArray likesData = (JSONArray) likesJson.get("data");
                likes.addAll(likesData);
                JSONObject paging = (JSONObject) likesJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading likes data failed for url: " + prevUrl);
            }
        }

        if(!likes.isEmpty())
        {
            JSONObject obj = new JSONObject();
            obj.put("data", likes);
            writeLikesJson(obj);
        }
    }

    private void writeLikesJson(JSONObject likesJson)
    {
        String dir = Util.buildPath("download", Util.getCurDateDirUtc());
        String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + postId + "_post_likes.json";
        try
        {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            likesJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean isFetchRequired()
    {
        return getLikesCount() < new Post(postId).getLikes();
    }

    private int getLikesCount()
    {
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "SELECT COUNT(*) AS Count FROM `Like` WHERE post_id=?";
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
