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
    private static final String fields = "id,message,created_time,updated_time,place,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)";
    private String accessToken;

    public PostsCollector(String username, String since, String until)
    {
        this.username = username;
        this.since = since;
        this.until = until;
        this.accessToken = Config.getAccessToken();
    }

    private void collectPosts()
    {
        String url = Config.baseUrl + ("/") + username + "/posts";
        url += "?access_token=" + accessToken;
        url += "&include_hidden=" + true;
        url += "&since=" + since;
        url += "&until=" + until;
        url += "&fields=" + fields;

        while (null != url)
        {
            JSONObject postsJson = Util.getJson(url);
            String prevUrl = url;
            url = null;
            if(null != postsJson)
            {
                JSONArray postsData = (JSONArray) postsJson.get("data");
                Iterator itr = postsData.iterator();
                while (itr.hasNext())
                {
                    JSONObject postJson = (JSONObject) itr.next();
                    posts.add(new Post(username, postJson, Util.getDbDateTimeUtc()));
                }
                JSONObject paging = (JSONObject) postsJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading posts failed for url: " + prevUrl);
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
