package tptp;

import java.util.LinkedList;

import tptp.TptpParserOutput.Source;

public class InfoItem
  implements TptpParserOutput.InfoItem
  {  
    public static enum Kind 
    {
      Description,
      IQuote,
      InferenceStatus,
      InferenceRule,
      Refutation,
      GeneralFunction
    }
    
    
    public static class Description extends InfoItem {
      
      public Description(String description) {
        _kind = Kind.Description;
        _description = description;
      }
      
      public String getDescription() { return _description; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        return indent + "description(" + _description + ")";
      }
      
      
      private String _description;
      
    } // class Description
    
    
    
    public static class IQuote extends InfoItem {
      
      public IQuote(String text) {
        _kind = Kind.IQuote;
        _text = text;
      }
      
      public String getIQuoteText() { return _text; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        return indent + "iquote(" + _text + ")";
      }
      
      
      
      private String _text;
      
    } // class IQuote
    
    
    
    public static class InferenceStatus extends InfoItem {
      
      public 
      InferenceStatus(TptpParserOutput.StatusValue statusValue) {
        _kind = Kind.InferenceStatus;
        _statusValue = statusValue;
      }
      
      public TptpParserOutput.StatusValue getStatusValue() {
        return _statusValue;
      }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        return indent + "status(" + _statusValue + ")";
      }
      
      
      
      private TptpParserOutput.StatusValue _statusValue;
      
    } // class InferenceStatus
    
    
    
    public static class InferenceRule extends InfoItem {
      
      public 
      InferenceRule(String inferenceRule,
                    String inferenceId,
                    Iterable<TptpParserOutput.GeneralTerm> attributes) {
        _kind = Kind.InferenceRule;
        _inferenceRule = inferenceRule;
        _inferenceId = inferenceId;
        if (attributes != null) {
          _attributes = 
          new LinkedList<GeneralTerm>();
          for (TptpParserOutput.GeneralTerm term : attributes)
            _attributes.add((GeneralTerm)term);
        };
      }
      
      
      public String getIinferenceRule() { return _inferenceRule; }
      
      public String getInferenceId() { return _inferenceId; }
      
      public Iterable<GeneralTerm> getAttributes() { return _attributes; } 
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        String res = 
        indent + _inferenceRule + "(" + _inferenceId + ",[";
        if (_attributes != null) {
          assert !_attributes.isEmpty();
          res = res + _attributes.get(0);
          for (int n = 1; n < _attributes.size(); ++n)
            res = res + "," + _attributes.get(n);
        };
        res = res + "])";
        return res;
      }
      
      
      
      
      
      private String _inferenceRule;
      
      private String _inferenceId;
      
      private 
      LinkedList<GeneralTerm> _attributes = null;
      
    } // class InferenceRule
    
    
    
    public static class Refutation extends InfoItem {
      
      public Refutation(TptpParserOutput.Source fileSource) {
        _kind = Kind.Refutation;
        _fileSource = (Source)fileSource;
      }
      
      public Source getFileSource() { return _fileSource; }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        return indent + "refutation(" + _fileSource + ")";
      }
      
      
      
      
      private Source _fileSource;
      
    } // class Refutation
    
    
    
    public static class GeneralFunction extends InfoItem {
      
      public 
      GeneralFunction(TptpParserOutput.GeneralTerm generalFunction) {
        _kind = Kind.GeneralFunction;
        _generalFunction = (GeneralTerm)generalFunction;
      }
      
      
      public GeneralTerm getGeneralFunction() { 
        return _generalFunction;
      }
      
      public String toString() { return toString(new String("")); }
      
      public String toString(String indent) {
        return _generalFunction.toString(indent);
      }
      
      
      
      private GeneralTerm _generalFunction;
      
    } // class GeneralFunction
    
    
    
    
    public String toString() { return toString(new String("")); }
    
    public String toString(String indent) {
      switch (_kind)
      {
        case Description: 
        return ((InfoItem.Description)this).toString(indent);
        case IQuote: return ((InfoItem.IQuote)this).toString(indent);
        case InferenceStatus: 
        return ((InfoItem.InferenceStatus)this).toString(indent);
        case InferenceRule: 
        return ((InfoItem.InferenceRule)this).toString(indent);
        case Refutation: return ((InfoItem.Refutation)this).toString(indent);
        case GeneralFunction: 
        return ((InfoItem.GeneralFunction)this).toString(indent);
      };
      assert false;
      return null;
    }
    
    
    protected InfoItem.Kind _kind;
    
  }