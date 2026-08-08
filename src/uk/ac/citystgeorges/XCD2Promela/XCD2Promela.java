package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// import uk.ac.citystgeorges.XCD2Promela.XCDBaseVisitor;

public class XCD2Promela {
    static public String outputdir = "src-gen/";
    static public String resources = "/resources/";
    static public String resourceDefinitions = resources + "definitions/";
    static public String resourceTemplates = resources + "templates/";
    public static void main(String[] args) throws Exception {
        // create CharStreams from standard async/proc definitions
        org.antlr.v4.runtime.CharStream asyncDef
            = CharStreams
            .fromStream(XCD2Promela.class
                        .getResourceAsStream(resourceDefinitions
                                             + "async.xcd"));
        org.antlr.v4.runtime.CharStream procDef
            = CharStreams
            .fromStream(XCD2Promela.class
                        .getResourceAsStream(resourceDefinitions
                                             + "proc.xcd"));
        // create a CharStream that reads from standard input
        org.antlr.v4.runtime.CharStream input
            = CharStreams.fromStream(System.in);
        // create a lexer that feeds off of input CharStream
        Utils.util.reset_errors(); // ensure errors == 0 at this point.
        Utils.util.reset_warnings(); // ensure warnings == 0 at this point.
        // Parse async & proc first!
        int syntax_errors = 0;
        int semantic_errors = 0;
        EnvironmentCreationVisitor translator
            = new EnvironmentCreationVisitor(syntax_errors);
        {
            XCDLexer lexer = new XCDLexer(asyncDef);
            org.antlr.v4.runtime.CommonTokenStream tokens
                = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            XCDParser parser = new XCDParser(tokens);
            org.antlr.v4.runtime.tree.ParseTree tree
                = parser.compilationUnits();
            syntax_errors = parser.getNumberOfSyntaxErrors();
            T res1 = translator.visit(tree);
            {
                final var Err = System.err;
                // translator.message("There are "
                //                 + Utils.util.numberOfDelayedTasks()
                //                 + " delayed tasks");
                Utils.util.runDelayedTasks();
                semantic_errors = translator.get_semantic_errors();
                int warnings = translator.get_warnings();
                // Err.println("Async There were " + syntax_errors
                //             + " syntax errors");
                // Err.println("Async There were " + semantic_errors
                //             + " semantic errors");
                // Err.println("Async There were " + warnings + " warnings");
                if (0 != syntax_errors || 0 != semantic_errors)
                    Utils.util
                        .message("Unexpected errors in the definition of async"
                                 + "There were " + syntax_errors
                                 + " syntax errors and " + semantic_errors
                                 + " semantic errors");
            }
        }
        {
            XCDLexer lexer = new XCDLexer(procDef);
            org.antlr.v4.runtime.CommonTokenStream tokens
                = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            XCDParser parser = new XCDParser(tokens);
            org.antlr.v4.runtime.tree.ParseTree tree
                = parser.compilationUnits();
            syntax_errors = parser.getNumberOfSyntaxErrors();
            T res1 = translator.visit(tree);
            {
                final var Err = System.err;
                // translator.message("There are "
                //                 + Utils.util.numberOfDelayedTasks()
                //                 + " delayed tasks");
                Utils.util.runDelayedTasks();
                semantic_errors = translator.get_semantic_errors();
                int warnings = translator.get_warnings();
                // Err.println("Proc There were " + syntax_errors
                //             + " syntax errors");
                // Err.println("Proc There were " + semantic_errors
                //             + " semantic errors");
                // Err.println("Proc There were " + warnings + " warnings");
                if (0 != syntax_errors || 0 != semantic_errors)
                    Utils.util
                        .message("Unexpected errors in the definition of proc"
                                 + "There were " + syntax_errors
                                 + " syntax errors and " + semantic_errors
                                 + " semantic errors");
            }
        }
        // Now for the actual input.
        XCDLexer lexer = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        org.antlr.v4.runtime.CommonTokenStream tokens
            = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser = new XCDParser(tokens);
        // begin parsing at "compilationUnits" parse rule
        org.antlr.v4.runtime.tree.ParseTree tree = parser.compilationUnits();
        syntax_errors = parser.getNumberOfSyntaxErrors();
        Utils.myAssert(0 == syntax_errors && 0 == semantic_errors
                       , "Encountered unexpected syntax/semantic errors");

        Utils.myWarning("***First parser pass***");
        // ArrayList<String> res = new XCD2PromelaVisitor().visit(tree);
        Utils
            .myAssert(Utils.util.numberOfDelayedTasks() == 0
                      , "There should be no delayed tasks in the queue "
                      + "- aborting");
        T res1 = translator.visit(tree);
        semantic_errors = translator.get_semantic_errors();
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
        //           (resourceDefinitions + "XcD_PACKAGE.h"
        //            , "XcD_PACKAGE.h"
        //            , (String inp) -> {
        //               return inp;
        //           }));
        // // Add the M4 common definitions file & friends.
        { String[] files = { "0-common-defs.m4"
                             , "async.xcd"
                             , "proc.xcd" };
            for (var file : files)
                Utils.withInputAndFileToWrite
                    (resourceDefinitions + file
                     , file
                     , (String inp) -> {
                        return inp;
                    });
        }
        final var Err = System.err;
        // translator.mywarning("There are "
        //                   + Utils.util.numberOfDelayedTasks()
        //                   + " delayed tasks");
        if (0 == syntax_errors && 0 == semantic_errors) {
            Utils.util.runDelayedTasks();
        }
        Err.println("There were " + syntax_errors + " syntax errors");
        Err.println("There were " + semantic_errors + " semantic errors");
        Err.println("There were " + warnings + " warnings");
        if (0 != syntax_errors || 0 != semantic_errors) System.exit(1);
    }
}
