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

        collect(url);

        if(!likes.isEmpty())
        {
            if(Config.collectJson)
            {
                JSONObject obj = new JSONObject();
                obj.put("data", likes);
                writeLikesJson(obj);
            }

            List<Like> allLikes = new ArrayList<Like>();
            Iterator itr = likes.iterator();
            while (itr.hasNext())
            {
                JSONObject likeJson = (JSONObject) itr.next();
                Like like = new Like(postId, likeJson);
                allLikes.add(like);
            }
            if(Config.updateDb)
            {
                updateDb(allLikes);
            }
        }
    }

    private void collect(String url)
    {
        JSONObject likesJson = Util.getJson(url);
        if(null != likesJson)
        {
            JSONArray likesData = (JSONArray) likesJson.get("data");
            likes.addAll(likesData);
            JSONObject paging = (JSONObject) likesJson.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading likes failed for url: " + url);
        }
    }

    private void writeLikesJson(JSONObject likesJson)
    {
        String jsonDir = Util.buildPath(page, "posts", postId);
        String path = jsonDir + "/likes.json";
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

    public int getLikesCount()
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

    public void updateDb(List<Like> likes)
    {
        List<Like> insertLikes = new ArrayList<Like>();
        Connection connection = DbManager.getConnection();
        String query = "SELECT * FROM `Like` WHERE from_id=? AND post_id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Like like: likes)
            {
                statement.setString(1, like.getFromId());
                statement.setString(2, like.getPostId());
                resultSet = statement.executeQuery();
                if(resultSet.next())
                {
                }
                else
                {
                    insertLikes.add(like);
                }
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
        insertLikes(insertLikes);
    }

    public void insertLikes(List<Like> likes)
    {
        final int batchSize = 1000;
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO `Like` (from_id, from_name, post_id) VALUES (?,?,?)";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Like like: likes)
            {
                statement.setString(1, like.getFromId());
                statement.setString(2, like.getFromName());
                statement.setString(3, like.getPostId());
                statement.addBatch();

                if(++count % batchSize == 0)
                {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
        }
        catch (SQLException e)
        {
            System.err.println("failed to insert likes for post: " + postId);
        }
        finally
        {
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
