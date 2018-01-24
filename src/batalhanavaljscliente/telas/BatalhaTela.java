package batalhanavaljscliente.telas;

import batalhanavaljscliente.BatalhaNavalJSCliente;
import batalhanavaljscliente.entries.RespostaTiro;
import batalhanavaljscliente.entries.Tiro;
import batalhanavaljscliente.tabuleiros.TabuleiroPronto;
import batalhanavaljscliente.util.RectangleCoordenado;
import batalhanavaljscliente.util.RectangleNavio;
import java.rmi.RemoteException;
import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;

public class BatalhaTela extends TabuleiroPronto {

    private int contagemUsuario;
    private int contagemAdversario;

    Text helpText;
    Text helpText2;

    public static final int ESTADO_PREPARANDO = 1;
    public static final int ESTADO_PRONTO = 2;
    public static final int ESTADO_VEZ = 3;

    public static final Color COR_ACERTO = Color.RED;
    public static final Color COR_ERRO = Color.BLUE;
    public static final Color COR_BACKGROUND = Color.GREY;

    private int estado;

    private final MediaPlayer acertoMusica = new MediaPlayer(new Media(getClass().getResource("recursos/acerto.mp3").toString()));
    private final MediaPlayer erroMusica = new MediaPlayer(new Media(getClass().getResource("recursos/erro.mp3").toString()));

