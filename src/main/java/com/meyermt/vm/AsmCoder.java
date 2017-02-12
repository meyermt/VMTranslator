package com.meyermt.vm;

import java.util.List;
import java.util.function.Function;

/**
 * Writes assembly code from VM instructions.
 * Created by michaelmeyer on 2/3/17.
 */
public class AsmCoder {

    // shared asm translation
    private static final String MOVE_SP_UP =
            "@SP" + System.lineSeparator() +
            "AM=M-1" + System.lineSeparator() +
            "D=M" + System.lineSeparator()
    private static final String ADD_SUB_GT_LT_EQ_SHARED_START =
            MOVE_SP_UP +
            "A=A-1" + System.lineSeparator();
    private static final String ADD_END =
            "M=M+D";
    private static final String SUB_END =
            "M=M-D";
    private static final String NEG_ASM =
            "@SP" + System.lineSeparator() +
            "A=M-1" + System.lineSeparator() +
            "M=-M";
    private static final String NOT_ASM =
            "@SP" + System.lineSeparator() +
            "A=M-1" + System.lineSeparator() +
            "M=!M";
    private static final String AND_END =
            "M=M&D";
    private static final String OR_END =
            "M=M|D";

    private static final String ADD_ASM = ADD_SUB_GT_LT_EQ_SHARED_START + ADD_END;
    private static final String SUB_ASM = ADD_SUB_GT_LT_EQ_SHARED_START + SUB_END;
    private static final String AND_ASM = ADD_SUB_GT_LT_EQ_SHARED_START + AND_END;
    private static final String OR_ASM = ADD_SUB_GT_LT_EQ_SHARED_START + OR_END;

    // the segments
    private final static String ARGUMENT = "argument", LOCAL = "local", STATIC = "static", CONSTANT = "constant",
            THIS = "this", THAT = "that", POINTER = "pointer", TEMP = "temp";

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
        pushBuilder.append("@SP" + System.lineSeparator());
        pushBuilder.append("A=M" + System.lineSeparator());
        pushBuilder.append("M=D" + System.lineSeparator());
        pushBuilder.append("@SP" + System.lineSeparator());
        pushBuilder.append("M=M+1");
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
        popBuilder.append("D=D+A" + System.lineSeparator());
        popBuilder.append("@R13" + System.lineSeparator());
        popBuilder.append("M=D" + System.lineSeparator());
        popBuilder.append("@SP" + System.lineSeparator());
        popBuilder.append("AM=M-1" + System.lineSeparator());
        popBuilder.append("D=M" + System.lineSeparator());
        popBuilder.append("@R13" + System.lineSeparator());
        popBuilder.append("A=M" + System.lineSeparator());
        popBuilder.append("M=D");
        return popBuilder.toString();
    };

    public Function<List<String>, String> functionToAsm = (List<String> args) -> {
        String functionName = args.get(1);
        String argCount = args.get(2);

    }

    public Function<List<String>, String> callToAsm = (List<String> args) -> {
        String functionName = args.get(1);
        String argCount = args.get(2);
        StringBuilder callBuilder = new StringBuilder();
        // first save current stuff
        callBuilder.append("@")
    };

    public String getReturnAsm() {
        StringBuilder returnBuilder = new StringBuilder();
        returnBuilder.append("@LCL" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        returnBuilder.append("@5");
        returnBuilder.append("A=D-A");
        // TODO: May need to revisit this next code.
        returnBuilder.append("@RET" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // pop()
        returnBuilder.append("@ARG" + System.lineSeparator());
        returnBuilder.append("A=M" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // SP = ARG + 1
        returnBuilder.append("@SP" + System.lineSeparator());
        returnBuilder.append("M=D+1" + System.lineSeparator());
        // THAT = *(FRAME - 1)
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("A=M-1" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@THAT" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // THIS = *(FRAME - 2)
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@2" + System.lineSeparator());
        returnBuilder.append("A=D-A" + System.lineSeparator());
        returnBuilder.append("@THIS" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // ARG = *(FRAME - 3)
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@3" + System.lineSeparator());
        returnBuilder.append("A=D-A" + System.lineSeparator());
        returnBuilder.append("@ARG" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        // LCL = *(FRAME - 4)
        returnBuilder.append("@FRAME" + System.lineSeparator());
        returnBuilder.append("D=M" + System.lineSeparator());
        returnBuilder.append("@4" + System.lineSeparator());
        returnBuilder.append("A=D-A" + System.lineSeparator());
        returnBuilder.append("@LCL" + System.lineSeparator());
        returnBuilder.append("M=D" + System.lineSeparator());
        returnBuilder.append(this.goToAsm.apply("RET"));
    }

    public Function<String, String> labelAsm = (String label) -> "(" + label + ")";

    public Function<String, String> goToAsm = (String destination) ->
            "@" + destination + System.lineSeparator() +
            "0;JMP";

    public Function<String, String> ifGoToAsm = (String destination) ->
            MOVE_SP_UP +
            "@" + destination + System.lineSeparator() +
            "D;JNE";

    /*
        Standard assembly code that is shared in LT, EQ, and GT commands
     */
    private String getLTEQGT(String jump, Integer counter) {
        StringBuilder builder = new StringBuilder();
        builder.append(ADD_SUB_GT_LT_EQ_SHARED_START);
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
                segmentAsm = generatePushPopStart(type, fileName + "." + position, position);
                break;
            case TEMP:
                int tempPosition = 5 + position;
                segmentAsm = "@" + tempPosition + System.lineSeparator() +
                             "D=M" + System.lineSeparator();
                break;
            case THIS:
                segmentAsm = generatePushPopStart(type, "THIS", position);
                break;
            case THAT:
                segmentAsm = generatePushPopStart(type, "THAT", position);
                break;
            case POINTER:
                if (position == 0) {
                    segmentAsm = "@THIS" + System.lineSeparator() +
                                 "D=M" + System.lineSeparator();
                } else {
                    segmentAsm = "@THAT" + System.lineSeparator() +
                                 "D=M" + System.lineSeparator();
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
