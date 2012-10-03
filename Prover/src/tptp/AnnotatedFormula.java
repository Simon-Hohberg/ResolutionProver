package tptp;


/** Represents instances of &#60fof annotated&#62 in the BNF grammar. */
  public class AnnotatedFormula extends TopLevelItem {
    
    public AnnotatedFormula(String name,
                            TptpParserOutput.FormulaRole role,
                            TptpParserOutput.FofFormula formula,
                            TptpParserOutput.Annotations annotations,
                            int lineNumber)
    {
      _kind = TopLevelItem.Kind.Formula;
      _name = name;
      _role = role;
      _formula = (Formula)formula;
      _annotations = (Annotations)annotations;
      _lineNumber = lineNumber;
    }
    
    public String getName() { return _name; }

    public TptpParserOutput.FormulaRole getRole() { return _role; }
     
    public Formula getFormula() { return _formula; }
    
    public Annotations getAnnotations() { return _annotations; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent + "fof(" + _name + "," + _role + ",\n" +
                   _formula.toString(indent + "  ");
      if (_annotations != null) {
        res = res + ",\n" + _annotations.toString(indent + "  ") + "\n";
      }
      else
        res = res + "\n";
      res = res + indent + ").";
      return res;
    }
    
    private String _name;
    private TptpParserOutput.FormulaRole _role;
    private Formula _formula;
    private Annotations _annotations;
  } // class AnnotatedFormula