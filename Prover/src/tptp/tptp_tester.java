package tptp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import resolutionprover.ResolutionProver;

/**
 * This is a program to test TPTP file for being syntactically and semantically
 * correct.
 * 
 * The invocation is: tptp_tester [-o output_file] [-I include_dir] input_file
 * tptp_tester -h
 * 
 * input_file -- an input TPTP file to be checked -h -- prints help message and
 * exits -o output_file -- outputs the parsed tree into output_file -I
 * include_dir -- specifies where to look for included files; if -I is ommited,
 * all include directives will be ignored\n\n
 * 
 * 
 * @author Andrei Tchaltsev
 * @author Alexandre Riazanov
 * @since Mar 26, 2006
 * @since Apr 19, 2006, parsing of includes was added by Alexandre Riazanov
 * 
 */
public class tptp_tester {

  public static void help_print() {
    System.out
        .print("This is a program to test TPTP file for being syntactically correct.\n"
            + "The invocation is:\n"
            + "  tptp_tester [-h] [-o output_file] [-I include_dir] input_file\n"
            + "  tptp_tester -h\n\n"
            + "   input_file -- an input TPTP file to be checked\n"
            + "   -h -- tells the program to print help message and exit\n"
            + "   -o output_file -- tells the program to output the parsed formulas/clauses into output_file\n"
            + "   -I include_dir -- specifies where to look for included files; "
            + "                     if -I is ommited, all include directives will be ignored\n\n");
    return;
  }

  public static void main(String[] args) {
    String inFile = null; /* name of the input file */
    String outFile = null; /* name of the output file */
    String includeDir = null;
    try {

      /* process the command line options */
      /* there is input file */
      if (args.length == 1 && !args[0].equals("-h")) {
        inFile = args[0];
      }
      /* there are input and output files */
      else if (args.length == 3 && args[0].equals("-o")) {
        outFile = args[1];
        inFile = args[2];
      }
      /* there is an input file and an include directory */
      else if (args.length == 3 && args[0].equals("-I")) {
        inFile = args[2];
        includeDir = args[1];
      }
      /* there are input and output files, and an include directory */
      else if (args.length == 5 && args[0].equals("-o") && args[2].equals("-I")) {
        outFile = args[1];
        includeDir = args[3];
        inFile = args[4];
      }
      /* there are input and output files, and an include directory */
      else if (args.length == 5 && args[2].equals("-o") && args[0].equals("-I")) {
        outFile = args[3];
        includeDir = args[1];
        inFile = args[4];
      } else { /* print the help message and exit */
        help_print();
        return;
      }

      /*
       * check the input file for existence. This check is done just to output
       * nicer error messages
       */
      {
        File file = new File(inFile);
        if (!file.isFile()) {
          System.err.print("Error: cannot find an input file \"" + inFile
              + "\"\n");
          return;
        }
        if (!file.canRead()) {
          System.err.print("Error: cannot read an input file \"" + inFile
              + "\"\n");
          return;
        }
      }

      /* the whole output of the parser */
      LinkedList<TopLevelItem> allParsed = new LinkedList<TopLevelItem>();

      /* create a parser output object */
      SimpleTptpParserOutput outputManager = new SimpleTptpParserOutput();

      /* prepare the symbol table for semantic checks */

      _signature.put(new String("="), new SymbolDescriptor(true, 2)); // "=" is
                                                                      // a
                                                                      // binary
                                                                      // predicate

      /* parse the input file and semantically check the results */

      parseAndCheck(inFile, includeDir, outputManager, allParsed, 0);

      System.out.println("File \"" + inFile + "\" is OK");

      /* printing the parsed formulas/clauses to a file */
      if (outFile != null) {
        FileOutputStream out = new FileOutputStream(outFile);
        for (TptpParserOutput.TptpInput iter : allParsed) {
          out.write(("\n" + iter.toString()).getBytes());
        } /* for */
        System.out.println("Parsed formulas/clauses were printed to \""
            + outFile + "\"");
        out.close();
      } /* if */

      // *********************************************************************************
      // //
      // Added by Christoph Benzmueller, Sep 2012
      // *********************************************************************************
      // //
      List<Formula> formulae = new LinkedList<Formula>();
      for (TopLevelItem item : allParsed) {
        Formula formula = ((AnnotatedFormula) item).getFormula();
        formulae.add(formula);
      }
      ResolutionProver prover = new ResolutionProver(formulae);
      boolean proven = prover.prove();
      System.out.println("proven: " + proven);
    }
    // general ANTLR exception. It is enough to catch all ANTRL exceptions
    catch (antlr.ANTLRException e) {
      System.err.println("\nERROR during parsing \"" + inFile + "\": " + e);
    }
    // general exception. it is enough to catch all exceptions.
    catch (Exception e) { // general exception
      System.err.println("\nGENERAL exception: " + e);
      e.printStackTrace();
    }
  } /* end of main function */

