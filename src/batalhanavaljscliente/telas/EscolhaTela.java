package batalhanavaljscliente.telas;

import batalhanavaljscliente.BatalhaNavalJSCliente;
import batalhanavaljscliente.entries.Partida;
import batalhanavaljscliente.entries.RequisicaoPartida;
import java.rmi.RemoteException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;

public class EscolhaTela {

    public void iniciarTela() {
        Button btnEntrar = new Button("Entrar em partida");
        btnEntrar.setOnAction((ActionEvent event) -> {
            new Thread(() -> entrarEmPartida()).start();
        });

        Button btnCriar = new Button("Criar partida");
        btnCriar.setOnAction((ActionEvent event) -> {
            criarPartida();
        });

        VBox vBox = new VBox(btnEntrar, btnCriar);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);

        StackPane root = new StackPane(vBox);

        BatalhaNavalJSCliente.fxContainer.setScene(new Scene(root));
    }

    private void entrarEmPartida() {
        Platform.runLater(() -> {
            try {
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Entrar em partida");
                dialog.setResizable(true);

                Label label1 = new Label("Nome do host: ");
                TextField text1 = new TextField();

                GridPane grid = new GridPane();
                grid.add(label1, 1, 1);
                grid.add(text1, 2, 1);
                dialog.getDialogPane().setContent(grid);

                ButtonType buttonTypeOk = new ButtonType("Entrar", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

                final Button okButton = (Button) dialog.getDialogPane().lookupButton(buttonTypeOk);
                okButton.setDisable(true);

                text1.textProperty().addListener((observable, oldValue, newValue) -> {
                    okButton.setDisable(newValue.trim().length() < 1);
                });

                dialog.setResultConverter((ButtonType b) -> {
                    if (b == buttonTypeOk) {
                        return text1.getText();
                    }

                    return null;
                });

                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    Partida p = (Partida) BatalhaNavalJSCliente.space.takeIfExists(new Partida(result.get()), null, 0);

                    if (p == null) {
                        BatalhaNavalJSCliente.enviarMensagemErro("Partida não existe");
                    } else {
                        BatalhaNavalJSCliente.nomeOponente = p.host;
                        BatalhaNavalJSCliente.space.write(new RequisicaoPartida(p.host, BatalhaNavalJSCliente.nickname), null, Lease.FOREVER);
                        BatalhaNavalJSCliente.estadoOponente = BatalhaTela.ESTADO_PREPARANDO;
                        new PreparacaoTela().iniciarTela(true, false);
                    }
                }
            } catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException ex) {
                BatalhaNavalJSCliente.exibirException(ex);
            }
        });
    }

    private void criarPartida() {
        try {
            BatalhaNavalJSCliente.space.write(new Partida(BatalhaNavalJSCliente.nickname), null, Lease.FOREVER);
            new PreparacaoTela().iniciarTela(false, true);
        } catch (TransactionException | RemoteException ex) {
            BatalhaNavalJSCliente.exibirException(ex);
        }
    }
}
