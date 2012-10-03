package tptp;

import java.util.LinkedList;

/** Represents instances of &#60include&#62 in the BNF grammar. */
  public class IncludeDirective extends TopLevelItem {
    
    public IncludeDirective(String fileName,
                            Iterable<String> formulaSelection,
                            int lineNumber)
    {
      _kind = TopLevelItem.Kind.Include;
      _fileName = fileName;
      if (formulaSelection != null) {
        _formulaSelection = new LinkedList<String>();
        for (String name : formulaSelection) 
          _formulaSelection.add(name);
      };
      _lineNumber = lineNumber;
    }
    
    public String getFileName() { return _fileName; }
    
    Iterable<String> getFormulaSelection() { return _formulaSelection; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent + "include(" + _fileName;
      if (_formulaSelection != null) {
        res = res + ",\n" + indent + "  [";
        for (int n = 0; n < _formulaSelection.size(); ++n) {
          if (n != 0) res = res + ",\n";
          res = res + indent + "  " + _formulaSelection.get(n);
        };
        res = res + "\n" + indent + "  ]\n" + indent;  
      };
      res = res + ").";
      return res;
    }
    
    private String _fileName;
    private LinkedList<String> _formulaSelection = null;
  } // class IncludeDirective