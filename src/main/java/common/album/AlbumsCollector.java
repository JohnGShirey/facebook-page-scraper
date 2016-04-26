package common.album;

import common.Config;
import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlbumsCollector
{
    private String username;
    private List<Album> albums = new ArrayList<Album>();
    private static final String fields = "count,name,id,can_upload,cover_photo,created_time,description,from,link,type,updated_time";
    private String accessToken;

    public AlbumsCollector(String username)
    {
        this.username = username;
        accessToken = Config.getAccessToken();
    }

    public void collectAlbums()
    {
        String url = Config.baseUrl + ("/") + username + "/albums";
        url += "?access_token=" + accessToken;
        url += "&fields=" + fields;

        while (null != url)
        {
            JSONObject albumsJson = Util.getJson(url);
            if(null != albumsJson)
            {
                JSONArray albumsData = (JSONArray) albumsJson.get("data");
                Iterator itr = albumsData.iterator();
                while (itr.hasNext())
                {
                    JSONObject album = (JSONObject) itr.next();
                    albums.add(new Album(username, album));
                }

                url = null;
                JSONObject paging = (JSONObject) albumsJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading albums failed for url: " + url);
                if(Config.exitWhenFetchFails)
                {
                    System.exit(0);
                }
            }
        }
    }

    public List<Album> getAlbums()
    {
        return albums;
    }
}
