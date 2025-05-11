import java.util.Arrays;

public class Code {

    public static String dest(String dest){
        dest = dest == null?"null":dest;
        return switch (dest) {
            case "null" -> "000";
            case "M" -> "001";
            case "D" -> "010";
            case "DM", "MD" -> "011";
            case "A" -> "100";
            case "AM", "MA" -> "101";
            case "AD", "DA" -> "110";
            default -> "111";
        };
    }

    public static String comp(String comp){
        String a = comp.contains("M")?"1":"0";
        comp = comp.replace("M","A");

        String c = switch(comp){
           case "1" -> "111111";
           case "-1" -> "111010";
           case "D" -> "001100";
           case "A" -> "110000";
           case "!D" -> "001101";
           case "!A" -> "110001";
           case "-D" -> "001111";
           case "-A" -> "110011";
           case "D+1" -> "011111";
           case "A+1" -> "110111";
           case "D-1" -> "001110";
           case "A-1" -> "110010";
           case "D+A" -> "000010";
           case "D-A" -> "010011";
           case "A-D" -> "000111";
           case "D&A" -> "000000";
           case "D|A" -> "010101";
            default -> "101010";
       };
       return a+c;
    }

    public static String jump(String jump){
        jump = jump == null?"null":jump;
        return switch(jump){
            case "JGT" -> "001";
            case "JEQ" -> "010";
            case "JGE" -> "011";
            case "JLT" -> "100";
            case "JNE" -> "101";
            case "JLE" -> "110";
            case "JMP" -> "111";
            default -> "000";
        };
    }
}
