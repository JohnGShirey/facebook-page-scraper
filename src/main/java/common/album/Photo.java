package common.album;

import common.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

public class Photo
{
    private String id;
    private String username;
    private String albumId;
    private JSONObject photoJson;

    public Photo(String username, String albumId, JSONObject photoJson)
    {
        this.username = username;
        this.albumId = albumId;
        this.photoJson = photoJson;
        this.id = photoJson.get("id").toString();
    }

    public void writeJson()
    {
        String dir = Util.buildPath("images", username, "albums", albumId, "photos", id);
        String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + id + "_photo.json";
        try
        {
            FileWriter writer = new FileWriter(path);
            photoJson.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println(Util.getDbDateTimeEst() + " failed to write json file " + path);
        }
    }

    public void writeImages()
    {
        String dir = Util.buildPath("images", username, "albums", albumId, "photos", id, "images");
        if(null != photoJson.get("images"))
        {
            JSONArray images = (JSONArray) photoJson.get("images");
            Iterator iterator = images.iterator();
            while (iterator.hasNext())
            {
                JSONObject image = (JSONObject) iterator.next();
                String height = image.get("height").toString();
                String width = image.get("width").toString();
                String imageUrl = image.get("source").toString();
                String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + height + "X" + width + "_image.jpg";
                writeImage(imageUrl, path);
            }
        }
    }

    private void writeImage(String imageUrl, String destinationFile)
    {
        try
        {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            OutputStream out = new FileOutputStream(destinationFile);
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) != -1)
            {
                out.write(b, 0, length);
            }
            in.close();
            out.close();
        }
        catch (Exception e)
        {
            System.err.println(Util.getDbDateTimeEst() + " failed to write image file " + destinationFile);
        }
    }
}
