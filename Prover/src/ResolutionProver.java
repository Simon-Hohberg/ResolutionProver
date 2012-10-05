import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import tptp.Atomic;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;


public class ResolutionProver {

	private SortedSet<Disjunction> trace;
	private Queue<Disjunction> workingQueue;
	private Map<Formula, Set<Disjunction>> resolutionMap;
	
	private boolean isTautology;

	public ResolutionProver(List<Formula> formulae) {
	  Formula[] nformulae = new Formula[formulae.size()];
	  for (int i = 0; i < formulae.size(); i++) {
	    nformulae[i] = new Negation(formulae.get(i));
	  }
		trace = new TreeSet<Disjunction>();
		workingQueue = new LinkedList<Disjunction>();
		resolutionMap = new HashMap<Formula, Set<Disjunction>>();
		//negate formula
		Disjunction disjunction = new Disjunction(1, -1, nformulae);
		//initialize
		workingQueue.add(disjunction);
		isTautology = false;
	}
	
	public boolean prove() {
		
		while (!workingQueue.isEmpty() && !isTautology) {
			Disjunction disjunction = workingQueue.poll();
			disjunction.index = trace.size() + 1;
			trace.add(disjunction);
			reduce(disjunction);
		}
		
		return isTautology;
	}
	
	public void printTrace() {
		int commentIndent = Util.calculateCommentIndent(trace);
		int indexLength = String.format("%d", trace.size()).length();
		System.out.println();
		int i = 0;
		for (Disjunction disjunction : trace) {
			if (i+1 != disjunction.index)
				throw new IllegalStateException("Index of disjunction and its index in the trace list do not match! " + disjunction.index + " != " + (i+1));
			StringBuilder builder = new StringBuilder();
			builder.append(i+1);
			builder.append(". ");
			
			for (int j = String.format("%d", (i+1)).length(); j < indexLength; j++) {
				builder.append(" ");
			}
			builder.append(disjunction.toString(commentIndent));
			System.out.println(builder.toString());
			i++;
		}
	}

	/**
	 * Recursively reduces the given {@link Disjunction}'s complexity by 
	 * applying either the resolution rule or one of the expansion rules. Also 
	 * checks if the {@link Disjunction} is already empty.
	 * @param disjunction
	 */
	private void reduce(Disjunction disjunction) {
		int currentIndex = trace.size();	//we are always working on the last disjunction, index starts at 1
		printTrace();
		if (isTautology)
			return;
		
		if (isTautology = disjunction.isEmpty())
			return;
		
		//check if resolution rule can be applied
		if (canApplyResolution(disjunction))
			doResultion(disjunction);
		
		updateResolutionMap(disjunction);
		
		//expand disjunction and do recursion
		Disjunction newDisjunction = doExpansion(disjunction, currentIndex);
		if (newDisjunction != null)
			reduce(newDisjunction);
	}

