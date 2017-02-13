package com.meyermt.vm;

import java.util.List;
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
        List<String> cleanFileLines = reader.readAndClean();

        // first we will bootstrap the program
        AsmCoder coder = new AsmCoder();
        List<String> bootstrappedCode = coder.writeBootStrap();

        // stream over the file, parse and translate

        Parser parser = new Parser(coder, reader.getInputPath().getFileName().toString());
        List<String> assemblerOutput = cleanFileLines.stream()
                .map(line -> parser.parseAndTranslate(line))
                .collect(Collectors.toList());

        bootstrappedCode.addAll(assemblerOutput);
        // write the output
        AsmFileWriter writer = new AsmFileWriter(reader.getInputPath());
        writer.writeAsmFile(bootstrappedCode);
        //writer.writeAsmFile(assemblerOutput);
    }

}
