package tptp;

public class Symbol {
    
    public Symbol(String text,boolean isVariable) {
      _text = text;
      _isVariable = isVariable;
    }
    
    public boolean isVariable() { return _isVariable; }
    
    public String getText() { return _text; }
    
    /** @param obj must be convertible to Symbol, can be null */
    public boolean equals(Object obj) {
      if (obj == null) return false;
      return isVariable() == ((Symbol)obj).isVariable() &&
      _text.equals(((Symbol)obj)._text);
    }
    
    public int hashCode() {
      return 31 * _text.hashCode() + ((_isVariable)? 1 : 0);
    }
    
    public String toString() { return _text; }
    
    public String toString(String indent) {
      return indent + _text;
    }
    
    
    private String _text; 
    
    private boolean _isVariable;
    
  } // class Symbol