  /**
   * Parses the file <strong> fileName </strong> and checks the results
   * semantically. If <strong> includeDir </strong> != null, included files are
   * parsed recursively as soon as they appear.
   */
  public static void parseAndCheck(String fileName, String includeDir,
      SimpleTptpParserOutput outputManager, LinkedList<TopLevelItem> results,
      int recursionDepth) throws Exception {
    FileInputStream in = new FileInputStream(fileName);
    TptpLexer lexer = new TptpLexer(new DataInputStream(in));
    TptpParser parser = new TptpParser(lexer);

    for (TopLevelItem item = (TopLevelItem) parser.topLevelItem(outputManager); item != null; item = (TopLevelItem) parser
        .topLevelItem(outputManager)) {
      if (includeDir != null
          && item.getKind() == TptpParserOutput.TptpInput.Kind.Include) {
        if (recursionDepth >= 1024)
          throw new Exception(
              "Too many nested include directives (depth > 1024) " + "in "
                  + fileName + ", line " + item.getLineNumber() + ".");

        String relativeIncludeFileName = ((IncludeDirective) item)
            .getFileName();

        // The file name has to be truncated because it is quoted

        relativeIncludeFileName = relativeIncludeFileName.substring(1,
            relativeIncludeFileName.length() - 1);

        String includeFileName = includeDir + "/" + relativeIncludeFileName;

        parseAndCheck(includeFileName, includeDir, outputManager, results,
            recursionDepth + 1);
      } else {
        /*
         * Minimal semantic analysis: check that no symbol is used both as a
         * predicate and as a function or constant, and that no symbol is used
         * with different arities. checkSemantically(item) will throw
         * java.lang.Exception(<error description>) if something is wrong with
         * the item.
         */
        checkSemantically(item, fileName);

        results.add(item);
      }
      ;
    }
    ;

    in.close();

  } // parseAndCheck(..)

  /**
   * Checks whether <strong> item </strong> is semantically well-formed wrt the
   * signature <strong> _signature </strong>. Throws java.lang.Exception(<error
   * description>) if the use of some symbol in <strong> item </strong> is
   * inconsistent with its declaration in <strong> _signature </strong>. Side
   * effect: new symbols are declared in <strong> _signature </strong>.
   * 
   * @param item
   *          != null
   * @param fileName
   *          file name to be used in error messages
   */
  public static void checkSemantically(TopLevelItem item, String fileName)
      throws Exception {
    assert item != null;

    _semanticCheckFileName = fileName;
    _semanticCheckLineNumber = item.getLineNumber();

    switch (item.getKind()) {
    case Formula:
      checkSemantically(((AnnotatedFormula) item).getFormula());
      break;
    case Clause:
      checkSemantically(((AnnotatedClause) item).getClause());
      break;
    case Include:
      break; // nothing to check
    }
  } // checkSemantically(SimpleTptpParserOutput.TopLevelItem item)

  /**
   * Auxilliary for checkSemantically(SimpleTptpParserOutput.TopLevelItem
   * item,String fileName): checks whether <strong> formula </strong> is
   * semantically well-formed wrt the signature <strong> _signature </strong>.
   * Side effect: new symbols are declared in <strong> _signature </strong>.
   */
  public static void checkSemantically(Formula formula) throws Exception {

    switch (formula.getKind()) {
    case Atomic:
      checkSemantically((Atomic) formula);
      break;
    case Negation:
      checkSemantically(((Negation) formula).getArgument());
      break;
    case Binary:
      checkSemantically(((Binary) formula).getLhs());
      checkSemantically(((Binary) formula).getRhs());
      break;
    case Quantified:
      checkSemantically(((Quantified) formula).getMatrix());
      break;
    }
  } // checkSemantically(SimpleTptpParserOutput.Formula formula)

  /**
   * Auxilliary for checkSemantically(SimpleTptpParserOutput.TopLevelItem
   * item,String fileName): checks whether <strong> formula </strong> is
   * semantically well-formed wrt the signature <strong> _signature </strong>.
   * Side effect: new symbols are declared in <strong> _signature </strong>.
   */
  public static void checkSemantically(Clause clause) throws Exception {

    if (clause.getLiterals() != null) {

      for (Literal lit : clause.getLiterals())
        checkSemantically(lit.getAtom());

    }
    ;
  } // checkSemantically(SimpleTptpParserOutput.Clause clause)

