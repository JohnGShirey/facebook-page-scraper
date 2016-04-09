package common;

import db.DbManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config
{
    public static final String baseUrl = "https://graph.facebook.com/v2.5";
    public static String accessToken;
    public static List<String> pages;
    public static String dbUrl;
    public static String dbUser;
    public static String dbPass;
    public static String jsonDir;
    public static boolean collectOnce;
    public static String since;
    public static String until;
    public static boolean collectComments;
    public static boolean collectLikes;
    public static String insertQueueDir;
    public static String archiveDir;
    public static int statsDepth = 2; // days
    public static int statsInterval = 5; // minutes
    public static boolean statsHistory;

    static
    {
        init();
    }

    public static void init()
    {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try
        {
            if(new File("config.properties").exists())
            {
                inputStream = new FileInputStream("config.properties");
            }
            if(null == inputStream)
            {
                inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
            }
            properties.load(inputStream);
            accessToken = properties.getProperty("accessToken");
            jsonDir = properties.getProperty("jsonDir");
            collectOnce = properties.getProperty("collectOnce").toLowerCase().equals("true");
            pages = Arrays.asList(properties.getProperty("pages").split("\\s*,\\s*"));
            since = properties.getProperty("since");
            until = properties.getProperty("until");
            dbUrl = properties.getProperty("dbUrl");
            dbUser = properties.getProperty("dbUser");
            dbPass = properties.getProperty("dbPass");
            collectComments = properties.getProperty("collectComments").toLowerCase().equals("true");
            collectLikes = properties.getProperty("collectLikes").toLowerCase().equals("true");
            if(null != properties.getProperty("statsDepth") && properties.getProperty("statsDepth").matches("\\d+"))
            {
                statsDepth =  Integer.parseInt(properties.getProperty("statsDepth"));
            }
            if(null != properties.getProperty("statsInterval") && properties.getProperty("statsInterval").matches("\\d+"))
            {
                statsInterval =  Integer.parseInt(properties.getProperty("statsInterval"));
            }
            statsHistory = properties.getProperty("statsHistory").toLowerCase().equals("true");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(null != inputStream)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if(!isConfigValid())
        {
            System.exit(0);
        }

        Util.buildPath("archive");
        Util.buildPath("insert_queue");
        insertQueueDir = jsonDir + "/insert_queue";
        archiveDir = jsonDir + "/archive";
    }

    public static boolean isConfigValid()
    {
        if(null == accessToken || accessToken.isEmpty())
        {
            System.err.println(Util.getDbDateTimeEst() + " accessToken missing");
            return false;
        }

        if(pages == null || pages.size() == 0)
        {
            System.err.println(Util.getDbDateTimeEst() + " pages missing");
            return false;
        }

        if(null == since || since.isEmpty() || !since.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            System.err.println(Util.getDbDateTimeEst() + " invalid start date (since)");
            return false;
        }

        if(null == jsonDir || jsonDir.isEmpty())
        {
            System.err.println(Util.getDbDateTimeEst() + " json directory is required");
            return false;
        }

        File jsonDir = new File(Config.jsonDir);
        if(!jsonDir.exists() || !jsonDir.isDirectory())
        {
            System.err.println(Util.getDbDateTimeEst() + " invalid json directory");
            return false;
        }

        return true;
    }

    public static boolean isDbConfigValid()
    {
        if(null == dbUrl || null == dbUser || null == dbPass)
        {
            System.err.println(Util.getDbDateTimeEst() + " database connection parameters are required");
            return false;
        }

        if(!DbManager.isParamsValid())
        {
            System.err.println(Util.getDbDateTimeEst() + " invalid database parameters");
            return false;
        }

        return true;
    }
}
