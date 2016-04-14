package common.post;


import common.Util;
import common.page.Page;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostInserter
{
    private static Pattern postJsonPattern = Pattern.compile("([\\d]{2}-[\\d]{2}-[\\d]{2})_post_([\\d]+)_([\\d]+).json");

    private File postJsonFile;
    private String crawlDateTime;
    private String pageId;

    public PostInserter(File postJsonFile)
    {
        this.postJsonFile = postJsonFile;
        crawlDateTime = postJsonFile.getParentFile().getName() + " " + postJsonFile.getName().substring(0,8).replaceAll("-", ":");
        Matcher matcher = postJsonPattern.matcher(postJsonFile.getName());
        if(matcher.matches())
        {
            pageId = matcher.group(2);
        }
    }

    public void processPost()
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

        Page page = Page.getPage(pageId);
        Post post = new Post(Page.getUsername(pageId), postJson, crawlDateTime);
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
