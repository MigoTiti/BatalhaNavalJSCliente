package batalhanavaljscliente.entries;

import net.jini.core.entry.Entry;

public class TrocaDeEstado implements Entry {
    
    public String nickname;
    public Integer estado;

    public TrocaDeEstado() {
    }
    
    public TrocaDeEstado(String nickname, Integer estado) {
        this.nickname = nickname;
        this.estado = estado;
    }

    public TrocaDeEstado(String nickname) {
        this.nickname = nickname;
    }
}
