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
		
		List<Disjunction> propositionalDisjunctions = new LinkedList<Disjunction>();
		
		for (Disjunction disjunction : disjunctions) {
			//TODO put quantifiers infront of the formula
			disjunction = skolemize(disjunction);
			trace.add(disjunction);
			propositionalDisjunctions.add(removeAllQuantifier(disjunction));
		}
		
		disjunctions = propositionalDisjunctions;

		boolean result = super.prove();
		
		return result;
	}

	private Disjunction removeAllQuantifier(Disjunction disjunction) {
		Set<Formula> formulae = new HashSet<Formula>();
		for (Formula formula : disjunction.formulae) {
			if (formula.getKind() == Kind.Quantified) {
				Quantified quantified = (Quantified) formula;
				if (quantified.getQuantifier() == Quantifier.ForAll) {
					formulae.add(quantified.getMatrix());
				} else {
					throw new IllegalStateException("cannot remove all-quantifier since formula isn't all quantified: " + formula);
				}
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
			while (localFormula != null) {
				Set<String> seenAllQuantified = new HashSet<String>();
				
				//all
				if (tptp_tester.isGammaFormula(localFormula)) {
					Formula quantifiedForm = tptp_tester.getGammaX(localFormula);
					seenAllQuantified.add(((Quantified)localFormula).getVariable());
					localFormula = quantifiedForm;
					
				//exists
				} else if (tptp_tester.isDeltaFormula(localFormula)) {
					Formula quantifiedForm = tptp_tester.getDeltaX(localFormula);
					localFormula = tptp_tester.getDeltaT(quantifiedForm, createSkolem(seenAllQuantified), out);
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
		Set<TptpParserOutput.Term> terms = new HashSet<TptpParserOutput.Term>();
		for (String variable : seenAllQuantified) {
			terms.add(new Term(new Symbol(variable, true), null));
		}
		return new Term(new Symbol(tptp_tester.freshVariableName(), false), terms);
	}
}
