package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TwitterConfig
{
    public static String baseDir;
    public static String[] hashtags;
    public static String dbUrl;
    public static String dbUser;
    public static String dbPass;
    public static boolean shutdown = false;

    public static String downloadDir;
    public static String archiveDir;

    public static void init()
    {
        Properties properties = new Properties();

        InputStream inputStream = null;

        try
        {
            if (new File("twitter_config.properties").exists())
            {
                inputStream = new FileInputStream("twitter_config.properties");
            }
            if (null == inputStream)
            {
                inputStream = new FileInputStream(System.getProperty("user.home") + "/twitter/twitter_config.properties");
            }
            if (null == inputStream)
            {
                System.err.println("Could not find twitter_config.properties. Searched in base directory and " + System.getProperty("user.home") + "/twitter");
                System.exit(0);
            }

            properties.load(inputStream);
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

        if(null != properties.getProperty("hashtags") && !properties.getProperty("hashtags").isEmpty())
        {
            hashtags = properties.getProperty("hashtags").split("\\s*,\\s*");
        }

        System.out.println("hashtags=" + properties.getProperty("hashtags"));

        dbUrl = properties.getProperty("dbUrl");
        dbUser = properties.getProperty("dbUser");
        dbPass = properties.getProperty("dbPass");

        shutdown = properties.getProperty("shutdown", "false").toLowerCase().equals("true");

        baseDir = properties.getProperty("baseDir", System.getProperty("user.home") + "/twitter");
        if(!(new File(baseDir).exists() && new File(baseDir).isDirectory()))
        {
            System.err.println("baseDir does not exist");
        }

        System.out.println("baseDir=" + baseDir);

        downloadDir = baseDir + "/download";
        archiveDir = baseDir + "/archive";
    }
}