	/**
	 * Applies the resolution expansion rule on the provided {@link Disjunction}
	 * returning one of the resulting {@link Disjunction}s (expansion will result
	 * in more than one {@link Disjunction} only if alpha rule is applied).
	 * @param disjunction
	 * @param currentIndex
	 * @return One of the resulting {@link Disjunction}s.
	 */
	private Disjunction doExpansion(Disjunction disjunction, int currentIndex) {
		Iterator<Formula> iterator = disjunction.formulae.iterator();
		while (iterator.hasNext()) {
			Formula form = iterator.next();
			
			switch (form.getKind()) {
			
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
					return applyNegNegRule(disjunction, currentIndex, form);
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
					return applyAlphaRule(disjunction, currentIndex, form);
				//apply beta rule
				} else if (tptp_tester.isBetaFormula(form)) {
					return applyBetaRule(disjunction, currentIndex, form);
				}
				
			default:
				throw new IllegalStateException("Unexpected kind of formula " + form);
			}
		}
		return null;
	}

	private Disjunction applyBetaRule(Disjunction disjunction,
			int currentIndex, Formula form) {
		Disjunction newDisjunction;
		Formula beta1 = tptp_tester.getBeta1(form);
		Formula beta2 = tptp_tester.getBeta2(form);
		
		/*
		 * create a new disjunction that contains all formulas of 
		 * the old one, except the current formula and additionally 
		 * contains the betas, recursively reduce this new 
		 * disjunction
		 */
		newDisjunction = replaceElement(disjunction, form, beta1);
		newDisjunction.formulae.add(beta2);
		newDisjunction.rule = Rule.BETA;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(currentIndex);
		newDisjunction.index = currentIndex + 1;
		addToTrace(newDisjunction);
		
		return newDisjunction;
	}

	private Disjunction applyAlphaRule(Disjunction disjunction,
			int currentIndex, Formula form) {
		Disjunction newDisjunction;
		Formula alpha1 = tptp_tester.getAlpha1(form);
		Formula alpha2 = tptp_tester.getAlpha2(form);
		//recursively reduce one of the alphas and save the other one for later reduction
		Disjunction alpha2Disjunction = replaceElement(disjunction, form, alpha2);
		alpha2Disjunction.origin = new ArrayList<Integer>();
		alpha2Disjunction.origin.add(currentIndex);
		alpha2Disjunction.rule = Rule.ALPHA2;
		addToWorkingQueue(alpha2Disjunction);	//TODO not added to resolution map, yet
		
		newDisjunction = replaceElement(disjunction, form, alpha1);
		newDisjunction.rule = Rule.ALPHA1;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(currentIndex);
		newDisjunction.index = currentIndex + 1;
		addToTrace(newDisjunction);
		
		return newDisjunction;
	}

	private Disjunction applyNegNegRule(Disjunction disjunction,
			int currentIndex, Formula form) {
		Disjunction newDisjunction;
		Formula newForm = ((Negation)((Negation) form).getArgument()).getArgument();
		newDisjunction = replaceElement(disjunction, form, newForm);
		newDisjunction.rule = Rule.NEGNEG;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(currentIndex);
		newDisjunction.index = currentIndex + 1;
		addToTrace(newDisjunction);
		
		return newDisjunction;
	}

	/**
	 * Applies the resolution rule on all {@link Disjunction}s that share a 
	 * formula that is equal to one formula of the provided {@link Disjunction}
	 * and adds the newly created {@link Disjunction}s to the working queue.
	 * @param disjunction
	 */
	private void doResultion(Disjunction disjunction) {
		Formula resolutionFormula = getFormulaForResolution(disjunction);
		if (resolutionFormula == null)
			return;
		//get all disjunctions with which resolution can be done
		Set<Disjunction> resolutionDisjunctions = resolutionMap.get(resolutionFormula);
		for (Disjunction resolutionDisjunction : resolutionDisjunctions) {
			if (resolutionDisjunction.equals(disjunction))
				continue;
			Disjunction newDisjunction = applyResolution(disjunction, resolutionDisjunction, resolutionFormula);
			newDisjunction.origin = new ArrayList<Integer>();
			newDisjunction.origin.add(disjunction.index);
			newDisjunction.origin.add(resolutionDisjunction.index);
			newDisjunction.rule = Rule.RESOULUTION;
			addToWorkingQueue(newDisjunction);
		}
	}

	/**
	 * Searches for a {@link Formula} that is in the provided {@link Disjunction}
	 * and the negation of the {@link Formula} is in any other {@link Disjunction}.
	 * @param disjunction
	 * @return {@link Formula} that is contained in the given {@link Disjunction}
	 * and the negation in any other {@link Disjunction} or <code>null</code> if 
	 * there is none.
	 */
	private Formula getFormulaForResolution(Disjunction disjunction) {
		for (Formula formula : disjunction.formulae) {
			Formula negFormula;
			if (formula.getKind() == Kind.Negation) {
				negFormula = ((Negation)formula).getArgument();
			} else {
				negFormula = new Negation(formula);
			}
			Set<Disjunction> disjunctions = resolutionMap.get(negFormula);
			if (disjunctions != null)
				if (!disjunctions.isEmpty())
					if (disjunctions.size() > 1)
						return negFormula.getKind() == Kind.Negation ? formula : negFormula;	//return positive formula
					else if (!disjunctions.iterator().next().equals(disjunction))
						return negFormula.getKind() == Kind.Negation ? formula : negFormula;	//return positive formula
		}
		return null;
	}

	/**
	 * Updates the resolutionMap by adding the given {@link Disjunction}. 
	 * Associates all {@link Formula}e in the disjunction with it.
	 * @param disjunction
	 */
	private void updateResolutionMap(Disjunction disjunction) {
		for (Formula formula : disjunction.formulae) {
			Set<Disjunction> disjunctions = resolutionMap.get(formula);
			if (disjunctions == null) {
				disjunctions = new TreeSet<Disjunction>();
				resolutionMap.put(formula, disjunctions);
			}
			disjunctions.add(disjunction);
		}
	}

	/**
	 * Replaces one {@link Formula} with another one and removes negations and
	 * corresponding positives.
	 * @param formulae
	 * @param replacedForm
	 * @param newForm
	 * @return A new {@link SortedSet} containing all {@link Formula}e from the
	 * provided set except replacedForm
	 */
	private SortedSet<Formula> replaceElement(Set<Formula> formulae, Formula replacedForm, Formula newForm) {
		SortedSet<Formula> newFormulae = new TreeSet<Formula>(formulae);
		newFormulae.remove(replacedForm);
		//prevent positives and negatives in one disjunctions
		Formula negation = newForm.getKind() == Kind.Negation ? ((Negation)newForm).getArgument() : new Negation(newForm);
		if (newFormulae.contains(negation)) {
			newFormulae.remove(negation);
		} else {
			newFormulae.add(newForm);
		}
		return newFormulae;
	}
	
	/**
	 * Replaces one {@link Formula} with another one and removes negations and
	 * corresponding positives.
	 * @param disjunction
	 * @param replacedForm
	 * @param newForm
	 * @return A new {@link SortedSet} containing all {@link Formula}e from the
	 * provided set except replacedForm
	 */
	private Disjunction replaceElement(Disjunction disjunction, Formula replacedForm, Formula newForm) {
		return new Disjunction(replaceElement(disjunction.formulae, replacedForm, newForm));
	}
	
	/**
	 * Applies the resolution rule on both provided {@link Disjunction}s removing
	 * all positive and negated occurrences of the given {@link Form}.
	 * @param disjunction1
	 * @param disjunction2
	 * @param form
	 * @return A new {@link Disjunction} containing all {@link Formula}e of the
	 * two given {@link Disjunction}s except the provided {@link Formula} and
	 * negations of it.
	 */
	private Disjunction applyResolution(Disjunction disjunction1, Disjunction disjunction2, Formula form) {
		SortedSet<Formula> newFormulae = new TreeSet<Formula>();
		for (Formula f : disjunction1.formulae) {
			if (f.equals(form)) 
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation)f).getArgument().equals(form))
					continue;
			newFormulae.add(f);
		}
		for (Formula f : disjunction2.formulae) {
			if (f.equals(form)) 
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation)f).getArgument().equals(form))
					continue;
			//prevent positives and negatives in one disjunction
			Formula negation = f.getKind() == Kind.Negation ? ((Negation)f).getArgument() : new Negation(f);
			if (newFormulae.contains(negation)) {
				newFormulae.remove(negation);
			} else {
				newFormulae.add(f);
			}
		}
		return new Disjunction(newFormulae);
	}
	
	/**
	 * Checks if there is any disjunction that shares a negated {@link Formula} of
	 * one of the {@link Formula}e of the provided {@link Disjunction}.
	 * This {@link Formula} does not have to be {@link Atomic}, but can be more
	 * complex.
	 * @param disjunction
	 * @return
	 */
	private boolean canApplyResolution(Disjunction disjunction) {
		return getFormulaForResolution(disjunction) != null;
	}
	
	private void addToWorkingQueue(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			workingQueue.clear();
			isTautology = true;
			disjunction.index = trace.size()+1;
			trace.add(disjunction);
			return;
		}
		if (trace.contains(disjunction))
			return;
		workingQueue.add(disjunction);
	}
	
	private void addToTrace(Disjunction disjunction) {
		if (trace.contains(disjunction))
			return;
		trace.add(disjunction);
	}
}
