package cmdline;

import common.*;

import java.util.ArrayList;
import java.util.List;

public class FbCollector
{
    public static long configSince, configUntil, sincePointer, untilPointer;

    public static final int dayInMillis = 86400000;

    public static final int hourInMillis = 3600000;

    public static final int minuteInMillis = 60000;

    public static int scrapeCount = 0;

    public static void initTimePointers()
    {
        if(null == Config.until || Config.until.isEmpty() || !Config.until.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))
        {
            configUntil = System.currentTimeMillis();
        }
        else
        {
            configUntil = Util.toMillis(Config.until);
        }

        configSince = Util.toMillis(Config.since);

        untilPointer = configUntil;
    }

    public static void main(String[] args) throws Exception
    {
        initTimePointers();

        System.out.println(Util.getDbDateTimeEst() + " started fetching data");

        if(Config.collectStats)
        {
            new StatsCollector().start();
        }
        else
        {
            for(String page: Config.pages)
            {
                PageCollector pageCollector = new PageCollector(page);
                pageCollector.collect();
            }
        }

        boolean fetch = true;

        while (fetch)
        {
            collectHistoricData();

            if(sincePointer == configSince)
            {
                scrapeCount++;

                System.out.println(Util.getDbDateTimeEst() + " scraped " + scrapeCount + " time(s)");

                Util.sleepMillis(StatsCollector.statsSlice);

                Config.init();

                initTimePointers();

                for(String page: Config.pages)
                {
                    PageCollector pageCollector = new PageCollector(page);
                    pageCollector.collect();
                }

                fetch = !Config.collectOnce;
            }
        }
    }

    public static void collectHistoricData()
    {
        sincePointer = untilPointer - dayInMillis;

        if(sincePointer < configSince)
        {
            sincePointer = configSince;
        }

        String tempSince = Util.getDateTimeUtc(sincePointer);

        String tempUntil = Util.getDateTimeUtc(untilPointer);

        System.out.println(Util.getDbDateTimeEst() + " fetching historic data from " + tempSince + " to " + tempUntil);

        List<String> posts = new ArrayList<String>();

        for(String page: Config.pages)
        {
            PostsCollector postsCollector = new PostsCollector(new Page(page), tempSince, tempUntil);
            postsCollector.collect();

            posts.addAll(postsCollector.postIds);
        }

        System.out.println(Util.getDbDateTimeEst() + " fetched " + posts.size() + " posts");

        new CommentsCollector(posts).start();

        Util.sleep(10 * posts.size());

        untilPointer = sincePointer;

        if(untilPointer == configSince)
        {
            untilPointer = configUntil;
        }
    }
}
