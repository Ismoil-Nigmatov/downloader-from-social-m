package com.example.downloaderfromsocialm.dto.tiktok;

import lombok.Data;

import java.util.List;

@Data
public class TikTok{
	private List<String> cover;
	private List<String> music;
	private List<String> author;
	private List<String> originalWatermarkedVideo;
	private List<String> avatarThumb;
	private List<String> description;
	private List<String> videoid;
	private String postType;
	private List<String> video;
	private List<String> customVerify;
	private List<String> region;
	private List<String> dynamicCover;
}