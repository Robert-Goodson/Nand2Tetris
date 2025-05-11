import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    public static void main(String ... args) throws IOException {
        File file = new File(args[0]);
        List<SyntaxAnalyzer> engines = new ArrayList<>();

        if (file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            for(File f: files){
                if(f.getName().endsWith(".jack")){
                    engines.add(new SyntaxAnalyzer(f));
                }
            }
        } else {
            engines.add(new SyntaxAnalyzer(file));
        }
        for(SyntaxAnalyzer engine: engines) {
            engine.compileClass();
            String fileName = engine.getName();
            engine.close();
            CompilationEngine writer = new CompilationEngine(new File(fileName+".xml"));
            writer.compileClass();
            writer.close();
        }
    }
}
