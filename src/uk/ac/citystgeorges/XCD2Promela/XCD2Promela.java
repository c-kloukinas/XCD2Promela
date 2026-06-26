package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
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
        int syntax_errors = parser.getNumberOfSyntaxErrors();
        Utils.myAssert(0 == syntax_errors, "Encountered syntax errors");

        Utils.myWarning("***First parser pass***");
        // ArrayList<String> res = new XCD2PromelaVisitor().visit(tree);
        EnvironmentCreationVisitor translator
            = new EnvironmentCreationVisitor(syntax_errors);
        List<Runnable> tasks = new ArrayList<Runnable>();
        T res1 = translator.visit(tree);
        int semantic_errors = translator.get_semantic_errors();
        int warnings = translator.get_warnings();
        // if (res != null)
        //     for (String s : res) {
        //      System.err.println("RES: " + s);
        //     }
        /**
         * Now translate the AST, using the environments, to Promela
         * (with cpp)
         */
        // Utils.myWarning("***Starting translation***");
        // LstStr res = new Translate2CppPromelaVisitor().visit(tree);

        // XCD2PromelaListener translator = new XCD2PromelaListener();
        // ParseTreeWalker.DEFAULT.walk(translator, tree); // initiate tree walk with listener translator

/*
        // Create a generic parse tree walker that can trigger callbacks
        ParseTreeWalker walker = new ParseTreeWalker();
        // Walk the tree created during the parse, trigger callbacks
        walker.walk(new ShortToUnicodeString(), tree);
        System.out.println(); // print a \n after translation
*/
        // // Read the XcD_PACKAGE.h file and write it in the output directory
        // tasks.add(() -> Utils.withInputAndFileToWrite
        //           ("/resources/definitions/XcD_PACKAGE.h"
        //            , "XcD_PACKAGE.h"
        //            , (String inp) -> {
        //               return inp;
        //           }));
        // Add the M4 common definitions file.
        tasks.add(() -> Utils.withInputAndFileToWrite
                  ("/resources/definitions/0-common-defs.m4"
                   , "0-common-defs.m4"
                   , (String inp) -> {
                      return inp;
                  }));
        final var Err = System.err;
        if (0 == syntax_errors && 0 == semantic_errors)
            for (Runnable task : tasks)
                try {
                    task.run();
                } catch (Exception e) {
                    Err.println(e);
                }
        Err.println("There were " + syntax_errors + " syntax errors");
        Err.println("There were " + semantic_errors + " semantic errors");
        Err.println("There were " + warnings + " warnings");
    }
}
