package batalhanavaljscliente.entries;

import net.jini.core.entry.Entry;

public class RespostaTiro implements Entry {
    
    public String nickname;
    public Boolean acertou;
    public Integer x;
    public Integer y;

    public RespostaTiro() {
    }

    public RespostaTiro(String nickname, Boolean acertou) {
        this.nickname = nickname;
        this.acertou = acertou;
    }

    public RespostaTiro(String nickname, Boolean acertou, Integer x, Integer y) {
        this.nickname = nickname;
        this.acertou = acertou;
        this.x = x;
        this.y = y;
    }
}
