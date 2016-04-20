package common;

import db.DbManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Config
{
    public static final String baseUrl = "https://graph.facebook.com/v2.6";
    public static final int dayInMillis = 86400000;
    public static final int hourInMillis = 3600000;
    public static final int minuteInMillis = 60000;

    private static String accessToken;
    private static List<String> accessTokenPool = new ArrayList<String>();
    private static int atPoolPointer = 0;
    public static List<String> pages = new ArrayList<String>();

    /* data collector specific parameters */
    public static int numOfScrapes;
    public static String jsonDir;
    public static String since;
    public static String until;
    public static boolean collectComments;
    public static boolean collectCommentReplies;
    public static boolean collectLikes;
    public static boolean scrapeHistory;

    /* database connection parameters */
    public static String dbUrl;
    public static String dbUser;
    public static String dbPass;

    /* only used by stats collector */
    public static int statsDepth;
    public static int statsInterval;
    public static boolean statsHistory;

    /* these dirs will be created if does not exist */
    public static String downloadDir;
    public static String archiveDir;

    /* optional */
    public static int delay = 1;

    /* used by pseudo tagger */
    public static String tagTable;
    public static List<Integer> excludeCodes = new ArrayList<Integer>();

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
            if(null == properties.getProperty("accessTokenPool") || properties.getProperty("accessTokenPool").isEmpty())
            {
                if(null != accessToken && !accessToken.isEmpty())
                {
                    accessTokenPool.add(accessToken);
                }
            }
            else
            {
                accessTokenPool = Arrays.asList(properties.getProperty("accessTokenPool").split("\\s*,\\s*"));
            }
            pages = Arrays.asList(properties.getProperty("pages").split("\\s*,\\s*"));

            if(null != properties.getProperty("numOfScrapes") && properties.getProperty("numOfScrapes").matches("\\d+"))
            {
                numOfScrapes =  Integer.parseInt(properties.getProperty("numOfScrapes"));
            }
            else
            {
                numOfScrapes = 0;
            }
            jsonDir = properties.getProperty("jsonDir");
            since = properties.getProperty("since");
            until = properties.getProperty("until");
            if(null == Config.until || Config.until.isEmpty() || !Config.until.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
            {
                Config.until = Util.getDateTimeUtc(System.currentTimeMillis());
            }
            collectComments = properties.getProperty("collectComments").toLowerCase().equals("true");
            collectCommentReplies = properties.getProperty("collectCommentReplies").toLowerCase().equals("true");
            collectLikes = properties.getProperty("collectLikes").toLowerCase().equals("true");
            scrapeHistory = properties.getProperty("scrapeHistory").toLowerCase().equals("true");

            dbUrl = properties.getProperty("dbUrl");
            dbUser = properties.getProperty("dbUser");
            dbPass = properties.getProperty("dbPass");

            if(null != properties.getProperty("statsDepth") && properties.getProperty("statsDepth").matches("\\d+"))
            {
                statsDepth =  Integer.parseInt(properties.getProperty("statsDepth"));
            }
            else
            {
                statsDepth = 2;
            }
            if(null != properties.getProperty("statsInterval") && properties.getProperty("statsInterval").matches("\\d+"))
            {
                statsInterval =  Integer.parseInt(properties.getProperty("statsInterval"));
            }
            else
            {
                statsInterval = 5;
            }
            if(null != properties.getProperty("statsHistory") && !properties.getProperty("statsHistory").isEmpty())
            {
                statsHistory = properties.getProperty("statsHistory").toLowerCase().equals("true");
            }

            if(null != properties.getProperty("delay") && properties.getProperty("delay").matches("\\d+"))
            {
                delay = Integer.parseInt(properties.getProperty("delay"));
                delay = delay < 1 ? 1 : delay;
            }
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

        Util.buildPath("download");
        downloadDir = jsonDir + "/download";
        Util.buildPath("archive");
        archiveDir = jsonDir + "/archive";
    }

    public static boolean isConfigValid()
    {
        if(accessTokenPool.size() == 0)
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

    public static void initPseudoTaggingConfig()
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

            dbUrl = properties.getProperty("dbUrl");
            dbUser = properties.getProperty("dbUser");
            dbPass = properties.getProperty("dbPass");

            tagTable = properties.getProperty("tagTable");
            for(String s: properties.getProperty("excludeCodes").split("\\s*,\\s*"))
            {
                if(s.matches("\\d+"))
                {
                    excludeCodes.add(Integer.parseInt(s));
                }
            }
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
    }

    public static String getAccessToken()
    {
        // make sure access token pool pointer never goes out of bound
        if(atPoolPointer > 0)
        {
            atPoolPointer = atPoolPointer % accessTokenPool.size();
        }
        String accessToken = accessTokenPool.get(atPoolPointer);
        atPoolPointer = ++atPoolPointer % accessTokenPool.size();
        return accessToken;
    }
}
