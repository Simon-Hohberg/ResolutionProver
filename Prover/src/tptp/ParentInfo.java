package tptp;

import tptp.TptpParserOutput.Source;

public class ParentInfo implements TptpParserOutput.ParentInfo {
    
    public ParentInfo(TptpParserOutput.Source source,String parentDetails) {
      _source = (Source)source;
      _parentDetails = parentDetails;
    }
    
    Source getSource() { return _source; }
    
    String getParentDetails() { return _parentDetails; }
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      String res = indent + _source;
      if (_parentDetails != null)
        res = res + ":" + _parentDetails;
      return res;
    }
    
    
    private Source _source;
    
    private String _parentDetails;
    
  }