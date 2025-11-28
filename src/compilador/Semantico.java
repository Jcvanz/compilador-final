package compilador;
public class Semantico implements Constants
{
    String codigo;
    Pilha pilha_tipos = new Pilha(200);
    String operador_relacional; // { changed code }

    public void executeAction(int action, Token token)	throws SemanticError
    {
        switch (action) {
            case 100: 
                acao100(); 
                break;
            case 101: 
                acao101(); 
                break;
            case 102: 
                acao102(); 
                break;
            case 103: 
                acao103(token); 
                break;
            case 104:   
                acao104(token); 
                break;
            case 105: 
                acao105(token); 
                break;
            case 106: 
                acao106(); 
                break;
            case 107: 
                acao107(); 
                break;
            case 108: 
                acao108(); 
                break;
            case 109: 
                acao109(); 
                break;
            case 110: 
                acao110(); 
                break;
            case 111:
                acao111(token);
                break;
            case 112:
                acao112(token);
                break;
            case 113:
                acao113();
                break;
            case 114:
                acao114();
                break;
            case 117:
                acao117();
                break;
            default: throw new SemanticError("Ação semântica não implementada: " + action);
        }
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

    public void acao106 () {
        String primeiroTipo = pilha_tipos.pop();
        String segundoTipo = pilha_tipos.pop();
        String tipoResultado;

        if ("float64".equals(primeiroTipo) || "float64".equals(segundoTipo)) {
            tipoResultado = "float64";
        } else {
            tipoResultado = "int64";
        }

        pilha_tipos.push(tipoResultado);
        codigo += ("add\n");
    }

    public void acao107 () {
        String primeiroTipo = pilha_tipos.pop();
        String segundoTipo = pilha_tipos.pop();
        String tipoResultado;

        if ("float64".equals(primeiroTipo) || "float64".equals(segundoTipo)) {
            tipoResultado = "float64";
        } else {
            tipoResultado = "int64";
        }

        pilha_tipos.push(tipoResultado);
        codigo += ("sub\n");
    }

    public void acao108 () {
        String primeiroTipo = pilha_tipos.pop();
        String segundoTipo = pilha_tipos.pop();
        String tipoResultado;

        if ("float64".equals(primeiroTipo) || "float64".equals(segundoTipo)) {
            tipoResultado = "float64";
        } else {
            tipoResultado = "int64";
        }

        pilha_tipos.push(tipoResultado);
        codigo += ("mul\n");
    }

    public void acao109 () {
        String primeiroTipo = pilha_tipos.pop();
        String segundoTipo = pilha_tipos.pop();
        String tipoResultado;
        
        if ("float64".equals(primeiroTipo) || "float64".equals(segundoTipo)) {
            tipoResultado = "float64";
        } else {
            tipoResultado = "int64";
        }

        pilha_tipos.push(tipoResultado);
        codigo += ("div\n");
    }

    public void acao110 () { 
        String tipoOperando = pilha_tipos.pop();

        String tipoResultado = tipoOperando;
        pilha_tipos.push(tipoResultado);

        codigo += ("neg\n");
    }

    public void acao111 (Token token) {
        operador_relacional = token.getLexeme();
    }

    public void acao112 (Token token) throws SemanticError {
        String tipo1 = pilha_tipos.pop();
        String tipo2 = pilha_tipos.pop();

        if ((tipo1.equals("int64") && tipo2.equals("int64")) 
                || (tipo1.equals("float64") && tipo2.equals("float64"))
                || (tipo1.equals("string") && tipo2.equals("string"))
                || (tipo1.equals("int64") && tipo2.equals("float64"))
                || (tipo1.equals("float64") && tipo2.equals("int64"))) {
            pilha_tipos.push("bool");
        } else {
            throw new SemanticError("tipos incompatíveis em expressão relacional");
        }

        if (">".equals(operador_relacional)) {
            codigo += ("cgt\n");
        } else if ("<".equals(operador_relacional)) {
            codigo += ("clt\n");
        } else if ("==".equals(operador_relacional)) {
            codigo += ("ceq\n");
        } else if ("!=".equals(operador_relacional)) {
            codigo += ("ceq\n");
        } else if (">=".equals(operador_relacional)) {
            codigo += ("clt\n");
        } else if ("<=".equals(operador_relacional)) {
            codigo += ("cgt\n");
        }
    }

    public void acao113 () throws SemanticError {
        String tipo1 = pilha_tipos.pop();
        String tipo2 = pilha_tipos.pop();

        if ("bool".equals(tipo1) && "bool".equals(tipo2)) {
            pilha_tipos.push("bool");
        } else {
            throw new SemanticError("tipos incompatíveis em operador lógico AND");
        }

        codigo += ("and\n");
    }

    public void acao114 () throws SemanticError {
        String tipo1 = pilha_tipos.pop();
        String tipo2 = pilha_tipos.pop();

        if ("bool".equals(tipo1) && "bool".equals(tipo2)) {
            pilha_tipos.push("bool");
        } else {
            throw new SemanticError("tipos incompatíveis em operador lógico OR");
        }

        codigo += ("or\n");
    }

   public void acao117 () throws SemanticError {
        String tipoOperando = pilha_tipos.pop();
        
        if ("bool".equals(tipoOperando)) {
            pilha_tipos.push("bool");
        } else {
            throw new SemanticError("tipo incompatível em operador lógico NOT");
        }

        codigo += ("ldc.i4.0\n");
        codigo += ("ceq\n");
   }

}
