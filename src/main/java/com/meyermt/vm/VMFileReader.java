package com.meyermt.vm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads vm input file and cleans the comments and blank lines that exist in it.
 * Created by michaelmeyer on 2/3/17.
 */
public class VMFileReader {

    private final Path inputPath;
    private final static String VM_EXT = ".vm";

    /**
     * Instantiates a new Vm file reader.
     *
     * @param inputFile the input file
     */
    public VMFileReader(String inputFile) {
        this.inputPath = Paths.get(inputFile);
    }

    /**
     * Gets input path.
     *
     * @return the input path
     */
    public Path getInputPath() {
        return this.inputPath;
    }

    /**
     * Read and clean list.
     *
     * @return the list
     */
    public List<String> readAndClean() {
        return removeComments(readFile(inputPath));
    }

    /*
        Reads the file. Will exit the program if IOException encountered or file is not of .vm extension
    */
    private List<String> readFile(Path inputPath) {
        // if the filename doesn't have the .asm extension we will exit with helpful message
        if (!inputPath.toString().endsWith(VM_EXT)) {
            System.out.println("Only able to read files with .vm extension. Please rename file and try again.");
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
        Removes blank lines, tabs, and comments from code
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
