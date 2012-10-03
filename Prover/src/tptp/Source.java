package tptp;

import java.util.LinkedList;

import tptp.TptpParserOutput.IntroType;

public class Source
  implements TptpParserOutput.Source
  {  
    public static enum Kind 
    {
      Name,
      Inference,
      Internal,
      File,
      Creator,
      Theory
    }
    
    public static class Name extends Source {
      
      public Name(String text) {
        _kind = Kind.Name;
        _text = text;
      }
      
      String getText() { return _text; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) { return _text; }
      
      private String _text;
      
    } // class Name 
    
    public static class Inference extends Source {
      
      public 
      Inference(String inferenceRule,
                Iterable<TptpParserOutput.InfoItem> usefulInfo,
                Iterable<TptpParserOutput.ParentInfo> parentInfoList)
      {
        _kind = Kind.Inference;        
        _inferenceRule = inferenceRule;
        if (usefulInfo != null) {
          _usefulInfo = new LinkedList<InfoItem>();
          for (TptpParserOutput.InfoItem item : usefulInfo) 
            _usefulInfo.add((InfoItem)item);
        };
        if (parentInfoList != null) {
          _parentInfoList = new LinkedList<ParentInfo>();
          for (TptpParserOutput.ParentInfo par : parentInfoList)
            _parentInfoList.add((ParentInfo)par);
        };
      }
      
      public String getInferenceRule() { return _inferenceRule; }
      
      /** May return null. */
      public Iterable<InfoItem> getUsefulInfo() { return  _usefulInfo; }
      
      public Iterable<ParentInfo> getParentInfoList() { return _parentInfoList; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = indent + "inference(" + _inferenceRule + ",[";
        if (_usefulInfo != null) {
          assert !_usefulInfo.isEmpty();
          res = res + _usefulInfo.get(0);
          for (int n = 1; n < _usefulInfo.size(); ++n)
            res = res + ", " + _usefulInfo.get(n);
        };
        res = res + "],[";
        if (_parentInfoList != null) {
          res = res + _parentInfoList.get(0);
          for (int n = 1; n < _parentInfoList.size(); ++n)
            res = res + "," + _parentInfoList.get(n);
        };
        res = res + "])";
        return res;
      }
      
      
      private String _inferenceRule;
      
      private LinkedList<InfoItem> _usefulInfo = null;
      
      private LinkedList<ParentInfo> _parentInfoList = null;
      
    } // class Inference 
    
    
    public static class Internal extends Source {
      
      public Internal(TptpParserOutput.IntroType introType,
                      Iterable<TptpParserOutput.InfoItem> introInfo) {
        _kind = Kind.Internal;
        _introType = introType;
        if (introInfo != null) {
          _introInfo = new LinkedList<InfoItem>();
          for (TptpParserOutput.InfoItem item : introInfo)
            _introInfo.add((InfoItem)item);
        }
        else _introInfo = null;
        ;
      }
      
      public IntroType getIntroType() { return _introType; }
      
      public Iterable<InfoItem> getIntroInfo() { return _introInfo; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = indent + "introduced(" + _introType;
        if (_introInfo != null) {
          assert !_introInfo.isEmpty();
          res = res + ",[" + _introInfo.get(0);
          for (int n = 1; n < _introInfo.size(); ++n) 
            res = res + "," + _introInfo.get(n);
          res = res + "]";
        };
        res = res + ")";
        return res;
      }
      
      
      
      private IntroType _introType;
      
      private LinkedList<InfoItem> _introInfo = null;
      
    } // class Internal
    
    public static class File extends Source {
      
      public File(String fileName,String fileInfo) {
        _kind = Kind.File;
        _fileName = fileName;
        _fileInfo = fileInfo;
      }
      
      public String getFileName() { return _fileName; }
      
      public String getFileInfo() { return _fileInfo; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = indent + "file(" + _fileName;
        if (_fileInfo != null) res = res + "," + _fileInfo;
        res = res + ")";
        return res;
      }
      
      
      
      private String _fileName;
      
      /** If null, corresponds to the value 'unknown'. */
      private String _fileInfo;
      
    } // class File
    
    
    
    public static class Creator extends Source {
      
      public Creator(String creatorName,
                     Iterable<TptpParserOutput.InfoItem> usefulInfo) 
      {
        _kind = Kind.Creator;
        _creatorName = creatorName;
        if (usefulInfo != null) {
          _usefulInfo = new LinkedList<InfoItem>();
          for (TptpParserOutput.InfoItem item : usefulInfo)
            _usefulInfo.add((InfoItem)item);
        }
      }
      
      public String getCreatorName() { return _creatorName; }
      
      public Iterable<InfoItem> getUsefulInfo() { return _usefulInfo; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = indent + "creator(" + _creatorName;
        if (_usefulInfo != null) {
          assert !_usefulInfo.isEmpty();
          res = res + ",[" + _usefulInfo.get(0);
          for (int n = 1; n < _usefulInfo.size(); ++n)
            res = res + "," + _usefulInfo.get(n);
          res = res + "]";
        };
        res = res + ")";
        return res;
      }
      
      
      private String _creatorName;
      
      private LinkedList<InfoItem> _usefulInfo = null;
      
    } // class Creator
    
    
    
    
    public static class Theory extends Source {
      
      public Theory(String theoryName,
                    Iterable<TptpParserOutput.InfoItem> usefulInfo) {
        _kind = Kind.Theory;
        _theoryName = theoryName;
        if (usefulInfo != null) {
          _usefulInfo = new LinkedList<InfoItem>();
          for (TptpParserOutput.InfoItem item : usefulInfo)
            _usefulInfo.add((InfoItem)item);
        }
      }
      
      public String getTheoryName() { return _theoryName; }

      public Iterable<InfoItem> getUsefulInfo() { return _usefulInfo; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = indent + "theory(" + _theoryName;
        if (_usefulInfo != null) {
          assert !_usefulInfo.isEmpty();
          res = res + ",[" + _usefulInfo.get(0);
          for (int n = 1; n < _usefulInfo.size(); ++n)
            res = res + "," + _usefulInfo.get(n);
          res = res + "]";
        };
        res = res + ")";
        return res;
      }
      
      
      
      
      private String _theoryName;
      
      private LinkedList<InfoItem> _usefulInfo = null;
    } // class Theory
    
    
    
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      switch (_kind) 
      {
        case Name: return ((Source.Name)this).toString(indent);
        case Inference: return ((Source.Inference)this).toString(indent);
        case Internal: return ((Source.Internal)this).toString(indent);
        case File: return ((Source.File)this).toString(indent);
        case Creator: return ((Source.Creator)this).toString(indent);
        case Theory: return ((Source.Theory)this).toString(indent);
      };
      assert false;
      return null;
    }
    
    
    
    
    protected Source.Kind _kind;
    
  }