package org.twitter.sentiment.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@DefaultCoder(AvroCoder.class)
public class User implements Serializable {

  /**
   * Mapping to schema.json
   */
  public BigInteger id;
  public String name;
  public String screen_name;

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="EEE MMM dd HH:mm:ss ZZZZZ yyyy")
  public Date created_at;

  public boolean verified;
  public int followers_count;
  public int friends_count;
  public int favourites_count;
  public int statuses_count;
  public String lang;



  public TableRow toTableRow() {
    TableRow res = new TableRow();

    res.set("id", this.id);
    res.set("name", this.name);
    res.set("screen_name", this.screen_name);
    res.set("created_at", this.created_at.getTime() / 1000);
    res.set("verified", this.verified);
    res.set("followers_count", this.followers_count);
    res.set("friends_count", this.friends_count);
    res.set("favourites_count", this.favourites_count);
    res.set("statuses_count", this.statuses_count);
    res.set("lang", this.lang);

    return res;
  }

}
