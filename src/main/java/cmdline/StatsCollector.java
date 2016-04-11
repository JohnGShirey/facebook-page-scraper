package cmdline;

import common.*;

public class StatsCollector
{
    public static void main(String[] args)
    {
        Config.init();

        int depthDays = 1;

        while(Config.isDbConfigValid())
        {
            long statsSlice = depthDays * Config.dayInMillis;

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
                pageCollector.collectStats();

                PostsCollector postsCollector = new PostsCollector(new Page(page), tempSince, tempUntil);
                postsCollector.collectStats();

                postsCount += postsCollector.getPosts().size();
            }

            System.out.println(Util.getDbDateTimeEst() + " fetched " + postsCount + " posts");

            Util.sleep(Config.statsInterval * 60);

            if(++depthDays > Config.statsDepth)
            {
                depthDays = 1;
            }

            Config.init();
        }
    }
}
