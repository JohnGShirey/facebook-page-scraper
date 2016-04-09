package common;

import org.json.simple.JSONObject;

import java.util.List;

public class PageCollector extends Thread
{
    private String page;

    private static final String fields = "id,username,name,likes,talking_about_count,checkins,website,link,category,affiliation,about";

    public PageCollector(String page)
    {
        this.page = page;
    }

    public void collect()
    {
        JSONObject pageJson = getPageJson();
        if(null != pageJson)
        {
            Page page = new Page(pageJson, null);
            page.writeJson();
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + page);
            System.exit(0);
        }
    }

    public void collectStats()
    {
        JSONObject pageJson = getPageJson();
        if(null != pageJson)
        {
            Page page = new Page(pageJson, Util.getDbDateTimeUtc());
            page.updateDb();
        }
        else
        {
            System.err.println(Util.getDbDateTimeEst() + " cannot read data for facebook page: " + page);
            System.exit(0);
        }
    }

    private JSONObject getPageJson()
    {
        String url = Config.baseUrl + "/" + page + "?fields=" + fields + "&access_token=" + Config.accessToken;
        return Util.getJson(url);
    }
}
