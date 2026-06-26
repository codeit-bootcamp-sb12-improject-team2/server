DROP TABLE IF EXISTS comment_likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS article_views CASCADE;
DROP TABLE IF EXISTS article_interests CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS interest_keyword CASCADE;
DROP TABLE IF EXISTS articles CASCADE;
DROP TABLE IF EXISTS interests CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       nickname VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP WITH TIME ZONE,
                       is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE interests (
                           id UUID PRIMARY KEY,
                           name VARCHAR(30) NOT NULL UNIQUE,
                           subscriber_count INTEGER NOT NULL DEFAULT 0,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP WITH TIME ZONE,

                           CONSTRAINT chk_interests_subscriber_count
                               CHECK (subscriber_count >= 0)
);

CREATE TABLE interest_keyword (
                                  id UUID PRIMARY KEY,
                                  interest_id UUID NOT NULL,
                                  keyword VARCHAR(30) NOT NULL,

                                  CONSTRAINT fk_interest_keyword_interest
                                      FOREIGN KEY (interest_id)
                                          REFERENCES interests(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT uq_interest_keyword_interest_keyword
                                      UNIQUE (interest_id, keyword)
);

CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               interest_id UUID NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                               CONSTRAINT fk_subscriptions_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_subscriptions_interest
                                   FOREIGN KEY (interest_id)
                                       REFERENCES interests(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uq_subscriptions_user_interest
                                   UNIQUE (user_id, interest_id)
);

CREATE TABLE articles (
                          id UUID PRIMARY KEY,
                          source VARCHAR(30) NOT NULL,
                          source_url VARCHAR(255) NOT NULL UNIQUE,
                          title VARCHAR(255) NOT NULL,
                          publish_date TIMESTAMP WITH TIME ZONE NOT NULL,
                          summary TEXT NOT NULL,
                          view_count INTEGER NOT NULL DEFAULT 0,
                          comment_count INTEGER NOT NULL DEFAULT 0,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP WITH TIME ZONE,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

                          CONSTRAINT chk_articles_view_count
                              CHECK (view_count >= 0),

                          CONSTRAINT chk_articles_comment_count
                              CHECK (comment_count >= 0)
);

CREATE TABLE article_interests (
                                   id UUID PRIMARY KEY,
                                   article_id UUID NOT NULL,
                                   interest_id UUID NOT NULL,

                                   CONSTRAINT fk_article_interests_article
                                       FOREIGN KEY (article_id)
                                           REFERENCES articles(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_article_interests_interest
                                       FOREIGN KEY (interest_id)
                                           REFERENCES interests(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT uq_article_interests_article_interest
                                       UNIQUE (article_id, interest_id)
);

CREATE TABLE article_views (
                               id UUID PRIMARY KEY,
                               article_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

                               CONSTRAINT fk_article_views_article
                                   FOREIGN KEY (article_id)
                                       REFERENCES articles(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_article_views_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);

CREATE TABLE comments (
                          id UUID PRIMARY KEY,
                          article_id UUID NOT NULL,
                          user_id UUID NOT NULL,
                          content VARCHAR(255) NOT NULL,
                          like_count BIGINT NOT NULL DEFAULT 0,
                          is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
                          updated_at TIMESTAMP(6) WITH TIME ZONE,

                          CONSTRAINT fk_comments_article
                              FOREIGN KEY (article_id)
                                  REFERENCES articles(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_comments_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_comments_like_count
                              CHECK (like_count >= 0)
);

CREATE TABLE comment_likes (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               comment_id UUID NOT NULL,
                               created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),

                               CONSTRAINT fk_comment_likes_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_comment_likes_comment
                                   FOREIGN KEY (comment_id)
                                       REFERENCES comments(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uq_comment_likes_user_comment
                                   UNIQUE (user_id, comment_id)
);

CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               content VARCHAR(255) NOT NULL,
                               confirmed BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now(),
                               updated_at TIMESTAMP(6) WITH TIME ZONE,
                               resource_type VARCHAR(50) NOT NULL,
                               resource_id UUID NOT NULL,

                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);