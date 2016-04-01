package common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class PageInserter
{
    private File pageJsonFile;
    private String crawlDateTime;

    public PageInserter(File pageJsonFile)
    {
        this.pageJsonFile = pageJsonFile;
        crawlDateTime = pageJsonFile.getParentFile().getName() + " " + pageJsonFile.getName().substring(0,8).replaceAll("-", ":");
    }

    public void processPage()
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
}
