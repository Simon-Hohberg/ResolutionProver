package resolutionprover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tptp.AnnotatedFormula;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.Quantified;
import tptp.SimpleTptpParserOutput;
import tptp.Symbol;
import tptp.Term;
import tptp.TptpParserOutput;
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
			//TODO put quantifiers infront of the formula
			Disjunction skolemizedDisjunction = skolemize(disjunction);
			trace.add(skolemizedDisjunction);
			Disjunction notQuantifiedDisjunction = removeAllQuantifier(skolemizedDisjunction);
			//could be the same
			if (!skolemizedDisjunction.equals(notQuantifiedDisjunction)) {
				trace.add(notQuantifiedDisjunction);
				propositionalDisjunctions.add(notQuantifiedDisjunction);
			} else {
				propositionalDisjunctions.add(skolemizedDisjunction);
			}
		}
		
		disjunctions = propositionalDisjunctions;

		boolean result = super.prove();
		
		return result;
	}

	private Disjunction removeAllQuantifier(Disjunction disjunction) {
		Set<Formula> formulae = new HashSet<Formula>();
		for (Formula formula : disjunction.formulae) {
			//formula is quantified, there should be only all-quantified formulae
			if (formula.getKind() == Kind.Quantified) {
				Quantified quantified = (Quantified) formula;
				if (quantified.getQuantifier() == Quantifier.ForAll) {
					formulae.add(quantified.getMatrix());
				} else {
					throw new IllegalStateException("cannot remove all-quantifier since formula isn't all quantified: " + formula);
				}
			//formula is not quantified
			} else {
				formulae.add(formula);
			}
		}
		Disjunction resultDisjunction = new Disjunction(formulae);
		resultDisjunction.origin.add(disjunction);
		resultDisjunction.rule = Rule.DROP_ALL;
		return resultDisjunction;
	}

	private Disjunction skolemize(Disjunction disjunction) {
		List<Formula> formulae = new ArrayList<Formula>(disjunction.formulae.size());
		for (Formula formula : disjunction.formulae) {
			Formula localFormula = formula;
			Set<String> seenAllQuantified = new HashSet<String>();
			while (localFormula != null) {
				
				//all
				if (tptp_tester.isGammaFormula(localFormula)) {
					Formula innerFormular = tptp_tester.getGammaX(localFormula);
					seenAllQuantified.add(extractQuantifiedVariable(localFormula));
					localFormula = innerFormular;
					
				//exists
				} else if (tptp_tester.isDeltaFormula(localFormula)) {
					localFormula = tptp_tester.getDeltaT(localFormula, createSkolem(seenAllQuantified), out);
				} else {
					Formula newFormula = localFormula;
					formulae.add((Formula) out.createQuantifiedFormula(Quantifier.ForAll, seenAllQuantified, newFormula));
					localFormula = null;
				}
			}
		}
		Disjunction skolemizedDisjunction = new Disjunction(formulae);
		skolemizedDisjunction.origin.add(disjunction);
		skolemizedDisjunction.rule = Rule.SKOLEMIZATION;
		return skolemizedDisjunction;
	}

	private Term createSkolem(Set<String> seenAllQuantified) {
		//if there have not been seen any free variables yet, just use a new constant
		if (seenAllQuantified.isEmpty()) {
			//TODO is this a new constant?
			return new Term(new Symbol(tptp_tester.freshVariableName(), false), null);
		}
		Set<TptpParserOutput.Term> terms = new HashSet<TptpParserOutput.Term>();
		for (String variable : seenAllQuantified) {
			terms.add(new Term(new Symbol(variable, true), null));
		}
		//TODO is this a new function?
		return new Term(new Symbol(tptp_tester.freshVariableName(), false), terms);
	}
	
	/**
	 * Extracts the variable name used by the quantifier of the given gamma or 
	 * delta formula.
	 * @param formula
	 * @return Variable name referenced by the quantifier of the provided 
	 * formula
	 */
	private String extractQuantifiedVariable(Formula formula) {
		switch (formula.getKind()) {
		case Negation:
			Formula quantified = ((Negation)formula).getArgument();
			if (quantified.getKind() == Kind.Quantified) {
				return ((Quantified)quantified).getVariable();
			}
			throw new IllegalStateException(String.format("Cannot extract variable from formula %s that is not quantified", quantified));
		case Quantified:
			return ((Quantified)formula).getVariable();
		default:
			throw new IllegalStateException(String.format("Cannot extract variable from formula %s that is not quantified", formula));
		}
	}
}
