import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class JackTokenizer {
    private static final Set<String> keyWords = new HashSet<>(
            Arrays.asList("class","constructor","function","method","field",
            "static","var","int","char","boolean","void",
            "true","false","null","this","let",
            "do","if","else","while","return"));
    private static final String symbols = "{}()[].,;+-*%/&|<>=~";
    private final Scanner scanner;
    private String line;
    private String currentToken;
    private final String fileName;

    public JackTokenizer(File file) {
        if(!file.getName().endsWith(".jack")){
            throw new RuntimeException("Invalid file type");
        }
        try {
            this.scanner = new Scanner(file);
            this.line = scanner.nextLine().strip();
            fileName = file.getName().substring(0,file.getName().indexOf("."));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        scanner.close();
    }
    public boolean hasMoreTokens() {
        return (scanner.hasNext() || !line.isEmpty());
    }

    public void advance() {
        if (!hasMoreTokens()) {
            System.out.println("End of file");
        } else {
            if (line.isBlank()) {
                line = scanner.nextLine().strip();
                advance();
                return;
            }
            if (line.startsWith("//")) {
                line = scanner.nextLine().strip();
                advance();
            } else if (line.startsWith("/*")) {
                while (!line.contains("*/")) {
                    line = scanner.nextLine();
                }
                line = (line.length() > 2) ? line.substring(line.indexOf("*/") + 2) : "";
                advance();
            } else if (!line.isEmpty() && symbols.contains(String.valueOf(line.charAt(0)))) {
                currentToken = String.valueOf(line.charAt(0));
                line = line.substring(1);
            } else if (line.charAt(0)== '"') {
                line = line.substring(1);
                currentToken = line.substring(0,line.indexOf('"'));
                line = !line.endsWith("\"")?line.substring(line.indexOf('"')+1):"";
            } else {
                int i = 0;
                while (!symbols.contains(String.valueOf(line.charAt(i))) && line.charAt(i) != ' ') {
                    i++;
                }
                currentToken = line.substring(0, i);
                line = line.substring(i).strip();
            }
        }
        if (currentToken.isBlank()) {
            advance();
        }
    }

    public String tokenType(){
        String tokenType;
        if(keyWords.contains(currentToken)){
            tokenType = "KEYWORD";
        } else if(symbols.contains(currentToken)){
            tokenType = "SYMBOL";
        } else if(isInt(currentToken)){
            tokenType = "INT_CONST";
        } else if(validIdentifier(currentToken)) {
            tokenType = "IDENTIFIER";
        } else {
            tokenType="STRING_CONST";
        }
        return tokenType;
    }

    public String keyword(){
        if(tokenType().equals("KEYWORD")){
            return currentToken;
        } else {
            throw new RuntimeException("Improper token type for this function " + tokenType());
        }
    }

    public char symbol(){
        if(tokenType().equals("SYMBOL")){
            return currentToken.charAt(0);
        } else {
            throw new RuntimeException("Improper token type for this function " + tokenType());
        }
    }

    public String identifier(){
        if(tokenType().equals("IDENTIFIER")){
            return currentToken;
        } else {
            throw new RuntimeException("Improper token type for this function " + tokenType());
        }
    }

    public int intVal(){
        if(tokenType().equals("INT_CONST")){
            return Integer.parseInt(currentToken);
        } else {
            throw new RuntimeException("Improper token type for this function " + tokenType());
        }
    }

    public String stringVal(){
        if(tokenType().equals("STRING_CONST")){
            return currentToken;
        } else {
            throw new RuntimeException("Improper token type for this function " + tokenType());
        }
    }

    private boolean validIdentifier(String identifier){
        if(identifier.isEmpty()){
            return false;
        }
        if(Character.isDigit(identifier.charAt(0))){
            return false;
        }
        for(int i = 0; i < identifier.length(); i++){
            if(!Character.isLetterOrDigit(identifier.charAt(i)) && identifier.charAt(i) != '_'){
                return false;
            }
        }
        return true;
    }

    private boolean isInt(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false; // only '-' is not a number
                else continue;
            }
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public String getFileName(){
        return fileName;
    }
}
