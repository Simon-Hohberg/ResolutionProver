package tptp;

import java.util.LinkedList;


public class Atomic extends Formula
implements TptpParserOutput.AtomicFormula
{

  public Atomic(String predicate,
                Iterable<TptpParserOutput.Term> arguments) 
  {
    _kind = Kind.Atomic;
    _predicate = predicate;
    if (arguments != null) {
      _arguments = new LinkedList<Term>();
      for (TptpParserOutput.Term arg : arguments) {
        _arguments.add((Term)arg);
      };
    };
  }


  public String getPredicate() { return _predicate; }

  public int getNumberOfArguments() { 
    return (_arguments == null)? 0 : _arguments.size();
  }

  public Iterable<Term> getArguments() { return _arguments; }

  /** @param obj must be convertible to Atomic, can be null */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    return _kind == ((Formula)obj)._kind && 
    _predicate.equals(((Atomic)obj)._predicate) &&
    ( _arguments == null 
    ? ((Atomic)obj)._arguments == null
    : _arguments.equals(((Atomic)obj)._arguments));
  }


  public int hashCode() {
    int res = _kind.hashCode();
    res = 31 * res + _predicate.hashCode();
    res = 31 * res;
    if (_arguments != null) res += _arguments.hashCode();
    return res;
  }


  public String toString() { return toString(new String("")); }

  public String toString(String indent) {
    String res = indent;
    if (_predicate.compareTo("=") == 0) { /* equality infix opertor */
      assert _arguments != null && _arguments.size() == 2;
      res = res + _arguments.get(0) + "=" + _arguments.get(1);
    }
    else { /* usual predicate */
      res = res + _predicate;
      if (_arguments != null) {
        res = res + "(" + _arguments.get(0);
      for (int n = 1; n < _arguments.size(); ++n) {
        res = res + "," + _arguments.get(n);
      };
      res = res + ")";
      };
    }
    return res;
  }




  private String _predicate;

  private LinkedList<Term> _arguments = null;

} // class Atomic