package resolutionprover;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tptp.BooleanAtomic;
import tptp.Formula;


public class Disjunction implements Comparable<Disjunction> {

	public Set<Formula> formulae;
	
	/**
	 * Index of this disjunction
	 */
	public int index = -1;
	
	/**
	 * Index of the disjunction that is the origin for this disjunction
	 */
	public List<Disjunction> origin = null;
	
	/**
	 * Rule that produced this disjunction
	 */
	public Rule rule;
	
	public Disjunction(Formula... formulae) {
		this.origin = new ArrayList<Disjunction>();
		this.formulae = new HashSet<Formula>();
		for (Formula formula : formulae) {
			this.formulae.add(formula);
		}
	}
	
	public Disjunction(Collection<Formula> formulae) {
		this.formulae = new HashSet<Formula>(formulae);
		this.origin = new ArrayList<Disjunction>();
	}
	
	public Disjunction(Disjunction origin, Formula... formulae) {
		this(formulae);
		this.origin.add(origin);
	}
	
	public Disjunction(Disjunction origin, Collection<Formula> formulae) {
		this.formulae = new HashSet<Formula>(formulae);
		this.origin = new ArrayList<Disjunction>();
		this.origin.add(origin);
	}
	
	public Disjunction(int index, Disjunction origin, Formula... formulae) {
		this(formulae);
		this.origin.add(origin);
		this.index = index;
	}
	
	public Disjunction(int index, Disjunction origin, Collection<Formula> formulae) {
		this.formulae = new HashSet<Formula>(formulae);
		this.origin = new ArrayList<Disjunction>();
		this.origin.add(origin);;
		this.index = index;
	}
	
	@Override
	public String toString() {
		if (rule == null || origin == null)
			return formulae.toString();
		Set<Integer> originIndizes = new HashSet<Integer>();
		for (Disjunction d : origin)
			originIndizes.add(d.index);
		if (rule == Rule.AXIOM || rule == Rule.CONJECTURE)
			return String.format("%s  (%s)", formulae.toString(), rule.humanReadable);
		return String.format("%s  (from %s by applying %s)", formulae.toString(), Util.collectionToString(originIndizes), rule.humanReadable);
	}
	
	public String toString(int commentIndent) {
		String formulaeString = formulae.toString();
		if (rule == null || origin == null)
			return formulaeString;
		int formulaeStringLength = formulaeString.length();
		StringBuilder builder = new StringBuilder(formulaeString);
		for (int i = formulaeStringLength;  i < commentIndent; i++)
			builder.append(" ");
		Set<Integer> originIndizes = new HashSet<Integer>();
		for (Disjunction d : origin)
			originIndizes.add(d.index);
		if (rule == Rule.AXIOM || rule == Rule.CONJECTURE)
			builder.append(String.format("(%s)", rule.humanReadable));
		else
			builder.append(String.format("(from %s by applying %s)", Util.collectionToString(originIndizes), rule.humanReadable));
		return builder.toString();
	}
	
	@Override
	public int compareTo(Disjunction o) {
	  return new Integer(formulae.size()).compareTo(o.formulae.size());
	}
	
	public boolean isEmpty() {
		if (formulae.isEmpty())
			return true;
		if (formulae.size() == 1)
			if (formulae.iterator().next().equals(BooleanAtomic.FALSE))
				return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((formulae == null) ? 0 : formulae.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Disjunction other = (Disjunction) obj;
		if (formulae == null) {
			if (other.formulae != null)
				return false;
		} else if (!formulae.equals(other.formulae))
			return false;
		return true;
	}

	public boolean isTautology() {
		if (formulae.size() == 1)
			return formulae.iterator().next().equals(BooleanAtomic.TRUE);
		return false;
	}
}
