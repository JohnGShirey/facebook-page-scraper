package common.post;

import cmdline.Inserter;
import common.Config;
import common.Util;
import common.page.Page;
import db.DbManager;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Post
{
    private String username;
    private JSONObject post;
    private String id;
    private String message;
    private String createdAt;
    private int likes;
    private int comments;
    private int shares;
    private String updatedAt;
    private String crawlDateTimeUtc;

    public Post(String username, JSONObject postJson, String crawlDateTimeUtc)
    {
        this.username = username;
        this.post = postJson;
        id = postJson.get("id").toString();
        message = null != postJson.get("message") ? postJson.get("message").toString() : null;
        createdAt = null != postJson.get("created_time") ? postJson.get("created_time").toString() : null;
        shares = getSharesCount(postJson);
        likes = getLikesCount(postJson);
        comments = getCommentsCount(postJson);
        updatedAt = null != postJson.get("updated_time") ? postJson.get("updated_time").toString() : null;
        this.crawlDateTimeUtc = crawlDateTimeUtc;
    }

    public Post(String postId)
    {
        this.id = postId;
        this.username = Post.getUsername(id);
        Connection connection = DbManager.getConnection();
        String query = "SELECT likes,comments,shares FROM Post WHERE id=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, postId);
            resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                this.likes = resultSet.getInt(1);
                this.comments = resultSet.getInt(2);
                this.shares = resultSet.getInt(3);
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
    }

    public static int getLikesCount(JSONObject post)
    {
        int likesCount = 0;
        JSONObject likesObject = (JSONObject) post.get("likes");
        if(null != likesObject)
        {
            JSONObject likesSummaryObject = (JSONObject) likesObject.get("summary");
            if(null != likesSummaryObject)
            {
                likesCount = Integer.parseInt(likesSummaryObject.get("total_count").toString());
            }
        }
        return likesCount;
    }

    public static int getCommentsCount(JSONObject post)
    {
        int commentsCount = 0;
        JSONObject commentsObject = (JSONObject) post.get("comments");
        if(null != commentsObject)
        {
            JSONObject commentsSummaryObject = (JSONObject) commentsObject.get("summary");
            if(null != commentsSummaryObject)
            {
                commentsCount = Integer.parseInt(commentsSummaryObject.get("total_count").toString());
            }
        }
        return commentsCount;
    }

    public static int getSharesCount(JSONObject post)
    {
        int sharesCount = 0;
        JSONObject sharesObject = (JSONObject) post.get("shares");
        if(null != sharesObject && null != sharesObject.get("count"))
        {
            sharesCount = Integer.parseInt(sharesObject.get("count").toString());
        }
        return sharesCount;
    }

    public void writeJson()
    {
        String dir = Util.buildPath("download", Util.getCurDateDirUtc());
        String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + id + "_post.json";
        try
        {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            getPost().writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("failed to write json file " + path);
        }
    }

    public boolean postExists()
    {
        return DbManager.entryExists("Post", "id", getId());
    }

    public void updateDb()
    {
        insertPost();
    }

    private void insertPost()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Post " +
                "(id,page_id,message,created_at,updated_at,likes,comments,shares) " +
                "VALUES (?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE message=VALUES(message),updated_at=VALUES(updated_at)," +
                "likes=VALUES(likes),comments=VALUES(comments),shares=VALUES(shares)";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, id);
            statement.setString(2, Page.getPageId(username));
            statement.setString(3, message);
            statement.setString(4, Util.toDbDateTime(getCreatedAt()));
            statement.setString(5, Util.toDbDateTime(getUpdatedAt()));
            statement.setInt(6, likes);
            statement.setInt(7, comments);
            statement.setInt(8, shares);
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

    public void insertPostCrawl()
    {
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO PostCrawl "
                + "(crawl_date,post_id,likes,comments,shares) "
                + "VALUES (?,?,?,?,?)";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            statement.setString(1, crawlDateTimeUtc);
            statement.setString(2, id);
            statement.setInt(3, likes);
            statement.setInt(4, comments);
            statement.setInt(5, shares);
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

    public String getUsername()
    {
        return username;
    }

    public JSONObject getPost()
    {
        return post;
    }

    public String getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public int getLikes()
    {
        return likes;
    }

    public int getComments()
    {
        return comments;
    }

    public int getShares()
    {
        return shares;
    }

    public String getUpdatedAt()
    {
        return updatedAt;
    }

    public static String getUsername(String postId)
    {
        String pageId = DbManager.getFieldValue("Post", "page_id", "id", postId);
        return DbManager.getFieldValue("Page", "username", "id", pageId);
    }

    public static String getPageId(String postId)
    {
        return DbManager.getFieldValue("Post", "page_id", "id", postId);
    }
}
