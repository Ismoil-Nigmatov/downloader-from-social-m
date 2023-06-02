package com.example.downloaderfromsocialm.bot;

import com.example.downloaderfromsocialm.dto.tiktok.TikTok;
import com.example.downloaderfromsocialm.dto.youtube.FormatsItem;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramBotServiceImpl implements TelegramBotService{
    @Override
    public SendMessage sendStartMessage(String chatId) {
        return SendMessage.builder().text("Hello Dear , This bot was made to download a video, image , story from instagram , tiktok (no watermark) and from youtube. Please send the url").chatId(chatId).build();
    }

    @Override
    public InputStream sendTiktokVideo(TikTok tikTok) {
        try {
            URL url = new URL(tikTok.getVideo().get(0));
            URLConnection connection = url.openConnection();

            return connection.getInputStream();
        }catch (Exception e){
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public InputStream sendYoutubeVideo(FormatsItem formatsItem) {
        try {
            URL url = new URL(formatsItem.getUrl());
            URLConnection connection = url.openConnection();

            InputStream inputStream = connection.getInputStream();
            System.out.println(inputStream.available());
            return inputStream;
        }catch (Exception e){
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public InputStream sendInstagramVideoAndImage(String media) {
        try {
            URL url = new URL(media);
            URLConnection connection = url.openConnection();

            return connection.getInputStream();
        }catch (Exception e){
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public List<InputStream> sendInstagramCarousel(List<String> medias) {
        try {
            List<InputStream> inputStreams=new ArrayList<>();
            for (String media : medias) {
                URL url = new URL(media);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                inputStreams.add(inputStream);
            }
            return inputStreams;
        }catch (Exception e){
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public List<MultipartFile> inputToMultipart(List<InputStream> inputStreams) throws IOException {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < inputStreams.size(); i++) {
            InputStream inputStream = inputStreams.get(i);

            byte[] bytes = inputStream.readAllBytes();
            InputStream copyStream = new ByteArrayInputStream(bytes);

            DefaultDetector detector = new DefaultDetector();
            Metadata metadata = new Metadata();
            MediaType mediaType = detector.detect(copyStream, metadata);

            MultipartFile multipartFile = new MockMultipartFile("data "+i,"data "+i,mediaType.getType(),copyStream);
            multipartFiles.add(multipartFile);
        }
        return multipartFiles;
    }

    @SneakyThrows
    @Override
    public List<InputMedia> multipartToMediaList(List<MultipartFile> multipartFiles) {
        List<InputMedia> inputMediaList = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.getContentType().startsWith("video")) {
                InputMedia inputMedia = new InputMediaVideo();
                InputStream inputStream = multipartFile.getInputStream();
                inputMedia.setMedia(inputStream, multipartFile.getOriginalFilename());
                inputMediaList.add(inputMedia);
            } else {
                InputMedia inputMedia = new InputMediaPhoto();
                InputStream inputStream = multipartFile.getInputStream();
                inputMedia.setMedia(inputStream, multipartFile.getOriginalFilename());
                inputMediaList.add(inputMedia);
            }
        }
        return inputMediaList;
    }
}

