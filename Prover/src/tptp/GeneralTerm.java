package tptp;

import java.util.LinkedList;

public class GeneralTerm
  implements TptpParserOutput.GeneralTerm
  {
    public GeneralTerm(String function,
                       Iterable<TptpParserOutput.GeneralTerm> arguments) {
      assert function != null;
      _kind = GeneralTermKind.Function;
      _str = function;
      if (arguments != null) {
        _arguments = new LinkedList<GeneralTerm>();
        for (TptpParserOutput.GeneralTerm arg : arguments)
          _arguments.add((GeneralTerm)arg);
      };
      _left = null;
      _right = null;
    }
    
    public GeneralTerm(Iterable<TptpParserOutput.GeneralTerm> elements) {
      _kind = GeneralTermKind.List;
      if (elements != null) {
        _arguments = new LinkedList<GeneralTerm>();
        for (TptpParserOutput.GeneralTerm el : elements)
          _arguments.add((GeneralTerm)el);
      };
      _str = null;
      _left = null;
      _right = null;
    }

    public GeneralTerm(TptpParserOutput.GeneralTerm left,
                       TptpParserOutput.GeneralTerm right) {
      _kind = GeneralTermKind.Colon;
      assert left != null;
      assert right != null;
      _left = (GeneralTerm)left;
      _right = (GeneralTerm)right;
      _str = null;
      _arguments = null;
    }
    
    public GeneralTerm(String str) {
      _kind = GeneralTermKind.DistinctObject;
      assert str != null;
      _str = str;
      _left = null;
      _right = null;
      _arguments = null;
    }

    public boolean isFunction() { return _kind == GeneralTermKind.Function; }
    public boolean isList() { return _kind == GeneralTermKind.List; }
    public boolean isColon() { return _kind == GeneralTermKind.Colon; }
    public boolean isDistinctObject() 
                          { return _kind == GeneralTermKind.DistinctObject; }
    
    /** Precondition: isFunction(). */
    public String getFunction() {
      assert isFunction();
      return _str;
    }
    
    /** Precondition: isFunction(). */
    public Iterable<GeneralTerm> getArguments() {
      assert isFunction();
      return _arguments;
    }
    
    /** Precondition: isList(). */
    public Iterable<GeneralTerm> getListElements() {
      assert isList();
      return _arguments;
    }
    
    /** Precondition: isColon(). */
    public GeneralTerm getLeftColonOperand() {
      assert isColon();
      return _left;
    }

    /** Precondition: isColon(). */
    public GeneralTerm getRightColonOperand() {
      assert isColon();
      return _right;
    }
    
    /** Precondition: isDistinctObject(). */
    public String getDistinctObject() {
      assert isDistinctObject();
      return _str;
    }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent;
      switch (_kind) {
      case Function:
        res = res + _str;
        if (_arguments == null) return res; /* this is a <constant> */
        assert !_arguments.isEmpty();
        res = res + "(";
        res = res + _arguments.get(0);
        for (int n = 1; n < _arguments.size(); ++n) 
          res = res + "," + _arguments.get(n);
        res = res + ")";
        break;
      case List:
        /* this is empty <general list> */
        if (_arguments == null) return res + "[]";
        res = res + "[";
        res = res + _arguments.get(0);
        for (int n = 1; n < _arguments.size(); ++n) 
          res = res + "," + _arguments.get(n);
        res = res + "]";
        break;
      case Colon:
        res = res + _left;
        res = res + ":";
        res = res + _right;
        break;
      case DistinctObject:
        res = res + _str;
        break;
      }
      return res;
    }
    
    private enum GeneralTermKind {
      Function,
      List,
      Colon,
      DistinctObject
    };
    
    private GeneralTerm.GeneralTermKind _kind;
    
    private String _str;
    private LinkedList<GeneralTerm> _arguments = null;
    private GeneralTerm _left;
    private GeneralTerm _right;
  }