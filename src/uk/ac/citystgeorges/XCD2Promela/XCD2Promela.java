package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.ArrayList;

// import uk.ac.citystgeorges.XCD2Promela.XCDBaseVisitor;

public class XCD2Promela {
    static public String outputdir = "src-gen/";

    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(System.in);
        // create a lexer that feeds off of input CharStream
        XCDLexer lexer = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser = new XCDParser(tokens);

        // begin parsing at "compilationUnits" parse rule
        ParseTree tree = parser.compilationUnits();

        // ArrayList<String> res = new XCD2PromelaVisitor().visit(tree);
        Void res = new EnvironmentCreationVisitor().visit(tree);
        // if (res != null)
        //     for (String s : res) {
        //      System.err.println("RES: " + s);
        //     }
        /**
         * Now translate the AST, using the environments, to Promela
         * (with cpp)
         */
        res = new Translate2CppPromelaVisitor().visit(tree);

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
        Utils.withInputAndFileToWrite
            ("/resources/XcD_PACKAGE.h"
             , "XcD_PACKAGE.h"
             , (String inp) -> {
                return inp;
            });
    }
}
