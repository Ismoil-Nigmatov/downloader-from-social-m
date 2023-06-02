package com.example.downloaderfromsocialm.dto.youtube;

import lombok.Data;

@Data
public class CaptionTracksItem{
	private String baseUrl;
	private boolean isTranslatable;
	private String vssId;
	private String name;
	private String languageCode;
}