package com.meyermt.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Writes assembly code from VM instructions.
 * Created by michaelmeyer on 2/3/17.
 */
public class AsmCoder {

    // shared asm translation
    private static final String MOVE_SP_UP_STORE_IN_D =
            "@SP" + System.lineSeparator() +
            "AM=M-1" + System.lineSeparator() +
            "D=M" + System.lineSeparator();
    private static final String MOVE_SP_UP = "A=A-1" + System.lineSeparator();
    private static final String GET_TWO_ON_STACK = MOVE_SP_UP_STORE_IN_D + MOVE_SP_UP;
    private static final String ADD_END = "M=M+D", SUB_END = "M=M-D";
    private static final String AND_END = "M=M&D", OR_END = "M=M|D";
    private static final String ADD_ASM = GET_TWO_ON_STACK + ADD_END;
    private static final String SUB_ASM = GET_TWO_ON_STACK + SUB_END;
    private static final String AND_ASM = GET_TWO_ON_STACK + AND_END;
    private static final String OR_ASM = GET_TWO_ON_STACK + OR_END;

    private static final String MOVE_ONE_UP_STACK =
            "@SP" + System.lineSeparator() +
            "A=M-1" + System.lineSeparator();
    private static final String NEG_ASM = MOVE_ONE_UP_STACK + "M=-M";
    private static final String NOT_ASM = MOVE_ONE_UP_STACK + "M=!M";

    private static final String PUSH_VALUE_IN_D =
        "@SP" + System.lineSeparator() +
        "A=M" + System.lineSeparator() +
        "M=D" + System.lineSeparator() +
        "@SP" + System.lineSeparator() +
        "M=M+1";

    // the segments
    private final static String ARGUMENT = "argument", LOCAL = "local", STATIC = "static", CONSTANT = "constant",
            THIS = "this", THAT = "that", POINTER = "pointer", TEMP = "temp";

    private int returnAddrCounter = 0;

    public List<String> writeBootStrap() {
        List<String> instructions = new ArrayList<>();
        instructions.add("@256");
        instructions.add("D=A");
        instructions.add("@SP");
        instructions.add("M=D");
        String[] callArgs = {"call", "Sys.init", "0", ""};
        List<String> call = Arrays.asList(callArgs);
        instructions.add(callToAsm.apply(call));
        return instructions;
    }

    /**
     * Instantiates a new Asm coder.
     */
    public AsmCoder() {
    }

    /**
     * Gets add asm.
     *
     * @return the add asm
     */
    public String getAddAsm() {
        return ADD_ASM;
    }

    /**
     * Gets sub asm.
     *
     * @return the sub asm
     */
    public String getSubAsm() {
        return SUB_ASM;
    }

    /**
     * Gets neg asm.
     *
     * @return the neg asm
     */
    public String getNegAsm() {
        return NEG_ASM;
    }

    /**
     * Gets not asm.
     *
     * @return the not asm
     */
    public String getNotAsm() {
        return NOT_ASM;
    }

    /**
     * Gets and asm.
     *
     * @return the and asm
     */
    public String getAndAsm() {
        return AND_ASM;
    }

    /**
     * Gets or asm.
     *
     * @return the or asm
     */
    public String getOrAsm() {
        return OR_ASM;
    }

    /**
     * Function that reads in a variable counter and returns greater than assembly code.
     */
    public Function<Integer, String> greaterThanAsm = (Integer counter) -> getLTEQGT("GT", counter);

    /**
     * Function that reads in a variable counter and returns less than assembly code.
     */
    public Function<Integer, String> lessThanAsm = (Integer counter) -> getLTEQGT("LT", counter);

    /**
     * Function that reads in a variable counter and returns equal to assembly code.
     */
    public Function<Integer, String> equalToAsm = (Integer counter) -> getLTEQGT("EQ", counter);

    /**
     * Function that reads in push type, segment, position, and filename in an array and outputs push assembly code.
     */
    public Function<List<String>, String> pushToAsm = (List<String> args) -> {
        String pushType = args.get(0);
        String segment = args.get(1);
        int position = Integer.parseInt(args.get(2));
        String fileName = args.get(3);
        StringBuilder pushBuilder = new StringBuilder();
        pushBuilder.append(getSegmentTranslation(pushType, segment, position, fileName));
        pushBuilder.append(PUSH_VALUE_IN_D);
        return pushBuilder.toString();
    };

    /**
     * Function that reads in pop type, segment, position, and filename in an array and outputs pop assembly code.
     */
    public Function<List<String>, String> popToAsm = (List<String> args) -> {
        String popType = args.get(0);
        String segment = args.get(1);
        int position = Integer.parseInt(args.get(2));
        String fileName = args.get(3);
        StringBuilder popBuilder = new StringBuilder();
        popBuilder.append(getSegmentTranslation(popType, segment, position, fileName));
        if (!segment.equals(POINTER)) {
            if (!segment.equals(TEMP) && !segment.equals(STATIC)) {
                popBuilder.append("D=D+A" + System.lineSeparator());
            }
            popBuilder.append("@R13" + System.lineSeparator());
            popBuilder.append("M=D" + System.lineSeparator());
            popBuilder.append("@SP" + System.lineSeparator());
            popBuilder.append("AM=M-1" + System.lineSeparator());
            popBuilder.append("D=M" + System.lineSeparator());
            popBuilder.append("@R13" + System.lineSeparator());
            popBuilder.append("A=M" + System.lineSeparator());
            popBuilder.append("M=D");
        }
        return popBuilder.toString();
    };

