import java.io.DataInputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import tptp.AnnotatedFormula;
import tptp.Formula;
import tptp.SimpleTptpParserOutput;
import tptp.TopLevelItem;
import tptp.TptpLexer;
import tptp.TptpParser;
import static org.junit.Assert.*;


public class ResolutionProverTest {
  
  @Test
  public void proveSimpleFormula() throws RecognitionException, TokenStreamException {
    assertTrue(prove(parse("fof(axiom1,axiom,(p|~p)).")));
  }
  
  private boolean prove(List<Formula> formulae) {
    return new ResolutionProver(formulae).prove();
  }
  
  private List<Formula> parse(String tptp) throws RecognitionException, TokenStreamException {
    LinkedList<Formula> formulae = new LinkedList<Formula>();
    SimpleTptpParserOutput outputManager = new SimpleTptpParserOutput();
    TptpLexer lexer = new TptpLexer(new StringReader(tptp));
    TptpParser parser = new TptpParser(lexer);
    
    TopLevelItem item;
    while((item = (TopLevelItem) parser.topLevelItem(outputManager)) != null) {
      if (item instanceof AnnotatedFormula) {
        formulae.add(((AnnotatedFormula)item).getFormula());
      }
    }
    
    return formulae;
  }
  
}
