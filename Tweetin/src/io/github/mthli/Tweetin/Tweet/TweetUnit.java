package io.github.mthli.Tweetin.Tweet;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import com.twitter.Extractor;
import io.github.mthli.Tweetin.Data.DataRecord;
import io.github.mthli.Tweetin.R;
import io.github.mthli.Tweetin.Twitter.TwitterUnit;
import twitter4j.*;

import java.util.List;

public class TweetUnit {
    private Context context;

    private String useScreenName;
    private String me;

    public TweetUnit(Context context) {
        this.context = context;

        this.useScreenName = TwitterUnit.getUseScreenNameFromSharedPreferences(context);
        this.me = context.getString(R.string.tweet_info_retweeted_by_me);
    }

    public String getPictureURLFromStatus(Status status) {
        URLEntity[] urlEntities;
        MediaEntity[] mediaEntities;

        if (status.isRetweet()) {
            urlEntities = status.getRetweetedStatus().getURLEntities();
            mediaEntities = status.getRetweetedStatus().getMediaEntities();
        } else {
            urlEntities = status.getURLEntities();
            mediaEntities = status.getMediaEntities();
        }

        /* Support for *.png and *.jpg */
        for (MediaEntity mediaEntity : mediaEntities) {
            if (mediaEntity.getType().equals(context.getString(R.string.picture_media_type))) {
                return mediaEntity.getMediaURL();
            }
        }

        /* Support for Instagram */
        for (URLEntity urlEntity : urlEntities) {
            String expandedURL = urlEntity.getExpandedURL();
            if (expandedURL.startsWith(context.getString(R.string.picture_instagram_prefix))) {
                return expandedURL + context.getString(R.string.picture_instagram_suffix);
            }
        }

        // TODO: Support more, such us *.gif

        return null;
    }

    public String getDetailTextFromStatus(Status status) {
        URLEntity[] urlEntities;
        MediaEntity[] mediaEntities;

        String text;

        if (status.isRetweet()) {
            urlEntities = status.getRetweetedStatus().getURLEntities();
            mediaEntities = status.getRetweetedStatus().getMediaEntities();
            text = status.getRetweetedStatus().getText();
        } else {
            urlEntities = status.getURLEntities();
            mediaEntities = status.getMediaEntities();
            text = status.getText();
        }

        for (URLEntity urlEntity : urlEntities) {
            text = text.replace(
                    urlEntity.getURL(),
                    urlEntity.getExpandedURL()
            );
        }

        for (MediaEntity mediaEntity : mediaEntities) {
            text = text.replace(
                    mediaEntity.getURL(),
                    mediaEntity.getMediaURL()
            );
        }

        return text;
    }

