package cmdline;

import common.Comment;
import common.CommentsCollector;
import common.CommentsInserter;
import common.Config;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test
{
    public static void main(String[] args)
    {
        Config.init();
        List<String> posts = new ArrayList<String>();
        for(String postId: posts)
        {
            List<Comment> allComments = new ArrayList<Comment>();
            List<Comment> comments = getComments(postId, false);
            allComments.addAll(comments);
            for(Comment comment: comments)
            {
                allComments.addAll(getComments(comment.getId(), true));
            }
            CommentsInserter inserter = new CommentsInserter(postId);
            inserter.updateDb(allComments);
        }
    }

    public static List<Comment> getComments(String parentId, boolean commentReply)
    {
        List<Comment> allComments = new ArrayList<Comment>();
        CommentsCollector commentsCollector = new CommentsCollector(parentId);
        String url = Config.baseUrl + "/" + parentId + "/comments";
        url += "?access_token=" + Config.accessToken;
        url += "&fields=" + CommentsCollector.fields;
        commentsCollector.collect(url);
        Iterator itr = commentsCollector.comments.iterator();
        while (itr.hasNext())
        {
            JSONObject commentJson = (JSONObject) itr.next();
            Comment comment = new Comment(commentJson, parentId, commentReply);
            allComments.add(comment);
        }
        return allComments;
    }
}
