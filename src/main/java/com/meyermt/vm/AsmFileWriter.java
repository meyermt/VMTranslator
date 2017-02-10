package com.meyermt.vm;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Writes assembly code file
 * Created by michaelmeyer on 2/3/17.
 */
public class AsmFileWriter {

    private final Path outputPath;
    private final static String VM_EXT = "vm";
    private final static String ASM_EXT = "asm";

    /**
     * Instantiates a new Asm file writer using output path.
     *
     * @param outputPath the output path
     */
    public AsmFileWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Write asm file to output path.
     *
     * @param asmCode the asm code to be written out.
     */
    public void writeAsmFile(List<String> asmCode) {
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
