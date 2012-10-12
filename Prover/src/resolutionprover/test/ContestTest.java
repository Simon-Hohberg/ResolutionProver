package resolutionprover.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import resolutionprover.PropositionalProver;
import tptp.AnnotatedFormula;
import static resolutionprover.test.ResolutionProverTest.parseReader;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContestTest {

  private static List<AnnotatedFormula> phil, sudoku, sudokuC1, sudokuC2,
      miami;

  @BeforeClass
  public static void setupFormulas() throws RecognitionException,
      TokenStreamException, FileNotFoundException {
    phil = parseFile("test/contest/ContestTheorems/Philosophen.p");
    sudoku = parseFile("test/contest/ContestTheorems/SudokuTheorem1.p");
    miami = parseFile("test/contest/ContestTheorems/MiamiDegree.p");
    sudokuC1 = parseFile("test/contest/ContestNonTheorems/SudokuCounterSatisfiable1.p");
    sudokuC2 = parseFile("test/contest/ContestNonTheorems/SudokuCounterSatisfiable2.p");
  }

  @Test
  public void provePhil() {
    assertTrue(prove(phil));
  }

  @Test
  public void proveSudoku() {
    assertTrue(prove(sudoku));
  }

  @Test
  public void proveMiami() {
    assertTrue(prove(miami));
  }

  @Test
  public void notProveSudoku1() {
    assertFalse(prove(sudokuC1));
  }

  @Test
  public void notProveSudoku2() {
    assertFalse(prove(sudokuC2));
  }

  private boolean prove(List<AnnotatedFormula> formulae) {
    return new PropositionalProver(formulae.toArray(new AnnotatedFormula[0]))
        .prove();
  }

  private static List<AnnotatedFormula> parseFile(String filename)
      throws RecognitionException, TokenStreamException, FileNotFoundException {
    return parseReader(new FileReader(filename));
  }

}
