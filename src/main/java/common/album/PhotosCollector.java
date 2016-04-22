package common.album;

import common.Config;
import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhotosCollector
{
    private String username;
    private String albumId;
    private List<Photo> photos = new ArrayList<Photo>();
    private static final String fields = "id,height,from,created_time,images,link,album,name,page_story_id,picture,updated_time,icon,place,width";
    private String accessToken;

    public PhotosCollector(String username, String albumId)
    {
        this.username = username;
        this.albumId = albumId;
        accessToken = Config.getAccessToken();
    }

    public void collectPhotos()
    {
        String url = Config.baseUrl + ("/") + albumId + "/photos";
        url += "?access_token=" + accessToken;
        url += "&fields=" + fields;

        while (null != url)
        {
            JSONObject photosJson = Util.getJson(url);
            if(null != photosJson)
            {
                JSONArray photosData = (JSONArray) photosJson.get("data");
                Iterator itr = photosData.iterator();
                while (itr.hasNext())
                {
                    JSONObject photo = (JSONObject) itr.next();
                    getPhotos().add(new Photo(getUsername(), getAlbumId(), photo));
                }

                url = null;
                JSONObject paging = (JSONObject) photosJson.get("paging");
                if(null != paging && null != paging.get("next"))
                {
                    url = paging.get("next").toString();
                }
            }
            else
            {
                System.err.println(Util.getDbDateTimeEst() + " reading albums failed for url: " + url);
            }
        }
    }

    public String getUsername()
    {
        return username;
    }

    public String getAlbumId()
    {
        return albumId;
    }

    public List<Photo> getPhotos()
    {
        return photos;
    }
}
