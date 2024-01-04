package com.percussion.ant.install;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class PSTaskTestUtils {

    protected static Path getRepositoryFileFromResources() throws URISyntaxException {
        return Paths.get(PSTaskTestUtils.class.getResource("test_embeddedrx.properties").toURI());
    }

    protected static void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src)
                .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    protected static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
            //Set the copied file to be writeable so config updates don't fail - source control sets read-oly attr on resources when checked in
            dest.toFile().setWritable(true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
