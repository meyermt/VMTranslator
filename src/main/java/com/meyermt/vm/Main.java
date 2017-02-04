package com.meyermt.vm;

import java.util.List;

/**
 * Created by michaelmeyer on 2/3/17.
 */
public class Main {

    public static void main(String[] args) {
        VMFileReader reader = new VMFileReader(args[0]);
        List<String> cleanFileLines = reader.readAndClean();
    }

}
