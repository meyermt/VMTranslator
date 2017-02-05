package com.meyermt.vm;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Created by michaelmeyer on 2/3/17.
 */
public class AsmFileWriter {

    private final Path outputPath;
    private final static String VM_EXT = "vm";
    private final static String ASM_EXT = "asm";

    public AsmFileWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    /*
        Writes output to a file with .hack extension. Will truncate/write over existing .hack files if there
    */
    public void writeAsmFile(List<String> asmCode) {
        // create the output file
        String fileName = outputPath.getFileName().toString();
        String outputFileName = fileName.replace(VM_EXT, ASM_EXT);
        try {
            String outputDir = outputPath.toRealPath(NOFOLLOW_LINKS).getParent().toString();
            Path outputPath = Paths.get(outputDir, outputFileName);
            Files.write(outputPath, asmCode, Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("Issue encountered writing output file for: " + outputFileName);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
