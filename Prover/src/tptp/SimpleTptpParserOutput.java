package tptp;

import java.util.Hashtable;
import java.util.LinkedList;

import org.junit.internal.matchers.SubstringMatcher;

import tptp.TptpParserOutput.Term;

/**
 * A simple implementation of the interface TptpParserOutput. This
 * implementation is likely to be sufficient for all simple uses of the parser.
 * 
 * @author Alexandre Riazanov
 * @author Andrei Tchaltsev
 * @since Feb 02, 2006
 * @since Apr 06, 2006
 */

public class SimpleTptpParserOutput implements TptpParserOutput {

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.TptpInput createFofAnnotated(String name,
      TptpParserOutput.FormulaRole role, TptpParserOutput.FofFormula formula,
      TptpParserOutput.Annotations annotations, int lineNumber) {
    return (TptpParserOutput.TptpInput) (new AnnotatedFormula(
        sharedCopyOf(name), role, formula, annotations, lineNumber));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.TptpInput createCnfAnnotated(String name,
      TptpParserOutput.FormulaRole role, TptpParserOutput.CnfFormula clause,
      TptpParserOutput.Annotations annotations, int lineNumber) {

    return (TptpParserOutput.TptpInput) (new AnnotatedClause(
        sharedCopyOf(name), role, clause, annotations, lineNumber));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpInput createIncludeDirective(String fileName,
      Iterable<String> formulaSelection, int lineNumber) {
    // strings in formulaSelection don't get shared,
    // there seems to be no practical need for this
    return (TptpParserOutput.TptpInput) (new IncludeDirective(
        sharedCopyOf(fileName), formulaSelection, lineNumber));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.FofFormula createBinaryFormula(
      TptpParserOutput.FofFormula lhs,
      TptpParserOutput.BinaryConnective connective,
      TptpParserOutput.FofFormula rhs) {
    Formula key = tptp_tester.replaceEquivalences(new Binary(lhs, connective,
        rhs));
    Formula res = _formulaTable.get(key);
    if (res == null) {
      _formulaTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.FofFormula createNegationOf(
      TptpParserOutput.FofFormula formula) {
    Formula key = new Negation(formula);
    Formula res = _formulaTable.get(key);
    if (res == null) {
      _formulaTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.FofFormula createQuantifiedFormula(
      TptpParserOutput.Quantifier quantifier, Iterable<String> variableList,
      TptpParserOutput.FofFormula formula) {
    assert variableList != null && variableList.iterator().hasNext();
    TptpParserOutput.FofFormula key = formula;
    for (String var : variableList) {
      tptp.Term boundVariableTerm = (tptp.Term) this.createVariableTerm(var);
      String freshVar = tptp_tester.freshVariableName();
      tptp.Term replaceTerm = (tptp.Term) this.createVariableTerm(freshVar);
      Quantified quantified = new Quantified(quantifier, sharedCopyOf(freshVar),
          tptp_tester.substituteFormula((Formula) key, boundVariableTerm,
              replaceTerm, this));
      key = (TptpParserOutput.FofFormula) (quantified);
    }
    ;

    // Here the key is fully formed

    Formula res = _formulaTable.get((Formula) key);
    if (res == null) {
      _formulaTable.put((Formula) key, (Formula) key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.CnfFormula createClause(
      Iterable<TptpParserOutput.Literal> literals) {
    return (TptpParserOutput.CnfFormula) (new Clause(literals));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.FofFormula atomAsFormula(
      TptpParserOutput.AtomicFormula atom) {
    return (TptpParserOutput.FofFormula) atom;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public Literal createLiteral(boolean positive,
      TptpParserOutput.AtomicFormula atom) {
    Literal key = new tptp.Literal(positive, atom);
    Literal res = _literalTable.get(key);
    if (res == null) {
      _literalTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.AtomicFormula createPlainAtom(String predicate,
      Iterable<TptpParserOutput.Term> arguments) {
    assert arguments == null || arguments.iterator().hasNext();
    Formula key = new Atomic(sharedCopyOf(predicate), arguments);
    Formula res = _formulaTable.get(key);
    if (res == null) {
      _formulaTable.put(key, key);
      return (TptpParserOutput.AtomicFormula) key;
    } else
      return (TptpParserOutput.AtomicFormula) res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.AtomicFormula createSystemAtom(String predicate,
      Iterable<TptpParserOutput.Term> arguments) {
    assert arguments == null || arguments.iterator().hasNext();
    Formula key = new Atomic(sharedCopyOf(predicate), arguments);
    Formula res = _formulaTable.get(key);
    if (res == null) {
      _formulaTable.put(key, key);
      return (TptpParserOutput.AtomicFormula) key;
    } else
      return (TptpParserOutput.AtomicFormula) res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.AtomicFormula createEqualityAtom(
      TptpParserOutput.Term lhs, TptpParserOutput.Term rhs) {
    String predicate = new String("=");
    LinkedList<TptpParserOutput.Term> arguments = new LinkedList<TptpParserOutput.Term>();
    arguments.add(lhs);
    arguments.add(rhs);
    return createPlainAtom(predicate, arguments);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.AtomicFormula builtInTrue() {
    return BooleanAtomic.TRUE;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.AtomicFormula builtInFalse() {
    return BooleanAtomic.FALSE;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Term createVariableTerm(String variable) {
    Symbol sym = new Symbol(sharedCopyOf(variable), true);
    Term key = new tptp.Term(sym, null);
    Term res = _termTable.get(key);
    if (res == null) {
      _termTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Term createPlainTerm(String function,
      Iterable<TptpParserOutput.Term> arguments) {
    Symbol sym = new Symbol(sharedCopyOf(function), false);
    Term key = new tptp.Term(sym, arguments);
    Term res = _termTable.get(key);
    if (res == null) {
      _termTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Term createSystemTerm(String function,
      Iterable<TptpParserOutput.Term> arguments) {
    Symbol sym = new Symbol(sharedCopyOf(function), false);
    Term key = new tptp.Term(sym, arguments);
    Term res = _termTable.get(key);
    if (res == null) {
      _termTable.put(key, key);
      return key;
    } else
      return res;
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Annotations createAnnotations(
      TptpParserOutput.Source source,
      Iterable<TptpParserOutput.InfoItem> usefulInfo) {
    return new tptp.Annotations(source, usefulInfo);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createSourceFromName(String name) {
    return new tptp.Source.Name(sharedCopyOf(name));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createSourceFromInferenceRecord(
      String inferenceRule, Iterable<TptpParserOutput.InfoItem> usefulInfo,
      Iterable<TptpParserOutput.ParentInfo> parentInfoList) {
    return new tptp.Source.Inference(sharedCopyOf(inferenceRule), usefulInfo,
        parentInfoList);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createInternalSource(
      TptpParserOutput.IntroType introType,
      Iterable<TptpParserOutput.InfoItem> introInfo) {
    return new tptp.Source.Internal(introType, introInfo);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createSourceFromFile(String fileName,
      String fileInfo) {
    return new tptp.Source.File(sharedCopyOf(fileName),
        fileInfo != null ? sharedCopyOf(fileInfo) : null);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createSourceFromCreator(String creatorName,
      Iterable<TptpParserOutput.InfoItem> usefulInfo) {
    return new tptp.Source.Creator(sharedCopyOf(creatorName), usefulInfo);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.Source createSourceFromTheory(String theoryName,
      Iterable<TptpParserOutput.InfoItem> usefulInfo) {
    return new tptp.Source.Theory(sharedCopyOf(theoryName), usefulInfo);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createDescriptionInfoItem(String singleQuoted) {
    return new tptp.InfoItem.Description(sharedCopyOf(singleQuoted));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createIQuoteInfoItem(String singleQuoted) {
    return new tptp.InfoItem.IQuote(sharedCopyOf(singleQuoted));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createInferenceStatusInfoItem(
      TptpParserOutput.StatusValue statusValue) {
    return new tptp.InfoItem.InferenceStatus(statusValue);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createInferenceRuleInfoItem(
      String inferenceRule, String inferenceId,
      Iterable<TptpParserOutput.GeneralTerm> attributes) {
    return new tptp.InfoItem.InferenceRule(sharedCopyOf(inferenceRule),
        sharedCopyOf(inferenceId), attributes);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createRefutationInfoItem(
      TptpParserOutput.Source fileSource) {
    return new tptp.InfoItem.Refutation(fileSource);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.InfoItem createGeneralFunctionInfoItem(
      TptpParserOutput.GeneralTerm generalFunction) {
    return new tptp.InfoItem.GeneralFunction(generalFunction);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.GeneralTerm createGeneralFunction(String function,
      Iterable<TptpParserOutput.GeneralTerm> arguments) {
    return new tptp.GeneralTerm(sharedCopyOf(function), arguments);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.GeneralTerm createGeneralList(
      Iterable<TptpParserOutput.GeneralTerm> list) {
    return new tptp.GeneralTerm(list);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.GeneralTerm createGeneralColon(
      TptpParserOutput.GeneralTerm left, TptpParserOutput.GeneralTerm right) {
    return new tptp.GeneralTerm(left, right);
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.GeneralTerm createGeneralDistinctObject(String str) {
    return new tptp.GeneralTerm(sharedCopyOf(str));
  }

  /** Implements the corresponding spec in TptpParserOutput. */
  public TptpParserOutput.ParentInfo createParentInfo(
      TptpParserOutput.Source source, String parentDetails) {
    return new tptp.ParentInfo(source, parentDetails == null ? null
        : sharedCopyOf(parentDetails));
  }

  /*-------------------------------------------------*/
  /* Methods to be called by the client code: */
  /*-------------------------------------------------*/

  public SimpleTptpParserOutput() {
    _stringTable = new Hashtable<String, String>();
    _termTable = new Hashtable<Term, Term>();
    _literalTable = new Hashtable<Literal, Literal>();
    _formulaTable = new Hashtable<Formula, Formula>();
  } // SimpleTptpParserOutput()

  /**
   * Reinitialises everything. Note that after a call to reset() objects created
   * by various method calls prior to that call to reset(), are considered
   * invalid and should not be used in any way.
   */
  public void reset() {
    _stringTable.clear();
    _termTable.clear();
    _literalTable.clear();
    _formulaTable.clear();
  }

  /*-------------------------------------------------*/
  /* Private methods: */
  /*-------------------------------------------------*/

  /**
   * Returns the copy of <strong> str </strong> stored in _stringTable; the copy
   * is created if necessary.
   */
  private String sharedCopyOf(String str) {
    assert str != null;

    String res = _stringTable.get(str);

    if (res == null) {
      _stringTable.put(str, str);
      return str;
    } else
      return res;
  }

  /*-------------------------------------------------*/
  /* Attributes: */
  /*-------------------------------------------------*/

  /** Maintains sharing of String objects. */
  private Hashtable<String, String> _stringTable;

  /**
   * Maintains sharing of Term objects.
   * 
   * @see methods for creating different kinds of Term objects
   */
  private Hashtable<Term, Term> _termTable;

  /**
   * Maintains sharing of Literal objects.
   * 
   * @see createLiteral(boolean positive,TptpParserOutput.AtomicFormula atom)
   */
  private Hashtable<Literal, Literal> _literalTable;

  /**
   * Maintains sharing of Formula objects.
   * 
   * @see methods for creating different kinds of Formula objects
   */
  private Hashtable<Formula, Formula> _formulaTable;

} // class SimpleParserOutput