    public SpannableString getSpanFromText(String text) {
        Extractor extractor = new Extractor();
        List<String> urlList = extractor.extractURLs(text);
        List<String> userList = extractor.extractMentionedScreennames(text);
        List<String> tagList = extractor.extractHashtags(text);

        SpannableString span = new SpannableString(text);

        for (String url : urlList) {
            span.setSpan(
                    new TweetURLSpan(context, url),
                    text.indexOf(url),
                    text.indexOf(url) + url.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        for (String user : userList) {
            span.setSpan(
                    new TweetUserSpan(context, user),
                    text.indexOf(user),
                    text.indexOf(user) + user.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        for (String tag : tagList) {
            span.setSpan(
                    new TweetTagSpan(context, tag),
                    text.indexOf(tag),
                    text.indexOf(tag) + tag.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return span;
    }

    public SpannableString getSpanFromTweet(Tweet tweet) {
        return getSpanFromText(tweet.getText());
    }

    public Tweet getTweetFromStatus(Status status) {
        Tweet tweet = new Tweet();

        if (status.isRetweet()) {
            tweet.setAvatarURL(status.getRetweetedStatus().getUser().getOriginalProfileImageURL());
            tweet.setName(status.getRetweetedStatus().getUser().getName());
            tweet.setScreenName(status.getRetweetedStatus().getUser().getScreenName());
            tweet.setCreatedAt(status.getRetweetedStatus().getCreatedAt().getTime());
            Place place = status.getRetweetedStatus().getPlace();
            if (place != null) {
                tweet.setCheckIn(place.getFullName());
            } else {
                tweet.setCheckIn(null);
            }
            tweet.setProtect(status.getRetweetedStatus().getUser().isProtected());
            tweet.setPictureURL(getPictureURLFromStatus(status.getRetweetedStatus()));
            tweet.setText(getDetailTextFromStatus(status.getRetweetedStatus()));
            tweet.setRetweetedByName(status.getUser().getName());
            tweet.setFavorite(status.getRetweetedStatus().isFavorited());

            tweet.setStatusId(status.getRetweetedStatus().getId());
            tweet.setInReplyToStatusId(status.getRetweetedStatus().getInReplyToStatusId());
            tweet.setRetweetedByScreenName(status.getUser().getScreenName());
        } else {
            tweet.setAvatarURL(status.getUser().getOriginalProfileImageURL());
            tweet.setName(status.getUser().getName());
            tweet.setScreenName(status.getUser().getScreenName());
            tweet.setCreatedAt(status.getCreatedAt().getTime());
            Place place = status.getPlace();
            if (place != null) {
                tweet.setCheckIn(place.getFullName());
            } else {
                tweet.setCheckIn(null);
            }
            tweet.setProtect(status.getUser().isProtected());
            tweet.setPictureURL(getPictureURLFromStatus(status));
            tweet.setText(getDetailTextFromStatus(status));
            tweet.setRetweetedByName(null);
            tweet.setFavorite(status.isFavorited());

            tweet.setStatusId(status.getId());
            tweet.setInReplyToStatusId(status.getInReplyToStatusId());
            tweet.setRetweetedByScreenName(null);
        }

        if (status.isRetweetedByMe() || status.isRetweeted()) {
            tweet.setRetweetedByName(me);
            tweet.setRetweetedByScreenName(useScreenName);
        }

        tweet.setDetail(false);

        return tweet;
    }

    public Tweet getTweetFromDataRecord(DataRecord record) {
        Tweet tweet = new Tweet();

        tweet.setAvatarURL(record.getAvatarURL());
        tweet.setName(record.getName());
        tweet.setScreenName(record.getScreenName());
        tweet.setCreatedAt(record.getCreatedAt());
        tweet.setCheckIn(record.getCheckIn());
        tweet.setProtect(record.isProtect());
        tweet.setPictureURL(record.getPictureURL());
        tweet.setText(record.getText());
        tweet.setRetweetedByName(record.getRetweetedByName());
        tweet.setFavorite(record.isFavorite());

        tweet.setStatusId(record.getStatusId());
        tweet.setInReplyToStatusId(record.getInReplyToStatusId());
        tweet.setRetweetedByScreenName(record.getRetweetedByScreenName());

        tweet.setDetail(false);

        return tweet;
    }

    public DataRecord getDataRecordFromTweet(Tweet tweet) {
        DataRecord record = new DataRecord();

        record.setAvatarURL(tweet.getAvatarURL());
        record.setName(tweet.getName());
        record.setScreenName(tweet.getScreenName());
        record.setCreatedAt(tweet.getCreatedAt());
        record.setCheckIn(tweet.getCheckIn());
        record.setProtect(tweet.isProtect());
        record.setPictureURL(tweet.getPictureURL());
        record.setText(tweet.getText());
        record.setRetweetedByName(tweet.getRetweetedByName());
        record.setFavorite(tweet.isFavorite());

        record.setStatusId(tweet.getStatusId());
        record.setInReplyToStatusId(tweet.getInReplyToStatusId());
        record.setRetweetedByScreenName(tweet.getRetweetedByScreenName());

        return record;
    }

    public DataRecord getDataRecordFromStatus(Status status) {
        DataRecord record = new DataRecord();

        if (status.isRetweet()) {
            record.setAvatarURL(status.getRetweetedStatus().getUser().getOriginalProfileImageURL());
            record.setName(status.getRetweetedStatus().getUser().getName());
            record.setScreenName(status.getRetweetedStatus().getUser().getScreenName());
            record.setCreatedAt(status.getRetweetedStatus().getCreatedAt().getTime());
            Place place = status.getRetweetedStatus().getPlace();
            if (place != null) {
                record.setCheckIn(place.getFullName());
            } else {
                record.setCheckIn(null);
            }
            record.setProtect(status.getRetweetedStatus().getUser().isProtected());
            record.setPictureURL(getPictureURLFromStatus(status.getRetweetedStatus()));
            record.setText(getDetailTextFromStatus(status.getRetweetedStatus()));
            record.setRetweetedByName(status.getUser().getName());
            record.setFavorite(status.getRetweetedStatus().isFavorited());

            record.setStatusId(status.getRetweetedStatus().getId());
            record.setInReplyToStatusId(status.getRetweetedStatus().getInReplyToStatusId());
            record.setRetweetedByScreenName(status.getUser().getScreenName());
        } else {
            record.setAvatarURL(status.getUser().getOriginalProfileImageURL());
            record.setName(status.getUser().getName());
            record.setScreenName(status.getUser().getScreenName());
            record.setCreatedAt(status.getCreatedAt().getTime());
            Place place = status.getPlace();
            if (place != null) {
                record.setCheckIn(place.getFullName());
            } else {
                record.setCheckIn(null);
            }
            record.setProtect(status.getUser().isProtected());
            record.setPictureURL(getPictureURLFromStatus(status));
            record.setText(getDetailTextFromStatus(status));
            record.setRetweetedByName(null);
            record.setFavorite(status.isFavorited());

            record.setStatusId(status.getId());
            record.setInReplyToStatusId(status.getInReplyToStatusId());
            record.setRetweetedByScreenName(null);
        }

        if (status.isRetweetedByMe() || status.isRetweeted()) {
            record.setRetweetedByName(me);
            record.setRetweetedByScreenName(useScreenName);
        }

        return record;
    }
}
