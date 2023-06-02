package com.example.downloaderfromsocialm.dto.youtube;

import lombok.Data;

@Data
public class FormatsItem{
	private int itag;
	private int fps;
	private String projectionType;
	private int bitrate;
	private String mimeType;
	private String approxDurationMs;
	private String url;
	private String audioSampleRate;
	private String quality;
	private String qualityLabel;
	private int audioChannels;
	private int width;
	private int height;
	private String contentLength;
	private int averageBitrate;
}