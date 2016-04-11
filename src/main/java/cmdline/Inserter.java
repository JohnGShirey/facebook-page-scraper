package cmdline;

import common.*;

import java.io.*;

public class Inserter
{
    public static void main(String[] args)
    {
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
                    if(file.getName().contains("_page_"))
                    {
                        PageInserter pageInserter = new PageInserter(file);
                        pageInserter.processPage();
                    }
                    else if(file.getName().contains("_post_"))
                    {
                        PostInserter postInserter = new PostInserter(file);
                        postInserter.processPost();
                    }
                    else if(file.getName().contains("_comments_"))
                    {
                        CommentsInserter commentsInserter = new CommentsInserter(file);
                        commentsInserter.processComments();
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
