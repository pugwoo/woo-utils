package com.pugwoo.wooutils.io;

import com.pugwoo.wooutils.string.StringTools;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testGetClasspathFile() throws IOException {
        String content = IOUtils.readClasspathResourceAsString("sql/my.sql");
        assert StringTools.isNotBlank(content);
        assert content.equals("select *\r\nfrom dual\r\n");

        content = IOUtils.readClasspathResourceAsString("sql/my-no-new-line.sql");
        assert StringTools.isNotBlank(content);
        assert content.equals("select *\r\nfrom dual");
    }

}
