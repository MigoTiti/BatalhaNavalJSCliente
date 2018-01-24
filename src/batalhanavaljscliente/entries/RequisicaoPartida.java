package batalhanavaljscliente.entries;

import net.jini.core.entry.Entry;

public class RequisicaoPartida implements Entry {   

    public String nome;
    public String jogador;

    public RequisicaoPartida() {
    }
    
    public RequisicaoPartida(String host, String jogador) {
        this.nome = host;
        this.jogador = jogador;
    }
}
