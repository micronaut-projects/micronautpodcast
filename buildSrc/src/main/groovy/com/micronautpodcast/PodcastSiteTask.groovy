package com.micronautpodcast

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.text.DateFormat
import java.text.SimpleDateFormat

@CacheableTask
@CompileStatic
class PodcastSiteTask extends DefaultTask {
    static DateFormat JSON_FEED_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

    @OutputDirectory
    final DirectoryProperty outputDirectory

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFile
    final RegularFileProperty rssFile

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFile
    final RegularFileProperty template

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFile
    final RegularFileProperty episodeTemplate

    @Optional
    @Input
    final Property<String> twitter

    @Optional
    @Input
    final Property<String> mail

    @Optional
    @Input
    final Property<String> linkedin

    @Optional
    @Input
    final Property<String> github

    @Optional
    @Input
    final Property<String> gitter

    @Optional
    @Input
    final Property<String> rss

    @Optional
    @Input
    final Property<String> spotify

    @Optional
    @Input
    final Property<String> amazon

    @Optional
    @Input
    final Property<String> youtube

    @Optional
    @Input
    final Property<String> applePodcasts

    @Optional
    @Input
    final Property<String> pocketCasts

    @Optional
    @Input
    final Property<String> radioPublic

    @Optional
    @Input
    final Property<String> iTunesId

    @Input
    final Property<Integer> buttonWidth

    @Optional
    @Input
    final Property<String> artwork

    PodcastSiteTask() {
        outputDirectory = project.objects.directoryProperty()
        template = project.objects.fileProperty()
        episodeTemplate = project.objects.fileProperty()
        rssFile = project.objects.fileProperty()
        rss = project.objects.property(String)
        twitter = project.objects.property(String)
        github = project.objects.property(String)
        gitter = project.objects.property(String)
        linkedin = project.objects.property(String)
        mail = project.objects.property(String)
        artwork = project.objects.property(String)
        spotify = project.objects.property(String)
        amazon = project.objects.property(String)
        youtube = project.objects.property(String)
        applePodcasts = project.objects.property(String)
        pocketCasts = project.objects.property(String)
        radioPublic = project.objects.property(String)
        iTunesId = project.objects.property(String)
        buttonWidth = project.objects.property(Integer)
    }

    @TaskAction
    void generateSite() {
        String text = rssFile.get().asFile.text
        Node rssNode = new XmlParser().parseText(text)

        Podcast podcast = parsePodcast(rssNode)


        File outputFile = outputDirectory.file("index.html").get().asFile
        outputFile.createNewFile()
        outputFile.text = indexText(podcast)

        for (Episode e : podcast.episodes) {
            String filename = (e.getEpisode() + "").padLeft(3, '0') + ".html"
            outputFile = outputDirectory.file(filename).get().asFile
            outputFile.createNewFile()
            outputFile.text = indexText(podcast, e)
        }
    }

    String parseApplePodcastId(String applePodcastUrl) {
        String prefix = 'id'
        if (applePodcastUrl != null && applePodcastUrl.contains(prefix)) {
            return applePodcastUrl.substring(applePodcastUrl.indexOf(prefix) + prefix.length())
        }
        return null
    }

    String subscribeText(String name) {
        String text = ''
        if (rss.get())  {
            text += linkTo(rss.get(), name, 'RSS', buttonWidth.get(), 'rss.svg')
        }
        if (applePodcasts.get())  {
            text += linkTo(applePodcasts.get(), name, 'Apple Podcasts', buttonWidth.get(), 'itunes.svg')
        
        }
        if (iTunesId.get()) {
            text += linkTo('https://overcast.fm/itunes' + iTunesId.get(), name, 'Overcast', buttonWidth.get(), 'overcast.svg')
        
            text += linkTo('https://castro.fm/itunes/' + iTunesId.get(), name, 'Castro', buttonWidth.get(), 'castro.svg')
        }
        if (pocketCasts.get())  {
            text += linkTo(pocketCasts.get(), name, 'Pocket Casts', buttonWidth.get(), 'pocketcasts.svg')
        }
        if (spotify.get())  {
            text += linkTo(spotify.get(), name, 'Spotify', buttonWidth.get(), 'spotify.svg')
        }
        if (amazon.get())  {
            text += linkTo(amazon.get(), name, 'Amazon Music', buttonWidth.get(), 'amazonmusic.png')
        }
        if (youtube.get())  {
            text += linkTo(youtube.get(), name, 'Youtube', null, 'youtube.svg', buttonWidth.get())
        }
        if (radioPublic.get())  {
            text += linkTo(radioPublic.get(), name, 'Radio Public', buttonWidth.get(), 'radioPublic.svg')
        }
        text
    }
    
    String linkTo(String link, 
        String podcastName, 
        String service,
        Integer buttonWidth,          
        String image,
        Integer buttonHeight = null) {    
        '<a href="' + link + '" title="Subscribe to ' + podcastName + ' via ' + service + '" '+ (buttonWidth != null ? 'width="' + buttonWidth + '"' : '') + '><img src="./assets/images/' + image + '" alt="' + service + '" width="' + buttonWidth + '"' + (buttonHeight != null ? (' height="' + buttonHeight + '"') : '' ) + '></a>'
    }

