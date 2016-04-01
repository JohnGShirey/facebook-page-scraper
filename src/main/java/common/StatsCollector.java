package common;

import cmdline.FbCollector;
import common.*;

public class StatsCollector extends Thread
{
    public static final int statsSlice = 5 * FbCollector.dayInMillis;

    public void run()
    {
        while (Config.collectStats)
        {
            long statsStartedAt = System.currentTimeMillis();

            long statsSince = statsStartedAt - statsSlice;

            long configSince = Util.toMillis(Config.since);

            statsSince = statsSince > configSince ? statsSince : configSince;

            String tempSince = Util.getDateTimeUtc(statsSince);

            String tempUntil = Util.getDateTimeUtc(statsStartedAt);

            System.out.println(Util.getDbDateTimeEst() + " fetching stats data from " + tempSince + " to " + tempUntil);

            int postsCount = 0;

            for(String page: Config.pages)
            {
                PageCollector pageCollector = new PageCollector(page);
                pageCollector.collect();

                PostsCollector postsCollector = new PostsCollector(new Page(page), tempSince, tempUntil);
                postsCollector.collect();

                postsCount += postsCollector.postIds.size();
            }

            System.out.println(Util.getDbDateTimeEst() + " fetched " + postsCount + " posts");

            Util.sleep(1200);

            Config.init();
        }
    }
}
