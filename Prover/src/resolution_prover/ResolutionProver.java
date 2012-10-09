package resolution_prover;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.tptp_tester;


public class ResolutionProver {

	private SortedSet<Disjunction> trace;
	private Queue<Disjunction> workingQueue;
	private Deque<Disjunction> resolved = new LinkedList<Disjunction>();
	private Map<Formula, Set<Disjunction>> resolutionMap;
	private Disjunction start;
	
	private boolean isTautology;

	public ResolutionProver(List<Formula> formulae) {
	  Formula[] nformulae = new Formula[formulae.size()];
	  for (int i = 0; i < formulae.size(); i++) {
	    nformulae[i] = neg(formulae.get(i));
	  }
		trace = new TreeSet<Disjunction>();
		workingQueue = new LinkedList<Disjunction>();
		resolutionMap = new HashMap<Formula, Set<Disjunction>>(); // TODO optimize hashCode() for Formula etc., make it semantic
		Disjunction disjunction = new Disjunction(1, -1, nformulae);
		start = disjunction;
		//initialize
		workingQueue.add(disjunction);
		isTautology = false;
	}
	
	public boolean prove() {
		
	  // normalize to KNF
	  System.out.print("Normalize...");
		while (!workingQueue.isEmpty() && !isTautology) {
			Disjunction disjunction = workingQueue.poll();
			disjunction.index = trace.size() + 1;
			trace.add(disjunction);
			reduce(disjunction);
		}
		
		// resolve
		System.out.println(String.format(" %d steps", trace.size()));
		System.out.print("Getting KNF...");
		List<Disjunction> final_conjunction = start.derivations();
		System.out.println(String.format(" %d terms", final_conjunction.size()));
		//System.out.println(final_conjunction);
		printTrace();
		System.out.println("Resolution...");
		workingQueue.addAll(final_conjunction);
		
		int i = 0;
		while(!isTautology && (!workingQueue.isEmpty() || !resolved.isEmpty())) {
		  Disjunction disjunction = resolved.isEmpty() ? workingQueue.poll() : resolved.removeFirst();
		  if (disjunction.has_derivations())
		    continue;
		  updateResolutionMap(disjunction);
		  doResolution(disjunction);
		  if (++i % 100 == 0)
		    System.out.println(workingQueue.size() + resolved.size());
		}
		//System.out.println(resolutionMap);
		printTrace();
		
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
			if (!disjunction.has_derivations())
			  builder.append('F');
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

		if (isTautology)
			return;
		
		if (isTautology = disjunction.isEmpty())
			return;
		
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
			// TODO literal (~$true...)
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
		assert false;
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
		disjunction.derivation(newDisjunction);
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
		workingQueue.add(alpha2Disjunction);	//TODO not added to resolution map, yet
		
		newDisjunction = replaceElement(disjunction, form, alpha1);
		newDisjunction.rule = Rule.ALPHA1;
		newDisjunction.origin = new ArrayList<Integer>();
		newDisjunction.origin.add(currentIndex);
		newDisjunction.index = currentIndex + 1;
		disjunction.derivation(newDisjunction, alpha2Disjunction);
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
		disjunction.derivation(newDisjunction);
		addToTrace(newDisjunction);
		
		return newDisjunction;
	}

	/**
	 * Applies the resolution rule on all {@link Disjunction}s that share a 
	 * formula that is equal to one formula of the provided {@link Disjunction}
	 * and adds the newly created {@link Disjunction}s to the working queue.
	 * @param disjunction
	 */
	private boolean doResolution(Disjunction disjunction) {
	  boolean resoluted = false;
	  
	  Disjunction resoluter = getDisjunctionForResolution(disjunction);
	  if (resoluter != null) {
	    resoluted = true;
      Disjunction newDisjunction = applyResolution(disjunction, resoluter);
      newDisjunction.origin = new ArrayList<Integer>();
      newDisjunction.origin.add(disjunction.index);
      newDisjunction.origin.add(resoluter.index);
      newDisjunction.rule = Rule.RESOLUTION;
      newDisjunction.index = trace.size() + 1;
      disjunction.derivation(newDisjunction);
      resoluter.derivation(newDisjunction);
      addToResolutionQueue(newDisjunction);
      addToTrace(newDisjunction);
	  }
		return resoluted;
	}
	
	private Disjunction applyResolution(Disjunction disjunction1,
      Disjunction disjunction2) {
	  SortedSet<Formula> d1 = new TreeSet<Formula>(disjunction1.formulae);
	  SortedSet<Formula> d2 = new TreeSet<Formula>(disjunction2.formulae);
    for (Formula form : disjunction1.formulae) {
      Formula negForm = neg(form);
      if (!disjunction1.formulae.contains(negForm))
        d2.remove(negForm);
    }
    for (Formula form : disjunction2.formulae) {
      Formula negForm = neg(form);
      if (!disjunction2.formulae.contains(negForm))
        d1.remove(negForm);
    }
    d1.addAll(d2);
    return new Disjunction(d1);
  }

  private Disjunction getDisjunctionForResolution(Disjunction disjunction) {
	  Disjunction match = null;
	  
	  for (Formula f : disjunction.formulae) {
	    Set<Disjunction> derived = new TreeSet<Disjunction>();
	    Set<Disjunction> f_disjunctions = resolutionMap.get(neg(f));
	    if (f_disjunctions == null)
	      continue;
	    for (Disjunction matching : f_disjunctions) {
	      if (matching.has_derivations()) {
	        derived.add(matching);
	        continue;
	      }
	      if (disjunction != matching) {
	        match = matching;
	        break;
	      }
	    }
	    if (!derived.isEmpty())
	      f_disjunctions.removeAll(derived);
	  }
	  
	  return match;
	}

	private Formula neg(Formula formula) {
	  Formula negFormula;
    if (formula.getKind() == Kind.Negation) {
      negFormula = ((Negation)formula).getArgument();
    } else {
      negFormula = new Negation(formula);
    }
	  return negFormula;
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
	  newFormulae.add(newForm);
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
	
	private void addToResolutionQueue(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			isTautology = true;
			disjunction.index = trace.size()+1;
			trace.add(disjunction);
			return;
		}
		if (trace.contains(disjunction))
			return;
		resolved.addFirst(disjunction);
	}
	
	private void addToTrace(Disjunction disjunction) {
		if (trace.contains(disjunction))
			return;
		trace.add(disjunction);
	}
}
