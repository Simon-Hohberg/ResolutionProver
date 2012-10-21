package tptp.test;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import tptp.Formula;

import static org.junit.Assert.*;
import static resolutionprover.test.ResolutionProverTest.parseFormula;

public class FormulaTest {
  private static Formula[] dAtomic, dAnd, dOr, dNot;

  private static Formula[] sAtomic1, sAtomic2, sAnd, sOr, sNot1, sNot2;

  @BeforeClass
  public static void setupFormulas() throws RecognitionException,
      TokenStreamException, FileNotFoundException {
//    dAtomic = new Formula[] { parseFormula("a"), parseFormula("b"),
//        parseFormula("ab"), parseFormula("~a") };
//    dAnd = new Formula[] { parseFormula("a&b"), parseFormula("b&c"),
//        parseFormula("~a&b") };
//    dOr = new Formula[] { parseFormula("a|b"), parseFormula("b|c"),
//        parseFormula("~a|b") };
//    dNot = new Formula[] { parseFormula("~p"), parseFormula("~~p"),
//        parseFormula("~x") };
//
//    sAtomic1 = new Formula[] { parseFormula("a"), parseFormula("a") };
//    sAtomic2 = new Formula[] { parseFormula("~a"), parseFormula("~a") };
//    sAnd = new Formula[] { parseFormula("a&b"), parseFormula("b&a") };
//    sOr = new Formula[] { parseFormula("a|b"), parseFormula("b|a") };
//    sNot1 = new Formula[] { parseFormula("~p"), parseFormula("~~~p"),
//        parseFormula("~~~~~p") };
//    sNot2 = new Formula[] { parseFormula("~~p"), parseFormula("~~~~p"),
//        parseFormula("~~~~~~p") };
  }
  
  @Test
  public void hashesShouldDiffer() {
    assertTrue(allHashesDiffer(dAtomic));
    assertTrue(allHashesDiffer(dAnd));
    assertTrue(allHashesDiffer(dOr));
    assertTrue(allHashesDiffer(dNot));
  }

  @Test
  public void hashesShouldEqual() {
    assertTrue(allHashesEqual(sAtomic1));
    assertTrue(allHashesEqual(sAtomic2));
    assertTrue(allHashesEqual(sAnd));
    assertTrue(allHashesEqual(sOr));
    assertTrue(allHashesEqual(sNot1));
    assertTrue(allHashesEqual(sNot2));
  }

  private boolean allHashesDiffer(Formula... formulae) {
    Set<Integer> seenHashes = new TreeSet<Integer>();
    for (Formula f : formulae) {
      seenHashes.add(f.hashCode());
    }
    return seenHashes.size() == formulae.length;
  }

  private boolean allHashesEqual(Formula... formulae) {
    int fixedHash = formulae[0].hashCode();
    for (Formula f : formulae) {
      if (fixedHash != f.hashCode())
        return false;
    }
    return true;
  }

}
