package uk.ac.citystgeorges.XCD2Promela;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Iterator;
import java.util.ArrayList;

public class USet<T> {

    @SafeVarargs
    public static <T> Set<? extends T>
        setUnion(Collection<? extends T>... sets) {
        Stream<? extends T> emptyStream = new ArrayList<T>().stream();

        Stream<? extends T> finalStream = emptyStream;
        int howMany = sets.length;
        for (int i=0; i<howMany; ++i) {
            Stream<? extends T> cllctn = sets[i].stream();
            finalStream = Stream.concat(finalStream, cllctn);
        }

        return finalStream.collect(Collectors.toSet());
    }

    @SafeVarargs
    public static <T> Set<? extends T>
        setIntersection(Collection<? extends T>... sets) {
        Stream<? extends T> emptyStream = new ArrayList<T>().stream();

        Stream<? extends T> finalStream = emptyStream;
        int howMany = sets.length;
        if (howMany>0)
            finalStream = Stream.concat(finalStream, sets[0].stream());
        for (int i=1; i<howMany; ++i) {
            var theSet = sets[i];
            finalStream = finalStream.filter( el -> theSet.contains(el) );
        }

        return finalStream.collect(Collectors.toSet());
    }

    @SafeVarargs
    public static <T> Set<? extends T>
        setDifference(Collection<? extends T>... sets) {
        Stream<? extends T> emptyStream = new ArrayList<T>().stream();

        Stream<? extends T> finalStream = emptyStream;
        int howMany = sets.length;
        if (howMany>0)
            finalStream = Stream.concat(finalStream, sets[0].stream());
        for (int i=1; i<howMany; ++i) {
            var theSet = sets[i];
            finalStream = finalStream.filter( el -> !theSet.contains(el) );
        }

        return finalStream.collect(Collectors.toSet());
    }

    public static <T> Set<? extends T>
    /**
     * disjunction(a, b) = union(difference(a, b), difference(b, a))
     *
     * Essentially, we're keeping just the elements that belong to
     * only one set.
     */
        setDisjunction(Collection<? extends T> set1
                       , Collection<? extends T> set2) {
        return setUnion(setDifference(set1, set2)
                        , setDifference(set2, set1));
    }
}
