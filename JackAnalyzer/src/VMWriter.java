import java.io.*;

public class VMWriter {
    private final PrintWriter writer;

    public VMWriter(String fileName) {
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".vm")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writePush(String segment, int index) {
        segment = getSegment(segment);
        if (segment.equals("constant") && index < 0) {
            writer.println("push constant " + -index);
            writer.println("neg");
        } else {
            writer.println("push " + segment.toLowerCase() + " " + index);
        }
    }

    private String getSegment(String segment) {
        switch (segment) {
            case "VAR" -> segment = "local";
            case "ARG" -> segment = "argument";
            case "FIELD" -> segment = "this";
            case "STATIC" -> segment = "static";
            default -> segment = segment.toLowerCase();
        }
        return segment;
    }

    public void writePop(String segment, int index) {
        segment = getSegment(segment);
        writer.println("pop " + segment + " " + index);
    }

    public void writeArithmetic(String command) {
        switch(command){
            case "multiply","divide","mod" -> writeCall("Math." + command, 2);
            default -> writer.println(command.toLowerCase());
        }
    }

    public void writeLabel(String label) {
        writer.println("label " + label);
    }

    public void writeGoto(String label) {
        writer.println("goto " + label);
    }

    public void writeIf(String label) {
        writer.println("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        writer.println("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nVars) {
        writer.println("function " + name + " " + nVars);
    }

    public void writeReturn() {
        writer.println("return");
    }

    public void close() {
        writer.close();
    }
}
