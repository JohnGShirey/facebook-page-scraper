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
                        processPage(file);
                    }
                    else if(file.getName().contains("_post_"))
                    {
                        processPost(file);
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

    public static void processPage(File pageJsonFile)
    {
        JSONObject pageJson = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(pageJsonFile);
            JSONParser parser = new JSONParser();
            pageJson = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        String crawlDateTime = pageJsonFile.getParentFile().getName() + " " + pageJsonFile.getName().substring(0,8).replaceAll("-", ":");
        Page page = new Page(pageJson, crawlDateTime);
        boolean success = page.updateDb();
        if(success)
        {
            String dir = Util.buildPath("archive", page.getUsername(), pageJsonFile.getParentFile().getName());
            String path = dir + "/" + pageJsonFile.getName();
            success = pageJsonFile.renameTo(new File(path));
            if(!success)
            {
                System.out.println(Util.getDbDateTimeEst() + " failed to move " + pageJsonFile.getAbsolutePath() + " to " + path);
            }
        }
    }

    private static Pattern postJsonPattern = Pattern.compile("([\\d]{2}-[\\d]{2}-[\\d]{2})_post_([\\d]+)_([\\d]+).json");

    public static void processPost(File postJsonFile)
    {
        JSONObject postJson = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(postJsonFile);
            JSONParser parser = new JSONParser();
            postJson = (JSONObject) parser.parse(new InputStreamReader(is, Charset.forName("UTF-8")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try { if(null != is) is.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        Matcher matcher = postJsonPattern.matcher(postJsonFile.getName());
        if(matcher.matches())
        {
            String pageId = matcher.group(2);
            String crawlDateTime = postJsonFile.getParentFile().getName() + " " + postJsonFile.getName().substring(0,8).replaceAll("-", ":");
            Page page = Page.getPage(pageId);
            Post post = new Post(page, postJson, crawlDateTime);
            boolean success = post.updateDb();
            if(success)
            {
                String dir = Util.buildPath("archive", page.getUsername(), postJsonFile.getParentFile().getName());
                String path = dir + "/" + postJsonFile.getName();
                success = postJsonFile.renameTo(new File(path));
                if(!success)
                {
                    System.out.println(Util.getDbDateTimeEst() + " failed to move " + postJsonFile.getAbsolutePath() + " to " + path);
                }
            }
        }
    }
}
