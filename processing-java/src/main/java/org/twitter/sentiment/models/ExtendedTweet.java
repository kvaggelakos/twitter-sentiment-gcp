package org.twitter.sentiment.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@DefaultCoder(AvroCoder.class)
public class ExtendedTweet implements Serializable {

  public String full_text;

  public TableRow toTableRow() {
    TableRow res = new TableRow();

    res.set("full_text", this.full_text);

    return res;
  }

}