  /**
   * Auxilliary for checkSemantically(SimpleTptpParserOutput.TopLevelItem
   * item,String fileName): checks whether <strong> formula </strong> is
   * semantically well-formed wrt the signature <strong> _signature </strong>.
   * Side effect: new symbols are declared in <strong> _signature </strong>.
   */
  public static void checkSemantically(Atomic atom) throws Exception {

    String predicate = atom.getPredicate();

    int arity = atom.getNumberOfArguments();

    // Look up this symbol in the signature:
    SymbolDescriptor predicateDescriptor = _signature.get(predicate);

    if (predicateDescriptor == null) {
      // New symbol, has to be added to _signature
      _signature.put(predicate, new SymbolDescriptor(true, arity));
    } else {
      if (!predicateDescriptor.isPredicate) {
        throw new Exception("Semantic error in " + _semanticCheckFileName
            + ", line " + _semanticCheckLineNumber + ":" + "in atom " + atom
            + ": predicate " + predicate + " was used as a function elsewhere.");
      }
      ;

      if (predicateDescriptor.arity != arity) {
        throw new Exception("Semantic error in " + _semanticCheckFileName
            + ", line " + _semanticCheckLineNumber + ":" + "in atom " + atom
            + ": predicate " + predicate
            + " was used with a different arity elsewhere.");
      }
      ;
    }
    ;

    // Check the arguments

    if (atom.getArguments() != null)
      for (Term arg : atom.getArguments())
        checkSemantically(arg);

  } // checkSemantically(SimpleTptpParserOutput.Formula.Atomic atom)

