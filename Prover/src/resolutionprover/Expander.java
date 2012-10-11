package resolutionprover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
	private int index;
	public Set<Disjunction> trace;
	
	public Expander() {
		workingQueue = new LinkedList<Disjunction>();
		trace = new HashSet<Disjunction>();
	}
	
	/**
	 * Exhaustively applies the resolution expansion rules on the given 
	 * {@link Disjunction}. The resulting {@link Disjunction}s contain only
	 * {@link Atomic}s.
	 * @param disjunction
	 * @return {@link Collection} of {@link Disjunction} containing 
	 * {@link Atomic}s only
	 */
	public Collection<Disjunction> expand(Disjunction disjunction) {
		
		trace.clear();
		Set<Disjunction> atomicDisjunctions = new HashSet<Disjunction>();
		index = disjunction.index;
		workingQueue.add(disjunction);
		
		while (!workingQueue.isEmpty()) {
			Disjunction currentDisjunction = workingQueue.poll();
			Disjunction[] expandedDisjunctions = doExpansion(currentDisjunction);
			if (expandedDisjunctions != null) {
				for (Disjunction d : expandedDisjunctions) {
					if (!d.isTautology()) {
						workingQueue.add(d);
						trace.add(d);
					}
				}
			} else {
				atomicDisjunctions.add(currentDisjunction);
			}
		}
		
		return atomicDisjunctions;
	}
	
	/**
	 * Applies the resolution expansion rule on the provided {@link Disjunction}
	 * returning the resulting {@link Disjunction}s (expansion will result
	 * in more than one {@link Disjunction} only if alpha rule is applied) or
	 * <code>null</code> if the disjunction cannot be expanded since it contains
	 * only atomic formulas.
	 * @param disjunction
	 * @param currentIndex
	 * @return The resulting {@link Disjunction}s or <code>null</code>
	 * if the {@link Disjunction} cannot be expanded
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
				
				//assuming that all elements are atomic if the last element is atomic
				//this is the end of the recursion
				if (!iterator.hasNext())
					return null;
				//atomic formula is not the last, there might be further formulas
				//that have to be reduced
				continue;	//continue iteration

			case Negation:
				
				//apply neg neg rule
				if (tptp_tester.isNegNegFormula(form)) {
					return new Disjunction[] { applyNegNegRule(disjunction, form) };
				//check if argument of single neg is atomic
				} else if ((((Negation) form).getArgument()).getKind() == Kind.Atomic) {
					//similar to Atomic case
					//second case for the end of the recursion
					if (!iterator.hasNext())
						return null;
					//again, there might be further not atomic formulas after this one
					continue;	//continue iteration
				}
				
				//walk through -> binary negation
			case Binary:
				
				//apply alpha rule
				if (tptp_tester.isAlphaFormula(form)) {
					return applyAlphaRule(disjunction, form);
				//apply beta rule
				} else if (tptp_tester.isBetaFormula(form)) {
					return new Disjunction[] { applyBetaRule(disjunction,  form)};
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
		 * create a new disjunction that contains all formulas of 
		 * the old one, except the current formula and additionally 
		 * contains the betas, recursively reduce this new 
		 * disjunction
		 */
		newDisjunction = Util.replaceElement(disjunction, form, beta1);
		if (newDisjunction.isTautology())
			return newDisjunction;
		newDisjunction.formulae.add(beta2);
		newDisjunction.rule = Rule.BETA;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(disjunction.index);
		newDisjunction.index = ++index;
		
		return newDisjunction;
	}

	/**
	 * Creates two new {@link Disjunction}s by applying the alpha rule to the
	 * given {@link Formula}.
	 * @param disjunction
	 * @param form
	 * @return Two new {@link Disjunction}s containing alpha1, alpha2 
	 * respectively
	 */
	private Disjunction[] applyAlphaRule(Disjunction disjunction, Formula form) {
		Formula alpha1 = tptp_tester.getAlpha1(form);
		Formula alpha2 = tptp_tester.getAlpha2(form);

		//alpha1
		Disjunction alpha1Disjunction = Util.replaceElement(disjunction, form, alpha1);
		alpha1Disjunction.rule = Rule.ALPHA1;
		alpha1Disjunction.origin = new ArrayList<Integer>();
		alpha1Disjunction.origin.add(disjunction.index);
		alpha1Disjunction.index = ++index;
		
		//alpha2
		Disjunction alpha2Disjunction = Util.replaceElement(disjunction, form, alpha2);
		alpha2Disjunction.origin = new ArrayList<Integer>();
		alpha2Disjunction.origin.add(disjunction.index);
		alpha2Disjunction.rule = Rule.ALPHA2;
		alpha2Disjunction.index = ++index;
		
		return new Disjunction[] { alpha1Disjunction, alpha2Disjunction };
	}

	private Disjunction applyNegNegRule(Disjunction disjunction, Formula form) {
		Disjunction newDisjunction;
		Formula newForm = ((Negation)((Negation) form).getArgument()).getArgument();
		newDisjunction = Util.replaceElement(disjunction, form, newForm);
		if (newDisjunction == null)
			return null;
		newDisjunction.rule = Rule.NEGNEG;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(disjunction.index);
		newDisjunction.index = ++index;
		
		return newDisjunction;
	}
	
	public Set<Disjunction> getTrace() {
		return trace;
	}
}
