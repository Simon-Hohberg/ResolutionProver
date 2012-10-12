package resolutionprover;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tptp.Binary;
import tptp.Formula;
import tptp.Kind;
import tptp.Negation;
import tptp.Quantified;
import tptp.tptp_tester;
import tptp.TptpParserOutput.BinaryConnective;
import tptp.TptpParserOutput.Quantifier;

public class Prenex {
  private Formula formula;
  private Formula formulaUnquantified;
  private Quantified prenex = null;
  private List<QuantifiedVariable> quantifiedVariables = new LinkedList<QuantifiedVariable>();

  public Prenex(Formula formula) {
    this.formula = formula;
    unquantify();
  }

  private void unquantify() {
    switch (formula.getKind()) {
    case Binary:
      Binary binaryFormula = (Binary) formula;
      if (tptp_tester.isAlphaFormula(binaryFormula)) {
        Prenex alpha1Prenex, alpha2Prenex;
        alpha1Prenex = new Prenex(tptp_tester.getAlpha1(binaryFormula));
        alpha2Prenex = new Prenex(tptp_tester.getAlpha2(binaryFormula));
        quantifiedVariables.addAll(alpha1Prenex.quantifiedVariables);
        quantifiedVariables.addAll(alpha2Prenex.quantifiedVariables);
        formulaUnquantified = new Binary(alpha1Prenex.getFormulaUnquantified(),
            BinaryConnective.And, alpha2Prenex.getFormulaUnquantified());
      }
      if (tptp_tester.isBetaFormula(binaryFormula)) {
        Prenex beta1Prenex, beta2Prenex;
        beta1Prenex = new Prenex(tptp_tester.getBeta1(binaryFormula));
        beta2Prenex = new Prenex(tptp_tester.getBeta2(binaryFormula));
        quantifiedVariables.addAll(beta1Prenex.quantifiedVariables);
        quantifiedVariables.addAll(beta2Prenex.quantifiedVariables);
        formulaUnquantified = new Binary(beta1Prenex.getFormulaUnquantified(),
            BinaryConnective.Or, beta2Prenex.getFormulaUnquantified());
      }
      break;
    case Negation:
      Negation negation = (Negation) formula;
      Formula argument = negation.getArgument();
      Prenex negPrenex;
      if (argument.getKind() == Kind.Quantified) {
        Quantified quantified = (Quantified) argument;
        quantifiedVariables.add(new QuantifiedVariable(Util.negateQuantifier(quantified.getQuantifier()), quantified.getVariable()));
        negPrenex = new Prenex(Util.negate(quantified.getMatrix()));
        formulaUnquantified = negPrenex.getFormulaUnquantified();
      } else {
        negPrenex = new Prenex(argument);
        formulaUnquantified = Util.negate(negPrenex.getFormulaUnquantified());
      }
      quantifiedVariables.addAll(flip(negPrenex.quantifiedVariables));
      break;
    case Quantified:
      Quantified quantified = (Quantified) formula;
      Prenex innerQuantifiedPrenex = new Prenex(quantified.getMatrix());
      formulaUnquantified = innerQuantifiedPrenex.getFormulaUnquantified();
      quantifiedVariables.add(new QuantifiedVariable(
          quantified.getQuantifier(), quantified.getVariable()));
      quantifiedVariables.addAll(innerQuantifiedPrenex.quantifiedVariables);
    default:
      break;
    }
  }

  public Formula getFormulaUnquantified() {
    return formulaUnquantified == null ? formula : formulaUnquantified;
  }

  public Quantified getPrenex() {
    if (prenex == null) {
      if (!quantifiedVariables.isEmpty()) {
        Formula result = getFormulaUnquantified();
        Collections.reverse(quantifiedVariables);
        for (QuantifiedVariable quantifiedVariable : quantifiedVariables) {
          result = new Quantified(quantifiedVariable.getQuantifier(),
              quantifiedVariable.variable, result);
        }
        prenex = (Quantified) result;
      }
    }
    return prenex;
  }

  private List<QuantifiedVariable> flip(
      List<QuantifiedVariable> quantifiedVariablesList) {
    for (QuantifiedVariable quantifiedVariable : quantifiedVariablesList) {
      quantifiedVariable.flipQuantifier();
    }
    return quantifiedVariablesList;
  }

  class QuantifiedVariable {
    boolean for_all;
    String variable;

    public QuantifiedVariable(boolean for_all, String variable) {
      this.for_all = for_all;
      this.variable = variable;
    }

    public QuantifiedVariable(Quantifier quantifier, String variable) {
      this.for_all = quantifier == Quantifier.ForAll;
      this.variable = variable;
    }

    public void flipQuantifier() {
      for_all = !for_all;
    }

    public Quantifier getQuantifier() {
      return for_all ? Quantifier.ForAll : Quantifier.Exists;
    }

    @Override
    public String toString() {
      return (for_all ? "A " : "E ") + variable;
    }

  }

}
