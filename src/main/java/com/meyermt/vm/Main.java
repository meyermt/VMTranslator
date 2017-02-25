package com.meyermt.vm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main driver for program that translates vm code to assembly code.
 * Created by michaelmeyer on 2/3/17.
 */
public class Main {

    /**
     * The entry point of application. Drives reading of file, iteration over vm code into parser, and writing of code out
     * to file.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // read the .vm file in
        VMFileReader reader = new VMFileReader(args[0]);
        Map<String, List<String>> cleanFilesAndLines = reader.readFileOrFiles();

        // first we will bootstrap the program
        AsmCoder coder = new AsmCoder();
        List<String> bootstrappedCode = coder.writeBootStrap();

        // stream over the file, parse and translate

        Parser parser = new Parser(coder);
        List<String> assemblerOutput = cleanFilesAndLines.entrySet().stream()
                .flatMap(fileAndLines -> {
                    String fileName = fileAndLines.getKey().replace(".vm", "");
                    return fileAndLines.getValue().stream()
                            .map(line -> parser.parseAndTranslate(fileName, line));
                    })
                .collect(Collectors.toList());

        bootstrappedCode.addAll(assemblerOutput);
        // write the output
        AsmFileWriter writer = new AsmFileWriter(reader.getInputPath());
        writer.writeAsmFile(bootstrappedCode);
    }

}
