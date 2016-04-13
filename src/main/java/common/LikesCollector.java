package common;

import db.DbManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LikesCollector
{
    private String page;
    private String postId;
    public JSONArray likes = new JSONArray();
    public static final String fields = "id,name";

    public LikesCollector(String page, String postId)
    {
        this.page = page;
        this.postId = postId;
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + postId + "/likes";
        url += "?access_token=" + Config.accessToken;
        url += "&fields=" + fields;
        while (null != url)
        {
            JSONObject likesJson = Util.getJson(url);
            if(null != likesJson)
            {
                JSONArray likesData = (JSONArray) likesJson.get("data");
                likes.addAll(likesData);
                url = null;
                JSONObject paging = (JSONObject) likesJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading likes data failed for url: " + url);
            }
        }

        if(!likes.isEmpty())
        {

            JSONObject obj = new JSONObject();
            obj.put("data", likes);
            writeLikesJson(obj);

            List<Like> allLikes = new ArrayList<Like>();
            Iterator itr = likes.iterator();
            while (itr.hasNext())
            {
                JSONObject likeJson = (JSONObject) itr.next();
                Like like = new Like(postId, likeJson);
                allLikes.add(like);
            }
        }
    }

    private void writeLikesJson(JSONObject likesJson)
    {
        String dir = Util.buildPath("download", page, Util.getCurDateDirUtc());
        String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_post_likes_" + postId + ".json";
        try
        {
            FileWriter writer = new FileWriter(path);
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
