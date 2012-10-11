package resolutionprover;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import tptp.AnnotatedFormula;
import tptp.BooleanAtomic;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.TptpParserOutput.FormulaRole;


public class ResolutionProver {

	private List<Disjunction> disjunctions;
	private Set<Disjunction> trace;
	private PriorityQueue<Disjunction> workingQueue;
	private Map<Formula, Set<Disjunction>> resolutionMap;
	
	private Collection<Disjunction> atoms;
	
	private boolean isTautology;

	public ResolutionProver(Collection<Formula> axioms, Formula... conjectures) {
		init(axioms, conjectures);
	}
	
	public ResolutionProver(AnnotatedFormula... formulae) {
	  List<Formula> axs = new ArrayList<Formula>(formulae.length - 1);
	  List<Formula> conjs = new LinkedList<Formula>();
	  for (AnnotatedFormula af : formulae) {
	    if (af.getRole() == FormulaRole.Conjecture)
	      conjs.add(af.getFormula());
	    else
	      axs.add(af.getFormula());
	  }
	  init(axs, conjs.toArray(new Formula[0]));
  }
	
	private void init(Collection<Formula> axioms, Formula... conjectures) {
	  trace = new HashSet<Disjunction>();
    workingQueue = new PriorityQueue<Disjunction>();
    resolutionMap = new HashMap<Formula, Set<Disjunction>>();
    disjunctions = new ArrayList<Disjunction>(axioms.size() + conjectures.length);
    isTautology = false;
    
    int formula_no = 1;
    for (Formula axiom : axioms) {
      disjunctions.add(new Disjunction(formula_no++, 0, axiom));
    }
    for (Formula conjecture : conjectures) {
      disjunctions.add(new Disjunction(formula_no++, 0, Util.negate(conjecture)));
    }
	}
	
	/**
	 * Tries to prove the formula passed to this prover at creation by 
	 * disproving the negation of the formula.
	 * @return <code>true</code> if the formula was proven, otherwise
	 * <code>false</code>
	 */
	public boolean prove() {
		atoms = new LinkedList<Disjunction>();
	  for (Disjunction disjunction : disjunctions) {
  		trace.add(disjunction);
  		Expander expander = new Expander();
  		
  		atoms.addAll(expander.expand(disjunction));
  		trace.addAll(expander.getTrace());
	  }
		System.out.println("\nGot " + atoms.size() + " atoms. Doing resolution...");
		
		workingQueue.addAll(atoms);
		
		//printTrace(trace);
		while (!workingQueue.isEmpty() && !isTautology) {
			Disjunction disjunction = workingQueue.poll();
			System.out.println();
			addToTrace(disjunction);
			updateResolutionMap(disjunction);
			doResolution(disjunction);
			System.out.println(trace.size());
		}
		System.out.println("...done");
		printTrace(trace);
		return isTautology;
	}
	
	public static void printTrace(Collection<Disjunction> trace) {
		int commentIndent = Util.calculateCommentIndent(trace);
		int indexLength = String.format("%d", trace.size()).length();
		System.out.println();
		Disjunction[] traceArray = new Disjunction[trace.size()];
		Arrays.sort(trace.toArray(traceArray), new Comparator<Disjunction>() {

			@Override
			public int compare(Disjunction arg0, Disjunction arg1) {
				return new Integer(arg0.index).compareTo(arg1.index);
			}
			
		});
		for (Disjunction disjunction : traceArray) {
			StringBuilder builder = new StringBuilder();
			builder.append(disjunction.index);
			builder.append(". ");
			
			for (int j = String.format("%d", (disjunction.index)).length(); j < indexLength; j++) {
				builder.append(" ");
			}
			builder.append(disjunction.toString(commentIndent));
			System.out.println(builder.toString());
		}
	}

	/**
	 * Applies the resolution rule on all {@link Disjunction}s that share a 
	 * formula that is equal to one formula of the provided {@link Disjunction}
	 * and adds the newly created {@link Disjunction}s to the working queue.
	 * @param disjunction
	 */
	private void doResolution(Disjunction disjunction) {
		for (Formula resolutionFormula : disjunction.formulae) {
			Set<Disjunction> resolutionDisjunctions = resolutionMap.get(Util.negate(resolutionFormula));
			if (resolutionDisjunctions == null)
				continue;
			for (Disjunction d : resolutionDisjunctions) {
				if (d.equals(disjunction)) {
					disjunction.formulae.clear();
					disjunction.formulae.add(BooleanAtomic.TRUE);
					disjunction.rule = Rule.RESOLUTION;
					disjunction.origin = new ArrayList<Integer>();
					disjunction.origin.add(disjunction.index);
					addToWorkingQueue(disjunction);
					return;		//disjunction is tautology
				}
				Disjunction resolvent = applyResolution(disjunction, d, resolutionFormula);
				addToWorkingQueue(resolvent);
			}
		}
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
				disjunctions = new HashSet<Disjunction>();
				resolutionMap.put(formula, disjunctions);
			}
			disjunctions.add(disjunction);
		}
	}

	/**
	 * Applies the resolution rule on both provided {@link Disjunction}s removing
	 * all positive and negated occurrences of the given {@link Formula}.
	 * @param disjunction1
	 * @param disjunction2
	 * @param form
	 * @return A new {@link Disjunction} containing all {@link Formula}e of the
	 * two given {@link Disjunction}s except the provided {@link Formula} and
	 * negations of it.
	 */
	private Disjunction applyResolution(Disjunction disjunction1, Disjunction disjunction2, Formula form) {
	  Set<Formula> newFormulae = new HashSet<Formula>();
		for (Formula f : disjunction1.formulae) {
			if (f.equals(form)) 
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation)f).getArgument().equals(form))
					continue;
			newFormulae.add(f);
		}
		Disjunction resolvent = new Disjunction(newFormulae);
		resolvent.rule = Rule.RESOLUTION;
		resolvent.origin = new ArrayList<Integer>();
		resolvent.origin.add(disjunction1.index);
		resolvent.origin.add(disjunction2.index);
		for (Formula f : disjunction2.formulae) {
			if (f.equals(form)) 
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation)f).getArgument().equals(form))
					continue;
			//prevent positives and negatives in one disjunction
			Formula negation = f.getKind() == Kind.Negation ? ((Negation)f).getArgument() : new Negation(f);
			if (newFormulae.contains(negation)) {
				newFormulae.clear();
				newFormulae.add(BooleanAtomic.TRUE);
				return resolvent;
			} else {
				newFormulae.add(f);
			}
		}
		return resolvent;
	}
	
	/**
	 * Adds the provided {@link Disjunction} to the working queue also checking
	 * if this {@link Disjunction} is already empty.
	 * @param disjunction
	 */
	private void addToWorkingQueue(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			workingQueue.clear();
			isTautology = true;
			disjunction.index = trace.size()+1;
			trace.add(disjunction);
			return;
		}
		if (disjunction.isTautology())
			return;
		if (trace.contains(disjunction))
			return;
		addToTrace(disjunction);
		workingQueue.add(disjunction);
	}
	
	/**
	 * Adds the given {@link Disjunction} to the trace if there isn't already
	 * such a {@link Disjunction} in the trace. Furthermore sets the index of
	 * the {@link Disjunction}.
	 * @param disjunction
	 */
	private void addToTrace(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			isTautology = true;
			workingQueue.clear();
			return;
		}
		if (trace.contains(disjunction))
			return;
		disjunction.index = trace.size()+1;
		trace.add(disjunction);
	}
}
