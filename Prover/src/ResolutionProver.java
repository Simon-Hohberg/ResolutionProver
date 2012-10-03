import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import tptp.Formula;
import tptp.Kind;
import tptp.Negation;


public class ResolutionProver {

	private List<Disjunction> resolutionList;
	private Queue<Disjunction> workingQueue;
	private Map<Formula, Set<Disjunction>> resolutionMap;
	
	private boolean isTautology;

	public ResolutionProver(Formula formula) {
		resolutionList = new ArrayList<Disjunction>();
		workingQueue = new LinkedList<Disjunction>();
		resolutionMap = new HashMap<Formula, Set<Disjunction>>();
		//negate formula
		Disjunction disjunction = new Disjunction(1, -1, new Negation(formula));
		//initialize
		workingQueue.add(disjunction);
		isTautology = false;
	}
	
	public boolean prove() {
		
		while (!workingQueue.isEmpty() && !isTautology) {
			Disjunction disjunction = workingQueue.poll();
			disjunction.index = resolutionList.size() + 1;
			resolutionList.add(disjunction);
			reduce(disjunction);
		}
		
		return isTautology;
	}
	
	public void printTrace() {
		int commentIndent = calculateCommentIndent();
		int indexLength = String.format("%d", resolutionList.size()).length();
		System.out.println();
		for (int i = 0; i < resolutionList.size(); i++) {
			Disjunction disjunction = resolutionList.get(i);
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
		}
	}

	private int calculateCommentIndent() {
		int maxLength = 0;
		for (Disjunction disjunction : resolutionList) {
			int length = disjunction.formulae.toString().length();
			if (length > maxLength)
				maxLength = length;
		}
		return maxLength + 2;
	}

	private void reduce(Disjunction disjunction) {
		
		int currentIndex = resolutionList.size();	//we are always working on the last disjunction, index starts at 1
		
		//empty disjunction => tautology
		if (disjunction.formulae.isEmpty()) {
			isTautology = true;
			return;
		}
		
		Disjunction newDisjunction;
		
		//check if resolution rule can be applied
		Formula resolutionFormula = getFormulaForResolution(disjunction);
		if (resolutionFormula != null) {
			Disjunction resolutionDisjunction = resolutionMap.get(resolutionFormula).iterator().next();
			newDisjunction = applyResolution(disjunction, resolutionDisjunction, resolutionFormula);
			newDisjunction.origin = new ArrayList<Integer>();
			newDisjunction.origin.add(disjunction.index);
			newDisjunction.origin.add(resolutionDisjunction.index);
			newDisjunction.rule = Rule.RESOULUTION;
			newDisjunction.index = currentIndex + 1;
			resolutionList.add(newDisjunction);
			reduce(newDisjunction);
			return;
		}
		
		updateResolutionMap(disjunction);
		
		//iterate over formulas in this disjunction
		for (int i = 0; i < disjunction.formulae.size(); i++) {
			Formula form = disjunction.formulae.get(i);
			
			switch (form.getKind()) {
			
			case Atomic:
				
				//assuming that all elements are atomic if the last element is atomic
				//this is the end of the recursion
				if (i == disjunction.formulae.size() - 1)
					return;
				//atomic formula is not the last, there might be further formulas
				//that have to be reduced
				continue;	//continue iteration

			case Negation:
				
				//apply neg neg rule
				if (tptp_tester.isNegNegFormula(form)) {
					Formula newForm = ((Negation)((Negation) form).getArgument()).getArgument();
					newDisjunction = replaceElement(disjunction, i, newForm);
					newDisjunction.rule = Rule.NEGNEG;
					newDisjunction.origin = new ArrayList<Integer>();
					newDisjunction.origin.add(currentIndex);
					newDisjunction.index = currentIndex + 1;
					resolutionList.add(newDisjunction);
					
					reduce(newDisjunction);
					return;

				//check if argument of single neg is atomic
				} else if ((((Negation) form).getArgument()).getKind() == Kind.Atomic) {
					//similar to Atomic case
					//second case for the end of the recursion
					if (i == disjunction.formulae.size() - 1)
						return;
					//again, there might be further not atomic formulas after this one
					continue;	//continue iteration
				}
				
				//walk through -> binary negation
			case Binary:
				
				//apply alpha rule
				if (tptp_tester.isAlphaFormula(form)) {
					Formula alpha1 = tptp_tester.getAlpha1(form);
					Formula alpha2 = tptp_tester.getAlpha2(form);
					//recursively reduce one of the alphas and save the other one for later reduction
					Disjunction alpha2Disjunction = replaceElement(disjunction, i, alpha2);
					alpha2Disjunction.origin = new ArrayList<Integer>();
					alpha2Disjunction.origin.add(currentIndex);
					alpha2Disjunction.rule = Rule.ALPHA2;
					workingQueue.add(alpha2Disjunction);	//TODO not added to resolution map, yet
					
					newDisjunction = replaceElement(disjunction, i, alpha1);
					newDisjunction.rule = Rule.ALPHA1;
					newDisjunction.origin = new ArrayList<Integer>();
					newDisjunction.origin.add(currentIndex);
					newDisjunction.index = currentIndex + 1;
					resolutionList.add(newDisjunction);
					
					reduce(newDisjunction);
					return;
				
				//apply beta rule
				} else if (tptp_tester.isBetaFormula(form)) {
					Formula beta1 = tptp_tester.getBeta1(form);
					Formula beta2 = tptp_tester.getBeta2(form);
					
					/*
					 * create a new disjunction that contains all formulas of 
					 * the old one, except the current formula and additionally 
					 * contains the betas, recursively reduce this new 
					 * disjunction
					 */
					newDisjunction = replaceElement(disjunction, i, beta1);
					newDisjunction.formulae.add(i+1, beta2);
					newDisjunction.rule = Rule.BETA;
					newDisjunction.origin = new ArrayList<Integer>();
					newDisjunction.origin.add(currentIndex);
					newDisjunction.index = currentIndex + 1;
					resolutionList.add(newDisjunction);
					
					reduce(newDisjunction);
					return;
				}
				
			default:
				throw new IllegalStateException("Unexpected kind of formula " + form);
			}
		}
		throw new IllegalStateException("This should never happen");
	}

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
					return negFormula;
		}
		return null;
	}

	private void updateResolutionMap(Disjunction disjunction) {
		for (Formula formula : disjunction.formulae) {
			Set<Disjunction> disjunctions = resolutionMap.get(formula);
			if (disjunctions == null) {
				disjunctions = new HashSet<Disjunction>();
				resolutionMap.put(formula, disjunctions);
			}
			disjunctions.add(disjunction);
		}
	}

	private List<Formula> replaceElement(List<Formula> formulae, int i, Formula newForm) {
		List<Formula> newFormulae = new ArrayList<Formula>();
		for (int j = 0; j < formulae.size(); j++) {
			if (j == i) {
				newFormulae.add(newForm);
			} else {
				newFormulae.add(formulae.get(j));
			}
		}
		return newFormulae;
	}
	
	private Disjunction replaceElement(Disjunction disjunction, int i, Formula newForm) {
		return new Disjunction(replaceElement(disjunction.formulae, i, newForm));
	}
	
	private Disjunction applyResolution(Disjunction disjunction1, Disjunction disjunction2, Formula form) {
		List<Formula> newFormulae = new ArrayList<Formula>();
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
			newFormulae.add(f);
		}
		return new Disjunction(newFormulae);
	}
}
