package tptp;

import java.util.LinkedList;

import tptp.TptpParserOutput.InfoItem;

public class Annotations 
  implements TptpParserOutput.Annotations
  {
    public Annotations(TptpParserOutput.Source source,
                       Iterable<TptpParserOutput.InfoItem> usefulInfo)
    {
      assert source != null;
      _source = (Source)source;
      if (usefulInfo != null) {
        _usefulInfo = new LinkedList<InfoItem>();
        for (TptpParserOutput.InfoItem item : usefulInfo)
          _usefulInfo.add((InfoItem)item);
      };
    }
    
    public Source getSource() { return _source; }
    
    public Iterable<InfoItem> usefulInfo() { return _usefulInfo; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent + _source;
      if (_usefulInfo != null)
      {
        assert !_usefulInfo.isEmpty();
        res = res + ", [";
        res = res + _usefulInfo.get(0);
        for (int n = 1; n < _usefulInfo.size(); ++n) 
          res = res + "," + _usefulInfo.get(n);
        res = res + "]";
      };
      return res;
    }
    
    
    
    private Source _source;
    
    private LinkedList<InfoItem> _usefulInfo = null;
    
  }