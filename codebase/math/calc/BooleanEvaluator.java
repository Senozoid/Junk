package codebase.math.calc;

import java.util.ArrayList;
import java.util.Scanner;

/*
TODO NOTES:
    1. Avoid the NOT operator (!):
        It is preferable to completely avoid it, because there are several bugs related to it that have not been fixed.
        It may not work as expected if used at the beginning of the expression or if it is the first thing inside a bracket.
        In those cases, it will invert the value of the entire subexpression instead of the immediately next term.
        If used immediately before a bracket, it may mess up everything... etc.
*/

public class BooleanEvaluator{
    private static final int operatorLength=2;//ol=1 for numbers, ol=2 for booleans
    private static final String[] validOperators={"==","<=","<<",">>",">=","!="};//if you change this, change the switch-case as well

    public static void main(String[] args){
        boolean status;
        Scanner test=new Scanner(System.in);

        System.out.print("Enter a boolean expression: ");
        try{
            status= evaluate(test.nextLine());
        }catch(IllegalBracketException ibe) {
            System.out.println("The expression you entered has a bracket problem.");
            ibe.printStackTrace();
            return;
        }catch(UnresolvedSymbolException use) {
            System.out.println("There is something wrong with the expression you entered.");
            use.printStackTrace();
            return;
        }
        System.out.println("The expression you entered evaluates to "+status+".");
    }

    public static boolean evaluate(String expression) throws IllegalBracketException, UnresolvedSymbolException{
        boolean status;

        expression=expression.strip();
        if(expression.length()<4) throw new UnresolvedSymbolException();
        if(expression.charAt(0)=='!') return !evaluate(expression.substring(1));

        if(expression.contains("(")||expression.contains(")")) status=evalBrackets(expression);
        else status=evalNoBrackets(expression);

        return status;
    }

    private static boolean evalBrackets(String expression) throws IllegalBracketException, UnresolvedSymbolException {
        StringBuilder bracketless;
        ArrayList<Boolean> subexEvals;
        ArrayList<String> subexes = new ArrayList<>();
        ArrayList<String> linkers = new ArrayList<>();
        int b=0,xl,terms;
        char c;
        StringBuilder exbuilder=null;
        StringBuilder opbuilder;
        int length=expression.length();

        for(int i = 0; i<length; i++){
            c=expression.charAt(i);
            if(c=='('){
                b++;

                if(exbuilder!=null&&!exbuilder.isEmpty()){
                    xl=exbuilder.length();
                    if(xl<=operatorLength) throw new UnresolvedSymbolException();
                    opbuilder=new StringBuilder();
                    for(int j=operatorLength; j>0; j--){
                        opbuilder.append(exbuilder.charAt(xl-j));
                    }
                    linkers.add(opbuilder.toString());
                    exbuilder.delete(xl-operatorLength,xl);
                    subexes.add(exbuilder.toString());
                }

                exbuilder=new StringBuilder();

                while(true){
                    if(++i>=length) throw new IllegalBracketException();
                    c=expression.charAt(i);

                    if(c=='(') b++;
                    else if(c==')'){
                        if(--b==0) break;
                    }

                    exbuilder.append(c);
                }

                subexes.add(exbuilder.toString());
                exbuilder=new StringBuilder();

                /*
                "subexp)||subexp"
                       ^here
                */
                i++;
                /*
                "subexp)||subexp"
                        ^here
                */
                if(i<length){
                    if(i<(length-operatorLength)){
                        opbuilder=new StringBuilder();
                        for(int j=i; i<(j+operatorLength); i++){
                            opbuilder.append(expression.charAt(i));
                        }
                        /*
                        "subexp)||subexp"
                                  ^here
                        */
                        linkers.add(opbuilder.toString());
                    }
                    else if (expression.charAt(i)=='('||expression.charAt(i)==')') throw new IllegalBracketException();
                    else throw new UnresolvedSymbolException();
                }
                i--;
                /*
                "subexp)||subexp"
                         ^here now
                "subexp)||subexp"
                          ^here when the next iteration begins
                */
            }
            else if(c==')') throw new IllegalBracketException();
            else{
                if(exbuilder==null) exbuilder=new StringBuilder();
                exbuilder.append(c);
            }
        }
        if(exbuilder!=null&&!exbuilder.isEmpty()) subexes.add(exbuilder.toString());

        subexEvals = new ArrayList<>();
        for(String sub : subexes) subexEvals.add(evaluate(sub));

        bracketless=new StringBuilder();

        terms=subexEvals.size();
        if(linkers.size()>terms) throw new UnresolvedSymbolException();
        else if(linkers.size()==terms){
            bracketless.append(linkers.get(0));
            linkers.remove(0);
        }

        for (int i = 0; i < terms; i++){
            bracketless.append(subexEvals.get(i));
            if(i<linkers.size()) bracketless.append(linkers.get(i));
        }

        return evaluate(bracketless.toString());
    }

    private static boolean evalNoBrackets(String expression) throws UnresolvedSymbolException {
        return evalOR(expression);
    }

    private static boolean evalOR(String expression) throws UnresolvedSymbolException {
        if(!expression.contains("||")) return evalAND(expression);
        boolean status=false;
        String[] subexes=expression.split("\\|\\|");
        for (String x:subexes) {
            if(evalAND(x)){
                status=true;
                break;
            }
        }
        return status;
    }

    private static boolean evalAND(String expression) throws UnresolvedSymbolException {
        if(!expression.contains("&&")) return evalTerm(expression);
        boolean status=true;
        String[] subexes=expression.split("&&");
        for (String x:subexes) {
            if(!evalTerm(x)) {
                status=false;
                break;
            }
        }
        return status;
    }

    private static boolean evalTerm(String term) throws UnresolvedSymbolException {
        boolean operation=false;
        boolean status=false;
        float[] operands=new float[2];
        String[] operandStrings;

        term=term.strip();
        if(term.length()<4) throw new UnresolvedSymbolException();
        if(term.charAt(0)=='!') return !evalTerm(term.substring(1));

        for (String operator : validOperators) {
            if (term.contains(operator)) {
                operandStrings=term.split(operator);
                if(operandStrings.length!=2) break;

                try{
                    for(int i = 0; i < 2; i++){
                        operands[i]=Float.parseFloat(operandStrings[i]);
                    }
                }catch(NumberFormatException nfe){
                    break;
                }

                status=switch(operator){//if you change this, change the static final array as well
                    case "==" -> operands[0]==operands[1];
                    case "<=" -> operands[0]<=operands[1];
                    case "<<" -> operands[0]<operands[1];
                    case ">>" -> operands[0]>operands[1];
                    case ">=" -> operands[0]>=operands[1];
                    case "!=" -> operands[0]!=operands[1];
                    default -> throw new UnresolvedSymbolException();
                };

                operation=true;
                break;
            }
        }
        if(!operation){
            if("true".equalsIgnoreCase(term)) status=true;
            else if(!"false".equalsIgnoreCase(term)) throw new UnresolvedSymbolException();
        }
        return status;
    }

}
