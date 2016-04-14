package common.like;

import common.page.Page;
import common.post.Post;
import common.Util;
import db.DbManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikesInserter
{
    private static Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})_(\\d+)_(\\d+)_post_likes.json");
    private File likesJsonFile;
    private String crawlDate;
    private String crawlTime;
    private String dbCrawlDateTime;
    private String pageId;
    private String username;
    private String postId;

    public LikesInserter(File likesJsonFile)
    {
        this.likesJsonFile = likesJsonFile;
        Matcher matcher = pattern.matcher(likesJsonFile.getName());
        if(matcher.matches())
        {
            crawlDate = matcher.group(1);
            crawlTime = matcher.group(2);
            dbCrawlDateTime = crawlDate + " " + crawlTime.replaceAll("-", ":");
            pageId = matcher.group(3);
            postId = pageId + "_" + matcher.group(4);
            this.username = Page.getUsername(pageId);
        }
    }

    public void processLikes()
    {
        JSONObject likesJson = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(likesJsonFile);
            JSONParser parser = new JSONParser();
            likesJson = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        List<Like> allLikes = new ArrayList<Like>();
        JSONArray likesData = (JSONArray) likesJson.get("data");
        Iterator itr = likesData.iterator();
        while (itr.hasNext())
        {
            JSONObject likeJson = (JSONObject) itr.next();
            Like like = new Like(postId, likeJson);
            allLikes.add(like);
        }

        updateDb(allLikes);

        Util.sleepMillis(100);

        boolean success = allLikes.size() <= DbManager.getInt("SELECT COUNT(*) AS count FROM `Like` WHERE post_id='" + postId + "'");

        if(success)
        {
            String dir = Util.buildPath("archive", username, "posts", postId);
            String path = dir + "/" + likesJsonFile.getName();
            success = likesJsonFile.renameTo(new File(path));
            if(!success)
            {
                System.out.println(Util.getDbDateTimeEst() + " failed to move " + likesJsonFile.getAbsolutePath() + " to " + path);
            }
        }
    }

    public void updateDb(List<Like> likes)
    {
        List<Like> insertLikes = new ArrayList<Like>();
        for(Like like: likes)
        {
            if(!likeExists(like))
            {
                insertLikes.add(like);
            }
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
            e.printStackTrace();
        }
        finally
        {
            if(null != statement) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if(null != connection) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean likeExists(Like like)
    {
        boolean exists = false;
        Connection connection = DbManager.getConnection();
        String query = "SELECT * FROM `Like` WHERE from_id=? AND post_id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, like.getFromId());
            statement.setString(2, like.getPostId());
            resultSet = statement.executeQuery();
            if(resultSet.next())
            {
                exists = true;
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

        return exists;
    }
}
