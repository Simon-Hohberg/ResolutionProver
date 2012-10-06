package resolution_prover.test;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import resolution_prover.ResolutionProver;
import tptp.AnnotatedFormula;
import tptp.Formula;
import tptp.Negation;
import tptp.SimpleTptpParserOutput;
import tptp.TopLevelItem;
import tptp.TptpLexer;
import tptp.TptpParser;
import static org.junit.Assert.*;


public class ResolutionProverTest {
  
  private static Formula simpleTautology, testFormula, atomic;
  private static List<Formula> miami_cs;
  
  @BeforeClass
  public static void setupFormulas() throws RecognitionException, TokenStreamException, FileNotFoundException {
    simpleTautology = parseFormula("p|~p");
    testFormula = parseFormula("((p & q) | (r => s)) => ((p | (r => s )) & (q | (r => s)))");
    atomic = parseFormula("p");
    miami_cs = parseReader(new FileReader("MiamiDegree.p"));
  }
  
  @Test
  public void proveSimpleFormula() {
    assertTrue(prove(simpleTautology));
  }
  
  @Test
  public void proveTestFormula() {
    assertTrue(prove(testFormula));
  }
  
  @Test
  public void notProve() {
    assertFalse(prove(atomic));
    assertFalse(prove(new Negation(simpleTautology)));
  }
  
  @Test
  public void proveMiamiCS() {
    assertTrue(prove(miami_cs));
  }
  
  private boolean prove(List<Formula> formulae) {
    return new ResolutionProver(formulae).prove();
  }
  
  private boolean prove(Formula formula) {
    return prove(Collections.singletonList(formula));
  }
  
  private static List<Formula> parseTPTP(String tptp) throws RecognitionException, TokenStreamException {
    return parseReader(new StringReader(tptp));
  }
  
  private static List<Formula> parseReader(Reader in) throws RecognitionException, TokenStreamException {
    LinkedList<Formula> formulae = new LinkedList<Formula>();
    SimpleTptpParserOutput outputManager = new SimpleTptpParserOutput();
    TptpLexer lexer = new TptpLexer(in);
    TptpParser parser = new TptpParser(lexer);
    
    TopLevelItem item;
    while((item = (TopLevelItem) parser.topLevelItem(outputManager)) != null) {
      if (item instanceof AnnotatedFormula) {
        formulae.add(((AnnotatedFormula)item).getFormula());
      }
    }
    
    return formulae;
  }
  
  private static Formula parseFormula(String formula) throws RecognitionException, TokenStreamException {
    return parseTPTP("fof(axiom1,axiom,(" + formula + ")).").get(0);
  }
  
}
