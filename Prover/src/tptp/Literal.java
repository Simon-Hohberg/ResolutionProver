package tptp;

public class Literal 
  implements TptpParserOutput.Literal
  {
    
    public Literal(boolean positive,
                   TptpParserOutput.AtomicFormula atom)
    {
      _isPositive = positive;
      _atom = (Atomic)atom;
    }
    
    public boolean isPositive() { return _isPositive; }
    
    public Atomic getAtom() { return _atom; }
    
    /** @param obj must be convertible to Literal, can be null */
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (this == obj) return true;
      return _isPositive == ((Literal)obj)._isPositive &&
      _atom.equals(((Literal)obj)._atom);
      
    }
    
    public int hashCode() {
      return 31 * _atom.hashCode() + (_isPositive? 1 : 0);
    }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      if (_isPositive) return _atom.toString(indent);
      return indent + "~" + _atom + "";
    }
    
    
    
    private boolean _isPositive;
    private Atomic _atom;
    
  }