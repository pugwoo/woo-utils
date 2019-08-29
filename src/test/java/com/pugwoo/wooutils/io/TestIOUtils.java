package com.pugwoo.wooutils.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestIOUtils {

    @Test
    public void testListFiles() throws IOException {
        List<File> files = IOUtils.listFiles(new File("c:\\"));
        System.out.println(files.size());

        files = IOUtils.listFiles(new File("c:\\"), "\\.jpg");
        System.out.println(files.size());
        for(File file : files) {
            System.out.println(file.getPath());
        }
    }

}
