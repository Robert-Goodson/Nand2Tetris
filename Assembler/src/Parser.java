import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Parser {
    private Scanner sc;
    private String instruction;
    private final String A = "A_INSTRUCTION";
    private final String C = "C_INSTRUCTION";
    private final String L = "L_INSTRUCTION";

    public Parser(String fileName) {
        try {
            this.sc = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMoreLines() {
        return sc.hasNextLine();
    }

    public void advance() {
        String line = sc.nextLine().trim();
        if (line.startsWith("//") || line.isEmpty()) {
            if(sc.hasNextLine()) this.advance();
        } else {
            this.instruction = line;
        }
    }

    public String getInstruction(){
        return instruction;
    }
    public String instructionType() {
        if (instruction.startsWith("@")) {
            return A;
        } else if (instruction.startsWith("(") && instruction.endsWith(")")) {
            return L;
        } else {
            return C;
        }
    }

    public String symbol() {
        if (instructionType().equals(A)) {
            return instruction.substring(1);

        } else {
            return instruction.substring(1, instruction.length() - 1);
        }
    }

    public String dest() {
        return instruction.contains("=")?instruction.substring(0, instruction.indexOf('=')):null;
    }

    public String comp() {
        if (this.dest() != null){
            if(this.jump()!= null){
                return instruction.substring(instruction.indexOf("=")+1,instruction.indexOf(";"));
            } else {
                return instruction.substring(instruction.indexOf("=")+1);
            }
        } else {
            if(this.jump()!=null){
                return instruction.substring(0,instruction.indexOf(";"));
            } else {
                return instruction;
            }
        }
    }

        public String jump() {
            return instruction.contains(";")?instruction.substring(instruction.indexOf(';') + 1):null;
        }

        public void end() {
            sc.close();
        }
    }