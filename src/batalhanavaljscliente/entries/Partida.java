package batalhanavaljscliente.entries;

import net.jini.core.entry.Entry;

public class Partida implements Entry{
    
    public String host;

    public Partida() {
    }

    public Partida(String host) {
        this.host = host;
    }
}
