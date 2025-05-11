import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String,SymbolTableEntry> table;
    private int nStatics, nFields, nArgs, nVars = 0;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void reset() {
        table.clear();
        nStatics = 0;
        nVars = 0;
        nArgs = 0;
        nFields = 0;
    }
    
    public void define(String name, String type, String kind){
        SymbolTableEntry entry = new SymbolTableEntry(name, type, kind);
        switch (entry.getKind()){
            case "STATIC" -> entry.setIndex(nStatics++);
            case "FIELD" -> entry.setIndex(nFields++);
            case "ARG" -> entry.setIndex(nArgs++);
            case "VAR" -> entry.setIndex(nVars++);
        }
        table.put(entry.getName(),entry);
    }
    
    public int varCount(String kind){
        return switch(kind){
            case "STATIC" -> nStatics;
            case "FIELD" -> nFields;
            case "ARG" -> nArgs;
            case "VAR" -> nVars;
            default -> throw new RuntimeException("Invalid Kind " + kind);
        };
    }

    public String kindOf(String name){
        SymbolTableEntry entry = table.get(name);
        return entry != null? entry.getKind() : "NONE";
    }

    public String typeOf(String name) {
        SymbolTableEntry entry = table.get(name);
        return entry != null? entry.getType() : "NONE";
    }

    public int indexOf(String name) {
        SymbolTableEntry entry = table.get(name);
        return entry != null? entry.getIndex() : -1;
    }

}
