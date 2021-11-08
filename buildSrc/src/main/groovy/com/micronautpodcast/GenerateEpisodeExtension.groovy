package com.micronautpodcast

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

class GenerateEpisodeExtension {
    final DirectoryProperty inputDirectory
    GenerateEpisodeExtension(Project project) {
        inputDirectory = project.objects.directoryProperty()
    }
}
