package org.twitter.sentiment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DefaultCoder(AvroCoder.class)
public class Sentiment implements Serializable {

  /**
   * Mapping to schema.json
   */
  public float score;
  public float magnitude;


  public TableRow toTableRow() {
    TableRow res = new TableRow();

    res.set("score", this.score);
    res.set("magnitude", this.magnitude);

    return res;
  }

}
