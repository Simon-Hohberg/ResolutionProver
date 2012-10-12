package resolutionprover.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import resolutionprover.FirstOrderProver;
import resolutionprover.PropositionalProver;
import resolutionprover.Util;
import tptp.AnnotatedFormula;
import tptp.SimpleTptpParserOutput;
import tptp.TopLevelItem;
import tptp.TptpLexer;
import tptp.TptpParser;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ResolutionProverTest {

	private static AnnotatedFormula testFirstOrder, testFirstOrder2, simpleFirstOrder, equivalence,
			simpleTautology1, simpleTautology2, simpleTautology3, testFormula,
			atomic, alt, ffalse, ftrue;
	private static List<AnnotatedFormula> miami_cs, sudoku_counter1, sudoku_counter2, sudoku;

	@BeforeClass
	public static void setupFormulas() throws RecognitionException,
			TokenStreamException, FileNotFoundException {
		simpleTautology1 = parseFormula("p|~p");
		simpleTautology2 = parseFormula("(a=>b)|(b=>a)");
		simpleTautology3 = parseFormula("(a=>(b=>c))=>((a=>b)=>(a=>c))");
		testFormula = parseFormula("((p & q) | (r => s)) => ((p | (r => s )) & (q | (r => s)))");
		atomic = parseFormula("p");
		miami_cs = parseReader(new FileReader("test/MiamiDegree.p"));
		alt = parseFormula("(c|~c)|(~c|c)");
		equivalence = parseFormula("a <=> a");
		ffalse = parseFormula("$false");
    ftrue = parseFormula("$true");
		simpleFirstOrder = parseFormula("?[X]:(![Y]: ( r(X,Y) => r(X,Y) ))");
		//TODO lhs and rhs of "=>" are null, shitty parser?
		testFirstOrder = parseFormula("( ?[W]: ![X]: r(X,W,f(X,W)) ) => ( ?[W]: ![X]: ?[Y]: r(X,W,Y) )");
		testFirstOrder2 = parseFormula("![X]:(r(X)) => ![X]:(r(X))");
		sudoku = parseReader(new FileReader("test/SudokuTheorem1.p"));
		sudoku_counter1 = parseReader(new FileReader("test/SudokuCounterSatisfiable1.p"));
		sudoku_counter2 = parseReader(new FileReader("test/SudokuCounterSatisfiable2.p"));
	}

	@Test
	public void proveSimpleFormula1() {
		assertTrue(prove(simpleTautology1));
	}

	@Test
	public void proveSimpleFormula2() {
		assertTrue(prove(simpleTautology2));
	}

	@Test
	public void proveSimpleFormula3() {
		assertTrue(prove(simpleTautology3));
	}

	@Test
	public void proveTestFormula() {
		// assertTrue(prove(alt));
		assertTrue(prove(testFormula));
	}

	@Test
	public void proveEquivalenceTest() {
		assertTrue(prove(equivalence));
	}

	@Test
	public void simpleFirstOrderTest() {
		assertTrue(proveFirstOrder(simpleFirstOrder));
	}
	
	@Test
	public void testFirstOrderTest() {
		assertTrue(proveFirstOrder(testFirstOrder));
	}
	
	@Test
  public void testFirstOrderTest2() {
    assertTrue(proveFirstOrder(testFirstOrder2));
  }
	
	@Test
	public void notProveSoduko1() {
	  assertFalse(prove(sudoku_counter1));
	}
	
	@Test
  public void notProveSoduko2() {
	  assertFalse(prove(sudoku_counter2));
  }
	
	@Test
  public void proveSoduko() {
    assertTrue(prove(sudoku));
  }
	
	private boolean proveFirstOrder(AnnotatedFormula formula) {
		return new FirstOrderProver(formula).prove();
	}
	
	@Test
	public void notProve() {
		assertFalse(prove(atomic));
		assertFalse(prove(Util.negate(simpleTautology1)));
		assertFalse(prove(Util.negate(simpleTautology2)));
		assertFalse(prove(Util.negate(simpleTautology3)));
		assertFalse(prove(Util.negate(alt)));
	}

	@Test
	public void proveMiamiCS() {
		assertTrue(prove(miami_cs));
	}
	
	@Test
	public void proveTrue() {
	  assertTrue(prove(ftrue));
	  assertFalse(prove(Util.negate(ftrue)));
	}
	
	@Test
  public void notProveFalse() {
    assertFalse(prove(ffalse));
    assertTrue(prove(Util.negate(ffalse)));
  }

	private boolean prove(List<AnnotatedFormula> formulae) {
		return new PropositionalProver(
				formulae.toArray(new AnnotatedFormula[0])).prove();
	}

	private boolean prove(AnnotatedFormula... formula) {
		return prove(Arrays.asList(formula));
	}

	private static List<AnnotatedFormula> parseTPTP(String tptp)
			throws RecognitionException, TokenStreamException {
		return parseReader(new StringReader(tptp));
	}

	public static List<AnnotatedFormula> parseReader(Reader in)
			throws RecognitionException, TokenStreamException {
		LinkedList<AnnotatedFormula> formulae = new LinkedList<AnnotatedFormula>();
		SimpleTptpParserOutput outputManager = new SimpleTptpParserOutput();
		TptpLexer lexer = new TptpLexer(in);
		TptpParser parser = new TptpParser(lexer);

		TopLevelItem item;
		while ((item = (TopLevelItem) parser.topLevelItem(outputManager)) != null) {
			if (item instanceof AnnotatedFormula) {
				formulae.add((AnnotatedFormula) item);
			}
		}

		return formulae;
	}

	public static AnnotatedFormula parseFormula(String formula)
			throws RecognitionException, TokenStreamException {
		return parseTPTP("fof(conjecture1,conjecture,(" + formula + ")).").get(
				0);
	}
}