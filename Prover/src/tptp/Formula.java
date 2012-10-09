package tptp;

public class Formula implements Comparable<Formula>, TptpParserOutput.FofFormula {
    public Kind getKind() { return _kind; }
    
    /** @param obj must be convertible to Formula, can be null */
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (this == obj) return true;
      if (_kind != ((Formula)obj)._kind) return false;
      switch (_kind) 
      {
        case Atomic: 
        return ((Atomic)this).equals((Atomic)obj);
        case Negation:
        return ((Negation)this).equals((Negation)obj);
        case Binary:
        return ((Binary)this).equals((Binary)obj);
        case Quantified:
        return ((Quantified)this).equals((Quantified)obj);
      };
      assert false;
      return false;
    } // equals(Object obj)
    
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      switch (_kind) 
      {
        case Atomic: return ((Atomic)this).toString(indent);
        case Negation: return ((Negation)this).toString(indent);
        case Binary: return ((Binary)this).toString(indent);
        case Quantified: return ((Quantified)this).toString(indent);
      };
      assert false;
      return null;
    }
    
    
    
    //================== Attributes: =========================
    
    protected Kind _kind;



    @Override
    public int compareTo(Formula o) {
      return toString().compareTo(o.toString());
    }
    
  }