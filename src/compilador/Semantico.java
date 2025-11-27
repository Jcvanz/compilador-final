package compilador;
public class Semantico implements Constants
{
    String codigo;
    Pilha pilha_tipos = new Pilha(200);

    public void executeAction(int action, Token token)	throws SemanticError
    {
        
        if(action == 100) acao100();
        else if(action == 101) acao101();
        else if(action == 102) acao102();
        else if(action == 103) acao103(token);
        else if(action == 104) acao104(token);
        else if(action == 105) acao105(token);
        
        //System.out.println("Ação #"+action+", Token: "+token);
    }

    public void acao100 () { 
        codigo += (".assembly extern mscorlib {}\n" +
                       ".assembly _programa{}\n" +
                       ".module _programa.exe\n" +
                       "" +
                       ".class public _unica{\n" +
                       ".method static public void _principal(){\n" +
                       ".entrypoint\n");
    }

    public void acao101 () {
        codigo += ("ret\n"+
                       "}\n" +
                       "}");
    }

    public void acao102 () {
        String temp = pilha_tipos.pop();
        if(temp == "int64") {
            codigo += ("conv.i8\n" + "call void [mscorlib]System.Console::Write(int64)\n");
        } else {
            codigo += (String.format("call void [mscorlib]System.Console::WriteLine(%s)\n", temp));
        }
    }
    
    public void acao103 (Token token) {
        pilha_tipos.push("int64");
        codigo += ("ldc.i8 " + token.getLexeme());
        codigo += ("conv.r8\n");
    }
    
    public void acao104 (Token token) {
        pilha_tipos.push("float64");
        codigo += ("ldc.i8 " + token.getLexeme());
    }

    public void acao105 (Token token) {
        pilha_tipos.push("string");
        codigo += ("ldstr " + token.getLexeme());
    }

}
