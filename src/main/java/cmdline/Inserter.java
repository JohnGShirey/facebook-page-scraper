package cmdline;

import common.*;
import common.comment.CommentsInserter;
import common.like.LikesInserter;
import common.page.PageInserter;
import common.post.PostInserter;

import java.io.*;
import java.util.Arrays;

public class Inserter
{
    public static void main(String[] args)
    {
        Config.initInserter();

        while (true)
        {
            File[] dateDirs = new File(Config.downloadDir).listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    boolean accept = file.isDirectory();
                    if(accept) accept = file.getName().matches("\\d{4}-\\d{2}-\\d{2}");
                    // uncomment this line if you don't want to insert from current date dir
                    // if(accept) accept = !file.getName().equals(Util.getCurDateDirUtc());
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
}
