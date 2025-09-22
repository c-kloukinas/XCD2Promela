package uk.ac.citystgeorges.XCD2Promela;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
// import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;

public class TestGrammar {
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(System.in);
        // create a lexer that feeds off of input CharStream
        XCDLexer lexer = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser = new XCDParser(tokens);
        ParseTree tree = parser.compilationUnits(); // begin parsing at compilationUnits rule
        System.out.println(tree.toStringTree(parser)); // print LISP-style tree
    }
}
