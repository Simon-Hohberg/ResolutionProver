package tptp;

import tptp.TptpParserOutput.TptpInput.Kind;

/** A common base for the classes AnnotatedFormula, AnnotatedClause
  *  and IncludeDirective.
  */
  public class TopLevelItem implements TptpParserOutput.TptpInput {
    
    public Kind getKind() { return _kind; }
    
    public int getLineNumber() { return _lineNumber; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      switch (_kind) {
        case Formula: return ((AnnotatedFormula)this).toString(indent);
        case Clause: return ((AnnotatedClause)this).toString(indent);
        case Include: return ((IncludeDirective)this).toString(indent);
      };
      assert false;
      return null;
    }
    
    
    protected Kind _kind;
    
    protected int _lineNumber; 
    
  } // class TopLevelItem