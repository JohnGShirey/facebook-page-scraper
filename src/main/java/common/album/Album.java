package common.album;

import common.Util;
import org.json.simple.JSONObject;

import java.io.FileWriter;

public class Album
{
    private String username;
    private String id;
    private String name;
    private JSONObject albumJson;

    public Album(String username, JSONObject albumJson)
    {
        this.username = username;
        this.albumJson = albumJson;
        this.username = username;
        this.id = albumJson.get("id").toString();
        this.name = albumJson.get("name").toString();
    }

    public void writeJson()
    {
        String dir = Util.buildPath("images", getUsername(), "albums", getId());
        String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + getId() + "_album.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            getAlbumJson().writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println(Util.getDbDateTimeEst() + " failed to write json file " + path);
        }
    }

    public String getUsername()
    {
        return username;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public JSONObject getAlbumJson()
    {
        return albumJson;
    }
}
