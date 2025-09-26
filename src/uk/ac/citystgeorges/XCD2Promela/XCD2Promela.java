package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.ArrayList;

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
	if (res != null)
	    for (String s : res) {
		System.err.println("RES: " + s);
	    }
	// XCD2PromelaListener translator = new XCD2PromelaListener();
	// ParseTreeWalker.DEFAULT.walk(translator, tree); // initiate tree walk with listener translator

/*
        // Create a generic parse tree walker that can trigger callbacks
        ParseTreeWalker walker = new ParseTreeWalker();
        // Walk the tree created during the parse, trigger callbacks
        walker.walk(new ShortToUnicodeString(), tree);
        System.out.println(); // print a \n after translation
*/
    }
}
