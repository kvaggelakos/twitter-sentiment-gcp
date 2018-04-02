// Configure the Google Cloud provider
provider "google" {
  region      = "us-central1"
  project     = "${var.project-name}"
  credentials = "${file("${path.module}/../common/credentials.json")}"
}

# Pub/sub
resource "google_pubsub_topic" "tweet-topic" {
  name = "tweets"
}

# Bigquery dataset and table
resource "google_bigquery_dataset" "twitter" {
  dataset_id    = "twitter"
  friendly_name = "twitter"
  description   = "Tweets with their sentiment"
  location      = "US"
}

resource "google_bigquery_table" "default" {
  dataset_id = "${google_bigquery_dataset.twitter.dataset_id}"
  table_id   = "tweets"
  schema     = "${file("${path.module}/../common/schema.json")}"
}

# Bucket for dataflow code/jobs
resource "google_storage_bucket" "twitter-dataflow" {
  name     = "twitter-dataflow"
  location = "US"
}
