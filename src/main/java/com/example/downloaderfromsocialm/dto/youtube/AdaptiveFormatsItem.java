package com.example.downloaderfromsocialm.dto.youtube;

import lombok.Data;

@Data
public class AdaptiveFormatsItem{
	private int itag;
	private String projectionType;
	private int bitrate;
	private String mimeType;
	private String audioQuality;
	private String approxDurationMs;
	private String url;
	private String audioSampleRate;
	private String quality;
	private int audioChannels;
	private String contentLength;
	private Object loudnessDb;
	private int averageBitrate;
	private boolean highReplication;
	private int fps;
	private String qualityLabel;
	private int width;
	private int height;
}