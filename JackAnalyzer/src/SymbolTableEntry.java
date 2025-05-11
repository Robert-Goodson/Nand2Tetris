import java.util.Arrays;
import java.util.List;

public class SymbolTableEntry {
    private static final String[] KINDS = {"STATIC", "FIELD", "ARG", "VAR"};
    private String name;
    private String type;
    private String kind;
    private int index;

    public SymbolTableEntry(String name, String type, String kind){
        if(validKind(kind)){
            this.name = name;
            this.type = type;
            this.kind = kind;
        } else {
            throw new RuntimeException("Invalid kind: " + kind);
        }
    }

    private boolean validKind(String kind){
        List<String> kinds = Arrays.asList(KINDS);
        return kinds.contains(kind);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
