package com.example.downloaderfromsocialm.dto.instagram;

import lombok.Data;

import java.util.List;

@Data
public class InstagramCarousel {
	private String Type;
	private String thumbnail;
	private String API;
	private List<String> media;
}