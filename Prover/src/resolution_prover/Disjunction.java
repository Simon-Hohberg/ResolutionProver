package resolution_prover;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import tptp.Formula;

public class Disjunction implements Comparable<Disjunction> {

  public SortedSet<Formula> formulae;

  /**
   * Index of this disjunction
   */
  public int index = -1;

  /**
   * Index of the disjunction that is the origin for this disjunction
   */
  public List<Integer> origin = null;

  /**
   * Rule that produced this disjunction
   */
  public Rule rule;

  /**
   * Has this {@link Disjunction} any derived {@link Disjunction}s? TODO should
   * use a real tree for derivations
   */
  private Disjunction[] derivations = new Disjunction[0];

  public Disjunction(Formula... formulae) {
    this.formulae = new TreeSet<Formula>();
    for (Formula formula : formulae) {
      this.formulae.add(formula);
    }
  }

  public Disjunction(SortedSet<Formula> formulae) {
    this.formulae = formulae;
  }

  public Disjunction(int origin, Formula... formulae) {
    this(formulae);
    this.origin = new ArrayList<Integer>();
    this.origin.add(origin);
  }

  public Disjunction(int origin, SortedSet<Formula> formulae) {
    this.formulae = formulae;
    this.origin = new ArrayList<Integer>();
    this.origin.add(origin);
  }

  public Disjunction(int index, int origin, Formula... formulae) {
    this(formulae);
    this.origin = new ArrayList<Integer>();
    this.origin.add(origin);
    this.index = index;
  }

  public Disjunction(int index, int origin, SortedSet<Formula> formulae) {
    this.formulae = formulae;
    this.origin = new ArrayList<Integer>();
    this.origin.add(origin);
    this.index = index;
  }

  @Override
  public String toString() {
    if (rule == null || origin == null)
      return formulae.toString();
    return String.format("%s  %s(%s)", formulae.toString(), rule.humanReadable,
        Util.collectionToString(origin));
  }

  public String toString(int commentIndent) {
    String formulaeString = formulae.toString();
    if (rule == null || origin == null)
      return formulaeString;
    int formulaeStringLength = formulaeString.length();
    StringBuilder builder = new StringBuilder(formulaeString);
    for (int i = formulaeStringLength; i < commentIndent; i++)
      builder.append(" ");
    builder.append(String.format("%s(%s)", rule.humanReadable, Util
        .collectionToString(origin)));
    return builder.toString();
  }

  public void derivation(Disjunction... disjunctions) {
    derivations = disjunctions;
  }

  public boolean has_derivations() {
    return derivations.length > 0;
  }

  public List<Disjunction> derivations() {
    LinkedList<Disjunction> derived = new LinkedList<Disjunction>();
    if (has_derivations())
      for (Disjunction d : derivations)
        derived.addAll(d.derivations());
    else
      derived.add(this);
    return derived;
  }

  @Override
  public int compareTo(Disjunction o) {
    return new Integer(index).compareTo(o.index);
  }

  public boolean isEmpty() {
    return formulae.isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Disjunction))
      return false;
    Disjunction comp = (Disjunction) obj;
    for (Formula f : formulae) {
      if (!comp.formulae.contains(f))
        return false;
    }
    return true;
  }
}
