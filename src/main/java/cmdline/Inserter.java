package cmdline;

import common.*;
import common.comment.CommentsInserter;
import common.like.LikesInserter;
import common.page.PageInserter;
import common.post.PostInserter;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Inserter
{
    public static void main(String[] args)
    {
        Config.init();

        if(!Config.isDbConfigValid())
        {
            System.err.println("Exiting. Check Configuration.");
            System.exit(0);
        }

        while (true)
        {
            File[] dateDirs = new File(Config.downloadDir).listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    boolean accept = file.isDirectory();
                    if(accept) accept = file.getName().matches("\\d{4}-\\d{2}-\\d{2}");
                    if(accept) accept = !file.getName().equals(Util.getCurDateDirUtc());
                    return accept;
                }
            });

            Arrays.sort(dateDirs);

            for(File dateDir: dateDirs)
            {
                System.out.println(Util.getDbDateTimeEst() + " started inserting json files from " + dateDir.getName());

                for(File file: getFilesEndsWith(dateDir, "page.json"))
                {
                    new PageInserter(file).processPage();
                }

                for(File file: getFilesEndsWith(dateDir, "post.json"))
                {
                    new PostInserter(file).processPost();
                }

                for(File file: getFilesEndsWith(dateDir, "post_comments.json"))
                {
                    new CommentsInserter(file).processComments();
                }

                for(File file: getFilesEndsWith(dateDir, "post_likes.json"))
                {
                    new LikesInserter(file).processLikes();
                }

                for(File file: getFilesEndsWith(dateDir, "comment_replies.json"))
                {
                    new CommentsInserter(file).processComments();
                }

                System.out.println(Util.getDbDateTimeEst() + " completed inserting json files from " + dateDir.getName());
            }

            Util.sleep(600);
        }
    }

    public static File[] getFilesEndsWith(File dateDir, final String endsWith)
    {
        File[] files = dateDir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(endsWith);
            }
        });
        return files;
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
