package common.page;

import common.Config;
import common.Util;
import common.page.Page;
import org.json.simple.JSONObject;

public class PageCollector extends Thread
{
    private String username;

    private static final String fields = "id,username,name,likes,talking_about_count,checkins,website,link,category,affiliation,about";

    public PageCollector(String username)
    {
        this.username = username;
    }

    public void collect()
    {
        String url = Config.baseUrl + "/" + username + "?fields=" + fields + "&access_token=" + Config.accessToken;
        JSONObject pageJson = Util.getJson(url);
        if(null != pageJson)
        {
            Page page = new Page(pageJson, null);
            page.writeJson();
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + username);
            System.exit(0);
        }
    }

    public void collectStats()
    {
        String url = Config.baseUrl + "/" + username + "?fields=" + fields + "&access_token=" + Config.accessToken;
        JSONObject pageJson = Util.getJson(url);
        if(null != pageJson)
        {
            Page page = new Page(pageJson, Util.getDbDateTimeUtc());
            page.updateDb();
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + username);
            System.exit(0);
        }
    }
}
