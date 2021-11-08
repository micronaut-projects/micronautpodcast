package com.micronautpodcast

import com.mpatric.mp3agic.ID3v1
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.InvalidDataException
import com.mpatric.mp3agic.Mp3File
import com.mpatric.mp3agic.UnsupportedTagException
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.text.DateFormat
import java.text.SimpleDateFormat

@CacheableTask
@CompileStatic
class GenerateEpisodeTask extends DefaultTask {
    public static final String FILE_PREFIX = "micronautpodcast"
    public static final String FILE_SUFFIX = ".mp3"

    @PathSensitive(PathSensitivity.NONE)
    @InputDirectory
    final DirectoryProperty inputDirectory

    GenerateEpisodeTask() {
        inputDirectory = project.objects.directoryProperty()
    }

    @TaskAction
    void generateEpisode() {
        File dir = inputDirectory.get().asFile
        File latestMp3File = (dir.listFiles() as List<File>)
                .findAll { file -> file.name.endsWith("mp3") }
                .sort { -it.lastModified() }?.head()


        final DateFormat JSON_FEED_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String now = JSON_FEED_FORMAT.format(new Date());
        try {
            Mp3File mp3File = new Mp3File(latestMp3File)
            String title = null;
            String comment = null
            String guid = latestMp3File.name.replaceAll(FILE_SUFFIX, "")
            String str = guid.replace(FILE_PREFIX, "");
            do {
                if (str.startsWith("0")) {
                    str = str.substring(1);
                }
            } while (str.startsWith("0"));
            Integer episode = null;
            try {
                episode = Integer.valueOf(str);
            } catch (NumberFormatException e) {

            }
            Integer season = 1;
            String album = null;
            String artist = null;
            if (mp3File.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3File.getId3v1Tag();
                title =  id3v1Tag.getTitle();
                comment =  id3v1Tag.getComment();
                album = id3v1Tag.getAlbum();
                artist = id3v1Tag.getArtist();
            } else if (mp3File.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                title = id3v2Tag.getTitle();
                comment = id3v2Tag.getComment();
                album = id3v2Tag.getAlbum();
                artist = id3v2Tag.getArtist();
            }
            if (title != null && comment != null && album != null && artist != null) {
                Mp3 mp3 = new Mp3(title, comment, album, artist);
                System.out.println("---");
                System.out.println("title: " + mp3.getTitle());
                System.out.println("season: " + season);
                System.out.println("episode: " + episode);
                System.out.println("enclosureUrl: https://mp3.micronautpodcast.com/" + latestMp3File.name);
                System.out.println("summary: " + mp3.getAlbum());
                System.out.println("pubDate: " + now);
                System.out.println("episodeType: FULL");
                System.out.println("author: " + mp3.getArtist());
                System.out.println("enclosureType: audio/mp3");
                System.out.println("guid: " + guid);
                System.out.println("explicit: false");
                System.out.println("---");
                System.out.println(mp3.getComment());
            }

        } catch (IOException e) {
            System.out.println("IOException fetching mp3");
        } catch (InvalidDataException e) {
            System.out.println("InvalidDataException fetching mp3");
        } catch (UnsupportedTagException e) {
            System.out.println("UnsupportedTagException fetching mp3");
        }
    }
}
