import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CompilationEngine {
    private final VMWriter writer;
    private static final String[] arr = {"add", "sub", "multiply", "divide", "and", "or", "lt", "gt", "eq", "mod"};
    private static final List<String> list = Arrays.asList(arr);
    private static final Set<String> ops = new HashSet<>(list);
    private static final SymbolTable classTable = new SymbolTable();
    private static final SymbolTable subroutineTable = new SymbolTable();
    private static final Set<String> TAGS = new HashSet<>();  //xml tags of interest
    private final File file; //xml file to compile
    private final Scanner sc;
    private String type;
    private String kind;
    private String name;
    private String className;
    private String token;
    private int counter;
    private boolean pushThis;


    public CompilationEngine(File file) throws FileNotFoundException {

        this.file = file;
        sc = new Scanner(new File(file.getAbsolutePath()));
        token = sc.next();
        writer = new VMWriter(file.getName().substring(0, file.getName().indexOf(".")));
        //initialize TAGS, only runs first time constructor is called
        if (TAGS.isEmpty()) {
            TAGS.add("<class>");
            TAGS.add("<classVarDec>");
            TAGS.add("<parameterList>");
            TAGS.add("<subroutineDec>");
            TAGS.add("<varDec>");
            TAGS.add("<subroutineBody>");
            TAGS.add("<statements>");
            TAGS.add("<letStatement>");
            TAGS.add("<ifStatement>");
            TAGS.add("<doStatement>");
            TAGS.add("<whileStatement>");
            TAGS.add("<returnStatement>");
            TAGS.add("<expression>");
            TAGS.add("<term>");
            TAGS.add("<expressionList>");
        }
    }

    public void compileClass() {
        if (!token.equals("<class>")) {
            throw new RuntimeException("Expected <class> got " + token);
        }
        classTable.reset();
        className = advance(5);
        token = advance(5);
        while (token.equals("<classVarDec>")) {
            compileClassVarDec();
        }
        while (token.equals("<subroutineDec>")) {
            compileSubroutineDec();
            token = advance(1);
        }
        token = advance(3);
        if (!token.equals("</class>")) {
            throw new RuntimeException("Expected </class> got " + token);
        }
    }

    public void compileClassVarDec() {
        String kind, name;

        while (token.equals("<classVarDec>")) {
            kind = advance(2);
            type = advance(3);
            name = advance(3);
            classTable.define(name, type, kind.toUpperCase());
            token = advance(3);
            while (token.equals(",")) {
                name = advance(3);
                token = advance(3);
                classTable.define(name, type, kind.toUpperCase());
            }
            if (token.equals(";")) {
                token = advance(3);
            }
        }
    }

    public void compileSubroutineDec() {
        if (!token.equals("<subroutineDec>")) {
            throw new RuntimeException("Expected <subroutineDec> got " + token);
        }
        subroutineTable.reset();
        kind = advance(2);
        type = advance(3); //class name if constructor
        name = advance(3); //"new" if constructor
        if (kind.equals("method")) {
            subroutineTable.define("this", className, "ARG");
        }
        System.out.println(name);
        token = advance(5);
        compileParameterList();
        token = advance(4);
        compileSubroutineBody();

        token = sc.next();
        if (!token.equals("</subroutineDec>")) {
            throw new RuntimeException("Expected </subroutineDec> got " + token);
        }
    }

    private void compileParameterList() {
        String type, name;
        if (!token.equals("<parameterList>")) {
            throw new RuntimeException("Expected <parameterList> got " + token);
        }
        token = advance(1);
        while (token.equals("<keyword>") || token.equals("<identifier>")) {
            type = advance(1);
            name = advance(3);
            subroutineTable.define(name, type, "ARG");
            token = advance(2);
            if (token.equals("<symbol>")) {
                token = advance(3);
            }
        }
        if (!token.equals("</parameterList>")) {
            throw new RuntimeException("Expected </parameterList> got " + token);
        }
    }

    public void compileSubroutineBody() {
        if (!token.equals("<subroutineBody>")) {
            throw new RuntimeException("Expected <subroutineBody> instead encountered " + token);
        } else {
            token = advance(4);
            while (token.equals("<varDec>")) {
                compileVarDec();
                token = advance(1);
            }
            switch (kind) {
                case "function" -> writer.writeFunction(className + "." + name, subroutineTable.varCount("VAR"));
                case "constructor" -> {
                    writer.writeFunction(className + "." + name, subroutineTable.varCount("VAR"));
                    writer.writePush("constant", classTable.varCount("FIELD"));
                    writer.writeCall("Memory.alloc", 1);
                    writer.writePop("pointer", 0);
                }
                case "method" -> {
                    writer.writeFunction(className + "." + name, subroutineTable.varCount("VAR"));
                    writer.writePush("argument", 0);
                    writer.writePop("pointer", 0);
                }
            }
            if (type.equals("method")) {
                writer.writePush("argument", 0);
                writer.writePop("pointer", 0);
            }
            compileStatements();
            token = advance(4);
            if (!token.equals("</subroutineBody>")) {
                throw new RuntimeException("Expected </subroutineBody> got " + token);
            }
        }
    }

    public void compileVarDec() {
        String type, name;
        if (!token.equals("<varDec>")) {
            throw new RuntimeException("Expected <varDec> got " + token);
        }
        type = advance(5);
        name = advance(3);
        subroutineTable.define(name, type, "VAR");
        token = advance(3);
        while (token.equals(",")) {
            name = advance(3);
            subroutineTable.define(name, type, "VAR");
            token = advance(3);
        }
        token = advance(2);
        if (!token.equals("</varDec>")) {
            throw new RuntimeException("Expected </varDec> got " + token);
        }
    }

    public void compileStatements() {
        if (!token.equals("<statements>")) {
            throw new RuntimeException("Expected <statements> instead encountered " + token);
        } else {
            token = sc.next();
            while (!token.equals("</statements>")) {
                switch (token) {
                    case "<letStatement>" -> compileLetStatement();
                    case "<doStatement>" -> compileDoStatement();
                    case "<ifStatement>" -> compileIfStatement();
                    case "<whileStatement>" -> compileWhileStatement();
                    case "<returnStatement>" -> compileReturnStatement();
                }
                token = sc.next();
            }
        }
    }

    private void compileLetStatement() {
        boolean array = false;
        String kind;
        int index;
        if (!token.equals("<letStatement>")) {
            throw new RuntimeException("Expected <letStatement> got " + token);
        }
        token = advance(5);
        name = token;
        kind = (subroutineTable.kindOf(name).equals("NONE")) ? classTable.kindOf(name) : subroutineTable.kindOf(name);
        index = subroutineTable.indexOf(name) < 0 ? classTable.indexOf(name) : subroutineTable.indexOf(name);
        token = advance(3);
        if (token.equals("[")) {
            array = true;
            token = advance(2);
            writer.writePush(kind, index);
            compileExpression();
            writer.writeArithmetic("add");
            token = advance(7);
        } else {
            token = advance(2);
        }
        compileExpression();
        if (array) {
            writer.writePop("temp", 0);
            writer.writePop("pointer", 1);
            writer.writePush("temp", 0);
            writer.writePop("that", 0);
        } else {
            writer.writePop(kind, index);
        }
        token = advance(4);
        if (!token.equals("</letStatement>")) {
            throw new RuntimeException("Expected </letStatement> got " + token);
        }
    }

    private void compileDoStatement() {
        boolean method = false;
        String name, kind = "";
        int index = -1;
        if (!token.equals("<doStatement>")) {
            throw new RuntimeException("Expected <doStatement> instead encountered " + token);
        } else {
            name = advance(5);
            String obj;
            if (!subroutineTable.kindOf(name).equals("NONE")) {
                obj = name;
                name = subroutineTable.typeOf(name);
                kind = subroutineTable.kindOf(obj);
                index = subroutineTable.indexOf(obj);
                method = true;
            } else if (!classTable.kindOf(name).equals("NONE")) {
                obj = name;
                name = classTable.typeOf(name);
                kind = classTable.kindOf(obj);
                index = classTable.indexOf(obj);
                method = true;
            }
            token = advance(3);
            if (token.equals(".")) {
                name += token;
                name += advance(3);
                token = advance(5);
            } else {
                name = className + "." + name;
                method = true;
                token = advance(2);
                kind = "pointer";
                index = 0;
            }
            if (method) writer.writePush(kind, index);
            int args = compileExpressionList();
            if (method) {
                writer.writeCall(name, args + 1);
            } else {
                writer.writeCall(name, args);
            }
            writer.writePop("temp", 0);
            token = advance(7);
            if (!token.equals("</doStatement>")) {
                throw new RuntimeException("Expected </doStatement> got " + token);
            }
        }
    }

    private void compileIfStatement() {
        String ifLabel, endLabel;
        ifLabel = "FALSE$" + counter;
        endLabel = "END$" + counter++;
        token = advance(7);
        compileExpression();
        writer.writeArithmetic("not");
        writer.writeIf(ifLabel);
        token = advance(7);
        compileStatements();
        token = advance(4);
        writer.writeGoto(endLabel);
        writer.writeLabel(ifLabel);
        if (token.equals("<keyword>")) {
            token = advance(6);
            compileStatements();
            token = advance(4);
        }
        writer.writeLabel(endLabel);
    }

    private void compileWhileStatement() {
        String loop, end;
        loop = "LOOP$" + counter;
        end = "END$" + counter++;
        token = advance(7);
        writer.writeLabel(loop);
        compileExpression();
        writer.writeArithmetic("not");
        writer.writeIf(end);
        token = advance(7);
        compileStatements();
        writer.writeGoto(loop);
        writer.writeLabel(end);
        token = advance(4);
        if (!token.equals("</whileStatement>")) {
            throw new RuntimeException("Expected </whileStatement> got " + token);
        }
    }

    private void compileReturnStatement() {
        if (!token.equals("<returnStatement>")) {
            throw new RuntimeException("Expected <returnStatement> got " + token);
        }
        if (type.equals("void")) {
            writer.writePush("constant", 0);
            writer.writeReturn();
            token = advance(7);
        } else {
            token = advance(4);
            if (token.equals("<symbol>")) {
                advance(1);
                compileExpression();
            } else {
                compileExpression();
            }
            token = advance(4);
            writer.writeReturn();
        }
        if (!token.equals("</returnStatement>")) {
            throw new RuntimeException("Expected </returnStatement> got " + token);
        }
    }

    private int compileExpressionList() {
        if (!token.equals("<expressionList>")) {
            throw new RuntimeException("Expected <expressionList> got " + token);
        }
        int nArgs = 0;
        token = advance(1);
        while (token.equals("<expression>")) {
            compileExpression();
            token = advance(1);
            if (token.equals("<nArgs>")) {
                nArgs = Integer.parseInt(sc.next());
                token = advance(2);
            } else {
                token = advance(3);
            }
        }
        if (!token.equals("</expressionList>")) {
            throw new RuntimeException("Expected </expressionList> got " + token);
        }
        return nArgs;
    }

    private void compileExpression() {
        String op;
        if (!token.equals("<expression>")) {
            throw new RuntimeException("Expected <expression> got " + token);
        }
        token = sc.next();
        while (!token.equals("</expression>")) {
            compileTerm();
            token = sc.next();
            if (!token.equals("</expression>")) {
                op = sc.next();
                op = switch (op) {
                    case "&gt;" -> "gt";
                    case "&lt;" -> "lt";
                    case "=" -> "eq";
                    case "&amp;" -> "and";
                    case "|" -> "or";
                    case "~" -> "not";
                    case "+" -> "add";
                    case "-" -> "sub";
                    case "*" -> "multiply";
                    case "/" -> "divide";
                    case "%" -> "mod";
                    default -> op;
                };
                if (op.equals("(")) {
                    token = advance(2);
                    compileExpression();
                } else if (ops.contains(op)) {
                    token = advance(2);
                    compileTerm();
                    token = sc.next();
                    writer.writeArithmetic(op);
                }
            } else if (pushThis) {
                writer.writePush("pointer", 0);
                pushThis = false;
            }
        }
    }

    private void compileTerm() {
        String op, name, kind, type;
        int index;
        if (!token.equals("<term>")) {
            throw new RuntimeException("Expected <term> got " + token);
        }
        token = sc.next();
        switch (token) {
            case "<integerConstant>" -> {
                writer.writePush("constant", Integer.parseInt(sc.next()));
                token = advance(2);
            }
            case "<stringConstant>" -> {
                int length;
                token = sc.nextLine();
                token = token.substring(1, token.length() - 18);
                length = token.length();
                writer.writePush("constant", length);
                writer.writeCall("String.new", 1);
                writer.writePop("temp", 0);
                for (int i = 0; i < token.length(); i++) {
                    char c = token.charAt(i);
                    writer.writePush("temp", 0);
                    writer.writePush("constant", c);
                    writer.writeCall("String.appendChar", 2);
                    if (i + 1 == token.length()) break;
                    writer.writePop("temp", 0);
                }

                token = advance(1);
            }
            case "<symbol>" -> {
                token = sc.next();
                token = switch (token) {
                    case "-" -> "neg";
                    case "~" -> "not";
                    default -> token;
                };
                if (token.equals("(")) {
                    token = advance(2);
                    compileExpression();
                    token = advance(4);
                } else {
                    op = token;
                    token = advance(2);
                    compileTerm();
                    writer.writeArithmetic(op);
                    token = advance(1);
                }
            }
            case "<identifier>" -> {
                token = advance(1);
                identAndThis();
            }
            case "<keyword>" -> {
                token = advance(1);
                name = token;
                switch (token) {
                    case "true" -> {
                        writer.writePush("constant", 0);
                        writer.writeArithmetic("not");
                    }
                    case "this" -> identAndThis();
                    case "false", "null" -> writer.writePush("constant", 0);
                }
                if(!name.equals("this")) {
                    token = advance(2);
                }
            }
        }
        if (!token.equals("</term>")) {
            throw new RuntimeException("Expected </term> got " + token);
        }
    }

    private String advance(int skip) {
        for (int i = 0; i < skip - 1; i++) {
            if (!sc.hasNext()) {
                throw new RuntimeException("Expected to advance " + skip + "reached EOF after " + (i + 1));
            }
            sc.next();
        }
        return sc.next();
    }

    private void identAndThis(){
        int index;
        boolean object = false;
        String kind, name, type;
        name = token;
        if (!subroutineTable.kindOf(name).equals("NONE") || !classTable.kindOf(name).equals("NONE")) {
            if (subroutineTable.kindOf(name).equals("NONE")) {
                kind = classTable.kindOf(name);
                index = classTable.indexOf(name);
                type = classTable.typeOf(name);
            } else {
                kind = subroutineTable.kindOf(name);
                index = subroutineTable.indexOf(name);
                type = subroutineTable.typeOf(name);
            }
            object = true;
        } else {
            kind = "NONE";
            type = "NONE";
            index = -1;
        }
        token = advance(2);
        if (!token.equals("</term>")) {
            token = advance(1);
            if (!token.equals(";")) {
                switch (token) {
                    case "." -> {
                        token = advance(3);
                        if (object) {
                            name = type + "." + token;
                            writer.writePush(kind, index);
                        } else {
                            name += "." + token;
                        }
                        token = advance(5);
                        index = compileExpressionList();
                        if (object) {
                            index++;
                        }
                        token = advance(4);
                        writer.writeCall(name, index);
                    }
                    case "(" -> {
                        token = advance(2);
                        name = className + "." + name;
                        writer.writePush("pointer", 0);
                        index = compileExpressionList();
                        token = advance(4);
                        writer.writeCall(name, index + 1);
                    }
                    case "[" -> {
                        token = advance(2);
                        writer.writePush(kind, index);
                        compileExpression();
                        writer.writeArithmetic("add");
                        writer.writePop("pointer", 1);
                        writer.writePush("that", 0);
                        token = advance(4);
                    }
                }
            }
        } else if (!kind.equals("NONE")) {
            writer.writePush(kind, index);
        } else if (name.equals("this")){
            writer.writePush("pointer",0);
        }
    }
    public void close() {
        boolean closed;
        sc.close();
        writer.close();
        if (file.exists()) {
            //closed = file.delete();
            // if (!closed) throw new RuntimeException("Error closing xml file");
        }
    }
}

