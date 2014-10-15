package com.davidsw.googleplayservice.gradle;

import com.davidsw.googleplayservice.gradle.PlayServicesDsl
import org.gradle.api.*; 
import java.nio.channels.FileChannel;
import java.io.File;

class PreprocessorPlugin implements Plugin<Project> {
    private Project project;
    private PlayServicesDsl playServices;
    def EXPLODED_AAR_DIR
    def EXPLODED_PLAY_SERVICE_AAR_DIR
    def GOOGLE_PLAY_SERVICES_VERSION

    def void apply(Project project) {
        println "[Plugin] GooglePlayServicePlugin"
        EXPLODED_AAR_DIR = "${project.buildDir}/intermediates/exploded-aar"
        this.project = project
        playServices = project.extensions.create('playServices', PlayServicesDsl, project)

        def stripTaskName = "stripPlayServices"
        project.configure(project) {
            if(it.hasProperty("android")) {
                project.task(stripTaskName) << {
                    stripPlayServices()
                }

                project.android.applicationVariants.all { variant ->
                    println "compile${variant.name.capitalize()}Java.dependsOn(${stripTaskName})"
                    tasks.getByPath("compile${variant.name.capitalize()}Java").dependsOn(stripTaskName)
                }
            }
        }
    }

    def GOOGLE_PLAY_SERVICES_NESTED_PATH = "com.google.android.gms/play-services"

    def PLAY_SERVICES_FILENAME="classes.jar"
    def PLAY_SERVICES_TEMP_DIR="google-play-services-temp"
    def PLAY_SERVICES_NESTED_PATH="com/google/android/gms"
    def PLAY_SERVICES_OUTPUT_FILE="google-play-services-STRIPPED.jar"

    private void stripPlayServices() {
        GOOGLE_PLAY_SERVICES_VERSION = playServices.getVersion();
        EXPLODED_PLAY_SERVICE_AAR_DIR = EXPLODED_AAR_DIR + "/" + GOOGLE_PLAY_SERVICES_NESTED_PATH + "/" + GOOGLE_PLAY_SERVICES_VERSION;

        unJar();
        removeUnselectedComponents();
        createStrippedJar();
        cleanup();
    }

    private void unJar() {
        String tempDirPath = EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_TEMP_DIR;
        File tempDir = new File(tempDirPath);
        if (tempDir.exists()) {
            deleteFile(tempDir);
        }
        tempDir.mkdirs();
        copyFile(new File(EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_FILENAME),
                new File(tempDirPath + "/" + PLAY_SERVICES_FILENAME));

        // Extract archive
        ProcessBuilder pb = new ProcessBuilder("jar", "xf", "${PLAY_SERVICES_FILENAME}");
        pb.directory(new File(tempDirPath))
        pb.start().waitFor()
    }

    private void removeUnselectedComponents() {
        String tempDirPath = EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_TEMP_DIR;
        List<String> components = playServices.getComponents();
        File tempDir = new File(tempDirPath + "/" + PLAY_SERVICES_NESTED_PATH);
        if (!tempDir.isDirectory()) {
            println "destDir (" + tempDir.getAbsolutePath() + ") is not a directory"
        } else {
            File[] files = tempDir.listFiles();
            for (File f : files) {
                if (!isSelectedCompoment(f.getName())) {
                    println f.getName() + " is removed."
                    deleteFile(f);
                }
            }
        }
    }

    private void createStrippedJar() {
        String tempDirPath = EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_TEMP_DIR;
        ProcessBuilder pb = new ProcessBuilder("jar", "cf", "$PLAY_SERVICES_OUTPUT_FILE", "com");
        pb.directory(new File(tempDirPath));
        pb.start().waitFor();

        // delete old classes.jar and copy stripped one to destination
        deleteFile(new File(EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_FILENAME));
        copyFile(new File(tempDirPath + "/" + PLAY_SERVICES_OUTPUT_FILE),
                new File(EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_FILENAME));
    }

    private cleanup() {
        deleteFile(new File(EXPLODED_PLAY_SERVICE_AAR_DIR + "/" + PLAY_SERVICES_TEMP_DIR));
    }

    private boolean isSelectedCompoment(String name) {
        for (String selectedName : playServices.getComponents()) {
            if (selectedName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // Private helper methods

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists() && !destFile.createNewFile())
            throw new IOException("Could not create " + destFile.toString());

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) {
                    // No files in the directory, let's delete the directory.
                    return file.delete();
                }

                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        deleteFile(subFile);
                    } else {
                        subFile.delete();
                    }
                }
            }
            // else, `file` is a File
        }
        return file.delete();
    }
}
