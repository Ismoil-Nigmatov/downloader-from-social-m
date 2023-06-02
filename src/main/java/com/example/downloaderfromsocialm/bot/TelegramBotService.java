package com.example.downloaderfromsocialm.bot;

import com.example.downloaderfromsocialm.dto.tiktok.TikTok;
import com.example.downloaderfromsocialm.dto.youtube.FormatsItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public interface TelegramBotService {
    SendMessage sendStartMessage(String chatId);

    InputStream sendTiktokVideo(TikTok tikTok);

    InputStream sendYoutubeVideo(FormatsItem formatsItem);

    InputStream sendInstagramVideoAndImage(String media);

    List<InputStream> sendInstagramCarousel(List<String> medias);

    List<MultipartFile> inputToMultipart(List<InputStream> inputStreams) throws IOException;

    List<InputMedia> multipartToMediaList(List<MultipartFile> multipartFiles);
}
