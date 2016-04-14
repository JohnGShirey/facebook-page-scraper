package cmdline;

import common.*;
import common.comment.CommentsInserter;
import common.like.LikesInserter;
import common.page.PageInserter;
import common.post.PostInserter;

import java.io.*;

public class Inserter
{
    public static void main(String[] args)
    {
        Config.init();

        if(!Config.isDbConfigValid())
        {
            System.exit(0);
        }

        while (true)
        {
            File[] dateDirs = new File(Config.downloadDir).listFiles(new FileFilter()
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
                    if(file.getName().endsWith("page.json"))
                    {
                        new PageInserter(file).processPage();
                    }
                    else if(file.getName().endsWith("post.json"))
                    {
                        new PostInserter(file).processPost();
                    }
                    else if(file.getName().endsWith("post_comments.json"))
                    {
                        new CommentsInserter(file).processComments();
                    }
                    else if(file.getName().endsWith("comment_replies.json"))
                    {
                        new CommentsInserter(file).processComments();
                    }
                    else if(file.getName().endsWith("post_likes.json"))
                    {
                        new LikesInserter(file).processLikes();
                    }
                    Util.sleepMillis(100);
                }
            }
            Util.sleep(300);
        }
    }

    private class CleanArchive extends Thread
    {
        public void run()
        {
            while (true)
            {
                File[] candidateDirs = new File(Config.archiveDir).listFiles(new FileFilter()
                {
                    @Override
                    public boolean accept(File file)
                    {
                        return file.isDirectory();
                    }
                });
                for(File candiDir: candidateDirs)
                {
                    File[] dateDirs = candiDir.listFiles(new FileFilter()
                    {
                        @Override
                        public boolean accept(File file)
                        {
                            return file.isDirectory();
                        }
                    });
                    for(File dateDir: dateDirs)
                    {
                        File[] pageFiles = dateDir.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
                                return name.endsWith(".json") && name.contains("_page_");
                            }
                        });

                        File[] postFiles = dateDir.listFiles(new FilenameFilter()
                        {
                            @Override
                            public boolean accept(File dir, String name)
                            {
                                return name.endsWith(".json") && name.contains("_post_");
                            }
                        });
                    }
                }
                Util.sleep(300);
            }
        }
    }
}
