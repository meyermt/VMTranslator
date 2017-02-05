package com.meyermt.vm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by michaelmeyer on 2/3/17.
 */
public class VMFileReader {

    private final Path inputPath;
    private final static String VM_EXT = ".vm";

    public VMFileReader(String inputFile) {
        this.inputPath = Paths.get(inputFile);
    }

    public Path getInputPath() {
        return this.inputPath;
    }

    public List<String> readAndClean() {
        return removeComments(readFile(inputPath));
    }

    /*
    Reads the file. Will exit the program if IOException encountered or file is not of .asm extension
*/
    private List<String> readFile(Path inputPath) {
        // if the filename doesn't have the .asm extension we will exit with helpful message
        if (!inputPath.toString().endsWith(VM_EXT)) {
            System.out.println("Only able to read files with .asm extension. Please rename file and try again.");
            System.exit(1);
        }
        try {
            return Files.readAllLines(inputPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to read file: " + inputPath);
            System.exit(1);
        }
        // can't actually hit this but needed to compile
        return null;
    }

    /*
        Removes whitespace, blank lines, and comments from code
    */
    private List<String> removeComments(List<String> fileLines) {
        return fileLines.stream()
                .map(commentful -> commentful.replaceAll("(//.*)", ""))
                // remove tab characters, although in theory there shouldn't be any
                .map(tabful -> tabful.replaceAll("\t", ""))
                // remove blank lines after comment removal in case comment was the whole line
                .filter(line -> !line.equals(""))
                .collect(Collectors.toList());
    }
}
