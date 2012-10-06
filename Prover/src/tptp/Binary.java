package tptp;


public class Binary extends Formula {

  public Binary(TptpParserOutput.FofFormula lhs,
                TptpParserOutput.BinaryConnective connective,
                TptpParserOutput.FofFormula rhs)
  {
    _kind = Kind.Binary;
    _lhs = (Formula)lhs;
    _connective = connective;
    _rhs = (Formula)rhs;
  }

  public TptpParserOutput.BinaryConnective getConnective() {
    return _connective;
  }

  public Formula getLhs() { return _lhs; }

  public Formula getRhs() { return _rhs; }

  /** @param obj must be convertible to Binary, can be null */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    return _kind == ((Formula)obj)._kind && 
    _connective == ((Binary)obj)._connective &&
    _lhs.equals(((Binary)obj)._lhs) &&
    _rhs.equals(((Binary)obj)._rhs);
  }


  public String toString() { return toString(new String("")); }

  public String toString(String indent) {
    return indent + "(" + _lhs + _connective + _rhs + ")";
  }



  private Formula _lhs;
  private TptpParserOutput.BinaryConnective _connective;
  private Formula _rhs;
} // class Binary