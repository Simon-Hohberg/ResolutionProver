package resolutionprover;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import tptp.Atomic;
import tptp.BooleanAtomic;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.tptp_tester;

public class Expander {

  private Queue<Disjunction> workingQueue;
  public Set<Disjunction> seenDisjunctions;
  public List<Disjunction> trace;

  public Expander() {
    workingQueue = new LinkedList<Disjunction>();
    seenDisjunctions = new HashSet<Disjunction>();
    trace = new LinkedList<Disjunction>();
  }

  /**
   * Exhaustively applies the resolution expansion rules on the given
   * {@link Disjunction}. The resulting {@link Disjunction}s contain only
   * {@link Atomic}s. Atomics will be present in the trace, already.
   * 
   * @param disjunction
   * @return {@link Collection} of {@link Disjunction} containing {@link Atomic}
   *         s only
   */
  public Collection<Disjunction> expand(Disjunction disjunction) {

    seenDisjunctions.clear();
    trace.clear();

    // order is important, since disjunction would not be added to trac/queue
    // when already present in the seen-set
    addToWorkingQueue(disjunction);
    seenDisjunctions.add(disjunction);

    Set<Disjunction> atomicDisjunctions = new HashSet<Disjunction>();

    while (!workingQueue.isEmpty()) {
      Disjunction currentDisjunction = workingQueue.poll();
      Disjunction[] expandedDisjunctions = doExpansion(currentDisjunction);
      if (expandedDisjunctions != null) {
        for (Disjunction d : expandedDisjunctions) {
          addToTrace(d);
          if (d.isFalse())
            return Collections.emptySet();
          if (!d.isTautology()) {
            addToWorkingQueue(d);
          } else
            atomicDisjunctions.add(currentDisjunction);
          seenDisjunctions.add(d);
        }
      } else {
        atomicDisjunctions.add(currentDisjunction);
      }
    }

    return atomicDisjunctions;
  }

  private void addToWorkingQueue(Disjunction d) {
    if (!seenDisjunctions.contains(d))
      workingQueue.add(d);
  }

  private void addToTrace(Disjunction d) {
    if (!seenDisjunctions.contains(d))
      trace.add(d);
  }

  /**
   * Applies the resolution expansion rule on the provided {@link Disjunction}
   * returning the resulting {@link Disjunction}s (expansion will result in more
   * than one {@link Disjunction} only if alpha rule is applied) or
   * <code>null</code> if the disjunction cannot be expanded since it contains
   * only atomic formulas.
   * 
   * @param disjunction
   * @param currentIndex
   * @return The resulting {@link Disjunction}s or <code>null</code> if the
   *         {@link Disjunction} cannot be expanded
   */
  private Disjunction[] doExpansion(Disjunction disjunction) {
    Iterator<Formula> iterator = disjunction.formulae.iterator();
    while (iterator.hasNext()) {
      Formula form = iterator.next();
      switch (form.getKind()) {
      case Boolean:
        if (form == BooleanAtomic.TRUE) {
          disjunction.formulae.clear();
          disjunction.formulae.add(BooleanAtomic.TRUE);
        } else {
          disjunction.formulae.remove(BooleanAtomic.FALSE);
        }
        return new Disjunction[] { disjunction };
      case Atomic:

        // assuming that all elements are atomic if the last element is atomic
        // this is the end of the recursion
        if (!iterator.hasNext())
          return null;
        // atomic formula is not the last, there might be further formulas
        // that have to be reduced
        continue; // continue iteration

      case Negation:

        // apply neg neg rule
        if (tptp_tester.isNegNegFormula(form)) {
          return new Disjunction[] { applyNegNegRule(disjunction, form) };
          // check if argument of single neg is atomic
        } else {
          Formula argument = ((Negation) form).getArgument();
          if (argument.getKind() == Kind.Boolean) {
            return new Disjunction[] { applyBoolean(disjunction,
                (Negation) form) };
          }
          if (argument.getKind() == Kind.Atomic) {
            // similar to Atomic case
            // second case for the end of the recursion
            if (!iterator.hasNext())
              return null;
            // again, there might be further not atomic formulas after this one
            continue; // continue iteration
          }
        }

        // walk through -> binary negation
      case Binary:

        // apply alpha rule
        if (tptp_tester.isAlphaFormula(form)) {
          return applyAlphaRule(disjunction, form);
          // apply beta rule
        } else if (tptp_tester.isBetaFormula(form)) {
          return new Disjunction[] { applyBetaRule(disjunction, form) };
        }

      default:
        throw new IllegalStateException("Unexpected kind of formula " + form);
      }
    }
    assert false;
    return null;
  }

  private Disjunction applyBetaRule(Disjunction disjunction, Formula form) {
    Disjunction newDisjunction;
    Formula beta1 = tptp_tester.getBeta1(form);
    Formula beta2 = tptp_tester.getBeta2(form);

    /*
     * create a new disjunction that contains all formulas of the old one,
     * except the current formula and additionally contains the betas,
     * recursively reduce this new disjunction
     */
    newDisjunction = Util.replaceElement(disjunction, form, beta1);
    newDisjunction.rule = Rule.BETA;
    newDisjunction.origin.add(disjunction);
    if (newDisjunction.isTautology())
      return newDisjunction;
    newDisjunction.formulae.add(beta2);

    return newDisjunction;
  }

  private Disjunction applyBoolean(Disjunction disjunction, Negation negatedBool) {
    Disjunction newDisjunction;
    BooleanAtomic resultAtomic = Util.negate((BooleanAtomic) negatedBool
        .getArgument());
    if (resultAtomic == BooleanAtomic.TRUE) {
      newDisjunction = new Disjunction(disjunction, resultAtomic);
    } else {
      newDisjunction = Util.replaceElement(disjunction, negatedBool,
          resultAtomic);
      newDisjunction.rule = Rule.BOOLEAN;
      newDisjunction.origin.add(disjunction);
    }

    return newDisjunction;
  }

  /**
   * Creates two new {@link Disjunction}s by applying the alpha rule to the
   * given {@link Formula}.
   * 
   * @param disjunction
   * @param form
   * @return Two new {@link Disjunction}s containing alpha1, alpha2 respectively
   */
  private Disjunction[] applyAlphaRule(Disjunction disjunction, Formula form) {
    Formula alpha1 = tptp_tester.getAlpha1(form);
    Formula alpha2 = tptp_tester.getAlpha2(form);

    // alpha1
    Disjunction alpha1Disjunction = Util.replaceElement(disjunction, form,
        alpha1);
    alpha1Disjunction.rule = Rule.ALPHA1;
    alpha1Disjunction.origin.add(disjunction);

    // alpha2
    Disjunction alpha2Disjunction = Util.replaceElement(disjunction, form,
        alpha2);
    alpha2Disjunction.origin.add(disjunction);
    alpha2Disjunction.rule = Rule.ALPHA2;

    return new Disjunction[] { alpha1Disjunction, alpha2Disjunction };
  }

  private Disjunction applyNegNegRule(Disjunction disjunction, Formula form) {
    Disjunction newDisjunction;
    Formula newForm = ((Negation) ((Negation) form).getArgument())
        .getArgument();
    newDisjunction = Util.replaceElement(disjunction, form, newForm);
    newDisjunction.rule = Rule.NEGNEG;
    newDisjunction.origin.add(disjunction);

    return newDisjunction;
  }

  public List<Disjunction> getTrace() {
    return trace;
  }
}
