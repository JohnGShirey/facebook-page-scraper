package cmdline.twitter;

import common.TwitterConfig;
import common.Util;
import db.DbManager;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamingCollector
{
    private static BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(2000);
    private static BlockingQueue<Status> statusQueue = new LinkedBlockingQueue<Status>(2000);

    public static void main(String[] args) throws TwitterException, IOException
    {
        new StreamingCollector().stream();
    }

    private void stream()
    {
        TwitterConfig.init();
        StatusListener listener = new StatusListener()
        {
            public void onStatus(Status status)
            {
                try
                {
                    msgQueue.put(DataObjectFactory.getRawJSON(status));
                    statusQueue.put(status);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            public void onException(Exception e)
            {
                e.printStackTrace();
            }
            public void onScrubGeo(long var1, long var3){}
            public void onStallWarning(StallWarning var1){}
        };
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        FilterQuery fq = new FilterQuery();
        fq.track(TwitterConfig.hashtags);
        twitterStream.addListener(listener);
        TweetsProcessor processor = new TweetsProcessor(twitterStream);
        processor.start();
        twitterStream.filter(fq);
    }

    private class TweetsProcessor extends Thread
    {
        private TwitterStream stream;
        private String startDateUtc = null;
        private Map<String, Integer> map = new HashMap<String, Integer>();

        int count = 0;

        private TweetsProcessor(TwitterStream stream)
        {
            this.stream = stream;
            startDateUtc = Util.getDbDateTimeUtc();
        }

        public void run()
        {
            try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }

            while (!statusQueue.isEmpty())
            {
                if(statusQueue.size() % 100 == 0)
                {
                    System.out.println("queue size: " + statusQueue.size());
                }
                try
                {
                    process(statusQueue.take(), msgQueue.take());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(statusQueue.isEmpty())
                {
                    try { Thread.sleep(60000); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }

        private void process(Status status, String msg)
        {
            writeJson(status.getId(), msg);
            processHashtags(status);
            if(++count % 1000 == 0)
            {
                count = 0;
                TwitterConfig.init();
                if(TwitterConfig.shutdown)
                {
                    stream.shutdown();
                }
            }
        }

        private void writeJson(long id, String msg)
        {
            String dir = Util.buildPathTwitter("download", Util.getCurDateDirUtc());
            String path = dir + "/" + Util.getCurDateTimeDirUtc() + "_" + id + "_status.json";
            try
            {
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
                writer.write(msg);
                writer.close();
            }
            catch (Exception e)
            {
                System.err.println(Util.getDbDateTimeEst() + " failed to write json file " + path);
                e.printStackTrace();
            }
        }

        private void processHashtags(Status status)
        {
            String date = Util.getDbDateTimeUtc(status.getCreatedAt());
            if( ! date.substring(0, 13).equals(startDateUtc.substring(0, 13)) )
            {
                updateDb();
                map.clear();
                startDateUtc = date;
            }
            //System.out.println(status.getCreatedAt() + " : " + status.getUser().getName() + " : " + status.getText());
            for(HashtagEntity hashtagEntity: status.getHashtagEntities())
            {
                //System.out.println(hashtagEntity.getText());
                for(String kw: TwitterConfig.hashtags)
                {
                    if(kw.substring(1).toLowerCase().equals(hashtagEntity.getText().toLowerCase()))
                    {
                        Integer num = map.get(kw);
                        if(null == num)
                        {
                            num = 0;
                        }
                        map.put(kw, ++num);
                    }
                }
            }
            if( statusQueue.isEmpty() && TwitterConfig.shutdown )
            {
                updateDb();
                map.clear();
            }
        }

        private void updateDb()
        {
            System.out.println(startDateUtc);
            Connection connection = DbManager.getConnection(TwitterConfig.dbUrl, TwitterConfig.dbUser, TwitterConfig.dbPass);
            for(Map.Entry<String, Integer> entry: map.entrySet())
            {
                System.out.println(entry.getKey() + " : " + entry.getValue());
                Integer trackId = updateTrack(connection, entry.getKey());
                if(null != trackId)
                {
                    updateShare(connection, trackId, entry.getValue());
                }
            }
            try { if(null != connection) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        private Integer updateTrack(Connection connection, String keyword)
        {
            Integer trackId = DbManager.getInt(connection, "SELECT id FROM `Track` WHERE keyword='" + keyword + "'");
            if(null == trackId)
            {
                String query = "INSERT INTO `Track` (keyword) VALUES (?)";
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try
                {
                    statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, keyword);
                    statement.executeUpdate();
                    resultSet = statement.getGeneratedKeys();
                    if(resultSet.next())
                    {
                        trackId = resultSet.getInt(1);
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try { if(null != resultSet) resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
                    try { if(null != statement) statement.close(); } catch (SQLException e) { e.printStackTrace(); }
                }
            }
            return trackId;
        }

        private void updateShare(Connection connection, int trackId, int tweets)
        {
            String query = "INSERT INTO `Share` (track_id,date_hour,tweets) VALUES (?, ?, ?)";
            PreparedStatement statement = null;
            try
            {
                statement = connection.prepareStatement(query);
                statement.setInt(1, trackId);
                statement.setString(2, startDateUtc.substring(0,13) + ":00:00");
                statement.setInt(3, tweets);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try { if(null != statement) statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
