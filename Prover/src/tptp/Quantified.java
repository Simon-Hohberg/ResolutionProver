package tptp;


public class Quantified extends Formula {

  public Quantified(TptpParserOutput.Quantifier quantifier,
                    String variable,
                    TptpParserOutput.FofFormula matrix)
  {
    _kind = Kind.Quantified;
    _quantifier = quantifier;
    _variable = variable;
    _matrix = (Formula)matrix;
  }

  public TptpParserOutput.Quantifier getQuantifier() {
    return _quantifier;
  }

  public String getVariable() { return _variable; }

  public Formula getMatrix() { return _matrix; }

  /** @param obj must be convertible to Quantified, can be null */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    return _kind == ((Formula)obj)._kind && 
    _quantifier == ((Quantified)obj)._quantifier &&
    _variable.equals(((Quantified)obj)._variable) &&
    _matrix.equals(((Quantified)obj)._matrix);
  }


  public String toString() { return toString(new String("")); }

  public String toString(String indent) {
    return 
    indent + _quantifier + " [" + _variable + "] : (" + _matrix + ")";
  }


  private TptpParserOutput.Quantifier _quantifier;
  private String _variable;
  private Formula _matrix;
} // class Quantified