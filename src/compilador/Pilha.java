package compilador;

public class Pilha {
    
    private String[] pilha;
    private int posicao;

    public Pilha(int tamanho) {
        this.pilha = new String[tamanho];
        posicao = 0;
    }

    public boolean pilhaVazia() {
        if (this.posicao == 0) {return true;}
        else {return false;}
    }

    public void push(String value) {
        this.pilha[posicao] = value;
        posicao++;
    }

    public String pop() {
        String temp = this.pilha[posicao-1] ;
        posicao--;
        return temp;
    }

}

