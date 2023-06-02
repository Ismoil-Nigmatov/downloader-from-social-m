package com.example.downloaderfromsocialm.dto.youtube;

import lombok.Data;

import java.util.List;

@Data
public class StoryboardsItem{
	private String thumbsCount;
	private int storyboardCount;
	private String columns;
	private String width;
	private String interval;
	private String rows;
	private List<String> url;
	private String height;
}