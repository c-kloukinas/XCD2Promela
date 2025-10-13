package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.ArrayList;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;

// import uk.ac.citystgeorges.XCD2Promela.XCDBaseVisitor;

public class XCD2Promela {
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(System.in);
        // create a lexer that feeds off of input CharStream
        XCDLexer lexer = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser = new XCDParser(tokens);

        ParseTree tree = parser.compilationUnits(); // begin parsing at "compilationUnits" parse rule

        ArrayList<String> res = new XCD2PromelaVisitor().visit(tree);
        // if (res != null)
        //     for (String s : res) {
        //      System.err.println("RES: " + s);
        //     }

        // XCD2PromelaListener translator = new XCD2PromelaListener();
        // ParseTreeWalker.DEFAULT.walk(translator, tree); // initiate tree walk with listener translator

/*
        // Create a generic parse tree walker that can trigger callbacks
        ParseTreeWalker walker = new ParseTreeWalker();
        // Walk the tree created during the parse, trigger callbacks
        walker.walk(new ShortToUnicodeString(), tree);
        System.out.println(); // print a \n after translation
*/
        // Read the XcD_PACKAGE.h file and write it in the output directory
        try (InputStream in
             = XCD2Promela.class.getResourceAsStream("/resources/XcD_PACKAGE.h");
             BufferedReader reader
             = new BufferedReader(new InputStreamReader(in));
             FileWriter xcdhdr
             = mynewoutput("XcD_PACKAGE.h")) {
            // Write header file to output
            int inputchar = -1;
            while ((inputchar = reader.read()) != -1)
                xcdhdr.write(inputchar);
        }
    }
    static public String outputdir = "src-gen/";
    static public FileWriter mynewoutput(String fname) throws IOException {
        Files.createDirectories(Path.of(outputdir));
        return new FileWriter(XCD2Promela.outputdir + fname); }
}
