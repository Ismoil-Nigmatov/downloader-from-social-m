package com.example.downloaderfromsocialm.bot;

import com.example.downloaderfromsocialm.dto.instagram.Instagram;
import com.example.downloaderfromsocialm.dto.instagram.InstagramCarousel;
import com.example.downloaderfromsocialm.dto.tiktok.TikTok;
import com.example.downloaderfromsocialm.dto.youtube.FormatsItem;
import com.example.downloaderfromsocialm.dto.youtube.YouTube;
import com.example.downloaderfromsocialm.entity.User;
import com.example.downloaderfromsocialm.repository.UserRepository;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBot extends TelegramLongPollingBot {

    private int fileSizeLimit;
    private List<String> allowedUpdates;


    private final UserRepository userRepository;

    private final TelegramBotService telegramBotService;

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (userRepository.existsById(String.valueOf(update.getMessage().getChatId()))) {
            User user = userRepository.findById(String.valueOf(update.getMessage().getChatId())).orElseThrow(RuntimeException::new);
            if (user.getQuota() != 0L) {
                Long quota = user.getQuota();
                user.setQuota(quota - 1);
                userRepository.save(user);
            } else {
                execute(SendMessage.builder().text("You have no quotas left, in order to continue using the bot, please buy quotas. You can buy here @ismoil_in").chatId(String.valueOf(update.getMessage().getChatId())).build());
            }
        }//state of checking quotas
        else {
            User user = new User();
            user.setChatId(String.valueOf(update.getMessage().getChatId()));
            user.setFirstName(update.getMessage().getFrom().getFirstName());
            user.setLastName(update.getMessage().getFrom().getLastName());
            user.setUserName(update.getMessage().getFrom().getUserName());
            userRepository.save(user);
        }//new user

        if (update.hasMessage() && (update.getMessage().getText().equals("/start"))) {
            execute(telegramBotService.sendStartMessage(update.getMessage().getChatId().toString()));

        }

        if (update.getMessage().getText().startsWith("https://vt.tiktok.com/") || (update.getMessage().getText().startsWith("https://www.tiktok.com/"))) {

            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {
                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/vid/index?url=" + update.getMessage().getText())
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {
                                    String body = response.getResponseBody();
                                    TikTok tikTok = new Gson().fromJson(body, TikTok.class);

                                    InputStream inputStream = telegramBotService.sendTiktokVideo(tikTok);
                                    try {
                                        execute(SendVideo.builder().chatId(update.getMessage().getChatId().toString()).video(new InputFile(inputStream, "youtubeVideo")).build());
                                        log.info("TikTok video was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                                    } catch (TelegramApiException e) {
                                        log.error(e.toString());
                                    }
                                }
                        )
                        .join();

                client.close();
            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        else

        if (update.getMessage().getText().startsWith("https://youtube.com/shorts/")){
            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {

                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://ytstream-download-youtube-videos.p.rapidapi.com/dl?id=" + update.getMessage().getText().substring(27, 38))
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "ytstream-download-youtube-videos.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {

                            String body = response.getResponseBody();
                            YouTube youTube = new Gson().fromJson(body, YouTube.class);

                            List<FormatsItem> formats = youTube.getFormats();
                            FormatsItem formatsItem = new FormatsItem();
                            for (int i = 0; i < formats.size() - 1; i++) {
                                if (formats.get(i).getHeight() > formats.get(i + 1).getHeight()) {
                                    formatsItem = formats.get(i);
                                } else {
                                    formatsItem = formats.get(i + 1);
                                }
                            }


                            InputStream inputStream = telegramBotService.sendYoutubeVideo(formatsItem);
                            try {
                                execute(SendVideo.builder().chatId(update.getMessage().getChatId().toString()).video(new InputFile(inputStream, "youtubeVideo")).build());
                                log.info("YouTube shorts was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                            } catch (Exception e) {
                                log.error(String.valueOf(e));
                                try {
                                    execute(SendMessage.builder().text("Failed to download video").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                    execute(SendMessage.builder().text("Possible reason : Request Entity Too Large").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                } catch (TelegramApiException ex) {
                                    log.error(ex.toString());
                                }
                            }
                        })
                        .join();

                client.close();
            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        else

        if(update.getMessage().getText().startsWith("https://www.youtube.com/shorts/")){
            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {

                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://ytstream-download-youtube-videos.p.rapidapi.com/dl?id=" + update.getMessage().getText().substring(31))
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "ytstream-download-youtube-videos.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {

                            String body = response.getResponseBody();
                            YouTube youTube = new Gson().fromJson(body, YouTube.class);

                            List<FormatsItem> formats = youTube.getFormats();
                            FormatsItem formatsItem = new FormatsItem();
                            for (int i = 0; i < formats.size() - 1; i++) {
                                if (formats.get(i).getHeight() > formats.get(i + 1).getHeight()) {
                                    formatsItem = formats.get(i);
                                } else {
                                    formatsItem = formats.get(i + 1);
                                }
                            }

                            InputStream inputStream = telegramBotService.sendYoutubeVideo(formatsItem);
                            try {
                                execute(SendVideo.builder().chatId(update.getMessage().getChatId().toString()).video(new InputFile(inputStream, "youtubeVideo")).build());
                                log.info("YouTube shorts was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                            } catch (Exception e) {
                                log.error(String.valueOf(e));
                                try {
                                    execute(SendMessage.builder().text("Failed to download video").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                    execute(SendMessage.builder().text("Possible reason : Request Entity Too Large").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                } catch (TelegramApiException ex) {
                                    log.error(ex.toString());
                                }
                            }
                        })
                        .join();

                client.close();
            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        else

        if (update.getMessage().getText().startsWith("https://www.youtube.com/")) {

            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {

                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://ytstream-download-youtube-videos.p.rapidapi.com/dl?id=" + update.getMessage().getText().substring(32))
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "ytstream-download-youtube-videos.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {
                                    String body = response.getResponseBody();
                                    YouTube youTube = new Gson().fromJson(body, YouTube.class);

                                    List<FormatsItem> formats = youTube.getFormats();
                                    FormatsItem formatsItem = new FormatsItem();
                                    for (int i = 0; i < formats.size() - 1; i++) {
                                        if (formats.get(i).getHeight() > formats.get(i + 1).getHeight()) {
                                            formatsItem = formats.get(i);
                                        } else {
                                            formatsItem = formats.get(i + 1);
                                        }
                                    }

                                    InputStream inputStream = telegramBotService.sendYoutubeVideo(formatsItem);
                            try  {
                                ZipFile zipFile = new ZipFile("archive.zip");
                                ZipParameters parameters = new ZipParameters();
                                parameters.setFileNameInZip("video.mp4");
                                zipFile.addStream(inputStream, parameters);
                                        execute(SendDocument.builder().chatId(update.getMessage().getChatId().toString()).document(new InputFile(new File("archive.zip"))).build());
                                        log.info("YouTube video was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                                    } catch (Exception e) {
                                        log.error(String.valueOf(e));
                                        try {
                                            execute(SendMessage.builder().text("Failed to download video").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                            execute(SendMessage.builder().text("Possible reason : Request Entity Too Large").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                        } catch (Exception exception) {
                                            log.error(exception.toString());
                                        }
                                    }
                                }
                        )
                        .join();
                client.close();
            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        else

        if (update.getMessage().getText().startsWith("https://youtu.be/")){
            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {

                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://ytstream-download-youtube-videos.p.rapidapi.com/dl?id=" + update.getMessage().getText().substring(17))
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "ytstream-download-youtube-videos.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {

                            String body = response.getResponseBody();
                            YouTube youTube = new Gson().fromJson(body, YouTube.class);

                            List<FormatsItem> formats = youTube.getFormats();
                            FormatsItem formatsItem = new FormatsItem();
                            for (int i = 0; i < formats.size() - 1; i++) {
                                if (formats.get(i).getHeight() > formats.get(i + 1).getHeight()) {
                                    formatsItem = formats.get(i);
                                } else {
                                    formatsItem = formats.get(i + 1);
                                }
                            }

                            InputStream inputStream = telegramBotService.sendYoutubeVideo(formatsItem);
                            try {
                                ZipFile zipFile = new ZipFile("archive.zip");
                                ZipParameters parameters = new ZipParameters();
                                parameters.setFileNameInZip("video.mp4");
                                zipFile.addStream(inputStream, parameters);
                                execute(SendDocument.builder().chatId(update.getMessage().getChatId().toString()).document(new InputFile(new File("archive.zip"))).build());
                                log.info("YouTube video was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                            } catch (Exception e) {
                                log.error(String.valueOf(e));
                                try {
                                    execute(SendMessage.builder().text("Failed to download video").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                    execute(SendMessage.builder().text("Possible reason : Request Entity Too Large").chatId(String.valueOf(update.getMessage().getChatId())).build());
                                } catch (TelegramApiException ex) {
                                    log.error(ex.toString());
                                }
                            }
                        })
                        .join();

                client.close();
            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        else

        if ((update.getMessage().getText().startsWith("https://www.instagram.com/")) || (update.getMessage().getText().startsWith("https://instagram.com"))) {

            execute(SendMessage.builder().text("Wait, Please...").chatId(String.valueOf(update.getMessage().getChatId())).build());

            try {

                AsyncHttpClient client = new DefaultAsyncHttpClient();
                client.prepare("GET", "https://instagram-downloader-download-instagram-videos-stories.p.rapidapi.com/index?url="+update.getMessage().getText())
                        .setHeader("X-RapidAPI-Key", "8196fbbdfamsh6929d53aa1ecf11p13dd2ejsn2599b79bab13")
                        .setHeader("X-RapidAPI-Host", "instagram-downloader-download-instagram-videos-stories.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {
                            String body = response.getResponseBody();
                            try {
                                Instagram instagram = new Gson().fromJson(body, Instagram.class);
                                InputStream inputStream = telegramBotService.sendInstagramVideoAndImage(instagram.getMedia());
                                if (instagram.getType().equals("Post-Video")) {
                                    execute(SendVideo.builder().chatId(update.getMessage().getChatId().toString()).video(new InputFile(inputStream, "instagramVideo")).build());
                                    log.info("Instagram video was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                                }
                                else {
                                    execute(SendPhoto.builder().chatId(update.getMessage().getChatId().toString()).photo(new InputFile(inputStream, "instagramImage")).build());
                                    log.info("Instagram image was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                                }
                            }catch (Exception e){
                                InstagramCarousel instagramCarousel = new Gson().fromJson(body, InstagramCarousel.class);
                                List<InputStream> inputStreams = telegramBotService.sendInstagramCarousel(instagramCarousel.getMedia());
                                try {
                                    List<MultipartFile> multipartFiles = telegramBotService.inputToMultipart(inputStreams);
                                    List<InputMedia> inputMediaList = telegramBotService.multipartToMediaList(multipartFiles);
                                    execute(SendMediaGroup.builder().medias(inputMediaList).chatId(update.getMessage().getChatId()).build());
                                    log.info("Instagram carousel was sent to user -> " + update.getMessage().getChatId() + " -> " + update.getMessage().getFrom().getFirstName());
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        })
                        .join();

                client.close();

            }catch (Exception e){
                execute(SendMessage.builder().chatId(update.getMessage().getChatId()).text("Failed to download").build());
                log.error(e.toString());
            }
        }

        }

}