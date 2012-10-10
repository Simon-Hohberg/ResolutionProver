package tptp.test;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import resolutionprover.Disjunction;
import resolutionprover.Expander;
import tptp.Formula;

import static org.junit.Assert.*;
import static resolutionprover.test.ResolutionProverTest.parseFormula;

public class ExpanderTest {
  private static Formula[] dAtomic, dAnd, dOr, dNot;

  private static Formula[] sAtomic1, sAtomic2, sAnd, sOr, sNot1, sNot2;

  private Expander expander = new Expander();
  
  @BeforeClass
  public static void setupFormulas() throws RecognitionException,
      TokenStreamException, FileNotFoundException {
    dAtomic = new Formula[] { parseFormula("a"), parseFormula("b"),
        parseFormula("ab"), parseFormula("~a") };
    dAnd = new Formula[] { parseFormula("a&b"), parseFormula("b&c"),
        parseFormula("~a&b") };
    dOr = new Formula[] { parseFormula("a|b"), parseFormula("b|c"),
        parseFormula("~a|b") };
    dNot = new Formula[] { parseFormula("~p"), parseFormula("~~p"),
        parseFormula("~x") };

    sAtomic1 = new Formula[] { parseFormula("a"), parseFormula("a") };
    sAtomic2 = new Formula[] { parseFormula("~a"), parseFormula("~a") };
    sAnd = new Formula[] { parseFormula("a&b"), parseFormula("b&a") };
    sOr = new Formula[] { parseFormula("a|b"), parseFormula("b|a") };
    sNot1 = new Formula[] { parseFormula("~p"), parseFormula("~~~p"),
        parseFormula("~~~~~p") };
    sNot2 = new Formula[] { parseFormula("~~p"), parseFormula("~~~~p"),
        parseFormula("~~~~~~p") };
  }
  
  @Test
  public void simpleExpansion() {
	Disjunction disjunction = new Disjunction(dAtomic);
	disjunction.index = 1;
	Collection<Disjunction> atomics = expander.expand(disjunction);
	Assert.assertTrue(atomics.size() == 1 && atomics.contains(disjunction));
  }

}
