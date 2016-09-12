package cmdline.twitter;

import twitter4j.*;
import java.io.IOException;

public class StreamingCollector
{
    public static void main(String[] args) throws TwitterException, IOException
    {
        StatusListener listener = new StatusListener()
        {
            public void onStatus(Status status)
            {
                System.out.println(status.getCreatedAt() + " : " + status.getUser().getName() + " : " + status.getText());
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            public void onException(Exception ex)
            {
                ex.printStackTrace();
            }
            public void onScrubGeo(long var1, long var3){}
            public void onStallWarning(StallWarning var1){}
        };
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        FilterQuery fq = new FilterQuery();
        String keywords[] = {"#MAGA"};
        fq.track(keywords);
        twitterStream.addListener(listener);
        twitterStream.filter(fq);
    }
}
