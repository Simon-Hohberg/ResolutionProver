package resolutionprover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import tptp.AnnotatedFormula;
import tptp.Atomic;
import tptp.Binary;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.Quantified;
import tptp.SimpleTptpParserOutput;
import tptp.Symbol;
import tptp.Term;
import tptp.TptpParserOutput;
import tptp.TptpParserOutput.BinaryConnective;
import tptp.TptpParserOutput.Quantifier;
import tptp.tptp_tester;

public class FirstOrderProver extends PropositionalProver {

  private SimpleTptpParserOutput out = new SimpleTptpParserOutput();

  public FirstOrderProver(AnnotatedFormula... formulae) {
    super(formulae);
  }

  public FirstOrderProver(Collection<Formula> axioms, Formula... conjectures) {
    super(axioms, conjectures);
  }

  @Override
  public boolean prove() {

    System.out.println("\n************ First Order Proof ************");

    List<Disjunction> propositionalDisjunctions = new LinkedList<Disjunction>();

    for (Disjunction disjunction : disjunctions) {
      Disjunction prenex = prenex(disjunction);
      trace.add(prenex);
      Disjunction skolemizedDisjunction = skolemize(prenex);
      trace.add(skolemizedDisjunction);
      Disjunction notQuantifiedDisjunction = removeAllQuantifier(skolemizedDisjunction);
      // could be the same
      if (!skolemizedDisjunction.equals(notQuantifiedDisjunction)) {
        trace.add(notQuantifiedDisjunction);
        propositionalDisjunctions.add(notQuantifiedDisjunction);
      } else {
        propositionalDisjunctions.add(skolemizedDisjunction);
      }
    }

    disjunctions = propositionalDisjunctions;

    boolean result = super.prove();

//    if (!result && !rest.isEmpty()) {
//      unify();
//      performResolution(rest);
//    }

    return result;
  }

  private void unify() {
    Map<Term, Term> mapping = new HashMap<Term, Term>();
    Map<String, List<Disjunction>> predicates = new HashMap<String, List<Disjunction>>();
    for (Disjunction disjunction : rest) {
      for (Formula formula : disjunction.formulae) {
        if (formula.getKind() == Kind.Atomic) {
          String predicate = ((Atomic) formula).getPredicate();
          List<Disjunction> disjunctions = predicates.get(predicate);
          if (disjunctions == null)
            predicates.put(predicate,
                disjunctions = new LinkedList<Disjunction>());
          disjunctions.add(disjunction);
        }
      }
    }
    Disjunction last = null;
    for (Entry<String, List<Disjunction>> entry : predicates.entrySet()) {
      String predicate = entry.getKey();
      for (Disjunction disjunction : entry.getValue()) {
        if (last != null) {
          Formula formula1, formula2;
          formula1 = last.formulae.iterator().next();
          formula2 = disjunction.formulae.iterator().next();
          if (formula1.getKind() == Kind.Atomic && formula2.getKind() == Kind.Atomic) {
            Atomic atomic1, atomic2;
            atomic1 = (Atomic) formula1;
            atomic2 = (Atomic) formula2;
            Iterator<Term> iterator1 = atomic1.getArguments().iterator();
            Iterator<Term> iterator2 = atomic2.getArguments().iterator();
            while (iterator1.hasNext() && iterator2.hasNext()) {
              Term t1 = iterator1.next();
              Term t2 = iterator2.next();
              
            }
          } else
            throw new IllegalStateException("FOOOOOO");
        }
        last = disjunction;
      }
    }
  }

  private Disjunction prenex(Disjunction disjunction) {
    boolean applied = false;
    List<Formula> prenexes = new ArrayList<Formula>(disjunction.formulae.size());
    for (Formula formula : disjunction.formulae) {
      Prenex prenex = new Prenex(formula);
      Formula prenexed = prenex.getPrenex();
      if (prenexed != null) {
        applied = true;
        prenexes.add(prenexed);
      } else
        prenexes.add(formula);
    }

    Disjunction result;
    if (applied) {
      result = new Disjunction(disjunction, prenexes);
      result.rule = Rule.PRENEX;
    } else
      result = disjunction;

    return result;
  }

  private Disjunction removeAllQuantifier(Disjunction disjunction) {
    Set<Formula> formulae = new HashSet<Formula>();
    for (Formula formula : disjunction.formulae) {
      formulae.add(removeAllQuantifiers(formula));
    }
    Disjunction resultDisjunction = new Disjunction(formulae);
    resultDisjunction.origin.add(disjunction);
    resultDisjunction.rule = Rule.DROP_ALL;
    return resultDisjunction;
  }

  private Formula removeAllQuantifiers(Formula formula) {
    if (formula.getKind() != Kind.Quantified)
      return formula;
    Quantified quantified = (Quantified) formula;
    // formula is quantified, there should be only all-quantified formulae
    if (quantified.getQuantifier() == Quantifier.ForAll) {
      return removeAllQuantifiers(quantified.getMatrix());
    } else {
      throw new IllegalStateException(
          "cannot remove all-quantifier since formula isn't all quantified: "
              + formula);
    }
  }

  private Disjunction skolemize(Disjunction disjunction) {
    List<Formula> formulae = new ArrayList<Formula>(disjunction.formulae.size());
    for (Formula formula : disjunction.formulae) {
      Formula localFormula = formula;
      Set<String> seenAllQuantified = new HashSet<String>();
      while (localFormula != null) {

        // all
        if (tptp_tester.isGammaFormula(localFormula)) {
          Formula innerFormular = tptp_tester.getGammaX(localFormula);
          seenAllQuantified.add(extractQuantifiedVariable(localFormula));
          localFormula = innerFormular;

          // exists
        } else if (tptp_tester.isDeltaFormula(localFormula)) {
          localFormula = tptp_tester.getDeltaT(localFormula,
              createSkolem(seenAllQuantified), out);
        } else {
          Formula newFormula = localFormula;
          formulae.add((Formula) out.createQuantifiedFormula(Quantifier.ForAll,
              seenAllQuantified, newFormula));
          localFormula = null;
        }
      }
    }
    Disjunction skolemizedDisjunction = new Disjunction(disjunction, formulae);
    skolemizedDisjunction.rule = Rule.SKOLEMIZATION;
    return skolemizedDisjunction;
  }

  private Term createSkolem(Set<String> seenAllQuantified) {
    // if there have not been seen any free variables yet, just use a new
    // constant
    if (seenAllQuantified.isEmpty()) {
      // TODO is this a new constant?
      return new Term(new Symbol(tptp_tester.freshVariableName(), false), null);
    }
    Set<TptpParserOutput.Term> terms = new HashSet<TptpParserOutput.Term>();
    for (String variable : seenAllQuantified) {
      terms.add(new Term(new Symbol(variable, true), null));
    }
    // TODO is this a new function?
    return new Term(new Symbol(tptp_tester.freshVariableName(), false), terms);
  }

  /**
   * Extracts the variable name used by the quantifier of the given gamma or
   * delta formula.
   * 
   * @param formula
   * @return Variable name referenced by the quantifier of the provided formula
   */
  private String extractQuantifiedVariable(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula quantified = ((Negation) formula).getArgument();
      if (quantified.getKind() == Kind.Quantified) {
        return ((Quantified) quantified).getVariable();
      }
      throw new IllegalStateException(String.format(
          "Cannot extract variable from formula %s that is not quantified",
          quantified));
    case Quantified:
      return ((Quantified) formula).getVariable();
    default:
      throw new IllegalStateException(String.format(
          "Cannot extract variable from formula %s that is not quantified",
          formula));
    }
  }
}
