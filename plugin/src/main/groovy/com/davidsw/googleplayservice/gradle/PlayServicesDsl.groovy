package com.davidsw.googleplayservice.gradle;

import org.gradle.api.*;

class PlayServicesDsl {
    private final Project project
    private List<String> componentList;

    PlayServicesDsl(Project theProject) {
        this.project = theProject

        project.repositories {
            mavenCentral()
        }
    }

    public List<String> getComponents() {
        return componentList;
    }

    // -- DSL Methods ------------------------------------------------------------------------------

    void version(final String version = '5.0.+') {
        project.dependencies {
            compile "com.google.android.gms:play-services:$version"
        }
    }

    void components(String... components) {
        componentList = Arrays.asList(components)
    }

}