package common;

import cmdline.FbCollector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private Page page;
    private String since;
    private String until;
    public List<String> postIds;
    public int commentsCount = 0;
    public int likesCount = 0;
    public static String fields;

    public PostsCollector(Page page, String since, String until)
    {
        this.page = page;
        this.since = since;
        this.until = until;
        postIds = new ArrayList<String>();
        if(null == Config.postFields || Config.postFields.isEmpty())
        {
            fields = "id,message,created_time,updated_time,place,tags,shares," +
                    "likes.limit(1).summary(true),comments.limit(1).summary(true)";
        }
        else
        {
            fields = Config.postFields;
        }
    }

    public void collect()
    {
        String url = Config.baseUrl + ("/") + (page.getUsername()) + "/posts";
        url += "?access_token=" + Config.accessToken;
        url += "&include_hidden=" + true;
        url += "&since=" + since;
        url += "&until=" + until;
        url += "&fields=" + fields;

        collect(url);

        if(!FbCollector.collectStats)
        {
            for(String postId: postIds)
            {
                if(Config.collectComments)
                {
                    CommentsCollector commentsCollector = new CommentsCollector(page.getUsername(), postId);
                    if(FbCollector.scrapeCount == 0 || commentsCollector.isFetchRequired())
                    {
                        commentsCollector.collect();
                        commentsCount += commentsCollector.comments.size();
                    }
                }
                if(Config.collectLikes)
                {
                    LikesCollector likesCollector = new LikesCollector(page.getUsername(), postId);
                    if(FbCollector.scrapeCount == 0 || likesCollector.isFetchRequired())
                    {
                        likesCollector.collect();
                        likesCount += likesCollector.likes.size();
                    }
                }
            }
        }
    }

    private void collect(String url)
    {
        JSONObject posts = Util.getJson(url);
        if(null != posts)
        {
            JSONArray postsData = (JSONArray) posts.get("data");
            Iterator itr = postsData.iterator();
            while (itr.hasNext())
            {
                JSONObject postJson = (JSONObject) itr.next();
                Post post = new Post(page, postJson);
                if(Config.collectJson)
                {
                    post.writeJson();
                }
                if(Config.updateDb)
                {
                    post.updateDb();
                }
                postIds.add(post.getId());
            }
            JSONObject paging = (JSONObject) posts.get("paging");
            if(null != paging && null != paging.get("next"))
            {
                collect(paging.get("next").toString());
            }
        }
        else
        {
            System.err.println("reading posts failed for url: " + url);
        }
    }
}
