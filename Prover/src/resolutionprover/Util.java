package resolutionprover;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tptp.AnnotatedFormula;
import tptp.BooleanAtomic;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;


public class Util {

	public static <E> String collectionToString(Collection<E> collection) {
		if (collection.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		Iterator<E> iterator = collection.iterator();
		builder.append(iterator.next());
		for (int i = 1; i < collection.size(); i++) {
			if (i == collection.size() - 1)
				builder.append(" and ");
			else
				builder.append(", ");
			builder.append(iterator.next());
		}
		return builder.toString();
	}

	public static int calculateCommentIndent(Collection<Disjunction> disjunctions) {
		int maxLength = 0;
		for (Disjunction disjunction : disjunctions) {
			int length = disjunction.formulae.toString().length();
			if (length > maxLength)
				maxLength = length;
		}
		return maxLength + 2;
	}

	/**
	 * Negates the given {@link Formula} by either creating a new 
	 * {@link Negation} if the {@link Formula} isn't a {@link Negation} itself
	 * or returning the argument of the {@link Negation}. Therefore no double
	 * {@link Negation} can be created using this method.
	 * @param formula
	 * @return The semantic negation of the provided {@link Formula}.
	 */
	public static Formula negate(Formula formula) {
		Formula negFormula;
		if (formula.getKind() == Kind.Negation) {
			negFormula = ((Negation)formula).getArgument();
		} else {
			negFormula = new Negation(formula);
		}
		return negFormula;
	}
	
	public static AnnotatedFormula negate(AnnotatedFormula formula) {
    formula._formula = negate(formula.getFormula());
	  return formula;
  }
	
	public static Formula[] negateAll(Formula... formulae) {
		Formula[] negated = new Formula[formulae.length];
		for (int i = 0; i < formulae.length; i++) {
			negated[i] = negate(formulae[i]);
		}
		return negated;
	}
	
	public static Formula[] negateAll(List<Formula> formulae) {
		return negateAll(formulae.toArray(new Formula[formulae.size()]));
	}
	
	public static AnnotatedFormula[] negateAllAnnotated(List<AnnotatedFormula> annotatedFormulas) {
	  AnnotatedFormula[] negatedAnnotatedFormulas = new AnnotatedFormula[annotatedFormulas.size()];
	  for (int i = 0; i < negatedAnnotatedFormulas.length; i++)
	    negatedAnnotatedFormulas[i] = negate(annotatedFormulas.get(i));
	  return negatedAnnotatedFormulas;
	}

	/**
	 * Replaces one {@link Formula} with another one and removes negations and
	 * corresponding positives.
	 * @param formulae
	 * @param replacedForm
	 * @param newForm
	 * @return A new {@link Set} containing all {@link Formula}e from the
	 * provided set except replacedForm
	 */
	public static Set<Formula> replaceElement(Set<Formula> formulae, Formula replacedForm, Formula newForm) {
		Set<Formula> newFormulae = new HashSet<Formula>(formulae);
		newFormulae.remove(replacedForm);
		//positives and negatives in one disjunction => true, therefore ignore whole disjunction
		Formula negation = negate(newForm);
		if (newFormulae.contains(negation)) {
			newFormulae.clear();
			newFormulae.add(BooleanAtomic.TRUE);
			return newFormulae;
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
	 * @return A new {@link Set} containing all {@link Formula}e from the
	 * provided set except replacedForm
	 */
	public static Disjunction replaceElement(Disjunction disjunction, Formula replacedForm, Formula newForm) {
		Set<Formula> formulae = replaceElement(disjunction.formulae, replacedForm, newForm);
		return new Disjunction(formulae);
	}
}
