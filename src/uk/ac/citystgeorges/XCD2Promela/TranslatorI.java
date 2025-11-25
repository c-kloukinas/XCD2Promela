package uk.ac.citystgeorges.XCD2Promela;
import org.antlr.v4.runtime.ParserRuleContext;
/**
 * Goal of this interface is to help split the actual translation code
 * in separate files, one per the context that it applies to.
 */
public interface TranslatorI {
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx);
}