    /**
     * Function that reads in function vm instructions and outputs function assembly code
     */
    public Function<List<String>, String> functionToAsm = (List<String> args) -> {
        String currentFunctionName = args.get(1);
        Integer argCount = Integer.parseInt(args.get(2));
        StringBuilder functionBuilder = new StringBuilder();
            functionBuilder.append("(" + currentFunctionName + ")");
        for (int i = 0; i < argCount; i++) {
            functionBuilder.append(System.lineSeparator());
            functionBuilder.append("D=0" + System.lineSeparator());
            functionBuilder.append(PUSH_VALUE_IN_D);
        }
        return functionBuilder.toString();
    };

    /**
     * Function that reads in call vm instructions and outputs call assembly code
     */
    public Function<List<String>, String> callToAsm = (List<String> args) -> {
        returnAddrCounter++;
        String functionName = args.get(1);
        Integer argCount = Integer.parseInt(args.get(2));
        String filename = args.get(3);
        int argPosToMoveBack = argCount + 5;
        StringBuilder callBuilder = new StringBuilder();
        // first push current stuff
        callBuilder.append("@returnAddr" + returnAddrCounter + System.lineSeparator());
        callBuilder.append("D=A" + System.lineSeparator());
        callBuilder.append(PUSH_VALUE_IN_D + System.lineSeparator());
        callBuilder.append("@LCL" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append(PUSH_VALUE_IN_D + System.lineSeparator());
        callBuilder.append("@ARG" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append(PUSH_VALUE_IN_D + System.lineSeparator());
        callBuilder.append("@THIS" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append(PUSH_VALUE_IN_D + System.lineSeparator());
        callBuilder.append("@THAT" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append(PUSH_VALUE_IN_D + System.lineSeparator());

        callBuilder.append("@SP" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append("@" + argPosToMoveBack + System.lineSeparator());
        callBuilder.append("D=D-A" + System.lineSeparator());
        callBuilder.append("@ARG" + System.lineSeparator());
        callBuilder.append("M=D" + System.lineSeparator());
        callBuilder.append("@SP" + System.lineSeparator());
        callBuilder.append("D=M" + System.lineSeparator());
        callBuilder.append("@LCL" + System.lineSeparator());
        callBuilder.append("M=D" + System.lineSeparator());
        callBuilder.append("@" + functionName + System.lineSeparator());
        callBuilder.append("0;JMP" + System.lineSeparator());
        callBuilder.append("(returnAddr" + returnAddrCounter + ")");
        return callBuilder.toString();
    };

    /**
     * Function that returns static "return" assembly code
     */
    public String getReturnAsm() {
        StringBuilder returnBuilder = new StringBuilder();
        returnBuilder.append("@LCL" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        commonReturnForFrame(returnBuilder, "5");
        commonReturnMemory(returnBuilder, "RET");
        returnBuilder.append("@RET" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // pop()
        String[] popArg = {"pop", "argument", "0", ""};
        returnBuilder.append(popToAsm.apply(Arrays.asList(popArg)) + System.lineSeparator());
        // SP = ARG + 1. Assumes we still have the address for ARG
        returnBuilder.append("D=A" + System.lineSeparator());
        returnBuilder.append("@SP" + System.lineSeparator());
        returnBuilder.append("M=D+1" + System.lineSeparator());
        // THAT = *(FRAME - 1)
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("A=M-1" + System.lineSeparator());
        commonReturnMemory(returnBuilder, "THAT");
        // THIS = *(FRAME - 2)
        commonReturnForFrame(returnBuilder, "2");
        commonReturnMemory(returnBuilder, "THIS");
        // ARG = *(FRAME - 3)
        commonReturnForFrame(returnBuilder, "3");
        commonReturnMemory(returnBuilder, "ARG");
        // LCL = *(FRAME - 4)
        commonReturnForFrame(returnBuilder, "4");
        commonReturnMemory(returnBuilder, "LCL");

        //
        returnBuilder.append("@RET" + System.lineSeparator());
        returnBuilder.append("A=M" + System.lineSeparator());
        returnBuilder.append("0;JMP");
        return returnBuilder.toString();
    }

    /**
     * Helper method to return common 4 lines for FRAME variable return processing to increment up the stack
     * @param returnBuilder string builder for the return assembly code
     * @param frameNum number of addresses for FRAME to move up stack
     * @return string builder for the return assembly code
     */
    private StringBuilder commonReturnForFrame(StringBuilder returnBuilder, String frameNum) {
        returnBuilder.append("@" + frameNum + System.lineSeparator());
        returnBuilder.append("D=A" + System.lineSeparator());
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("A=M-D" + System.lineSeparator());
        return returnBuilder;
    }

    /**
     * Helper method to return common 3 lines for getting an value stored in memory, going to a given location in memory,
     * and storing the value at that location.
     * @param returnBuilder string builder for return assembly code
     * @param memLocation location in memory
     * @return string builder for the return assembly code
     */
    private StringBuilder commonReturnMemory(StringBuilder returnBuilder, String memLocation) {
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@" + memLocation + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        return returnBuilder;
    }

    /**
     * Function that reads in a label name and return label assembly code
     */
    public Function<String, String> labelAsm = (String label) -> "(" + label + ")";

    /**
     * Function that reads in destination for goto and return goto assembly code
     */
    public Function<String, String> goToAsm = (String destination) ->
            "@" + destination + System.lineSeparator() +
            "0;JMP";

    /**
     * Function that reads in if-goto destination and returns if-goto assembly code
     */
    public Function<String, String> ifGoToAsm = (String destination) ->
            MOVE_SP_UP_STORE_IN_D +
            "@" + destination + System.lineSeparator() +
            "D;JNE";

    /*
        Standard assembly code that is shared in LT, EQ, and GT commands
     */
    private String getLTEQGT(String jump, Integer counter) {
        StringBuilder builder = new StringBuilder();
        builder.append(GET_TWO_ON_STACK);
        builder.append("D=M-D" + System.lineSeparator());
        builder.append("@TRUE" + counter + System.lineSeparator());
        builder.append("D;J" + jump + System.lineSeparator());
        builder.append("@SP" + System.lineSeparator());
        builder.append("A=M-1" + System.lineSeparator());
        builder.append("M=0" + System.lineSeparator());
        builder.append("@CONTINUE" + counter + System.lineSeparator());
        builder.append("0;JMP" + System.lineSeparator());
        builder.append("(TRUE" + counter + ")" + System.lineSeparator());
        builder.append("@SP" + System.lineSeparator());
        builder.append("A=M-1" + System.lineSeparator());
        builder.append("M=-1" + System.lineSeparator());
        builder.append("(CONTINUE" + counter + ")");
        return builder.toString();

    }

    /*
        Translates segment to assembly code
     */
    private String getSegmentTranslation(String type, String segment, int position, String fileName) {
        String segmentAsm = "";
        switch (segment) {
            case CONSTANT:
                segmentAsm = "@" + position + System.lineSeparator() +
                             "D=A" + System.lineSeparator();
                break;
            case LOCAL:
                segmentAsm = generatePushPopStart(type, "LCL", position);
                break;
            case ARGUMENT:
                segmentAsm = generatePushPopStart(type, "ARG", position);
                break;
            case STATIC:
                if (type.equals("pop")) {
                    segmentAsm = "@" + fileName + "." + position + System.lineSeparator() +
                            "D=A" + System.lineSeparator();
                } else {
                    segmentAsm = "@" + fileName + "." + position + System.lineSeparator() +
                            "D=M" + System.lineSeparator();
                }
                break;
            case TEMP:
                int tempPosition = 5 + position;
                segmentAsm = "@" + tempPosition + System.lineSeparator() +
                             "D=A" + System.lineSeparator();
                break;
            case THIS:
                segmentAsm = generatePushPopStart(type, "THIS", position);
                break;
            case THAT:
                segmentAsm = generatePushPopStart(type, "THAT", position);
                break;
            case POINTER:

                // TODO: should clean this up, confusing as is
                if (position == 0) {
                    if (type.equals("pop")) {
                        segmentAsm = MOVE_SP_UP_STORE_IN_D + "@THIS" + System.lineSeparator() +
                                "M=D";
                    } else {
                        segmentAsm = "@THIS" + System.lineSeparator() +
                                "D=M" + System.lineSeparator();
                    }
                } else {
                    if (type.equals("pop")) {
                        segmentAsm = MOVE_SP_UP_STORE_IN_D + "@THAT" + System.lineSeparator() +
                                "M=D";
                    } else {
                        segmentAsm = "@THAT" + System.lineSeparator() +
                                "D=M" + System.lineSeparator();
                    }
                }
                break;
        }
        return segmentAsm;
    }

    /*
        Generated code that is similar between segments, differs on push and pop
     */
    private String generatePushPopStart(String type, String asmSeg, int position) {
        if (type.equals("push")) {
            return "@" + asmSeg + System.lineSeparator() +
                    "D=M" + System.lineSeparator() +
                    "@" + position + System.lineSeparator() +
                    "A=D+A" + System.lineSeparator() +
                    "D=M" + System.lineSeparator();
        } else {
            return "@" + asmSeg + System.lineSeparator() +
                    "D=M" + System.lineSeparator() +
                    "@" + position + System.lineSeparator();
        }
    }
}
