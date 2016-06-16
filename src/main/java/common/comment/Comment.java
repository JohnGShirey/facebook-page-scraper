package common.comment;

import db.DbManager;
import org.json.simple.JSONObject;

public class Comment
{
    private String id;
    private String message;
    private String createdAt;
    private int likes;
    private int replies;
    private String fromId;
    private String fromName;
    private String postId;
    private String parentCommentId;

    public Comment(JSONObject comment, String postId, String parentCommentId)
    {
        id = comment.get("id").toString();
        message = null != comment.get("message") ? comment.get("message").toString() : null;
        createdAt = null != comment.get("created_time") ? comment.get("created_time").toString() : null;
        likes = null != comment.get("like_count") ? Integer.parseInt(comment.get("like_count").toString()) : 0;
        replies = null != comment.get("comment_count") ? Integer.parseInt(comment.get("comment_count").toString()) : 0;
        JSONObject from = (JSONObject) comment.get("from");
        if(null != from)
        {
            fromId = null != from.get("id") ? from.get("id").toString() : "";
            fromName = null != from.get("name") ? from.get("name").toString() : "";
        }
        this.postId = postId;
        this.parentCommentId = parentCommentId;
    }

    public boolean commentExists()
    {
        return DbManager.entryExists("Comment", "id", getId());
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

    public String getFromId()
    {
        return fromId;
    }

    public String getFromName()
    {
        return fromName;
    }

    public int getReplies() {
        return replies;
    }

    public String getPostId()
    {
        return postId;
    }

    public String getParentCommentId()
    {
        return parentCommentId;
    }
}
