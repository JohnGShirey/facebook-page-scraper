package cmdline;

import common.*;
import common.comment.Comment;
import common.comment.CommentsCollector;
import common.like.LikesCollector;
import common.page.Page;
import common.page.PageCollector;
import common.post.Post;
import common.post.PostsCollector;

import java.util.ArrayList;
import java.util.List;

public class DataCollector
{
    public static int requests = 0;

    public static void main(String[] args) throws Exception
    {
        Config.initDataCollector();

        int scrapeCount = 0;

        boolean fetch = true;

        while (fetch)
        {
            System.out.println(Util.getDbDateTimeEst() + " started fetching data from " + Config.since + " until " + Config.until);

            collectData();

            scrapeCount++;

            System.out.println(Util.getDbDateTimeEst() + " scraped " + scrapeCount + " time(s)");

            Config.initDataCollector();

            if(Config.numOfScrapes != 0 && scrapeCount >= Config.numOfScrapes)
            {
                fetch = false;
            }
        }
    }

    public static void collectData()
    {
        List<Page> pages = new ArrayList<Page>();
        for(String username: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(username);
            pageCollector.collect();
            if(null != pageCollector.getPage())
            {
                pages.add(pageCollector.getPage());
            }
        }
        delay(Config.pages.size());

        List<Post> posts = new ArrayList<Post>();
        for(Page page: pages)
        {
            PostsCollector postsCollector = new PostsCollector(page.getUsername(), Config.since, Config.until);
            postsCollector.collect();
            posts.addAll(postsCollector.getPosts());
            System.out.println(Util.getDbDateTimeEst() + " fetched " + postsCollector.getPosts().size() + " posts from page " + page.getUsername());
            delay(postsCollector.getPosts().size());
        }
        System.out.println(Util.getDbDateTimeEst() + " fetched total " + posts.size() + " posts from " + Config.pages.size() + " pages");

        if(Config.collectComments)
        {
            collectComments(posts);
        }

        if(Config.collectLikes)
        {
            collectLikes(posts);
        }
    }

    public static void collectComments(List<Post> posts)
    {
        for(Post post: posts)
        {
            if(post.getComments() > 0)
            {
                System.out.println(Util.getDbDateTimeEst() + " fetching comments for post " + post.getId() + " created_at " + post.getCreatedAt());
                CommentsCollector commentsCollector = new CommentsCollector(post.getUsername(), post.getId());
                commentsCollector.collect();
                List<Comment> comments = commentsCollector.getComments();
                System.out.println(Util.getDbDateTimeEst() + " fetched " + comments.size() + " comments");
                delay(comments.size());

                if(Config.collectCommentReplies)
                {
                    collectCommentReplies(post, comments);
                }
            }
        }
    }

    public static void collectCommentReplies(Post post, List<Comment> comments)
    {
        for(Comment comment: comments)
        {
            if(comment.getReplies() > 0)
            {
                System.out.println(Util.getDbDateTimeEst() + " fetching replies for comment " + comment.getId());
                CommentsCollector repliesCollector = new CommentsCollector(post.getUsername(), post.getId(), comment.getId());
                repliesCollector.collect();
                System.out.println(Util.getDbDateTimeEst() + " fetched " + repliesCollector.getComments().size() + " replies");
                delay(repliesCollector.getComments().size());
            }
        }
    }

    public static void collectLikes(List<Post> posts)
    {
        for(Post post: posts)
        {
            if(post.getLikes() > 0)
            {
                System.out.println(Util.getDbDateTimeEst() + " fetching likes for post " + post.getId() + " created_at " + post.getCreatedAt());
                LikesCollector likesCollector = new LikesCollector(post.getUsername(), post.getId());
                likesCollector.collect();
                System.out.println(Util.getDbDateTimeEst() + " fetched " + likesCollector.likes.size() + " likes");
                delay(likesCollector.likes.size());
            }
        }
    }

    public static void delay(int numRequests)
    {
        requests += numRequests;
        if(requests > 100)
        {
            Util.sleepMillis(requests * Config.delay);
            requests = 0;
        }
    }
}
