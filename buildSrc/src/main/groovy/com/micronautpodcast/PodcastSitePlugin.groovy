package com.micronautpodcast

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

@CompileStatic
class PodcastSitePlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME_PODCAST_MEDIA = "podcastMedia"
    public static final String EXTENSION_NAME_BUILDINFO = "podcastMetadata"
    public static final String TASK_BUILD = "build"
    public static final String TASK_BUILD_PODCAST_SITE = "buildPodcastSite"
    public static final String TASK_GEN_EPISODE = "generateEpisode"
    public static final String GROUP_BUILD = "build"
    public static final String GROUP_PODCAST = "podcast"
    public static final String DESCRIPTION = "Generates a static site for a rss feed"

    @Override
    void apply(Project project) {
        project.getPlugins().apply(BasePlugin)

        PodcastMetadataExtension extension = project.extensions.create(EXTENSION_NAME_BUILDINFO, PodcastMetadataExtension, project)

        GenerateEpisodeExtension generateEpisodeExtension = project.extensions.create(EXTENSION_NAME_PODCAST_MEDIA, GenerateEpisodeExtension, project)

        project.tasks.register(TASK_GEN_EPISODE, GenerateEpisodeTask) { task ->
            task.setGroup(GROUP_PODCAST)
            task.setDescription("generate an episode markdown file for the newest mp3")
            task.inputDirectory.convention(generateEpisodeExtension.inputDirectory)
        }
        project.tasks.register(TASK_BUILD_PODCAST_SITE, PodcastSiteTask) { task ->
            task.setGroup(GROUP_BUILD)
            task.setDescription(DESCRIPTION)
            task.template.convention(extension.template)
            task.rssFile.convention(extension.rssFile)
            task.episodeTemplate.convention(extension.episodeTemplate)
            task.audioTemplate.convention(extension.audioTemplate)
            task.showNotesTemplate.convention(extension.showNotesTemplate)
            task.outputDirectory.convention(extension.outputDirectory)
            task.twitter.convention(extension.twitter)
            task.mail.convention(extension.mail)
            task.gitter.convention(extension.gitter)
            task.linkedin.convention(extension.linkedin)
            task.github.convention(extension.github)
            task.rss.convention(extension.rss)
            task.artwork.convention(extension.artwork)
            task.buttonWidth.convention(extension.buttonWidth)
            task.applePodcasts.convention(extension.applePodcasts)
            task.pocketCasts.convention(extension.pocketCasts)
            task.radioPublic.convention(extension.radioPublic)
            task.iTunesId.convention(extension.iTunesId)
            task.spotify.convention(extension.spotify)
            task.amazon.convention(extension.amazon)
            task.youtube.convention(extension.youtube)
        }

        project.tasks.named(TASK_BUILD).configure { task ->
            task.dependsOn(TASK_BUILD_PODCAST_SITE)
        }
    }
}
