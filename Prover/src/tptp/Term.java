package tptp;

import java.util.LinkedList;

public class Term implements TptpParserOutput.Term {

  public Term(Symbol topSymbol, Iterable<TptpParserOutput.Term> arguments) {
    _topSymbol = topSymbol;
    if (arguments != null) {
      _arguments = new LinkedList<Term>();
      for (TptpParserOutput.Term arg : arguments) {
        _arguments.add((Term) arg);
      }
      ;
    }
    ;
  }

  public Symbol getTopSymbol() {
    return _topSymbol;
  }

  public int getNumberOfArguments() {
    return (_arguments == null) ? 0 : _arguments.size();
  }

  public Iterable<Term> getArguments() {
    return _arguments;
  }

  /**
   * @param obj
   *          must be convertible to Term, can be null
   */
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (this == obj)
      return true;
    return _topSymbol.equals(((Term) obj)._topSymbol)
        && (_arguments == null ? ((Term) obj)._arguments == null : _arguments
            .equals(((Term) obj)._arguments));
  }

  public int hashCode() {
    return 31 * _topSymbol.hashCode()
        + ((_arguments == null) ? 0 : _arguments.hashCode());
  }

  public String toString() {
    return toString(new String(""));
  }

  public String toString(String indent) {
    String res = indent + _topSymbol;
    if (_arguments != null) {
      assert !_arguments.isEmpty();
      res = res + "(" + _arguments.get(0);
      for (int n = 1; n < _arguments.size(); ++n)
        res = res + "," + _arguments.get(n);
      res = res + ")";
    }
    ;
    return res;
  }

  private Symbol _topSymbol;

  private LinkedList<Term> _arguments = null;

}