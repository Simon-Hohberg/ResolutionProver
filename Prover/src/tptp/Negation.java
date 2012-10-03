package tptp;


public class Negation extends Formula {

  public Negation(TptpParserOutput.FofFormula argument) {
    _kind = Kind.Negation;
    _argument = (Formula)argument;
  }

  /** Returns the formula under the negation. */
  public Formula getArgument() {
    return _argument;
  }

  /** @param obj must be convertible to Negation, can be null */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    return _kind == ((Formula)obj)._kind && 
    _argument.equals(((Negation)obj)._argument);
  }

  public int hasCode() { return 31 * _kind.hashCode() + _argument.hashCode(); }

  public String toString() { return toString(new String("")); }

  public String toString(String indent) {
    return indent + "~(" + _argument + ")";
  }



  private Formula _argument;
} // class Negation