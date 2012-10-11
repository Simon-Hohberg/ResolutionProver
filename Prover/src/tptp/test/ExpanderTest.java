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
  
  @Test
  public void simpleExpansion() {
	Disjunction disjunction = new Disjunction(dAtomic);
	disjunction.index = 1;
	Collection<Disjunction> atomics = expander.expand(disjunction);
	Assert.assertTrue(atomics.size() == 1 && atomics.contains(disjunction));
  }

}
