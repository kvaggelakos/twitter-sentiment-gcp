package org.twitter.sentiment;

import org.apache.beam.sdk.options.PipelineOptions;

public interface TSPipelineOptions extends PipelineOptions {

  String getPubSubTopic();
  void setPubSubTopic(String topic);

  String getBqTable();
  void setBqTable(String bqTable);

}
