package common.post;

import common.Config;
import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private String username;
    private String since;
    private String until;
    private List<Post> posts = new ArrayList<Post>();
    private static final String fields = "id,message,created_time,updated_time,place,tags,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)";

    public PostsCollector(String username, String since, String until)
    {
        this.username = username;
        this.since = since;
        this.until = until;
    }

    private void collectPosts()
    {
        String url = Config.baseUrl + ("/") + username + "/posts";
        url += "?access_token=" + Config.accessToken;
        url += "&include_hidden=" + true;
        url += "&since=" + since;
        url += "&until=" + until;
        url += "&fields=" + fields;

        while (null != url)
        {
            JSONObject postsJson = Util.getJson(url);
            if(null != postsJson)
            {
                JSONArray postsData = (JSONArray) postsJson.get("data");
                Iterator itr = postsData.iterator();
                while (itr.hasNext())
                {
                    JSONObject postJson = (JSONObject) itr.next();
                    posts.add(new Post(username, postJson, Util.getDbDateTimeUtc()));
                }

                url = null;
                JSONObject paging = (JSONObject) postsJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading posts failed for url: " + url);
            }
        }
    }

    public void collect()
    {
        collectPosts();
        for(Post post: posts)
        {
            post.writeJson();
        }
    }

    public void collectStats()
    {
        collectPosts();
        for(Post post: posts)
        {
            post.updateDb();
            if(Config.statsHistory)
            {
                post.insertPostCrawl();
            }
        }
    }

    public List<Post> getPosts()
    {
        return posts;
    }
}
