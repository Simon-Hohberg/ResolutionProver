package resolutionprover;

import java.util.ArrayList;
import java.util.Collection;
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

public class PropositionalProver {

	protected List<Disjunction> disjunctions;
	protected List<Disjunction> trace;
	protected Set<Disjunction> seenDisjunctions;
	protected PriorityQueue<Disjunction> workingQueue;
	protected Map<Formula, Set<Disjunction>> resolutionMap;
    
	protected Collection<Disjunction> atoms;
    
	protected boolean isTautology;

	public PropositionalProver(Collection<Formula> axioms, Formula... conjectures) {
		init(axioms, conjectures);
	}

	public PropositionalProver(AnnotatedFormula... formulae) {
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
		trace = new LinkedList<Disjunction>();
		seenDisjunctions = new HashSet<Disjunction>();
		workingQueue = new PriorityQueue<Disjunction>();
		resolutionMap = new HashMap<Formula, Set<Disjunction>>();
		disjunctions = new ArrayList<Disjunction>(axioms.size()
				+ conjectures.length);
		isTautology = false;

		//add axioms
		for (Formula axiom : axioms) {
			Disjunction disjunction = new Disjunction(axiom);
			disjunction.rule = Rule.AXIOM;
			disjunctions.add(disjunction);
			trace.add(disjunction);
		}
		//add negated conjectures
		for (Formula conjecture : conjectures) {
			Disjunction disjunction = new Disjunction(Util.negate(conjecture));
			disjunction.rule = Rule.CONJECTURE;
			disjunctions.add(disjunction);
			trace.add(disjunction);
		}
	}

	/**
	 * Tries to prove the formula passed to this prover at creation by
	 * disproving the negation of the formula.
	 * 
	 * @return <code>true</code> if the formula was proven, otherwise
	 *         <code>false</code>
	 */
	public boolean prove() {
		long starTime = System.currentTimeMillis();
		System.out.println("\n----------- Propositional Proof -----------");
		//expand all disjunctions
		atoms = new LinkedList<Disjunction>();
		for (Disjunction disjunction : disjunctions) {
			seenDisjunctions.add(disjunction);
			
			Expander expander = new Expander();
			atoms.addAll(expander.expand(disjunction));
			trace.addAll(expander.getTrace());
			seenDisjunctions.addAll(expander.seenDisjunctions);
		}
		System.out.println("Got " + atoms.size()
				+ " disjunctions containing atoms only.\nDoing resolution...");

		workingQueue.addAll(atoms);

		//do resolution
		while (!workingQueue.isEmpty() && !isTautology) {
			Disjunction disjunction = workingQueue.poll();
			updateResolutionMap(disjunction);
			doResolution(disjunction);
		}
		System.out.println("...done");
		printTrace(trace);
		System.out.println(String.format("\n+++++++++++ Time: %.3fs +++++++++++", (System.currentTimeMillis()-starTime)/1000.0));
		return isTautology;
	}

	public static void printTrace(List<Disjunction> trace) {
		int commentIndent = Util.calculateCommentIndent(trace);
		int indexLength = String.format("%d", trace.size()).length();
		System.out.println();
		
		int i = 1;
		//print whole trace
		for (Disjunction disjunction : trace) {
			disjunction.index = i++;
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
	 * 
	 * @param disjunction
	 */
	private void doResolution(Disjunction disjunction) {
		for (Formula resolutionFormula : disjunction.formulae) {
			Set<Disjunction> resolutionDisjunctions = resolutionMap.get(Util
					.negate(resolutionFormula));
			if (resolutionDisjunctions == null)
				continue;
			//do resolution with all resolutions sharing the current formula
			for (Disjunction d : resolutionDisjunctions) {
				//if resolution disjunction is the same disjunction, it contains some A and ~A, so it is always true
				if (d.equals(disjunction)) {
					disjunction.formulae.clear();
					disjunction.formulae.add(BooleanAtomic.TRUE);
					disjunction.rule = Rule.RESOLUTION;
					disjunction.origin.add(disjunction);
					addToWorkingQueue(disjunction);
					return; // disjunction is tautology
				}
				Disjunction resolvent = applyResolution(disjunction, d,
						resolutionFormula);
				addToWorkingQueue(resolvent);
				
				//check if prove is already finished
				if (isTautology)
					return;
			}
		}
	}

	/**
	 * Updates the resolutionMap by adding the given {@link Disjunction}.
	 * Associates all {@link Formula}e in the disjunction with it.
	 * 
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
	 * Applies the resolution rule on both provided {@link Disjunction}s
	 * removing all positive and negated occurrences of the given
	 * {@link Formula}.
	 * 
	 * @param disjunction1
	 * @param disjunction2
	 * @param form
	 * @return A new {@link Disjunction} containing all {@link Formula}e of the
	 *         two given {@link Disjunction}s except the provided
	 *         {@link Formula} and negations of it.
	 */
	private Disjunction applyResolution(Disjunction disjunction1,
			Disjunction disjunction2, Formula form) {
		Set<Formula> newFormulae = new HashSet<Formula>();
		for (Formula f : disjunction1.formulae) {
			if (f.equals(form))
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation) f).getArgument().equals(form))
					continue;
			newFormulae.add(f);
		}
		Disjunction resolvent = new Disjunction(newFormulae);
		resolvent.rule = Rule.RESOLUTION;
		resolvent.origin.add(disjunction1);
		resolvent.origin.add(disjunction2);
		for (Formula f : disjunction2.formulae) {
			if (f.equals(form))
				continue;
			else if (f.getKind() == Kind.Negation)
				if (((Negation) f).getArgument().equals(form))
					continue;
			// prevent positives and negatives in one disjunction
			Formula negation = f.getKind() == Kind.Negation ? ((Negation) f)
					.getArgument() : new Negation(f);
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
	 * Adds the provided {@link Disjunction} to the working queue and trace also
	 * checking if this {@link Disjunction} is already a tautology or is already
	 * empty.
	 * 
	 * @param disjunction
	 */
	private void addToWorkingQueue(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			workingQueue.clear();
			isTautology = true;
			trace.add(disjunction);
			return;
		}
		//always add tautologies to trace for traces of "none proves"
		if (disjunction.isTautology()) {
			trace.add(disjunction);
			return;
		}
		if (seenDisjunctions.contains(disjunction))
			return;
		addToTrace(disjunction);
		seenDisjunctions.add(disjunction);
		workingQueue.add(disjunction);
	}

	/**
	 * Adds the given {@link Disjunction} to the trace if there isn't already
	 * such a {@link Disjunction} in the trace. Furthermore sets the index of
	 * the {@link Disjunction}.
	 * 
	 * @param disjunction
	 */
	private void addToTrace(Disjunction disjunction) {
		if (disjunction.isEmpty()) {
			isTautology = true;
			workingQueue.clear();
			return;
		}
		if (!seenDisjunctions.contains(disjunction))
			trace.add(disjunction);
	}
}
