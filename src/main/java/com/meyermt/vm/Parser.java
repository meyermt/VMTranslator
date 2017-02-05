package com.meyermt.vm;

import java.util.*;
import java.util.function.Function;

/**
 * Created by michaelmeyer on 2/3/17.
 */
public class Parser {

    private final AsmCoder coder;
    private final String fileName;
    // the arithmetic vm commands
    public final static String ADD = "add", SUB = "sub", NEG = "neg", EQ = "eq", GT = "gt", LT = "lt",
                                AND = "and", OR = "or", NOT = "not";
    // the push and pop commands
    private final static String POP = "pop", PUSH = "push";

    private Map<String, String> statArithmeticToAsm = new HashMap<>();
    private Map<String, Function<Integer, String>> dynArithmeticToAsm = new HashMap<>();
    private Map<String, Function<List<String>, String>> pushPopToAsm = new HashMap<>();
    private int commandCounter = 0;

    public Parser(AsmCoder coder, String fileName) {
        this.coder = coder;
        this.fileName = fileName.replace(".vm","");
        // pass in coder to constructor and to load. More unit-testable
        loadStatArithmeticMap(coder);
        loadDynArithmeticMap(coder);
        loadPushPopToAsm(coder);
    }

    public String parseAndTranslate(String vmLine) {
        List<String> args = new ArrayList<>(Arrays.asList(vmLine.split(" ")));
        args.add(fileName);
        if (statArithmeticToAsm.containsKey(args.get(0))) {
            return statArithmeticToAsm.get(args.get(0));
        } else if (dynArithmeticToAsm.containsKey(args.get(0))) {
            // dynamic arithmetic requires us to increment certain labels
            commandCounter++;
            return dynArithmeticToAsm.get(args.get(0)).apply(commandCounter);
        } else {
            // the static push and pop needs the file name
            return pushPopToAsm.get(args.get(0)).apply(args);
        }
    }

    private void loadStatArithmeticMap(AsmCoder coder) {
        statArithmeticToAsm.put(ADD, coder.getAddAsm());
        statArithmeticToAsm.put(SUB, coder.getSubAsm());
        statArithmeticToAsm.put(NEG, coder.getNegAsm());
        statArithmeticToAsm.put(NOT, coder.getNotAsm());
        statArithmeticToAsm.put(AND, coder.getAndAsm());
        statArithmeticToAsm.put(OR, coder.getOrAsm());
    }

    private void loadDynArithmeticMap(AsmCoder coder) {
        dynArithmeticToAsm.put(GT, coder.greaterThanAsm);
        dynArithmeticToAsm.put(LT, coder.lessThanAsm);
        dynArithmeticToAsm.put(EQ, coder.equalToAsm);
    }

    private void loadPushPopToAsm(AsmCoder coder) {
        pushPopToAsm.put(PUSH, coder.pushToAsm);
        pushPopToAsm.put(POP, coder.popToAsm);
    }


}
