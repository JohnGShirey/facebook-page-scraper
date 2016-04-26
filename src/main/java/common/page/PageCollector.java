package common.page;

import common.Config;
import common.Util;
import common.page.Page;
import org.json.simple.JSONObject;

public class PageCollector extends Thread
{
    private String username;
    private String accessToken;
    private Page page;

    // v2.4 and v2.5
    //private static final String fields = "id,username,name,likes,talking_about_count,checkins,website,link,category,affiliation,about";
    // v2.6 - "likes" is now "fan_count"
    private static final String fields = "id,username,name,fan_count,talking_about_count,checkins,website,link,category,affiliation,about";

    public PageCollector(String username)
    {
        this.username = username;
        this.accessToken = Config.getAccessToken();
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + username + "?fields=" + fields + "&access_token=" + accessToken;
        JSONObject pageJson = Util.getJson(url);
        if(null != pageJson)
        {
            page = new Page(pageJson, null);
            page.writeJson();
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + username);
            if(Config.exitWhenFetchFails)
            {
                System.exit(0);
            }
        }
    }

    public void collectStats()
    {
        String url = Config.baseUrl + "/" + username + "?fields=" + fields + "&access_token=" + accessToken;
        JSONObject pageJson = Util.getJson(url);
        if(null != pageJson)
        {
            Page page = new Page(pageJson, Util.getDbDateTimeUtc());
            page.updateDb();
            if(Config.statsHistory)
            {
                page.insertPageCrawl();
            }
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + username);
            if(Config.exitWhenFetchFails)
            {
                System.exit(0);
            }
        }
    }

    public Page getPage()
    {
        return page;
    }
}
