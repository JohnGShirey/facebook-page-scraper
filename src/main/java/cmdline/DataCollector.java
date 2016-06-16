package cmdline;

import common.*;
import common.comment.Comment;
import common.comment.CommentsCollector;
import common.like.LikesCollector;
import common.page.Page;
import common.page.PageCollector;
import common.post.Post;
import common.post.PostsCollector;
import db.DbManager;

import java.util.ArrayList;
import java.util.List;

public class DataCollector
{
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

    private static void collectData()
    {
        List<Post> posts = new ArrayList<Post>();

        if(null == Config.filterPostsSql)
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

            Util.sleepMillis(Config.pages.size() * Config.delay);

            for(Page page: pages)
            {
                PostsCollector postsCollector = new PostsCollector(page.getUsername(), Config.since, Config.until);

                postsCollector.collect();

                posts.addAll(postsCollector.getPosts());

                System.out.println(Util.getDbDateTimeEst() + " fetched " + postsCollector.getPosts().size() + " posts from page " + page.getUsername());

                Util.sleepMillis(postsCollector.getPosts().size() * Config.delay);
            }

            System.out.println(Util.getDbDateTimeEst() + " fetched total " + posts.size() + " posts from " + Config.pages.size() + " pages");
        }
        else
        {
            posts = getFilteredPosts();

            System.out.println(Util.getDbDateTimeEst() + " total posts from sql " + posts.size());
        }

        if(Config.collectComments)
        {
            System.out.println(Util.getDbDateTimeEst() + " started fetching comments for " + posts.size() + " posts");

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

                Util.sleepMillis(comments.size() * Config.delay);

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

                Util.sleepMillis(repliesCollector.getComments().size() * Config.delay);
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

                Util.sleepMillis(likesCollector.likes.size() * Config.delay);
            }
        }
    }

    private static List<Post> getFilteredPosts()
    {
        List<Post> filteredPosts = new ArrayList<Post>();

        List<String> availablePosts = DbManager.getStringValues(Config.filterPostsSql);

        for(String postId: DbManager.getStringValues(Config.filterPostsSql))
        {
            filteredPosts.add(new Post(postId));
        }

        return filteredPosts;
    }
}
