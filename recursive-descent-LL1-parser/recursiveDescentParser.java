import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 
 * 
 * Java program to implement a Recursive Descent Parser on a given LL(1) Grammar as follows
 * 
 * <program>          ::=={<statement_list>}
 * <statement-list>   ::== <statement>;<statement-list’>
 * <statement-list’>  ::== <statement-list>
 *                     |   ε
 * <statement>	      ::== call: <procedure_call>
 *  	               |   compute: <expression>
 * <procedure_call>   ::== id(<parameters>)
 * <parameters>	      ::== <factor><parameters’>
 * <parameters’>	  ::== ,<parameters>
 *                     |   ε
 * <expression>       ::== id=<factor><expression’>
 * <expression’>      ::== +<factor>
 *                     |   -<factor> 
 *                     |   ε
 * <factor>	          ::== id
 * 		               |   num
 * 
 * The grammar was modified from an initial non-LL(1) grammar that lacked left-factoring, provided as part of an 
 * assignment exercise at a software construction course, for a topic that focuses on syntax analysis. 
 * 
 * An algorithm to implement an LL(1) grammar was also provided as part of this course, which was used on the 
 * design methodology for the parsing methods of this class. 
 * 
 * 
 * @author Kemal Kilic
 * 
 */
public class recursiveDescentParser {

    /**
     * Enum class for more compact result representation
     * 
     */
    public enum Result {
        SUCCESS("SUCCESS"),
        ERROR("ERROR");

        private final String result;

        Result(final String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return result;
        }
    }


    //instance variables
    private String token;
    private String path;
    private BufferedReader reader;
    //private FileInputStream file;


    /*
     * Constructor 
     * 
     */
    public recursiveDescentParser(String path) {

        try {

            this.path = path;

            BufferedInputStream bufStr = new BufferedInputStream(
            new FileInputStream(this.path)
            );

            
            this.reader = new BufferedReader(
                new InputStreamReader(bufStr, StandardCharsets.UTF_8)
            );

        } catch ( IOException ioExp) {
            System.out.println(ioExp.getMessage()); 
            System.out.print("Invalid filepath or BufferedReader error!");
        }
        
    }

    
    /** 
     * @return String
     */
    private String getNextToken() {
        String tempNextToken = "";
        try {
            tempNextToken = reader.readLine();
            //System.out.println(tempNextToken); // uncomment to get input stream list output
        } catch (IOException ioExp) {
            System.out.println(ioExp.getMessage());
        } 

        if (tempNextToken == null) {
            try {
                System.out.println("End of file reached!");
                reader.close();
            } catch (IOException ioExp) {
                System.out.println(ioExp.getMessage());
            } 
            return "";
        } 

        return tempNextToken;
    }

    /*
     * Method for commencing the parsing from the initial nonterminal <program>
     */
    private Result beginParsing() {
        //debug code:
        // System.out.println("beginParsing is called");

        this.token = getNextToken();

        if(program() == Result.ERROR || ! token.equals("$")) {
            return Result.ERROR;
        } else {
            return Result.SUCCESS;
        }
    }

    /*
     * Method for production 1 : <program>           ::=={<statement_list>}
     */
    private Result program() {
        //debug code:
        // System.out.println("program is called");

        //get the initial { token
        if (token.equals("{")) {
            token = getNextToken();

            if (statementList() == Result.ERROR) {
                // syntax error in <statement_list> nonterminal
                return Result.ERROR;
            } else {
                // check if next token is }
                if (token.equals("}")) {
                    token = getNextToken();
                    return Result.SUCCESS;
                } else {
                    // } token missing
                    return Result.ERROR;
                }
            }
        } else {
            // { token is missing
            return Result.ERROR;
        }
    }

    /*
     * Method for production 2 : <statement-list>   ::== <statement>;<statement-list’>
     */
    private Result statementList() {
        //debug code:
        // System.out.println("statementList is called");


        if (statement() == Result.ERROR) {
            // case of <statement> terminal failure
            return Result.ERROR;
        } else if(token.equals(";")) {
            // check if the next token is ;
            token = getNextToken();
            return statementListPrime();
        } else {
            // case of missing ; after successful <statement> token
            return Result.ERROR;
        }
    }

    /*
     * Method for production 3 and 4 : <statement-list’>  ::== <statement-list>
     *                                                      |  epsilon 
     */
    private Result statementListPrime() {

        //debug code:
        // System.out.println("statementListPrime is called");


        // since there is epsilon in the production, we should look at the FOLLOW(<statement-list'>) set
        // FOLLOW(<statement-list'>) = {"}"}

        // for production-3:
        if (! token.equals("}")) {
            return statementList();
        } else {
            // for production-4 (with epsilon)
            return Result.SUCCESS;
        }
    }

