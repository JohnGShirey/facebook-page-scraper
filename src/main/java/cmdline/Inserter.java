package cmdline;

import common.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inserter
{
    public static void main(String[] args)
    {
        if(!Config.isDbConfigValid())
        {
            System.exit(0);
        }

        while (true)
        {
            File[] dateDirs = new File(Config.insertQueueDir).listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isDirectory();
                }
            });
            for(File dateDir: dateDirs)
            {
                File[] files = dateDir.listFiles(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".json");
                    }
                });

                for(File file: files)
                {
                    if(file.getName().contains("_page_"))
                    {
                        PageInserter pageInserter = new PageInserter(file);
                        pageInserter.processPage();
                    }
                    else if(file.getName().contains("_post_"))
                    {
                        PostInserter postInserter = new PostInserter(file);
                        postInserter.processPost();
                    }
                    else if(file.getName().contains("_comments_"))
                    {
                        CommentsInserter commentsInserter = new CommentsInserter(file);
                        commentsInserter.processComments();
                    }
                    Util.sleepMillis(100);
                }
            }
            Util.sleep(300);
        }
    }
}
