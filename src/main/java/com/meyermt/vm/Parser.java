package com.meyermt.vm;

import java.util.*;
import java.util.function.Function;

/**
 * Parser for incoming vm code. Parses the code by looking for spaces and then deciding if it is arithmetic or push/pop.
 * After that uses AsmCoder to actually translate arguments to assembly code.
 * Created by michaelmeyer on 2/3/17.
 */
public class Parser {

    private final AsmCoder coder;
    private final String fileName;

    // the arithmetic and return vm commands
    public final static String ADD = "add", SUB = "sub", NEG = "neg", EQ = "eq", GT = "gt", LT = "lt", AND = "and",
                                OR = "or", NOT = "not", RETURN = "return";
    // the push and pop commands
    private final static String POP = "pop", PUSH = "push";

    // the function and call commands
    private final static String FUNCTION = "function", CALL = "call";

    // label, goto, if-goto
    private final static String LABEL = "label", GOTO = "goto", IF_GOTO = "if-goto";

    private Map<String, String> statArithmeticToAsm = new HashMap<>();
    private Map<String, Function<String, String>> statControlToAsm = new HashMap<>();
    private Map<String, Function<Integer, String>> dynArithmeticToAsm = new HashMap<>();
    private Map<String, Function<List<String>, String>> argumentCommandToAsm = new HashMap<>();
    private int commandCounter = 0;

    /**
     * Instantiates a new Parser. Must have a filename in order to be able to pass to AsmCoder to name static variables.
     *
     * @param coder    the coder
     * @param fileName the file name
     */
    public Parser(AsmCoder coder, String fileName) {
        this.coder = coder;
        this.fileName = fileName.replace(".vm","");
        loadStatArithmeticMap(coder);
        loadDynArithmeticMap(coder);
        loadArgumentCommandToAsm(coder);
        loadStatControlMap(coder);
    }

    /**
     * Parse and translate string. Decides if command is what it considers dynamic or static arithmetic (dynamic means it
     * may use a counter to "dynamically" generate labels for the assembly code. If neither, assumes it is a push or pop
     * command.
     *
     * @param vmLine the vm line
     * @return the string
     */
    public String parseAndTranslate(String vmLine) {
        List<String> args = new ArrayList<>(Arrays.asList(vmLine.split(" ")));
        args.add(fileName);
        String command = args.get(0);
        if (statArithmeticToAsm.containsKey(command)) {
            return statArithmeticToAsm.get(command);
        } else if (dynArithmeticToAsm.containsKey(command)) {
            // dynamic arithmetic requires us to increment certain labels
            commandCounter++;
            return dynArithmeticToAsm.get(command).apply(commandCounter);
        } else if (statControlToAsm.containsKey(command)) {
            return statControlToAsm.get(command).apply(args.get(1));
        } else {
            return argumentCommandToAsm.get(command).apply(args);
        }
    }

    private void loadStatArithmeticMap(AsmCoder coder) {
        statArithmeticToAsm.put(ADD, coder.getAddAsm());
        statArithmeticToAsm.put(SUB, coder.getSubAsm());
        statArithmeticToAsm.put(NEG, coder.getNegAsm());
        statArithmeticToAsm.put(NOT, coder.getNotAsm());
        statArithmeticToAsm.put(AND, coder.getAndAsm());
        statArithmeticToAsm.put(OR, coder.getOrAsm());
        statArithmeticToAsm.put(RETURN, coder.getReturnAsm());
    }

    private void loadDynArithmeticMap(AsmCoder coder) {
        dynArithmeticToAsm.put(GT, coder.greaterThanAsm);
        dynArithmeticToAsm.put(LT, coder.lessThanAsm);
        dynArithmeticToAsm.put(EQ, coder.equalToAsm);
    }

    private void loadArgumentCommandToAsm(AsmCoder coder) {
        argumentCommandToAsm.put(PUSH, coder.pushToAsm);
        argumentCommandToAsm.put(POP, coder.popToAsm);
        argumentCommandToAsm.put(FUNCTION, coder.functionToAsm);
        argumentCommandToAsm.put(CALL, coder.callToAsm);
    }

    private void loadStatControlMap(AsmCoder coder) {
        statControlToAsm.put(LABEL, coder.labelAsm);
        statControlToAsm.put(GOTO, coder.goToAsm);
        statControlToAsm.put(IF_GOTO, coder.ifGoToAsm);
    }


}
