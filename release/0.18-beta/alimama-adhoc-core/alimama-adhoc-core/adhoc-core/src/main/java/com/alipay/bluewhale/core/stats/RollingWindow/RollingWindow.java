package com.alipay.bluewhale.core.stats.RollingWindow;

import java.util.Map;

import com.alipay.bluewhale.core.callback.RunnableCallback;


public class RollingWindow {
    public RunnableCallback updater;
    public RunnableCallback merger;
    public RunnableCallback extractor;
    public Integer bucket_size_secs;
    public Integer num_buckets;
    public Map<Integer, Object> buckets;

    public RunnableCallback getUpdater() {
	return updater;
    }

    public void setUpdater(RunnableCallback updater) {
	this.updater = updater;
    }

    public RunnableCallback getMerger() {
	return merger;
    }

    public void setMerger(RunnableCallback merger) {
	this.merger = merger;
    }

    public RunnableCallback getExtractor() {
	return extractor;
    }

    public void setExtractor(RunnableCallback extractor) {
	this.extractor = extractor;
    }

    public Integer getBucket_size_secs() {
	return bucket_size_secs;
    }

    public void setBucket_size_secs(Integer bucket_size_secs) {
	this.bucket_size_secs = bucket_size_secs;
    }

    public Integer getNum_buckets() {
	return num_buckets;
    }

    public void setNum_buckets(Integer num_buckets) {
	this.num_buckets = num_buckets;
    }

    public Map<Integer, Object> getBuckets() {
	return buckets;
    }

    public void setBuckets(Map<Integer, Object> buckets) {
	this.buckets = buckets;
    }

}