    public void iniciarTela(Set<RectangleNavio> naviosUsuario, int contagem, boolean host) {
        if (host) {
            estado = ESTADO_VEZ;
        } else {
            estado = ESTADO_PRONTO;
        }

        contagemUsuario = contagem;
        contagemAdversario = contagem;

        acertoMusica.setOnEndOfMedia(() -> {
            acertoMusica.seek(Duration.ZERO);
            acertoMusica.stop();
        });

        erroMusica.setOnEndOfMedia(() -> {
            erroMusica.seek(Duration.ZERO);
            erroMusica.stop();
        });

        BorderPane root = new BorderPane();

        VBox vboxUsuario = new VBox();
        VBox vboxAdversario = new VBox();

        vboxUsuario.setSpacing(20);
        vboxAdversario.setSpacing(20);

        vboxUsuario.setAlignment(Pos.CENTER);
        vboxAdversario.setAlignment(Pos.CENTER);

        helpText = new Text(BatalhaNavalJSCliente.nickname);
        helpText.setFill(Color.BLACK);
        helpText.setFont(Font.font("Arial", FontWeight.NORMAL, 20));

        helpText2 = new Text(BatalhaNavalJSCliente.nomeOponente);
        helpText2.setFill(Color.BLACK);
        helpText2.setFont(Font.font("Arial", FontWeight.NORMAL, 20));

        campoAdversario = new GridPane();
        campoUsuario = new GridPane();
        campoAdversario.setGridLinesVisible(true);
        campoUsuario.setGridLinesVisible(true);
        campoAdversario.setAlignment(Pos.CENTER);
        campoUsuario.setAlignment(Pos.CENTER);

        for (int i = 0; i < TAMANHO; i++) {
            campoAdversario.getColumnConstraints().add(new ColumnConstraints(TAMANHO_CELULA));
            campoUsuario.getColumnConstraints().add(new ColumnConstraints(TAMANHO_CELULA));

            campoAdversario.getRowConstraints().add(new RowConstraints(TAMANHO_CELULA));
            campoUsuario.getRowConstraints().add(new RowConstraints(TAMANHO_CELULA));
        }

        campoUsuarioMatriz = new RectangleCoordenado[TAMANHO][TAMANHO];
        campoAdversarioMatriz = new RectangleCoordenado[TAMANHO][TAMANHO];

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                RectangleCoordenado rectUsuario = gerarRect(i, j, true);
                campoUsuario.add(rectUsuario, i, j);
                campoUsuarioMatriz[i][j] = rectUsuario;

                RectangleCoordenado rectAdversario = gerarRect(i, j, false);
                campoAdversario.add(rectAdversario, i, j);
                campoAdversarioMatriz[i][j] = rectAdversario;
            }
        }

        StackPane campoUsuarioPronto = new StackPane();
        StackPane campoAdversarioPronto = new StackPane();

        VBox vBoxCampoUsuario = new VBox();
        vBoxCampoUsuario.setAlignment(Pos.CENTER);
        vBoxCampoUsuario.getChildren().addAll(campoUsuario);

        VBox vBoxCampoAdversario = new VBox();
        vBoxCampoAdversario.setAlignment(Pos.CENTER);
        vBoxCampoAdversario.getChildren().addAll(campoAdversario);

        HBox hBoxCampoUsuario = new HBox(vBoxCampoUsuario);
        hBoxCampoUsuario.setAlignment(Pos.CENTER);

        HBox hBoxCampoAdversario = new HBox(vBoxCampoAdversario);
        hBoxCampoAdversario.setAlignment(Pos.CENTER);

        campoUsuarioPronto.getChildren().addAll(hBoxCampoUsuario);

        Platform.runLater(() -> {
            naviosUsuario.stream().forEach((rectangleNavio) -> {
                rectangleNavio.setTranslateX(rectangleNavio.getTranslateX() - ((TAMANHO_CELULA * 6) - 7));
                rectangleNavio.setTranslateY(rectangleNavio.getTranslateY() - (TAMANHO_CELULA * 3));
                campoUsuarioPronto.getChildren().add(rectangleNavio);

                int x = rectangleNavio.getxCoordenada();
                int y = rectangleNavio.getyCoordenada();

                campoUsuarioMatriz[x][y].setOcupado(true);

                Color corAPreencher = COR_BACKGROUND;

                campoUsuarioMatriz[x][y].setFill(corAPreencher);

                switch (rectangleNavio.getTamanho()) {
                    case 5:
                        switch (rectangleNavio.getRotacao()) {
                            case 1:
                                campoUsuarioMatriz[x + 1][y].setOcupado(true);
                                campoUsuarioMatriz[x + 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 2][y].setOcupado(true);
                                campoUsuarioMatriz[x + 2][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 3][y].setOcupado(true);
                                campoUsuarioMatriz[x + 3][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 4][y].setOcupado(true);
                                campoUsuarioMatriz[x + 4][y].setFill(corAPreencher);
                                break;
                            case 2:
                                campoUsuarioMatriz[x][y + 1].setOcupado(true);
                                campoUsuarioMatriz[x][y + 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 2].setOcupado(true);
                                campoUsuarioMatriz[x][y + 2].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 3].setOcupado(true);
                                campoUsuarioMatriz[x][y + 3].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 4].setOcupado(true);
                                campoUsuarioMatriz[x][y + 4].setFill(corAPreencher);
                                break;
                            case 3:
                                campoUsuarioMatriz[x - 1][y].setOcupado(false);
                                campoUsuarioMatriz[x - 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 2][y].setOcupado(false);
                                campoUsuarioMatriz[x - 2][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 3][y].setOcupado(false);
                                campoUsuarioMatriz[x - 3][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 4][y].setOcupado(false);
                                campoUsuarioMatriz[x - 4][y].setFill(corAPreencher);
                                break;
                            case 4:
                                campoUsuarioMatriz[x][y - 1].setOcupado(true);
                                campoUsuarioMatriz[x][y - 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 2].setOcupado(true);
                                campoUsuarioMatriz[x][y - 2].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 3].setOcupado(true);
                                campoUsuarioMatriz[x][y - 3].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 4].setOcupado(true);
                                campoUsuarioMatriz[x][y - 4].setFill(corAPreencher);
                                break;
                        }
                        break;
                    case 4:
                        switch (rectangleNavio.getRotacao()) {
                            case 1:
                                campoUsuarioMatriz[x + 1][y].setOcupado(true);
                                campoUsuarioMatriz[x + 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 2][y].setOcupado(true);
                                campoUsuarioMatriz[x + 2][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 3][y].setOcupado(true);
                                campoUsuarioMatriz[x + 3][y].setFill(corAPreencher);
                                break;
                            case 2:
                                campoUsuarioMatriz[x][y + 1].setOcupado(true);
                                campoUsuarioMatriz[x][y + 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 2].setOcupado(true);
                                campoUsuarioMatriz[x][y + 2].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 3].setOcupado(true);
                                campoUsuarioMatriz[x][y + 3].setFill(corAPreencher);
                                break;
                            case 3:
                                campoUsuarioMatriz[x - 1][y].setOcupado(false);
                                campoUsuarioMatriz[x - 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 2][y].setOcupado(false);
                                campoUsuarioMatriz[x - 2][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 3][y].setOcupado(false);
                                campoUsuarioMatriz[x - 3][y].setFill(corAPreencher);
                                break;
                            case 4:
                                campoUsuarioMatriz[x][y - 1].setOcupado(true);
                                campoUsuarioMatriz[x][y - 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 2].setOcupado(true);
                                campoUsuarioMatriz[x][y - 2].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 3].setOcupado(true);
                                campoUsuarioMatriz[x][y - 3].setFill(corAPreencher);
                                break;
                        }
                        break;
                    case 3:
                        switch (rectangleNavio.getRotacao()) {
                            case 1:
                                campoUsuarioMatriz[x + 1][y].setOcupado(true);
                                campoUsuarioMatriz[x + 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x + 2][y].setOcupado(true);
                                campoUsuarioMatriz[x + 2][y].setFill(corAPreencher);
                                break;
                            case 2:
                                campoUsuarioMatriz[x][y + 1].setOcupado(true);
                                campoUsuarioMatriz[x][y + 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y + 2].setOcupado(true);
                                campoUsuarioMatriz[x][y + 2].setFill(corAPreencher);
                                break;
                            case 3:
                                campoUsuarioMatriz[x - 1][y].setOcupado(false);
                                campoUsuarioMatriz[x - 1][y].setFill(corAPreencher);
                                campoUsuarioMatriz[x - 2][y].setOcupado(false);
                                campoUsuarioMatriz[x - 2][y].setFill(corAPreencher);
                                break;
                            case 4:
                                campoUsuarioMatriz[x][y - 1].setOcupado(true);
                                campoUsuarioMatriz[x][y - 1].setFill(corAPreencher);
                                campoUsuarioMatriz[x][y - 2].setOcupado(true);
                                campoUsuarioMatriz[x][y - 2].setFill(corAPreencher);
                                break;
                        }
                        break;
                    case 2:
                        switch (rectangleNavio.getRotacao()) {
                            case 1:
                                campoUsuarioMatriz[x + 1][y].setOcupado(true);
                                campoUsuarioMatriz[x + 1][y].setFill(corAPreencher);
                                break;
                            case 2:
                                campoUsuarioMatriz[x][y + 1].setOcupado(true);
                                campoUsuarioMatriz[x][y + 1].setFill(corAPreencher);
                                break;
                            case 3:
                                campoUsuarioMatriz[x - 1][y].setOcupado(false);
                                campoUsuarioMatriz[x - 1][y].setFill(corAPreencher);
                                break;
                            case 4:
                                campoUsuarioMatriz[x][y - 1].setOcupado(true);
                                campoUsuarioMatriz[x][y - 1].setFill(corAPreencher);
                                break;
                        }
                        break;
                }
            });
        });

        campoAdversarioPronto.getChildren().addAll(hBoxCampoAdversario);

        vboxUsuario.getChildren().addAll(helpText, campoUsuarioPronto);
        vboxAdversario.getChildren().addAll(helpText2, campoAdversarioPronto);

        HBox hBoxCompleto = new HBox(vboxUsuario, vboxAdversario);
        hBoxCompleto.setSpacing(50);
        hBoxCompleto.setAlignment(Pos.CENTER);
        campoUsuarioPronto.setAlignment(Pos.TOP_LEFT);
        campoAdversarioPronto.setAlignment(Pos.TOP_LEFT);

        root.setCenter(hBoxCompleto);

        Button voltar = new Button("Sair da partida");
        HBox hBoxTop = new HBox(voltar);
        hBoxTop.setPadding(new Insets(20));
        voltar.setOnAction(event -> {
            BatalhaNavalJSCliente.createScene();
        });

        root.setTop(hBoxTop);

        BatalhaNavalJSCliente.fxContainer.setScene(new Scene(root));

        setVezDoUsuario();

        new Thread(() -> checarTiro()).start();
        new Thread(() -> checarRespostaTiro()).start();
    }

    private void checarTiro() {
        while (true) {
            try {
                Thread.sleep(100);

                Tiro tiro = (Tiro) BatalhaNavalJSCliente.space.takeIfExists(new Tiro(null, null, BatalhaNavalJSCliente.nomeOponente), null, 2000);

                if (tiro != null) {
                    boolean acertou = campoUsuarioMatriz[tiro.x][tiro.y].isOcupado();
                    if (acertou) {
                        contagemUsuario--;
                        campoUsuarioMatriz[tiro.x][tiro.y].setFill(COR_ACERTO);

                        if (contagemUsuario == 0) {
                            BatalhaNavalJSCliente.enviarMensagemInfo("Você perdeu!");
                        }
                    } else {
                        campoUsuarioMatriz[tiro.x][tiro.y].setFill(COR_ERRO);
                    }
                    
                    BatalhaNavalJSCliente.space.write(new RespostaTiro(BatalhaNavalJSCliente.nickname, acertou, tiro.x, tiro.y), null, Lease.FOREVER);
                    
                    estado = ESTADO_VEZ;
                    BatalhaNavalJSCliente.estadoOponente = ESTADO_PRONTO;
                    
                    setVezDoUsuario();
                }

            } catch (InterruptedException | UnusableEntryException | TransactionException | RemoteException ex) {
                BatalhaNavalJSCliente.exibirException(ex);
            }
        }
    }

    private void checarRespostaTiro() {
        while (true) {
            try {
                Thread.sleep(100);

                RespostaTiro res = (RespostaTiro) BatalhaNavalJSCliente.space.takeIfExists(new RespostaTiro(BatalhaNavalJSCliente.nomeOponente, null), null, 2000);

                if (res != null) {
                    boolean acertou = res.acertou;
                    if (acertou) {
                        contagemAdversario--;
                        tocarSom(true);
                        campoAdversarioMatriz[res.x][res.y].setFill(COR_ACERTO);

                        if (contagemAdversario == 0) {
                            BatalhaNavalJSCliente.enviarMensagemInfo("Você ganhou!");
                        }
                    } else {
                        tocarSom(false);
                        campoAdversarioMatriz[res.x][res.x].setFill(COR_ERRO);
                    }
                }

            } catch (InterruptedException | UnusableEntryException | TransactionException | RemoteException ex) {
                BatalhaNavalJSCliente.exibirException(ex);
            }
        }
    }

    private RectangleCoordenado gerarRect(int x, int y, boolean usuario) {
        RectangleCoordenado rect = new RectangleCoordenado(x, y, TAMANHO_CELULA - 1, TAMANHO_CELULA - 1, COR_BACKGROUND);
        rect.setStroke(Color.YELLOW);
        rect.setStrokeWidth(1);

        if (!usuario) {
            rect.setOnMouseClicked(event -> {
                if (contagemUsuario == 0) {

                } else if (contagemAdversario == 0) {

                } else if (estado == ESTADO_VEZ) {
                    if (!campoAdversarioMatriz[x][y].getFill().equals(COR_ACERTO) && !campoAdversarioMatriz[x][y].getFill().equals(COR_ERRO)) {
                        System.out.println("Atirou");
                        atirar(x, y);
                    } else {
                        BatalhaNavalJSCliente.enviarMensagemErro("Atire em um local vazio");
                    }
                } else {
                    BatalhaNavalJSCliente.enviarMensagemErro("Espere sua vez");
                }
            });
        }

        return rect;
    }

    private void atirar(int x, int y) {
        try {
            BatalhaNavalJSCliente.space.write(new Tiro(x, y, BatalhaNavalJSCliente.nickname), null, Lease.FOREVER);
            estado = ESTADO_PRONTO;
            BatalhaNavalJSCliente.estadoOponente = ESTADO_VEZ;

            setVezDoUsuario();
        } catch (TransactionException | RemoteException ex) {
            BatalhaNavalJSCliente.exibirException(ex);
        }
    }

    public int getContagemUsuario() {
        return contagemUsuario;
    }

    public int getContagemAdversario() {
        return contagemAdversario;
    }

    public void decrementarContagemUsuario() {
        contagemUsuario--;
    }

    public void decrementarContagemAdveersário() {
        contagemAdversario--;
    }

    private void tocarSom(boolean acerto) {
        if (acerto) {
            acertoMusica.play();
        } else {
            erroMusica.play();
        }
    }

    private void setVezDoUsuario() {
        if (estado == ESTADO_VEZ) {
            helpText2.setFill(Color.RED);
            helpText.setFill(Color.GREEN);

            if (contagemUsuario > 0)
                BatalhaNavalJSCliente.enviarMensagemInfo("Sua vez");
        } else {
            helpText.setFill(Color.RED);
            helpText2.setFill(Color.GREEN);
        }
    }
}