    /*
     * Method for production 5, 6:
     *       <statement>	     ::== call: <procedure_call>
     *       		                | compute: <expression>
     */
    private Result statement() {

        //debug code:
        // System.out.println("statement is called");

        // get the "call" token
        if (token.equals("call")) {
            token = getNextToken();

            //get the ":" token
            if (token.equals(":")) {
                token = getNextToken();
                return procedureCall();
            }
        } 
        // get the "compute" token for production 6
        else if (token.equals("compute")) {
            token = getNextToken();

            //get the ":" token
            if (token.equals(":")) {
                token = getNextToken();
                return expression();
            }
        }

        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned
        return Result.ERROR;
        
    }


    /*
     * Method for production 7:
     *          <procedure_call> ::== id(<parameters>)
     */
    private Result procedureCall() {
        
        //debug code:
        // System.out.println("procedureCall is called");


        // token was already moved from previous method
        // check for "id"
        if (token.equals("id")) {
            token = getNextToken();

            // check for "(" 
            if (token.equals("(")) {
                token = getNextToken();

                // go to <parameters> nonterminal's parsing
                if (parameters() == Result.SUCCESS) {

                    //check for ")" 
                    if (token.equals(")")) {
                        token = getNextToken();
                        return Result.SUCCESS;
                    }
                }

            }
        }

        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned

        return Result.ERROR;
    }

    /*
     * method for production 8:
     *          <parameters>	      ::== <factor><parameters’>
     */
    private Result parameters() {

        //debug code:
        // System.out.println("parameters is called");

        // if <factor> returns SUCCESS, go to <parameters'>
        if (factor() == Result.SUCCESS) {
            return parametersPrime();
        } else {
            return Result.ERROR;
        }
    }

    /*
     * Method for productions 9,10:
     *          <parameters’>	      ::== ,<parameters>
     *                                  |  epsilon
     */
    private Result parametersPrime() {

        //debug code:
        // System.out.println("parametersPrime is called");

        // handling the non-epsilon production 9

        // check for ","
        if (token.equals(",")) {
            token = getNextToken();

            // move on to the next nonterminal method for <parameters>
            return parameters();
        } 

        // FOLLOW(<parameters') = {")"}
        // checking if the required production is the epsilon including prod 10
        if (token.equals(")")) {
            // pop the non-terminal <parameters'> from the stack. 
            return Result.SUCCESS;
        }

        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned
        return Result.ERROR;

    }

    /*
     * method for production 11:
     *          <expression>  	      ::== id=<factor><expression’>
     */
    private Result expression() {

        //debug code:
        // System.out.println("expression is called");

        // check token "id"
        if (token.equals("id")) {
            token = getNextToken();

            //check token "="
            if (token.equals("=")) {
                token = getNextToken();

                // go to the method that checks the next token as a <factor> nonterminal
                if (factor() == Result.ERROR) {
                    // return ERROR if <factor> evaluates a syntax error
                    return Result.ERROR;
                } else {
                    // proceed to check the token for <expression'>
                    return expressionPrime();
                }
            }
        }

        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned
        return Result.ERROR;

    } 

    /*
     * Method for productions 12,13,14:
     *          <expression’> 	      ::== +<factor>
     *                                |    -<factor> 
     *                                |    ε
     */
    private Result expressionPrime() {

        //debug code:
        // System.out.println("expressionPrime is called");

        // check for token "+"
        if (token.equals("+")) {
            token = getNextToken();

            // go to the method that checks the next token as a <factor> nonterminal
            return factor();
        }

        //check for token "-"
        if (token.equals("-")) {
            token = getNextToken();

            // go to the method that checks the next token as a <factor> nonterminal
            return factor();
        }

        // FOLLOW(<expression'>) = {";"}
        // checking if the required production is the epsilon including prod 14
        if (token.equals(";")) {
            return Result.SUCCESS; // pop <expression'> from stack successfully
        }



        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned
        return Result.ERROR;
    }

    /*
     * Method for productions 15,16:
     *          <factor>	      ::== id
	 *	                          |    num
     */
    private Result factor() {

        //debug code:
        // System.out.println("factor is called");

        // check if token is "id"
        if (token.equals("id")) {
            token = getNextToken();
            return Result.SUCCESS; // pop <factor> from stack successfully
        }

        // check if token is "num"
        if (token.equals("num")) {
            token = getNextToken();
            return Result.SUCCESS; // pop <factor> from stack successfully
        }

        // if any of the previous if statements haven't given a syntax error
        // they should return the corresponding methods' results
        // so if the code reaches here, an error must be returned
        return Result.ERROR;

    }


    /*
     * Main method that gets the filename as a path string argument 
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            // get the first argument as path
            String thisPath = args[0];

            recursiveDescentParser thisParser = new recursiveDescentParser(thisPath);

            Result thisResult = thisParser.beginParsing();

            if (thisResult == Result.SUCCESS) {
                System.out.println(thisResult + ": the code has been successfully parsed!");
            } else {
                System.out.println(thisResult + ":  the code contains a syntax mistake!");
            }
            

        } else {
            System.out.println("Filename should be provided as an argument");
        }

        

    }





}