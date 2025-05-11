import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class Main {
    public static void main(String[] args) {
        SymbolTable symbolTable = new SymbolTable();
        String fileName = args[0].substring(0, args[0].indexOf("."));
        Parser parser1 = new Parser(args[0]);
        int lineCount = 0;

        //first pass
        while (parser1.hasMoreLines()) {
            parser1.advance();
            if (parser1.instructionType().equals("L_INSTRUCTION")) {
                symbolTable.addEntry(parser1.symbol(), lineCount);
            } else {
                lineCount++;
            }
        }
        System.out.println(lineCount);
        parser1.end();
        //second pass
        Parser parser2 = new Parser(args[0]);
        int variable = 16;


        try (PrintWriter writer = new PrintWriter(new File(fileName+".hack"))){
            while (parser2.hasMoreLines()) {
                parser2.advance();
                switch (parser2.instructionType()) {
                    case "A_INSTRUCTION":
                        String symbol = parser2.symbol();
                        if (!symbol.matches("\\d+")) {
                            if (!symbolTable.contains(symbol)) {
                                symbolTable.addEntry(symbol, variable++);
                            }
                            writer.println(String.format("%16s",Integer.toBinaryString(symbolTable.getAddress(symbol))).replace(" ", "0"));
                        } else {
                            writer.println(String.format("%16s",Integer.toBinaryString(Integer.parseInt(symbol))).replace(" ","0"));
                        }
                        break;
                    case "C_INSTRUCTION":
                        System.out.println(parser2.comp() + " " + parser2.dest() + " " + parser2.jump());
                        writer.println("111" + Code.comp(parser2.comp()) + Code.dest(parser2.dest()) + Code.jump(parser2.jump()));
                        break;
                    default:
                        break;
                }

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
