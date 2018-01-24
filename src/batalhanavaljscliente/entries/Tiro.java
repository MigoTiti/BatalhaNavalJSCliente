package batalhanavaljscliente.entries;

import net.jini.core.entry.Entry;

public class Tiro implements Entry {
    
    public Integer x;
    public Integer y;
    public String nickname;

    public Tiro() {
    }

    public Tiro(Integer x, Integer y, String nickname) {
        this.x = x;
        this.y = y;
        this.nickname = nickname;
    }
}
