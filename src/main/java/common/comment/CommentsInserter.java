package common.comment;

import common.page.Page;
import common.Util;
import common.post.Post;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsInserter
{
    private static Pattern pattern = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})_(\\d+)_(\\d+)_(post_comments|comment_replies).json");
    private File commentsJsonFile;
    private String postId;
    private String crawlDate;
    private String crawlTime;
    private String dbCrawlDateTime;
    private String pageId;
    private String username;
    private String commentId;

    public CommentsInserter(File commentsJsonFile)
    {
        this.commentsJsonFile = commentsJsonFile;
        Matcher matcher = pattern.matcher(commentsJsonFile.getName());
        if(matcher.matches())
        {
            crawlDate = matcher.group(1);
            crawlTime = matcher.group(2);
            dbCrawlDateTime = crawlDate + " " + crawlTime.replaceAll("-", ":");
            if(matcher.group(5).equals("post_comments"))
            {
                pageId = matcher.group(3);
                postId = matcher.group(3) + "_" + matcher.group(4);
            }
            else
            {
                commentId = matcher.group(3) + "_" + matcher.group(4);
                postId = DbManager.getFieldValue("Comment", "post_id", "id", commentId);
                pageId = Post.getPageId(postId);
            }
            username = Page.getUsername(pageId);
        }
    }

    public CommentsInserter(String postId)
    {
        this.postId = postId;
    }

    public void processComments()
    {
        JSONObject commentsJson = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(commentsJsonFile);
            JSONParser parser = new JSONParser();
            commentsJson = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        List<Comment> allComments = new ArrayList<Comment>();
        JSONArray commentsData = (JSONArray) commentsJson.get("data");
        Iterator itr = commentsData.iterator();
        while (itr.hasNext())
        {
            JSONObject commentJson = (JSONObject) itr.next();
            Comment comment = new Comment(commentJson, postId, commentId);
            allComments.add(comment);
        }

        int rowsUpdated = updateDb(allComments);

        Util.sleepMillis(100);

        boolean success = rowsUpdated == allComments.size();

        //System.out.println("comments: " + allComments.size() + " updated: " + rowsUpdated);

        /*if(null == commentId)
        {
            success = allComments.size() <= DbManager.getInt("SELECT COUNT(*) AS count FROM Comment WHERE post_id='" + postId + "'");
        }
        else
        {
            success = allComments.size() <= DbManager.getInt("SELECT COUNT(*) AS count FROM Comment WHERE parent_id='" + commentId + "'");
        }*/

        if(success)
        {
            String dir = Util.buildPath("archive", username, "posts", postId);
            String path = dir + "/" + commentsJsonFile.getName();
            success = commentsJsonFile.renameTo(new File(path));
            if(!success)
            {
                System.err.println(Util.getDbDateTimeEst() + " failed to move " + commentsJsonFile.getAbsolutePath() + " to " + path);
                System.exit(0);
            }
        }
    }

    public int updateDb(List<Comment> comments)
    {
        int rowsUpdated = 0;
        final int batchSize = 100;
        int count = 0;
        Connection connection = DbManager.getConnection();
        String query = "INSERT INTO Comment " +
                "(id, post_id, message, created_at, from_id, from_name, likes, replies, parent_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE post_id=VALUES(post_id),message=VALUES(message),likes=VALUES(likes),replies=VALUES(replies),parent_id=VALUES(parent_id)";
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(query);
            for(Comment comment: comments)
            {
                statement.setString(1, comment.getId());
                statement.setString(2, comment.getPostId());
                statement.setString(3, comment.getMessage());
                statement.setString(4, Util.toDbDateTime(comment.getCreatedAt()));
                statement.setString(5, comment.getFromId());
                statement.setString(6, comment.getFromName());
                statement.setInt(7, comment.getLikes());
                statement.setInt(8, comment.getReplies());
                statement.setString(9, comment.getParentCommentId());
                statement.addBatch();
                if(++count % batchSize == 0)
                {
                    int[] tempUpdated = statement.executeBatch();
                    for(int i: tempUpdated)
                    {
                        rowsUpdated += i;
                    }
                }
            }
            int[] tempUpdated = statement.executeBatch();
            for(int i: tempUpdated)
            {
                rowsUpdated += i;
            }
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
        return rowsUpdated;
    }
}
