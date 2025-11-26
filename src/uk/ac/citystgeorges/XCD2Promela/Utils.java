package uk.ac.citystgeorges.XCD2Promela;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.stream.Collectors;
// import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.Tree;

class Utils {
    static public FileWriter myNewOutput(String fname) throws IOException {
        Files.createDirectories(Path.of(XCD2Promela.outputdir));
        return new FileWriter(XCD2Promela.outputdir + fname); }


    public static void withFileWriteString(String fname
                                           , String out) {
        withFileToWrite(fname
                        , () -> {return out;});
    }
    public static void withFileToWrite(String fname
                                       , Supplier<String> supl) {
        try (FileWriter theConfig
             = myNewOutput(fname)) {
            String res = supl.get();
            theConfig.write(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void withInputAndFileToWrite(String fin
                                               , String fout
                                               , Function<String,String> func) {
        try (InputStream in
             = XCD2Promela.class.getResourceAsStream(fin)
             ; BufferedReader reader
             = new BufferedReader(new InputStreamReader(in))) {
            withFileToWrite
                (fout
                 , () -> {
                    return
                        func.apply(reader.lines()
                                   .collect(Collectors.joining("\n")));
                });
            // theConfig.write(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringfromFile(String fname){
        try (InputStream in
             = XCD2Promela.class.getResourceAsStream(fname);
             BufferedReader reader
             = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines()
                .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static public int ln=0;
    static public int atchar=0;
    static int gensymcounter=0;
    static public String newgensym(String prefix) {
        return "gensym_"
            + ((null==prefix) ? "" : (prefix+"_"))
            + ln +"_"+atchar +"_"+(gensymcounter++); }
    static public String newgensym() { return newgensym(null); }
    static public void resetln() {ln=-1; atchar=-1;}
    static public void updateln(Tree ctx) {
        Token tk = getAtoken(ctx);
        if (tk==null)
            { resetln(); return; }
        ln = tk.getLine(); atchar = tk.getStartIndex();
    }
    static public Token getAtoken(Tree tr) { // took me a while... -
                                             // simplified version of
                                             // updateln1 really
        if (null==tr || (tr instanceof Token)) return (Token)tr;
        Object pl = tr;
        do {
            Tree ch = ((Tree)pl).getChild(0);
            pl = (null==ch)?null:ch.getPayload();
        } while (null!=pl && !(pl instanceof Token));
        return (Token)pl;
    }
    public static void myAssert(boolean cond, String msg) {
        assert cond : msg ; if (!cond) throw new RuntimeException(msg); }
    public void myassert(boolean cond, String msg) {
        msg = "error(line " +ln + ", char " + atchar + "): " + msg;
        Utils.myAssert(cond,msg); }
    public static void myWarning(String msg) {
        System.err.println(msg); }
    public void mywarning(String msg) {
        msg = "warning(line " +ln + ", char " + atchar + "): " + msg;
        Utils.myWarning(msg); }
    public void mySyntaxCheck(boolean cond, String msg) {
        msg = "Syntax error (line " +ln + ", char " + atchar + "): " + msg;
        if (!cond) {
            System.err.println(msg);
            System.exit(1);
        }
    }

    public static final Utils util = new Utils();
}
