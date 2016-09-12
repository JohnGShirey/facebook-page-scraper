package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TwitterConfig
{
    private static void init()
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
                System.err.println("Could not find config.properties. Searched in base directory and " + System.getProperty("user.home") + "/fb-page-scraper");
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
    }
}