    @CompileDynamic
    String indexText(Podcast podcast, Episode e = null) {
        String fileText = template.get().asFile.text
        String publicationDate = JSON_FEED_FORMAT.format(new Date())
        fileText = fileText.replaceAll('@date_modified@', publicationDate)
        fileText = fileText.replaceAll('@date_published@', publicationDate)
        String applePodcastId = parseApplePodcastId(applePodcasts.get())
        if (applePodcastId) {
            fileText = fileText.replaceAll('@applePodcastId@', applePodcastId)
        }
        if (e) {
            fileText = fileText.replaceAll('@breadcrumbs@', '<div id="breadcrumbs"><div class="container"><a href="https://micronaut.io/">Home</a> » <a href="./index.html">Micronaut Podcast</a> » <span class="breadcrumb_last" aria-current="page">'+ e.title +'</span></div></div>')
        } else {
           fileText = fileText.replaceAll('@breadcrumbs@', '')
        }
        fileText = fileText.replaceAll('@copyright@', podcast.copyright)
        fileText = fileText.replaceAll('@subscribe@', subscribeText(podcast.title))
        fileText = fileText.replaceAll('@artwork@', artwork.getOrNull() ?: podcast.artwork)
        fileText = fileText.replaceAll('@author@', podcast.author)
        fileText = fileText.replaceAll('@podcastUrl@', podcast.link)
        fileText = fileText.replaceAll('@twitter@', twitter.get())
        fileText = fileText.replaceAll('@rss@', rss.get())
        fileText = fileText.replaceAll('@mail@', mail.get())
        fileText = fileText.replaceAll('@gitter@', gitter.get())
        fileText = fileText.replaceAll('@linkedin@', linkedin.get())
        fileText = fileText.replaceAll('@github@', github.get())
        fileText = fileText.replaceAll('@youtube@', youtube.get())
        fileText = fileText.replaceAll('@podcastName@', (e!= null ? (e.title + ' | ' + podcast.title) : podcast.title))
        fileText = fileText.replaceAll('@podcastDescription@', (e!= null ? (e.description) : podcast.description))

        String episodesText = ''
        for (Episode episode : (e != null ? [e] : podcast.episodes)) {
            String episodeFileText = episodeTemplate.get().asFile.text

            episodeFileText = episodeFileText.replaceAll('@episodesize@', "${episode.size}")
            episodeFileText = episodeFileText.replaceAll('@episodeurl@', "${episode.url}")
            episodeFileText = episodeFileText.replaceAll('@seasoncount@', "${episode.season}")
            episodeFileText = episodeFileText.replaceAll('@episodecount@', "${episode.episode}".padLeft(3, '0'))
            episodeFileText = episodeFileText.replaceAll('@episodetitle@', episode.title)
            episodeFileText = episodeFileText.replaceAll('@episodedate@', episode.pubDate.substring(0, 'Sat, 29 May 2021'.length()))
            episodeFileText = episodeFileText.replaceAll('@episodesummary@', episode.description)
            episodeFileText = episodeFileText.replaceAll('@episodenotes@', episode.showNotes)
            episodesText += episodeFileText
        }
        fileText = fileText.replaceAll('@episodes@', episodesText)
        fileText
    }

    @CompileDynamic
    Podcast parsePodcast(Node rss) {
        Podcast podcast = new Podcast()
        podcast.artwork = rss.channel.image.url.text()
        podcast.link = rss.channel.link.text()
        podcast.author = rss.get('itunes:author').text()
        podcast.title = rss.channel.title.text()
        podcast.copyright = rss.channel.copyright.text()
        podcast.description = rss.channel.description.text()
        for (int i = 0; i < rss.channel.item.size(); i++) {
            Episode episode = new Episode()
            episode.title = rss.channel.item[i].title.text()
            episode.description = rss.channel.item[i].description.text()
            episode.pubDate = rss.channel.item[i].pubDate.text()
            episode.url = rss.channel.item[i].enclosure['@url'].text()
            episode.size = new BigDecimal(rss.channel.item[i].enclosure['@length'].text())
            episode.size = episode.size.divide(new BigDecimal("1000000"))
            episode.size = episode.size.setScale(2, BigDecimal.ROUND_HALF_UP)
            try {
                episode.episode = Integer.valueOf(rss.channel.item[i]['itunes:episode'].text())
            } catch(NumberFormatException e) {
            }
            try {
                episode.season = Integer.valueOf(rss.channel.item[i]['itunes:season'].text())
            } catch(NumberFormatException e) {
            }

            episode.showNotes = rss.channel.item[i]['content:encoded'].text()
            podcast.episodes << episode
        }
        podcast.episodes.sort { a, b ->
            b.episode <=> a.episode
        }
        podcast
    }
}
