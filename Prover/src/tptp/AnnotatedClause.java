package tptp;


/** Represents instances of &#60cnf annotated&#62 in the BNF grammar. */
  public class AnnotatedClause extends TopLevelItem {
    
    public AnnotatedClause(String name,
                           TptpParserOutput.FormulaRole role,
                           TptpParserOutput.CnfFormula clause,
                           TptpParserOutput.Annotations annotations,
                           int lineNumber)
    {
      _kind = TopLevelItem.Kind.Clause;
      _name = name;
      _role = role;
      _clause = (Clause)clause;
      _annotations = (Annotations)annotations;
      _lineNumber = lineNumber;
    }
    
    public String getName() { return _name; }

    public TptpParserOutput.FormulaRole getRole() { return _role; } 

    public Clause getClause() { return _clause; }
    
    public Annotations getAnnotations() { return _annotations; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent + "cnf(" + _name + "," + _role + ",\n" +
                   _clause.toString(indent + "  ");
      if (_annotations != null) 
      {
        res = res + ",\n" + _annotations.toString(indent + "  ") + "\n";
      }
      else
        res = res + "\n";
      res = res + indent + ").";
      return res;
    }
    
    private String _name;
    private TptpParserOutput.FormulaRole _role;
    private Clause _clause;
    private Annotations _annotations;
  } // class AnnotatedClause