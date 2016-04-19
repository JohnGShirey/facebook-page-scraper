package common.post;


import common.Config;
import common.Util;
import common.page.Page;
import db.DbManager;
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
    private static Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})_(\\d+)_(\\d+)_post.json");
    private File postJsonFile;
    private String crawlDate;
    private String crawlTime;
    private String dbCrawlDateTime;
    private String pageId;
    private String username;

    public PostInserter(File postJsonFile)
    {
        this.postJsonFile = postJsonFile;
        Matcher matcher = pattern.matcher(postJsonFile.getName());
        if(matcher.matches())
        {
            crawlDate = matcher.group(1);
            crawlTime = matcher.group(2);
            pageId = matcher.group(3);
            dbCrawlDateTime = crawlDate + " " + crawlTime.replaceAll("-", ":");
            username = Page.getUsername(pageId);
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

        Post post = new Post(username, postJson, dbCrawlDateTime);

        post.updateDb();

        Util.sleepMillis(100);

        boolean success = post.getLikes() == DbManager.getInt("SELECT likes FROM `Post` WHERE id='" + post.getId() + "'");

        if(success)
        {
            String dir = Util.buildPath("archive", username, "posts", post.getId());
            String path = dir + "/" + postJsonFile.getName();
            success = postJsonFile.renameTo(new File(path));
            if(!success)
            {
                System.err.println(Util.getDbDateTimeEst() + " failed to move " + postJsonFile.getAbsolutePath() + " to " + path);
                System.exit(0);
            }

            if(Config.scrapeHistory)
            {
                post.insertPostCrawl();
            }
        }
    }
}
