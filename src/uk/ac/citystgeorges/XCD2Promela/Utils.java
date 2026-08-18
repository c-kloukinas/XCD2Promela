package uk.ac.citystgeorges.XCD2Promela;

import java.lang.Runnable;
import java.util.List;
import java.util.ArrayList;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.stream.Collectors;
// import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.Tree;

import java.util.Collection;            import java.util.Map;
import java.util.function.Predicate;    import java.util.function.BiPredicate;
import java.util.AbstractMap.SimpleImmutableEntry;

class Utils {
    private static FileWriter myNewOutput(String fname) throws IOException {
        Files.createDirectories(Path.of(XCD2Promela.outputdir));
        String fileName = XCD2Promela.outputdir;
        if (fname != null)
            if (! fname.equals(""))
                fileName +=
                    ( (fname.charAt(0) != '/') ? "/" : "" )
                    + fname;
        return new FileWriter(fileName); }

    private List<Runnable> delayedTasks = new ArrayList<Runnable>();
    public int numberOfDelayedTasks()
    {   return delayedTasks.size();   }
    public void addDelayedTask(Runnable task)
    {   delayedTasks.add(task);   }

    public void runDelayedTasks() {
        while (delayedTasks!=null && delayedTasks.size()!=0) {
            Runnable task = delayedTasks.get(0);
            delayedTasks.remove(0);
            try {
                task.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public static void withFileWriteString(String fname
                                           , String out) {
        withFileToWrite(fname
                        , () -> {return out;});
    }
    public static void withFileToWrite(String fname
                                       , Supplier<String> supl) {
        final String res = supl.get(); // capture the output string
        util.delayedTasks
            .add( () ->         // delay the actual file writing
                  {
                      try (FileWriter theConfig
                           = myNewOutput(fname)) {
                          theConfig.write(res);
                      } catch (IOException e) {
                          throw new RuntimeException(e);
                      }
                  }
                  );
    }

    public static String readInputFile(String fin) {
        try (InputStream in
             = XCD2Promela.class.getResourceAsStream(fin)
             ; BufferedReader reader
             = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines().collect(Collectors.joining("\n"))+"\n";
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
                                   .collect(Collectors.joining("\n"))
                                   // add final newline to file
                                   + "\n");
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
    public static void myAssertHard(boolean cond, String msg) {
        assert cond : msg ; if (!cond) throw new RuntimeException(msg); }
    public void myassertHard(boolean cond, String msg) {
        msg = "error(line " +ln + ", char " + atchar + "): " + msg;
        Utils.myAssertHard(cond,msg); }
    private static int errors = 0;
    public void reset_errors() {errors=0;}
    public int get_errors() {return errors;}
    public static void myAssert(boolean cond, String msg) {
        if (!cond) System.err.println(msg);
        errors += cond ? 0 : 1; }
    public void myassert(boolean cond, String msg) {
        msg = "error(line " + ln + ", char " + atchar + "): " + msg;
        Utils.myAssert(cond, msg); }
    private static int warnings = 0;
    public void reset_warnings() {warnings=0;}
    public int get_warnings() {return warnings;}
    public static void myWarning(String msg) {
        System.err.println(msg); ++warnings; }
    public void mywarning(String msg) {
        msg = "warning(line " + ln + ", char " + atchar + "): " + msg;
        Utils.myWarning(msg); }
    public void message(String msg) {
        msg = "NOTE(line " + ln + ", char " + atchar + "): " + msg;
        System.err.println(msg); }
    // public void mySyntaxCheckHard(boolean cond, String msg) {
    //     msg = "Syntax error (line " +ln + ", char " + atchar + "): " + msg;
    //     if (!cond) {
    //         System.err.println(msg);
    //         System.exit(1);
    //     }
    // }

    public static final Utils util = new Utils();

    static public <T> T nonNullCopy(T obj, Class<T> cl) {
        try {
            return (obj!=null)
                ? obj
                : cl.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException nsme) {
            myAssertHard(false
                         , "Class " + cl.getName()
                         + " has no default constructor\n"
                         + nsme);
        } catch (InstantiationException ie) {
            myAssertHard(false
                         , "Failed to call class " + cl.getName()
                         + "'s default constructor: InstantiationException\n"
                         + ie);
        } catch (IllegalAccessException iae) {
            myAssertHard(false
                         , "Failed to call class " + cl.getName()
                         + "'s default constructor: IllegalAccessException\n"
                         + iae);
        } catch (InvocationTargetException ite) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Throwable cause = ite.getCause();
            if (cause!=null)
                cause.printStackTrace(pw);
            myAssertHard(false
                         , "Failed to call class " + cl.getName()
                         + "'s default constructor: InvocationTargetException (wrapper for exception in constructor)\n"
                         + sw.toString());
        }
        return null;            // never happens - myAssertHard exits.
    }

static public <E> SimpleImmutableEntry<E,Boolean> findIf(Collection<E> container, Predicate<E> p) {
    for (E item : container)
        if (p.test(item)) return new SimpleImmutableEntry<>(item,true);
    return new SimpleImmutableEntry<>(null,false);
}

static public <E> SimpleImmutableEntry<E,Boolean> findIf(E[] container, Predicate<E> p) {
    for (E item : container)
        if (p.test(item)) return new SimpleImmutableEntry<>(item,true);
    return new SimpleImmutableEntry<>(null,false);
}

static public <K,V> SimpleImmutableEntry<SimpleImmutableEntry<K,V>,Boolean> findIf(Map<K,V> container, BiPredicate<K,V> p) {
    for (Map.Entry<K, V> entry : container.entrySet()) {
        K key = entry.getKey(); V value = entry.getValue();
        if (p.test(key, value))
            return new SimpleImmutableEntry<>
                (new SimpleImmutableEntry<>(key, value) , true);
    }
    return new SimpleImmutableEntry<>(null,false);
}

static public <E> boolean containsIf(Collection<E> container, Predicate<E> p) {
    for (E item : container) if (p.test(item)) return true;
    return false;
}

static public <E> boolean containsIf(E[] container, Predicate<E> p) {
    for (E item : container) if (p.test(item)) return true;
    return false;
}

static public <K,V> boolean containsIf(Map<K,V> container, BiPredicate<K,V> p) {
    for (Map.Entry<K, V> entry : container.entrySet()) {
        K key = entry.getKey(); V value = entry.getValue();
        if (p.test(key, value))
            return true;
    }
    return false;
}

}
