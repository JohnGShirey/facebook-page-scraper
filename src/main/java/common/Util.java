package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Util
{
    public static DateFormat dbDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getDbDateTimeEst()
    {
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = new Date(System.currentTimeMillis());
        return dbDateFormatter.format(date);
    }

    public static String getDbDateTimeUtc()
    {
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return dbDateFormatter.format(date);
    }

    public static String getCurDateTimeUtc()
    {
        DateFormat dbDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return dbDateFormatter.format(date);
    }

    public static String getCurDateTimeDirUtc()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String getCurDateDirUtc()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String getCurTimeDirUtc()
    {
        DateFormat formatter = new SimpleDateFormat("HH-mm-ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String getDateTimeUtc(long time)
    {
        DateFormat dbDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dbDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dbDateFormatter.format(new Date(time));
    }

    public static String getCurDateTimeDir()
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static long toMillis(String utcDateString)
    {
        return toMillis(utcDateString, TimeZone.getTimeZone("UTC"));
    }

    public static long toMillis(String dateString, TimeZone timeZone)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(timeZone);
        long time = 0;
        try
        {
            time = formatter.parse(dateString).getTime();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return time;
    }

    public static String toDbDateTime(String utcDate)
    {
        return utcDate.replaceFirst("\\+[\\d]+","").replaceFirst("T", " ");
    }

    /**
     * Build path for writing json file
     * Create directories along the path under main json collection directory
     */
    public static String buildPath(String... directories)
    {
        String dir = Config.baseDir;
        for(String temp: directories)
        {
            dir = dir + "/" + temp;
            if(!(new File(dir).exists()))
            {
                new File(dir).mkdir();
            }
        }
        return dir;
    }

    public static JSONObject getJson(String url)
    {
        JSONObject json = null;
        int retries = 0;
        while(null == json && retries++ < 5)
        {
            InputStream is = null;
            try
            {
                is = new URL(url).openStream();
                JSONParser parser = new JSONParser();
                json = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage());
                if(retries < 5)
                {
                    System.err.println(Util.getDbDateTimeEst() + " retrying fetch url: " + url);
                }
                else
                {
                    System.err.println(Util.getDbDateTimeEst() + " reading failed for url: " + url);
                    e.printStackTrace();
                }
                Util.sleep(30);
            }
            finally
            {
                try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
        return json;
    }

    public static void sleep(int seconds)
    {
        try
        {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void sleepMillis(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
