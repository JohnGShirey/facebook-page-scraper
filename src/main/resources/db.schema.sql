CREATE TABLE IF NOT EXISTS `Page` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `likes` int(11) DEFAULT '0',
  `talking_about` int(11) DEFAULT '0',
  `checkins` int(11) DEFAULT '0',
  `website` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `affiliation` varchar(255) DEFAULT NULL,
  `about` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `page_UNIQUE` (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Post` (
  `id` varchar(255) NOT NULL,
  `page_id` varchar(255) NOT NULL,
  `message` text,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `likes` int(11) DEFAULT '0',
  `comments` int(11) DEFAULT '0',
  `shares` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_page_id_idx` (`page_id`),
  CONSTRAINT `fk_page_id` FOREIGN KEY (`page_id`) REFERENCES `Page` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Comment` (
  `id` varchar(255) NOT NULL,
  `message` text,
  `created_at` datetime NOT NULL,
  `likes` int(11) DEFAULT '0',
  `from_id` varchar(255) NOT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `post_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_post_id_idx` (`post_id`),
  CONSTRAINT `fk_comment_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Like` (
  `from_id` varchar(255) NOT NULL,
  `from_name` varchar(255) NOT NULL,
  `post_id` varchar(255) NOT NULL,
  KEY `fk_post_id_idx` (`post_id`),
  CONSTRAINT `fk_like_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `PageCrawl` (
  `crawl_date` datetime NOT NULL,
  `page_id` varchar(255) NOT NULL,
  `likes` int(11) DEFAULT '0',
  `talking_about` int(11) DEFAULT '0',
  `checkins` int(11) DEFAULT '0',
  PRIMARY KEY (`crawl_date`,`page_id`),
  KEY `fk_page_crawl_page_id_idx` (`page_id`),
  CONSTRAINT `fk_page_crawl_page_id` FOREIGN KEY (`page_id`) REFERENCES `Page` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `PostCrawl` (
  `crawl_date` datetime NOT NULL,
  `post_id` varchar(255) NOT NULL,
  `likes` int(11) DEFAULT '0',
  `comments` int(11) DEFAULT '0',
  `shares` int(11) DEFAULT '0',
  PRIMARY KEY (`crawl_date`,`post_id`),
  KEY `fk_post_crawl_post_id_idx` (`post_id`),
  CONSTRAINT `fk_post_crawl_post_id` FOREIGN KEY (`post_id`) REFERENCES `Post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `PageCrawl`
DROP PRIMARY KEY;

ALTER TABLE `PostCrawl`
DROP PRIMARY KEY;

ALTER TABLE `Comment`
ADD COLUMN `replies` INT NULL DEFAULT 0 AFTER `post_id`;