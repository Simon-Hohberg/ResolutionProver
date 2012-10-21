package resolutionprover.test;

import static junit.framework.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

import resolutionprover.Disjunction;
import resolutionprover.Expander;
import tptp.Atomic;
import tptp.Formula;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ExpanderTest {
	
	private static Formula betaFormula, alphaFormula, negnegFormula;
	private static Collection<Disjunction> alphaExpanded, betaExpanded, negnegExpanded;

	@BeforeClass
	public static void setupFormulas() throws RecognitionException,
			TokenStreamException, FileNotFoundException {
		betaFormula = ResolutionProverTest.parseFormula("a | b").getFormula();
		betaExpanded = new HashSet<Disjunction>(); 
		betaExpanded.add(new Disjunction(new Atomic("a", null), new Atomic("b", null)));
		
		alphaFormula = ResolutionProverTest.parseFormula("a & b").getFormula();
		alphaExpanded = new HashSet<Disjunction>(); 
		alphaExpanded.add(new Disjunction(new Atomic("a", null)));
		alphaExpanded.add(new Disjunction(new Atomic("b", null)));
		
		negnegFormula = ResolutionProverTest.parseFormula("~(~(a))").getFormula();
		negnegExpanded = new HashSet<Disjunction>();
		negnegExpanded.add(new Disjunction(new Atomic("a", null)));
	}
	
  private Expander expander = new Expander();
  
  @Test
  public void alphaRuleExpansionTest() {
	Disjunction disjunction = new Disjunction(alphaFormula);
	Collection<Disjunction> atomics = expander.expand(disjunction);
	assertTrue(atomics.equals(alphaExpanded));
  }
  
  @Test
  public void betaRuleExpansionTest() {
	Disjunction disjunction = new Disjunction(betaFormula);
	Collection<Disjunction> atomics = expander.expand(disjunction);
	assertTrue(atomics.equals(betaExpanded));
  }
  
  @Test
  public void negnegRuleExpansionTest() {
	Disjunction disjunction = new Disjunction(negnegFormula);
	Collection<Disjunction> atomics = expander.expand(disjunction);
	assertTrue(atomics.equals(negnegExpanded));
  }
}