  /**
   * Auxilliary for checkSemantically(SimpleTptpParserOutput.TopLevelItem
   * item,String fileName): checks whether <strong> formula </strong> is
   * semantically well-formed wrt the signature <strong> _signature </strong>.
   * Side effect: new symbols are declared in <strong> _signature </strong>.
   */
  public static void checkSemantically(Term term) throws Exception {

    if (!term.getTopSymbol().isVariable()) {

      String function = term.getTopSymbol().getText();

      int arity = term.getNumberOfArguments();

      // Look up the function symbol in the signature
      SymbolDescriptor functionDescriptor = _signature.get(function);

      if (functionDescriptor == null) {
        // New symbol, has to be added to _signature
        _signature.put(function, new SymbolDescriptor(false, arity));
      } else {
        if (functionDescriptor.isPredicate) {
          throw new Exception("Semantic error in " + _semanticCheckFileName
              + ", line " + _semanticCheckLineNumber + ":" + "in term " + term
              + ": function " + function
              + " was used as a predicate elsewhere.");
        }
        ;

        if (functionDescriptor.arity != arity) {
          throw new Exception("Semantic error in " + _semanticCheckFileName
              + ", line " + _semanticCheckLineNumber + ":" + "in term " + term
              + ": function " + function
              + " was used with a different arity elsewhere.");
        }
        ;
      }
      ;

      // Check the arguments

      if (term.getArguments() != null)
        for (Term arg : term.getArguments())
          checkSemantically(arg);

    }
    ; // if (!term.getTopSymbol().isVariable())
  } // checkSemantically(SimpleTptpParserOutput.Term term)

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // See Fitting p.13
  public static int formulaDegreeAsInFitting(Formula formula) {
    int res = 0;
    switch (formula.getKind()) {
    case Atomic:
      res = 1;
      break;
    case Negation:
      int argdepth = formulaDegreeAsInFitting(((Negation) formula)
          .getArgument());
      res = 1 + argdepth;
      break;
    case Binary:
      int argldepth = formulaDegreeAsInFitting(((Binary) formula).getLhs());
      int argrdepth = formulaDegreeAsInFitting(((Binary) formula).getRhs());
      res = 1 + argldepth + argrdepth;
      break;
    case Quantified:
      int matrixdepth = formulaDegreeAsInFitting(((Quantified) formula)
          .getMatrix());
      res = 1 + matrixdepth;
      break;
    }
    return res;
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is $true
  public static boolean isTrue(Formula formula) {
    switch (formula.getKind()) {
    case Atomic:
      String pred = ((Atomic) formula).getPredicate();
      return pred.equals("$true");
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is $false
  public static boolean isFalse(Formula formula) {
    switch (formula.getKind()) {
    case Atomic:
      String pred = ((Atomic) formula).getPredicate();
      return pred.equals("$false");
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is atom different from $true and $false
  public static boolean isPureAtom(Formula formula) {
    switch (formula.getKind()) {
    case Atomic:
      return (!isTrue(formula) & !isFalse(formula));
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is ~ $true
  public static boolean isNegTrue(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      return isTrue(argument);
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is ~ $false
  public static boolean isNegFalse(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      return isFalse(argument);
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is ~ A where A is an atom different from $true and
  // $false
  public static boolean isNegAtom(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Atomic:
        return (!isTrue(argument) & !isFalse(argument));
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is ~ A where A is an atom different from $true and
  // $false
  public static Formula getNegAtom(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Atomic:
        if (!isTrue(argument) & !isFalse(argument)) {
          return argument;
        } else {
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is of form ~ ~ Z
  public static boolean isNegNegFormula(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Negation:
        return true;
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns Z if formula is of form ~ ~ Z
  public static Formula getNegNegFormula(Formula formula) {
    switch (formula.getKind()) {
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Negation:
        return ((Negation) argument).getArgument();
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is an alpha-formula
  public static boolean isAlphaFormula(Formula formula) {
    Formula argument;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      switch (connective) {
      case And:
        return true;
      case NotOr:
        return true;
      default:
        return false;
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        switch (connective) {
        case Or:
          return true;
        case NotAnd:
          return true;
        case Implication:
          return true;
        case ReverseImplication:
          return true;
        default:
          return false;
        }
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns the negation of a formula
  public static Formula negateFormula(Formula formula) {
    Formula newFormula = new Negation(formula);
    return newFormula;
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns alpha1 if formula is alpha, otherwise returns null
  public static Formula getAlpha1(Formula formula) {
    Formula argument;
    Formula returnFormula;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      returnFormula = ((Binary) formula).getLhs();
      switch (connective) {
      case And:
        return returnFormula;
      case NotOr:
        return negateFormula(returnFormula);
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        returnFormula = ((Binary) argument).getLhs();
        switch (connective) {
        case Or:
          return negateFormula(returnFormula);
        case NotAnd:
          return returnFormula;
        case Implication:
          return returnFormula;
        case ReverseImplication:
          return negateFormula(returnFormula);
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns alpha2 if formula is alpha, otherwise returns null
  public static Formula getAlpha2(Formula formula) {
    Formula argument;
    Formula returnFormula;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      returnFormula = ((Binary) formula).getRhs();
      switch (connective) {
      case And:
        return returnFormula;
      case NotOr:
        return negateFormula(returnFormula);
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        returnFormula = ((Binary) argument).getRhs();
        switch (connective) {
        case Or:
          return negateFormula(returnFormula);
        case NotAnd:
          return returnFormula;
        case Implication:
          return negateFormula(returnFormula);
        case ReverseImplication:
          return returnFormula;
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is a beta-formula
  public static boolean isBetaFormula(Formula formula) {
    Formula argument;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      switch (connective) {
      case Or:
        return true;
      case NotAnd:
        return true;
      case Implication:
        return true;
      case ReverseImplication:
        return true;
      default:
        return false;
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        switch (connective) {
        case And:
          return true;
        case NotOr:
          return true;
        default:
          return false;
        }
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns beta1 if formula is beta, otherwise returns null
  public static Formula getBeta1(Formula formula) {
    Formula argument;
    Formula returnFormula;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      returnFormula = ((Binary) formula).getLhs();
      switch (connective) {
      case Or:
        return returnFormula;
      case NotAnd:
        return negateFormula(returnFormula);
      case Implication:
        return negateFormula(returnFormula);
      case ReverseImplication:
        return returnFormula;
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        returnFormula = ((Binary) argument).getLhs();
        switch (connective) {
        case And:
          return negateFormula(returnFormula);
        case NotOr:
          return returnFormula;
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns beta2 if formula is beta, otherwise returns null
  public static Formula getBeta2(Formula formula) {
    Formula argument;
    Formula returnFormula;
    TptpParserOutput.BinaryConnective connective;
    switch (formula.getKind()) {
    case Binary:
      connective = ((Binary) formula).getConnective();
      returnFormula = ((Binary) formula).getRhs();
      switch (connective) {
      case Or:
        return returnFormula;
      case NotAnd:
        return negateFormula(returnFormula);
      case Implication:
        return returnFormula;
      case ReverseImplication:
        return negateFormula(returnFormula);
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Binary:
        connective = ((Binary) argument).getConnective();
        returnFormula = ((Binary) argument).getRhs();
        switch (connective) {
        case And:
          return negateFormula(returnFormula);
        case NotOr:
          return returnFormula;
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is a gamma-formula
  public static boolean isGammaFormula(Formula formula) {
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      switch (quantifier) {
      case ForAll:
        return true;
      default:
        return false;
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        switch (quantifier) {
        case Exists:
          return true;
        default:
          return false;
        }
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns gamma if formula is a gamma-formula; otherwise returns null
  public static Formula getGammaX(Formula formula) {
    // System.out.println("\n*** GammaX: " + formula.toString());
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    Formula matrix;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      matrix = ((Quantified) formula).getMatrix();
      switch (quantifier) {
      case ForAll:
        return matrix;
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        matrix = ((Quantified) argument).getMatrix();
        switch (quantifier) {
        case Exists:
          return matrix;
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns true if formula is a delta-formula
  public static boolean isDeltaFormula(Formula formula) {
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      switch (quantifier) {
      case Exists:
        return true;
      default:
        return false;
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        switch (quantifier) {
        case ForAll:
          return true;
        default:
          return false;
        }
      default:
        return false;
      }
    default:
      return false;
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns delta(X) if formula is a delta-formula; otherwise returns null
  public static Formula getDeltaX(Formula formula) {
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    Formula matrix;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      matrix = ((Quantified) formula).getMatrix();
      switch (quantifier) {
      case Exists:
        return matrix;
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        matrix = ((Quantified) argument).getMatrix();
        switch (quantifier) {
        case ForAll:
          return matrix;
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns gamma(t) if formula is a gamma-formula; otherwise returns null
  public static Formula getGammaT(Formula formula, Term term,
      SimpleTptpParserOutput out) {
    // System.out.println("\n*** GammaT: " + formula.toString() + " " +
    // term.toString());
    // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    String boundVariable;
    Term boundVariableTerm;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      boundVariable = ((Quantified) formula).getVariable();
      boundVariableTerm = (Term) out.createVariableTerm(boundVariable);
      switch (quantifier) {
      case ForAll:
        return substituteFormula(getGammaX(formula), boundVariableTerm, term,
            out);
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        boundVariable = ((Quantified) argument).getVariable();
        boundVariableTerm = (Term) out.createVariableTerm(boundVariable);
        switch (quantifier) {
        case Exists:
          return substituteFormula(getGammaX(formula), boundVariableTerm, term,
              out);
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // returns delta(t) if formula is a delta-formula; otherwise returns null
  public static Formula getDeltaT(Formula formula, Term term,
      SimpleTptpParserOutput out) {
    // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
    TptpParserOutput.Quantifier quantifier;
    Formula argument;
    String boundVariable;
    Term boundVariableTerm;
    switch (formula.getKind()) {
    case Quantified:
      quantifier = ((Quantified) formula).getQuantifier();
      boundVariable = ((Quantified) formula).getVariable();
      boundVariableTerm = (Term) out.createVariableTerm(boundVariable);
      switch (quantifier) {
      case Exists:
        return substituteFormula(getDeltaX(formula), boundVariableTerm, term,
            out);
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    case Negation:
      argument = ((Negation) formula).getArgument();
      switch (argument.getKind()) {
      case Quantified:
        quantifier = ((Quantified) argument).getQuantifier();
        boundVariable = ((Quantified) argument).getVariable();
        boundVariableTerm = (Term) out.createVariableTerm(boundVariable);
        switch (quantifier) {
        case ForAll:
          return substituteFormula(getDeltaX(formula), boundVariableTerm, term,
              out);
        default:
          throw new Error("Unexpected case: " + formula.toString());
        }
      default:
        throw new Error("Unexpected case: " + formula.toString());
      }
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // See Fitting's Textbook on p.25
  public static int formulaDepthAsInFitting(Formula formula) {
    if (isTrue(formula)) {
      return 0;
    } else if (isFalse(formula)) {
      return 0;
    } else if (isPureAtom(formula)) {
      return 0;
    } else if (isNegTrue(formula)) {
      return 1;
    } else if (isNegFalse(formula)) {
      return 1;
    } else if (isNegAtom(formula)) {
      return 0;
    } else if (isNegNegFormula(formula)) {
      return (formulaDepthAsInFitting(getNegNegFormula(formula)) + 1);
    } else if (isAlphaFormula(formula)) {
      return (Math.max(formulaDepthAsInFitting(getAlpha1(formula)),
          formulaDepthAsInFitting(getAlpha2(formula))) + 1);
    } else if (isBetaFormula(formula)) {
      return (Math.max(formulaDepthAsInFitting(getBeta1(formula)),
          formulaDepthAsInFitting(getBeta2(formula))) + 1);
    } else if (isGammaFormula(formula)) {
      return (formulaDepthAsInFitting(getGammaX(formula)) + 1);
    } else if (isDeltaFormula(formula)) {
      return (formulaDepthAsInFitting(getDeltaX(formula)) + 1);
    } else {
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // See Fitting's Textbook on p.25 and p.112
  public static int formulaRankAsInFitting(Formula formula) {
    // System.out.println("*** Enter formulaRankAsInFitting: " +
    // formula.toString());
    if (isTrue(formula)) {
      return 0;
    } else if (isFalse(formula)) {
      return 0;
    } else if (isPureAtom(formula)) {
      return 0;
    } else if (isNegTrue(formula)) {
      return 1;
    } else if (isNegFalse(formula)) {
      return 1;
    } else if (isNegAtom(formula)) {
      return 0;
    } else if (isNegNegFormula(formula)) {
      return (formulaRankAsInFitting(getNegNegFormula(formula)) + 1);
    } else if (isAlphaFormula(formula)) {
      return (formulaRankAsInFitting(getAlpha1(formula))
          + formulaRankAsInFitting(getAlpha2(formula)) + 1);
    } else if (isBetaFormula(formula)) {
      return (formulaRankAsInFitting(getBeta1(formula))
          + formulaRankAsInFitting(getBeta2(formula)) + 1);
    } else if (isGammaFormula(formula)) {
      return (formulaRankAsInFitting(getGammaX(formula)) + 1);
    } else if (isDeltaFormula(formula)) {
      return (formulaRankAsInFitting(getDeltaX(formula)) + 1);
    } else {
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  public static int clauseLength(Clause clause) {
    System.out.println(" clauseLength not yet implemented");
    return -1;
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  public static Term substituteTerm(Term term1, Term var, Term term2,
      SimpleTptpParserOutput out) {
    String topsymstring = term1.getTopSymbol().toString();
    int numberOfArgs = term1.getNumberOfArguments();
    LinkedList<Term> arguments = (LinkedList<Term>) term1.getArguments();
    Term res;
    if (numberOfArgs == 0) {
      if (term1.equals(var)) {
        res = term2;
      } else {
        res = term1;
      }
    } else {
      // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
      LinkedList<TptpParserOutput.Term> new_arguments = new LinkedList<TptpParserOutput.Term>();
      for (Term arg : arguments) {
        new_arguments.add(substituteTerm(arg, var, term2, out));
      }
      res = (Term) out.createPlainTerm(topsymstring, new_arguments);
    }
    // System.out.println(" SubstituteTerm(term1,var,term2): " +
    // term1.toString() + " " + var.toString() + " " + term2.toString());
    // System.out.println("                          Result: " + res);
    return res;
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // create fresh variable names
  protected static int varCounter = 0;

  public static String freshVariableName() {
    varCounter = ++varCounter;
    return ("V" + varCounter);
  }

  public static Term freshVariableTerm(SimpleTptpParserOutput out) {

    String newVarName = freshVariableName();
    Term var = (Term) out.createVariableTerm(newVarName);
    return var;
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  // Attention: this version of substitution does not yet check for variable
  // capturing!!!
  public static Formula substituteFormula(Formula formula, Term var, Term term,
      SimpleTptpParserOutput out) {
    // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
    switch (formula.getKind()) {
    case Atomic:
      String predicate = ((Atomic) formula).getPredicate();
      Iterable<Term> arguments = ((Atomic) formula).getArguments();
      LinkedList<TptpParserOutput.Term> new_arguments = new LinkedList<TptpParserOutput.Term>();
      for (Term arg : arguments) {
        new_arguments.add(substituteTerm(arg, var, term, out));
      }
      return (Formula) out.createPlainAtom(predicate, new_arguments);
    case Quantified:
      SimpleTptpParserOutput.Quantifier quantifier = ((Quantified) formula)
          .getQuantifier();
      String boundVariable = ((Quantified) formula).getVariable();
      Term boundVariableTerm = (Term) out.createVariableTerm(boundVariable);
      Formula matrix = ((Quantified) formula).getMatrix();

      String newVarName = freshVariableName();
      Term newVarTerm = (Term) out.createVariableTerm(newVarName);
      Formula renamedMatrix = substituteFormula(matrix, boundVariableTerm,
          newVarTerm, out);
      LinkedList<String> varlist = new LinkedList<String>();
      varlist.add(newVarName);
      Formula newMatrix = substituteFormula(renamedMatrix, var, term, out);
      return (Formula) out.createQuantifiedFormula(quantifier, varlist,
          newMatrix);
    case Binary:
      SimpleTptpParserOutput.BinaryConnective connective = ((Binary) formula)
          .getConnective();
      Formula lhs = ((Binary) formula).getLhs();
      Formula rhs = ((Binary) formula).getRhs();
      Formula newlhs = substituteFormula(lhs, var, term, out);
      Formula newrhs = substituteFormula(rhs, var, term, out);
      return (Formula) out.createBinaryFormula(newlhs, connective, newrhs);
    case Negation:
      Formula argument = ((Negation) formula).getArgument();
      Formula newargument = substituteFormula(argument, var, term, out);
      return (Formula) out.createNegationOf(newargument);
    default:
      throw new Error("Unexpected case: " + formula.toString());
    }
  }

  // *********************************************************************************
  // //
  // Added by Christoph Benzmueller, Sep 2012
  // *********************************************************************************
  // //
  public static void demoExample(TopLevelItem item, SimpleTptpParserOutput out) {

    switch (item.getKind()) {
    case Formula:
      Formula formula = ((AnnotatedFormula) item).getFormula();

      // ResolutionProver prover = new ResolutionProver(formula);
      // prover.prove();

      Formula equivalentFreeFormula = replaceEquivalences(formula);
      int res1 = formulaDegreeAsInFitting(equivalentFreeFormula);
      int res2 = formulaDepthAsInFitting(equivalentFreeFormula);
      int res3 = formulaRankAsInFitting(equivalentFreeFormula);
      boolean isTrue = isTrue(equivalentFreeFormula);
      boolean isFalse = isFalse(equivalentFreeFormula);
      boolean isPureAtom = isPureAtom(equivalentFreeFormula);
      boolean isNegTrue = isNegTrue(equivalentFreeFormula);
      boolean isNegFalse = isNegFalse(equivalentFreeFormula);
      boolean isNegAtom = isNegAtom(equivalentFreeFormula);
      boolean isNegNegFormula = isNegNegFormula(equivalentFreeFormula);
      boolean isAlpha = isAlphaFormula(equivalentFreeFormula);
      boolean isBeta = isBetaFormula(equivalentFreeFormula);
      boolean isGamma = isGammaFormula(equivalentFreeFormula);
      boolean isDelta = isDeltaFormula(equivalentFreeFormula);
      System.out.println(item.toString());
      System.out.println("\nOriginal formula: " + formula.toString());
      System.out.println("Equivalent free formula: "
          + equivalentFreeFormula.toString());
      System.out.println(" isTrue         : " + isTrue);
      System.out.println(" isFalse        : " + isFalse);
      System.out.println(" isPureAtom     : " + isPureAtom);
      System.out.println(" isNegTrue      : " + isNegTrue);
      System.out.println(" isNegFalse     : " + isNegFalse);
      System.out.println(" isNegAtom      : " + isNegAtom);
      System.out.println(" isNegNegForm   : " + isNegNegFormula);
      System.out.println(" isAlpha        : " + isAlpha);
      if (isAlpha) {
        Formula alpha1 = getAlpha1(equivalentFreeFormula);
        Formula alpha2 = getAlpha2(equivalentFreeFormula);
        System.out.println("  alpha1                  " + alpha1.toString());
        System.out.println("  alpha2                  " + alpha2.toString());
      }
      ;
      System.out.println(" isBeta         : " + isBeta);
      if (isBeta) {
        Formula beta1 = getBeta1(equivalentFreeFormula);
        Formula beta2 = getBeta2(equivalentFreeFormula);
        System.out.println("  beta1                   " + beta1.toString());
        System.out.println("  beta2                   " + beta2.toString());
      }
      ;
      System.out.println(" isGamma        : " + isGamma);
      if (isGamma) {
        Formula gamma = getGammaX(equivalentFreeFormula);
        System.out.println("  gamma                    " + gamma.toString());
        // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
        Term varX = (Term) out.createVariableTerm("X1");
        Formula gammaT = getGammaT(equivalentFreeFormula, varX, out);
        System.out.println("  gamma(T) for T=X1        " + gammaT.toString());
        Term term_a = (Term) out.createPlainTerm("a1", null);
        LinkedList<TptpParserOutput.Term> arguments = new LinkedList<TptpParserOutput.Term>();
        arguments.add(term_a);
        Term term_fa = (Term) out.createPlainTerm("g1", arguments);
        Formula gammaT2 = getGammaT(equivalentFreeFormula, term_fa, out);
        System.out.println("  gamma(T) for T=g1(a1)    " + gammaT2.toString());
        Term varZ = (Term) out.createVariableTerm("Z");
        arguments.add(varZ);
        Term term_faX = (Term) out.createPlainTerm("g2", arguments);
        Formula gammaT3 = getGammaT(equivalentFreeFormula, term_faX, out);
        System.out.println("  gamma(T) for T=g2(a1,Z)  " + gammaT3.toString());
      }
      ;
      System.out.println(" isDelta        : " + isDelta);
      if (isDelta) {
        Formula delta = getDeltaX(equivalentFreeFormula);
        System.out.println("  delta                    " + delta.toString());
        // SimpleTptpParserOutput out = new SimpleTptpParserOutput();
        Term varX = (Term) out.createVariableTerm("X1");
        Formula deltaT = getDeltaT(equivalentFreeFormula, varX, out);
        System.out.println("  delta(T) for T=X1        " + deltaT.toString());
        Term term_a = (Term) out.createPlainTerm("a1", null);
        LinkedList<TptpParserOutput.Term> arguments = new LinkedList<TptpParserOutput.Term>();
        arguments.add(term_a);
        Term term_fa = (Term) out.createPlainTerm("g1", arguments);
        Formula deltaT2 = getDeltaT(equivalentFreeFormula, term_fa, out);
        System.out.println("  delta(T) for T=g1(a1)    " + deltaT2.toString());
        Term varZ = (Term) out.createVariableTerm("Z");
        arguments.add(varZ);
        Term term_faX = (Term) out.createPlainTerm("g2", arguments);
        Formula deltaT3 = getDeltaT(equivalentFreeFormula, term_faX, out);
        System.out.println("  delta(T) for T=g2(a1,Z)  " + deltaT3.toString());
      }
      ;
      System.out.println(" Formula degree (Fitting) : " + res1);
      System.out.println(" Formula depth (Fitting)  : " + res2);
      System.out.println(" Formula rank  (Fitting)  : " + res3 + "\n");
      break;
    case Clause:
      int res4 = clauseLength(((AnnotatedClause) item).getClause());
      System.out.println(item.toString());
      System.out.println("Clause length: " + res4 + "\n");
      break;
    case Include:
      System.out.println("Include");
      break; // nothing to check
    }
  }

  /**
   * Replaces all equivalences with implications ((x <=> y) ~> (x => y) ^ (y =>
   * x)). The passed formula will not be altered.
   * 
   * @param inFormula
   * @return new formula that does not contain any equivalences
   */
  public static Formula replaceEquivalences(Formula inFormula) {
    switch (inFormula.getKind()) {
    case Binary:
      Binary binary = (Binary) inFormula;
      Formula lhs = binary.getLhs();
      Formula rhs = binary.getRhs();

      // recursion of left and right hand side
      Formula leftResult = replaceEquivalences(lhs);
      Formula rightResult = replaceEquivalences(rhs);

      // check if binary connective is equivalence
      if (binary.getConnective() == TptpParserOutput.BinaryConnective.Equivalence) {

        // actual replacement
        Binary leftImplication = new Binary(leftResult,
            TptpParserOutput.BinaryConnective.Implication, rightResult);
        Binary rightImplication = new Binary(rightResult,
            TptpParserOutput.BinaryConnective.Implication, leftResult);
        Binary conjunciton = new Binary(leftImplication,
            TptpParserOutput.BinaryConnective.And, rightImplication);

        return conjunciton;
      }

      return new Binary(leftResult, binary.getConnective(), rightResult);
    case Atomic:
      return inFormula; // TODO return a copy
    case Negation:
      // recursively check the argument, negate the result
      return new Negation(replaceEquivalences(((Negation) inFormula)
          .getArgument()));
    case Quantified:

      break;

    default:
      break;
    }
    return null;
  }

  /**
   * Keeps information about the usage of a symbol: the category (predicate or
   * function) and the arity.
   */
  public static class SymbolDescriptor {

    public SymbolDescriptor(boolean isPred, int ar) {
      isPredicate = isPred;
      arity = ar;
    }

    public boolean isPredicate;

    public int arity;

  } // class SymbolDescriptor

  /** Keeps descriptors of all symbols registered so far. */
  public static Hashtable<String, SymbolDescriptor> _signature = new Hashtable<String, SymbolDescriptor>();

  /**
   * This is here to avoid threading this information through different variants
   * of checkSemantically(_).
   */
  public static String _semanticCheckFileName;

  /**
   * This is here to avoid threading this information through different variants
   * of checkSemantically(_).
   */
  public static int _semanticCheckLineNumber;

} /* end of the class */
