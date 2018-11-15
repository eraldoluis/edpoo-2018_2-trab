package br.ufms.facom.edpoo.tt_search_compress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import edu.princeton.cs.algs4.BinaryIn;
import edu.princeton.cs.algs4.BinaryOut;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Aplicação que busca tweets e salva em um arquivo compactado usando o
 * algoritmo de Huffman.
 * 
 * @author Eraldo R. Fernandes (eraldo@facom.ufms.br)
 *
 */
public class AppBuscaTweets extends Application {
	/**
	 * Lista de tweets retornados pela busca.
	 */
	private List<String> tweets;

	/**
	 * TextField com os termos de busca.
	 */
	private TextField tfTermosBusca;

	/**
	 * API do Twitter.
	 */
	private Twitter apiTwitter;

	/**
	 * Janela principal da aplicação.
	 */
	private Stage janelaPrincipal;

	@Override
	public void start(Stage primaryStage) {
		// Referência para a janela principal.
		janelaPrincipal = primaryStage;

		// Título da janela.
		janelaPrincipal.setTitle("Busca Tweets");

		// Twitter API.
		apiTwitter = TwitterFactory.getSingleton();

		// Cria TextField para usuário digitar os termos de busca.
		tfTermosBusca = new TextField("termos de busca");

		// Botão de busca. Configura método 'buscar()' para tratar clique.
		Button btnBuscar = new Button("Busca");
		btnBuscar.setOnMouseClicked(e -> buscar());

		// Controle que exibe lista de tweets retornados pela busca.
		ListView<String> lstTweets = new ListView<>();

		// Lista de tweets para ser manipulada (inserção e remoção).
		tweets = lstTweets.getItems();

		// Configura lista de resultados para ajustar largura de acordo com a janela.
		lstTweets.setCellFactory(param -> new ListCell<String>() {
			{
				prefWidthProperty().bind(lstTweets.widthProperty().subtract(2));
				setMaxWidth(Control.USE_PREF_SIZE);
				setWrapText(true);
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);

				if (item != null && !empty) {
					setText(item);
				} else {
					setText(null);
				}
			}
		});

		// Botação para salvar tweets. Configura método 'salvar()' para tratar clique.
		Button btnSalvar = new Button("Salvar...");
		btnSalvar.setOnMouseClicked(e -> salvar());

		// Cria container e insere os controles.
		VBox container = new VBox(tfTermosBusca, btnBuscar, lstTweets, btnSalvar);

		// Incluir container na janela principal.
		janelaPrincipal.setScene(new Scene(container));

		// Exibe janela principal.
		janelaPrincipal.show();
	}

	/**
	 * Busca tweets usando os termos fornecidos pelo usuário.
	 */
	private void buscar() {
		try {
			// Limpa lista de tweets anteriores.
			tweets.clear();

			// Cria nova busca e configura língua para português.
			Query query = new Query(tfTermosBusca.getText());
			query.lang("pt");

			// Executa busca.
			QueryResult result = apiTwitter.search(query);

			// Percorre todos os tweets retornados.
			int numTweets = 0;
			for (Status status : result.getTweets()) {
				/*
				 * Substitui sequências de espaços por um único espaço, incluindo quebras de
				 * linha e tabulações.
				 */
				String tweetLimpo = status.getText().replaceAll("[\\t\\n\\r]+", " ");

				// Cria uma string com o nome do usuário e o texto do seu tweet.
				String tweet = String.format("@%s: %s", status.getUser().getScreenName(), tweetLimpo);

				// Adiciona texto na lista de tweets.
				tweets.add(tweet);

				++numTweets;
			}

			// Se houver mais uma página de resultados, executa a nova busca.
			if (result.hasNext()) {
				// Obtém query para próxima página.
				query = result.nextQuery();

				// Executa busca.
				result = apiTwitter.search(query);

				// Obtém tweets da próxima página.
				for (Status status : result.getTweets()) {
					/*
					 * Substitui sequências de espaços por um único espaço, incluindo quebras de
					 * linha e tabulações.
					 */
					String tweetLimpo = status.getText().replaceAll("[\\t\\n\\r]+", " ");

					// Cria uma string com o nome do usuário e o texto do seu tweet.
					String tweet = String.format("@%s: %s", status.getUser().getScreenName(), tweetLimpo);

					// Adiciona texto na lista de tweets.
					tweets.add(tweet);

					++numTweets;
				}
			}

			// Exibe mensagem de quantos tweets foram recuperados.
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Pesquisa Concluída");
			alert.setHeaderText("Pesquisa Concluída");
			alert.setContentText(String.format("Encontrados %d tweets", numTweets));
			alert.showAndWait();
		} catch (TwitterException e) {
			// Erro ao acessar API do Twitter.
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Erro");
			alert.setHeaderText(e.getClass().toString());
			alert.setContentText(e.getErrorMessage());
			alert.showAndWait();
		}
	}

	/**
	 * Salva a lista de tweets em um arquivo selecionado pelo usuário.
	 */
	private void salvar() {
		// Cria janela de seleção de arquivo a ser salvo.
		FileChooser fc = new FileChooser();
		fc.setTitle("Arquivo onde os tweets serão salvos");

		// Exibe janela e obtém arquivo selecionado.
		File f = fc.showSaveDialog(janelaPrincipal);

		if (f != null) {
			/*
			 * Se algum arquivo foi selecionado, então concatena todos os tweets, separados
			 * por quebra de linha.
			 */
			StringBuilder sb = new StringBuilder();
			for (String t : tweets)
				sb.append(t + "\n");

			// Cria stream de entrada a partir dos bytes da string criada acima.
			BinaryIn in = new BinaryIn(new ByteArrayInputStream(sb.toString().getBytes()));

			// Abre arquivo de saída como um stream binário.
			BinaryOut out = new BinaryOut(f.getAbsolutePath());

			while (!in.isEmpty())
				out.write(in.readBoolean());

			out.close();

			// Exibe aviso de sucesso.
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Tweets salvos");
			alert.setHeaderText("Pasta: " + f.getParentFile().getAbsolutePath());
			alert.setContentText("Arquivo: " + f.getName());
			alert.showAndWait();
		}
	}

	/**
	 * Lança aplicação.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
