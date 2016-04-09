package common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class PostsCollector
{
    private Page page;
    private String since;
    private String until;
    private List<Post> posts = new ArrayList<Post>();
    private static final String fields = "id,message,created_time,updated_time,place,tags,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)";

    public PostsCollector(Page page, String since, String until)
    {
        this.page = page;
        this.since = since;
        this.until = until;
    }

    private void collectPosts()
    {
        String url = Config.baseUrl + ("/") + (page.getUsername()) + "/posts";
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
                    posts.add(new Post(page, postJson, Util.getDbDateTimeUtc()));
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
        }
    }

    public List<Post> getPosts()
    {
        return posts;
    }
}
