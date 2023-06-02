package com.example.downloaderfromsocialm.dto.youtube;

import lombok.Data;

import java.util.List;

@Data
public class Captions{
	private List<CaptionTracksItem> captionTracks;
	private List<TranslationLanguagesItem> translationLanguages;
}