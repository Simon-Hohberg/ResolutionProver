package tptp;

import java.util.LinkedList;


public class Clause 
  implements TptpParserOutput.CnfFormula
  {
    public Clause(Iterable<TptpParserOutput.Literal> literals) {
      if (literals != null) {
        _literals = new LinkedList<Literal>();
        for (TptpParserOutput.Literal lit : literals) {
          _literals.add((Literal)lit);
        };
      };
    }
    
    public Iterable<Literal> getLiterals() { return _literals; } 
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      /* old style literal is converted to false clause*/
      if (_literals == null) return indent + "$false";
      
      assert !_literals.isEmpty();
      String res = _literals.get(0).toString(indent);
      for (int n = 1; n < _literals.size(); ++n)
        res = res + indent + "  |\n" + indent + _literals.get(n);
      return res;
    }
    
    
    
    private LinkedList<Literal> _literals = null;
    
  }