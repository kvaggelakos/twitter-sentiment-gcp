package org.twitter.sentiment.models;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.values.PInput;
import org.apache.beam.sdk.values.POutput;
import org.apache.commons.lang3.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@DefaultCoder(AvroCoder.class)
public class Tweet implements Serializable {

  /**
   * Mapping to schema.json
   */
  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="EEE MMM dd HH:mm:ss ZZZZZ yyyy")
  public Date created_at;

  public BigInteger id;
  public String id_str;
  public String text;
  public int quote_count;
  public int reply_count;
  public int retweet_count;
  public int favorite_count;
  public String lang;

  @Nullable
  public Sentiment sentiment;

  public ExtendedTweet extended_tweet;
  public User user;

  public TableRow toTableRow() {
    TableRow res = new TableRow();

    res.set("created_at", this.created_at.getTime() / 1000);
    res.set("id", this.id);
    res.set("id_str", this.id_str);
    res.set("text", this.text);
    res.set("quote_count", this.quote_count);
    res.set("reply_count", this.reply_count);
    res.set("retweet_count", this.retweet_count);
    res.set("favorite_count", this.favorite_count);
    res.set("lang", this.lang);

    res.set("sentiment", this.sentiment.toTableRow());
    res.set("extended_tweet", this.extended_tweet.toTableRow());
    res.set("user", this.user.toTableRow());

    return res;
  }


  public Tweet getClone() throws Exception {
    return (Tweet) SerializationUtils.clone(this);
  }

}
