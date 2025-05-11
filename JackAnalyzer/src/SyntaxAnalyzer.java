import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class SyntaxAnalyzer {
    private final JackTokenizer tokenizer;
    private final PrintWriter writer;
    private int numExpressions = 0;
    private boolean moreExpressions = true;
    private boolean firstPass = true;
    private String fileName;

    public SyntaxAnalyzer(File file) throws IOException {
        this.tokenizer = new JackTokenizer(file);
        writer = new PrintWriter(tokenizer.getFileName() + ".xml");
        this.tokenizer.advance();
        fileName = file.getName().substring(0,file.getName().indexOf('.'));
    }

    public void compileClass() {
        if (tokenizer.keyword().equals("class")) {
            writer.println("<class>");
            writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            if (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyword().equals("field") || tokenizer.keyword().equals("static")) {
                this.compileClassVarDec();
            }
            compileSubroutine();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            writer.println("</class>");
        }
    }

    public String getName(){
        return fileName;
    }

    public void compileClassVarDec() {
        if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ';') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            writer.println("</classVarDec>");
            if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("static") || tokenizer.keyword().equals("field"))) {
                compileClassVarDec();
            }
        } else {
            if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("field") || tokenizer.keyword().equals("static"))) {
                writer.println("<classVarDec>");
                writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
                tokenizer.advance();
                if (tokenizer.tokenType().equals("KEYWORD")) {
                    writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
                    tokenizer.advance();
                } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
                    writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                    tokenizer.advance();
                }
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                tokenizer.advance();
                if (tokenizer.symbol() == ',') {
                    writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    tokenizer.advance();
                    compileClassVarDec();
                } else {
                    compileClassVarDec();
                }
            } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                tokenizer.advance();
                if (tokenizer.symbol() == ',') {
                    writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    tokenizer.advance();
                }
                compileClassVarDec();
            }
        }
    }

    public void compileSubroutine() {
        if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("constructor") ||
                tokenizer.keyword().equals("method") || tokenizer.keyword().equals("function"))) {
            writer.println("<subroutineDec>");
            writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            tokenizer.advance();
            if (tokenizer.tokenType().equals("KEYWORD")) {
                writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            } else {
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            }
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            this.compileParameterList();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            compileSubroutineBody();
            writer.println("</subroutineDec>");
            if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("constructor") ||
                    tokenizer.keyword().equals("method") || tokenizer.keyword().equals("function"))) {
                compileSubroutine();
            }
        }
    }

    public void compileParameterList() {
        if (firstPass) {
            firstPass = false;
            writer.println("<parameterList>");
        }
        if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ')') {
            writer.println("</parameterList>");
            firstPass = true;
        } else {
            if (tokenizer.tokenType().equals("KEYWORD")) {
                writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            } else {
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            }
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
            if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
            }
            compileParameterList();
        }
    }

    public void compileSubroutineBody() {
        writer.println("<subroutineBody>");
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        this.compileVarDec();
        this.compileStatements();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("</subroutineBody>");
    }

    public void compileVarDec() {
        if(tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyword().equals("var")) {
            writer.println("<varDec>");
            writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            tokenizer.advance();
            if(tokenizer.tokenType().equals("KEYWORD")){
                writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            } else {
                writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            }
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
            compileVarDec();
        } else if (tokenizer.tokenType().equals("SYMBOL")){
            if(tokenizer.symbol() == ',') {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
                if(tokenizer.tokenType().equals("KEYWORD")){
                    writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
                } else {
                    writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                }
                tokenizer.advance();
                compileVarDec();
            } else {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
                writer.println("</varDec>");
                if(tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyword().equals("var")){
                    compileVarDec();
                }
            }
        }
    }
    
    public void compileStatements() {
        writer.println("<statements>");
        while (tokenizer.tokenType().equals("KEYWORD")) {
            if (tokenizer.tokenType().equals("KEYWORD")) {
                while (tokenizer.tokenType().equals("KEYWORD")) {
                    switch (tokenizer.keyword()) {
                        case "let" -> compileLet();
                        case "if" -> compileIf();
                        case "while" -> compileWhile();
                        case "do" -> compileDo();
                        case "return" -> compileReturn();
                        default -> throw new RuntimeException("Invalid token");
                    }
                }
            } else {
                throw new RuntimeException("Invalid token");
            }
        }
        writer.println("</statements>");
    }

    public void compileLet() {
        writer.println("<letStatement>");
        writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        tokenizer.advance();
        if (tokenizer.symbol() == '[') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            this.compileExpression();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
        }
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        this.compileExpression();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("</letStatement>");
    }

    public void compileIf() {
        writer.println("<ifStatement>");
        writeIfWhile();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        if (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyword().equals("else")) {
            writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            tokenizer.advance();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            compileStatements();
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
        }

        writer.println("</ifStatement>");
    }

    public void compileWhile() {
        writer.println("<whileStatement>");
        writeIfWhile();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("</whileStatement>");
    }

    public void compileDo() {
        writer.println("<doStatement>");
        writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        tokenizer.advance();
        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
        tokenizer.advance();
        if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '.') {
            writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
            tokenizer.advance();
            writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
            tokenizer.advance();
        }
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        this.compileExpressionList();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("</doStatement>");
    }

    public void compileReturn() {
        writer.println("<returnStatement>");
        writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        tokenizer.advance();
        if (!tokenizer.tokenType().equals("SYMBOL") || tokenizer.symbol() != ';') {
            this.compileExpression();
        }
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("</returnStatement>");
    }

    public void compileExpression() {
            writer.println("<expression>");
            while (!tokenizer.tokenType().equals("SYMBOL") || tokenizer.symbol() != ',' && tokenizer.symbol() != ';' && tokenizer.symbol() != ')' && tokenizer.symbol() != ']') {
                compileTerm();
                if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() != ',' && tokenizer.symbol() != ']' && tokenizer.symbol() != ';' && tokenizer.symbol() != ')') {
                    switch (tokenizer.symbol()) {
                        case '<' -> writer.println("<symbol> &lt; </symbol>");
                        case '>' -> writer.println("<symbol> &gt; </symbol>");
                        case '"' -> writer.println("<symbol> &quot; </symbol>");
                        case '&' -> writer.println("<symbol> &amp; </symbol>");
                        default -> writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    }
                    tokenizer.advance();
                }
            }
            writer.println("</expression>");
            if (tokenizer.symbol() == ';' || tokenizer.symbol() == ')') {
                moreExpressions = false;
            }
    }

    public void compileTerm() {
        int temp = 0;
        writer.println("<term>");
        if (tokenizer.tokenType().equals("IDENTIFIER")) {
            String prevToken = tokenizer.identifier();
            tokenizer.advance();
            switch (tokenizer.symbol()) {
                case '[' -> {
                    writer.println("<identifier> " + prevToken + " </identifier>");
                    writer.println("<symbol> [ </symbol>");
                    tokenizer.advance();
                    compileExpression();
                    writer.println("<symbol> ] </symbol>");
                    tokenizer.advance();
                }
                case '.', '(' -> {
                    writer.println("<identifier> " + prevToken + " </identifier>");
                    writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    if (tokenizer.symbol() == '.') {
                        tokenizer.advance();
                        writer.println("<identifier> " + tokenizer.identifier() + " </identifier>");
                        tokenizer.advance();
                        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    }
                    tokenizer.advance();
                    temp = compileExpressionList();
                    writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                    tokenizer.advance();
                }
                default -> writer.println("<identifier> " + prevToken + " </identifier>");
            }
        } else if (tokenizer.tokenType().equals("INT_CONST")) {
            writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
            tokenizer.advance();
        } else if (tokenizer.tokenType().equals("STRING_CONST")) {
            writer.println("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>");
            tokenizer.advance();
        } else if (tokenizer.tokenType().equals("KEYWORD")) {
            writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
            tokenizer.advance();
        } else if (tokenizer.tokenType().equals("SYMBOL")) {
            if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
                compileTerm();
            } else if (tokenizer.symbol() == '(') {
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
                compileExpression();
                writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                tokenizer.advance();
            }
        } else {
            throw new RuntimeException("Invalid token " + tokenizer.tokenType());
        }
        writer.println("</term>");
    }

    public int compileExpressionList() {
        moreExpressions = true;
        int numExpressions = 0;
        if (!tokenizer.tokenType().equals("SYMBOL") || tokenizer.symbol() != ')') {
            writer.println("<expressionList>");
            while (moreExpressions) {
                if (!tokenizer.tokenType().equals("SYMBOL") || tokenizer.symbol() != ')') {
                    compileExpression();
                    numExpressions++;
                    if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
                        moreExpressions = true;
                        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
                        tokenizer.advance();
                    }
                } else {
                    moreExpressions = false;
                }
            }
            writer.println("<nArgs> " + numExpressions + " </nArgs>");
            writer.println("</expressionList>");
        } else {
            writer.println("<expressionList>\n</expressionList>");
        }
        return numExpressions;
    }

    public void close() {
        tokenizer.close();
        writer.close();
    }

    private void writeIfWhile() {
        writer.println("<keyword> " + tokenizer.keyword() + " </keyword>");
        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        compileExpression();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        writer.println("<symbol> " + tokenizer.symbol() + " </symbol>");
        tokenizer.advance();
        compileStatements();

    }
}
