
import net.jini.space.JavaSpace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class Servidor extends JFrame {

	Lookup finder;
	JavaSpace space;
	//public static TreeMap<String, ArrayList<String>> salasList = new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER); //Nao importa o tamanho das letras das keys
	//public ArrayList<String> mensagensSalas;
	//public ArrayList<String> mensagensUsuarios;
	
	private boolean conectado = false;
	
	private JPanel contentPane;
	private JScrollPane spLog;
	private JTextPane tpLog;
	private JButton btnConectar;
	//Map das salas que estao vazias composto de nomeSala
	//e horario em secondsOfDay em que ficou vazia.
	private TreeMap<String, Integer> salasVazias;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Servidor frame = new Servidor();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Servidor() {
		setTitle("Servidor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 390, 355);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 0));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblServidor = new JLabel("Status");
		lblServidor.setForeground(new Color(255, 255, 255));
		lblServidor.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblServidor.setBounds(10, 14, 72, 22);
		contentPane.add(lblServidor);
		
		spLog = new JScrollPane();
		tpLog = new JTextPane();
		//O caret faz com que ocorra o autoscroll.
		DefaultCaret caret = (DefaultCaret)tpLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		spLog.setBounds(10, 47, 356, 258);
		contentPane.add(spLog);
		
		spLog.setViewportView(tpLog);
		
		btnConectar = new JButton("Conectar");
		btnConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				Thread t1 = new Thread()
				{
					public void run()
					{
						if (conectado)
						{
							desconectar();
						}
						else
						{
							conectar();
						}
				    }
				};
				t1.start();
			}
		});
		btnConectar.setBounds(263, 16, 103, 23);
		contentPane.add(btnConectar);
		
		//Inicializacao
		//~~
	}
	
	public void conectar()
	{
		if (!conectado)
		{
			btnConectar.setText("Conectando...");
			try
			{
				tpLog.setText(tpLog.getText() + "Procurando pelo servico JavaSpace...\n");
				System.out.print("Procurando pelo servico JavaSpace...\n");
				finder = new Lookup(JavaSpace.class);
				space = (JavaSpace) finder.getService();
	
				if (space == null) {
					tpLog.setText(tpLog.getText() + "O servico JavaSpace nao foi encontrado. Encerrando...\nServidor offline.\n");
					System.out.print("O servico JavaSpace nao foi encontrado. Encerrando...\nServidor offline.\n");
					//System.exit(-1);
					btnConectar.setText("Conectar");
					return;
				}
				else
				{
					tpLog.setText(tpLog.getText() + "O servico JavaSpace foi encontrado.\nServidor online!\n");
					System.out.print("O servico JavaSpace foi encontrado.\nServidor online!\n");
					Message template = new Message();
					Message message = new Message();
					message.mensagensSalas = new TreeMap<String, TreeMap<String, Queue<String>>>(String.CASE_INSENSITIVE_ORDER);
					message.mensagensUsuarios = new TreeMap<String, TreeMap<String, Queue<String>>>(String.CASE_INSENSITIVE_ORDER);
					message.salasList = new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
					message.tipoOperacao = null;
					message.tipoDestino = null;
					message.nomeRemetente = "";
					message.nomeDestino = "";
					message.conteudo = "";
					salasVazias = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
					int zum = 0;
					TreeMap<String, ArrayList<String>> salasListLocal = new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER);
					
					space.write(message, null, 1 * 60000);
					conectado = true;
					btnConectar.setText("Conectado");
					
					while (conectado)
					{
						
						message = (Message) space.read(template, null, 60 * 1000);
						//Condicao "conectado" necessarioa, pois como pode haver um delay para o space receber a msg,
						//entao pode ser que quando receber ja tenhamos clicado no botao desconectar. 
						if (conectado)
						{
							if ( message==null )
							{
								tpLog.setText(tpLog.getText() + "Mensagem recebida NULA.\n");
								System.out.print("Mensagem recebida NULA.\n");							
							}
							else
							{
								LocalTime tempoLocal = LocalTime.now();
								
								if ( message.tipoOperacao != TipoOperacao.AGUARDANDO_TAREFA )
								{
									
									message = (Message) space.take(template, null, 1 * 1000);
									if ( message==null )
									{
										tpLog.setText( tpLog.getText() + "ERRO. Um cliente pegou a tupla que usariamos." );
										System.out.println("ERRO. Um cliente pegou a tupla que usariamos.");
									}
									else								
									{	//Caso o cidadao invente de desconectar enquanto o take acima esta
										//sendo lido, entao o proximo trecho de codigo nao sera lido.
										if ( conectado )
										{
											
											if ( message.tipoOperacao==TipoOperacao.CONECTAR_USUARIO )
											{
												tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " conectou.\n");
												System.out.print("Usuário " + message.nomeRemetente + " conectou.\n");
											}
											else if ( message.tipoOperacao==TipoOperacao.ENTRAR_SALA )
											{
												//Utilizamos o message.conteudo como artificio para guardar o nome da antiga sala e agora usaremos aqui.
												//Se message.conteudo for vazio, entao o usuario nao estava em nenuhuma sala antes.
												if ( !message.conteudo.equals("") && message.salasList.get( message.conteudo ).isEmpty() )
												{	
													//Se nao tiver mais nenhum outro usuario ao deixar a sala,
													//entao inicia o contador desta sala colocando na lista de salas vazias.
													salasVazias.put( message.conteudo, tempoLocal.toSecondOfDay() );
												}
												//Tambem verificamos essa nova sala que ele entrou, se ja era alguma sala existente vazia que estava prestes a ser removida, entao cancelamos.
												if ( salasVazias.containsKey( message.nomeDestino ) )
												{
													salasVazias.remove( message.nomeDestino );
												}
												tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " entrou na sala "+ message.nomeDestino + ".\n");
												System.out.print("Usuário " + message.nomeRemetente + " entrou na sala "+ message.nomeDestino + ".\n");
											}
											else if ( message.tipoOperacao==TipoOperacao.SAIR_SALA )
											{	
												if ( !message.nomeDestino.equals("") && message.salasList.get( message.nomeDestino ).isEmpty() )
												{	
													//Se nao tiver mais nenhum outro usuario ao deixar a sala,
													//entao inicia o contador desta sala colocando na lista de salas vazias.
													salasVazias.put( message.nomeDestino, tempoLocal.toSecondOfDay() );
												}
												tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " deixou a sala "+ message.nomeDestino + ".\n");
												System.out.print("Usuário " + message.nomeRemetente + " deixou a sala "+ message.nomeDestino + ".\n");
											}
											else if ( message.tipoOperacao==TipoOperacao.WISPAR_USUARIO )
											{
												tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " conversa com usuário "+ message.nomeDestino + ".\n");
												System.out.print("Usuário " + message.nomeRemetente + " conversa com usuário "+ message.nomeDestino + ".\n");
											}
											else if ( message.tipoOperacao==TipoOperacao.NOVA_MENSAGEM )
											{
												Queue<String> fila; //Fila de mensagens
												String horario = tempoLocal.toString().substring(0, 8); //HH:mm:ss
												String mensagem = "["+horario+"] "+message.nomeRemetente+" diz:\n" + message.conteudo;
												
												if ( message.tipoDestino==TipoDestino.SALA )
												{	
													String sala = message.nomeDestino;
													String usuarioDaIteracao = "";
													EnviarFilaJMS ef;
													int c = 1;
													//Percorre todos usuarios da sala. O nomeDestino eh a sala que o usuario esta enviando msgs.
													for ( Map.Entry<String, Queue<String>> entry : message.mensagensSalas.get(sala).entrySet() )
													{
														usuarioDaIteracao = entry.getKey();
														System.out.println("usuarioDaIteracao: " + usuarioDaIteracao);
														//Se o usuario ja entrou nessa sala alguma vez na vida, saberemos atraves do mensagensSalas.
														//E caso ele tenha deixado a sala, o registro no mensagensSalas continuara, mas o registro no salasList sumira,
														//entao o usuario nao recebera a mensagem no primeiro momento. Usaremos o JMS como envio para o usuario receber depois.
									/*JMS AQUI!!!*/		if ( !message.salasList.get(sala).contains(usuarioDaIteracao) )
														{
															//Como o usuario da iteracao deixou a sala momentaneamente, entao usamos o JMS para enviar a mensagem para uma fila.
															//A mensagem sera um MapMessage contento um cabecalho identificador do usuario da iteracao, que sera usado para reenviar a msg depois.
															ef = new EnviarFilaJMS(sala, usuarioDaIteracao);
															ef.enviar(  mensagem );
															// O continue fara com que o restante do algoritmo desse laco seja ignorado, seguindo para a proxima iteracao.
															continue;
														}
														System.out.println("contador: " + c);
														c++;
														//adiciona a nova mensagem a fila de mensagens que todo usuario possui ao entrar na sala.
														fila = entry.getValue();
														fila.add( mensagem );
														message.mensagensSalas.get(sala).put(usuarioDaIteracao, fila);
													}
													message.conteudo = "";
													tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " enviou msg para sala "+ message.nomeDestino + ".\n");
													System.out.print("Usuário " + message.nomeRemetente + " enviou msg para sala "+ message.nomeDestino + ".\n");
												}
												else if ( message.tipoDestino==TipoDestino.USUARIO )
												{
													//adiciona a nova mensagem a fila de mensagens. nomeDestino pertence ao outro usuario que recebera mensagem do nosso usuarioRemetente.
													fila = message.mensagensUsuarios.get( message.nomeDestino ).get( message.nomeRemetente );
													fila.add( mensagem );
													message.mensagensUsuarios.get( message.nomeDestino ).put(message.nomeRemetente, fila);
													//Vice-versa, pois nosso destino ja recebeu as msgs dele. Mas e o nosso usuario? Entao repete o processo, mas invertendo.
													fila = message.mensagensUsuarios.get( message.nomeRemetente ).get( message.nomeDestino );
													fila.add( mensagem );													
													message.mensagensUsuarios.get( message.nomeRemetente ).put(message.nomeDestino, fila);
													
													message.conteudo = "";
													tpLog.setText(tpLog.getText() + "Usuário " + message.nomeRemetente + " enviou msg para usuário "+ message.nomeDestino + ".\n");
													System.out.print("Usuário " + message.nomeRemetente + " enviou msg para usuário "+ message.nomeDestino + ".\n");
												}
											}
											//Atualizando...
											message.tipoOperacao = TipoOperacao.AGUARDANDO_TAREFA;
											message.conteudo = "";
										}
										//Como dessa vez e um take, entao usamos o write pra
										//devolver pro java space a mensagem antiga ou atualizada.
										space.write(message, null, 60 * 1000);	
									}
									
								}								
								//Se o servidor estiver	no estado de AGUARDANDO_TAREFA, entao fique atualizando alguns dados pra nos.
								else
								{
									//Atualiza remocao de SALAS VAZIAS.						
									if ( !salasVazias.isEmpty() )
									{
										String sala = "";
										int velhoTempo, novoTempo, diferenca, tempoLimite=120; //600 para tempoLimite de 10min.
										boolean isTempoEstourado=false;
										
										//Professor, so digo isso, foi uma madrugada e um dia pra perceber que esse dito cujo tava pegando por referencia. lol
										//Ainda bem que tem o clone.
										salasListLocal = (TreeMap<String, ArrayList<String>>) message.salasList.clone();
										//Verificacao...
										novoTempo = tempoLocal.toSecondOfDay();
										for (Map.Entry<String, Integer> entry : salasVazias.entrySet())
										{
											sala = entry.getKey();
											velhoTempo = entry.getValue();
											//Se a condicao abaixo for verdadeira, significa que a msg foi enviada antes de meia noite e o tempo atual eh apos.
											//Como estamos trabalhando com diferencas pequenas, nao foi necessario levar em conta a data da msg.
											if ( velhoTempo > novoTempo )
											{
												novoTempo = novoTempo + 86400;
											}
											diferenca = novoTempo - velhoTempo;
											if ( diferenca >= tempoLimite )
											{
												isTempoEstourado = true;
												break;
											}
										}
										//Nao queremos dar take a toa. Entao checamos se durante a verificacao...
										//houve alguma sala que estourou o tempo limite de permanencia vazia.
										if ( isTempoEstourado )
										{
											message = (Message) space.take(template, null, 1 * 1000);
											
											if ( message==null )
											{
												tpLog.setText( tpLog.getText() + "ERRO. Um cliente pegou a tupla que usariamos." );
												System.out.println("ERRO. Um cliente pegou a tupla que usariamos.");
											}
											else								
											{
												//Como e um novo take, entao vamos confirmar mais uma vez, so pra garantir mesmo,
												//se a sala, variavel cujo ultimo valor recebido foi durante a verificacao..., ainda e uma sala vazia.
												if ( !sala.equals("") && salasVazias.containsKey(sala) )
												{
													//Agora removeremos pra valer!												
													for (Map.Entry<String, Integer> entry : salasVazias.entrySet())
													{
														sala = entry.getKey();
														velhoTempo = entry.getValue();
														if ( velhoTempo > novoTempo )
														{
															novoTempo = novoTempo + 86400;
														}
														diferenca = novoTempo - velhoTempo;
														System.out.println("Diferenca " + sala + ": " + diferenca);	
														if ( diferenca >= tempoLimite )
														{
															message.mensagensSalas.remove(sala);
															message.salasList.remove(sala);
															salasVazias.remove(sala);
															break;
														}
													}
												}
												//Como dessa vez e um take, entao usamos o write pra
												//devolver pro java space a mensagem antiga ou atualizada.
												space.write(message, null, 60 * 1000);	
											}
										}
									}
									
									//Atualiza remocao de MENSAGENS ANTIGAS (maior que 5min). Read sendo usado, e nao o take.
									int tempoMsg, tempoAtual, diferenca, tempoLimite=60, horaMsg = 0, minutoMsg = 0, segundoMsg = 0; //300 para tempoLimite de 5min.
									boolean isTempoEstourado=false;
									String mensagem = "";
									
									tempoAtual = tempoLocal.toSecondOfDay();
									
									//Primeiro SALAS:
									if ( !message.mensagensSalas.isEmpty() )
									{
										for (Map.Entry<String, TreeMap<String, Queue<String>>> entrySalas : message.mensagensSalas.entrySet())
										{
											if ( !message.mensagensSalas.get(entrySalas.getKey()).isEmpty() )
											{
												for (Map.Entry<String, Queue<String>> entryUsuarios : message.mensagensSalas.get(entrySalas.getKey()).entrySet())
												{
													Iterator<String> iteradorMensagens = message.mensagensSalas.get( entrySalas.getKey() ).get( entryUsuarios.getKey() ).iterator();
													while ( iteradorMensagens.hasNext() )
													{
														mensagem = iteradorMensagens.next();					//Ex. [23:59:59]
														horaMsg = Integer.parseInt( mensagem.substring(1, 3) );		// 23
														minutoMsg = Integer.parseInt( mensagem.substring(4, 6) );	//    59
														segundoMsg = Integer.parseInt( mensagem.substring(7, 9) );	//       59													
														tempoMsg = (horaMsg * 3600) + (minutoMsg * 60) + (segundoMsg);
														//Se a condicao abaixo for verdadeira, significa que a msg foi enviada antes de meia noite e o tempo atual eh apos.
														//Como estamos trabalhando com diferencas pequenas, nao foi necessario levar em conta a data da msg.
														if ( tempoMsg > tempoAtual )
														{
															tempoAtual = tempoAtual + 86400;
														}
														diferenca = tempoAtual - tempoMsg;
														if ( diferenca >= tempoLimite )
														{
															message.mensagensSalas.get(entrySalas.getKey()).get(entryUsuarios.getKey()).poll();
														}
														else
														{
															break; //Se a mensagem nao estourou o tempo, nao ha para que continuar verificando as proximas. Entao segue para o prox user.
														}
													}
												}
											}
										}
									}
									atualizarMensagensSalas( message.mensagensSalas );
									
									//Depois USUARIOS:
									if ( !message.mensagensUsuarios.isEmpty() )
									{
										for (Map.Entry<String, TreeMap<String, Queue<String>>> entryUsuariosDestino : message.mensagensUsuarios.entrySet())
										{
											if ( !message.mensagensUsuarios.get(entryUsuariosDestino.getKey()).isEmpty() )
											{
												for (Map.Entry<String, Queue<String>> entryUsuariosOrigem : message.mensagensUsuarios.get(entryUsuariosDestino.getKey()).entrySet())
												{
													Iterator<String> iteradorMensagens = message.mensagensUsuarios.get(entryUsuariosDestino.getKey()).get(entryUsuariosOrigem.getKey()).iterator();
													while ( iteradorMensagens.hasNext() )
													{
														mensagem = iteradorMensagens.next();					//Ex. [23:59:59]
														horaMsg = Integer.parseInt( mensagem.substring(1, 3) );		// 23
														minutoMsg = Integer.parseInt( mensagem.substring(4, 6) );	//    59
														segundoMsg = Integer.parseInt( mensagem.substring(7, 9) );	//       59													
														tempoMsg = (horaMsg * 3600) + (minutoMsg * 60) + (segundoMsg);
														//Se a condicao abaixo for verdadeira, significa que a msg foi enviada antes de meia noite e o tempo atual eh apos.
														//Como estamos trabalhando com diferencas pequenas, nao foi necessario levar em conta a data da msg.
														if ( tempoMsg > tempoAtual )
														{
															tempoAtual = tempoAtual + 86400;
														}
														diferenca = tempoAtual - tempoMsg;
														if ( diferenca >= tempoLimite )
														{
															message.mensagensUsuarios.get(entryUsuariosDestino.getKey()).get(entryUsuariosOrigem.getKey()).poll();
														}
														else
														{
															break; //Se a mensagem nao estourou o tempo, nao ha para que continuar verificando as proximas. Entao segue para o prox user.
														}
													}
												}
											}
										}
									}
									atualizarMensagensUsuarios( message.mensagensUsuarios );
									
								}
							}		
						}
						try { Thread.sleep(500); } catch (Exception e) {}						
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			conectado = false;
			btnConectar.setText("Conectar");
		}
	}

	private TreeMap<String, ArrayList<String>> removerSalasVazias(TreeMap<String, ArrayList<String>> salasList, int novoTempo)
	{
		TreeMap<String, ArrayList<String>> salasListLocal = salasList;
		String sala;
		int velhoTempo;
		int c = 1;
		//System.out.println("salasVazias: "+ salasVazias.toString());
		//System.out.println("salasListLocal: "+ salasListLocal.toString());
		
		for (Map.Entry<String, Integer> entry : salasVazias.entrySet())
		{
			System.out.println("it "+ c);
			sala = entry.getKey();			
			//Se a message.salasList AINDA estiver vazia,
			if ( salasListLocal.get(sala).isEmpty() )
			{	//entao:
				System.out.println("Sala "+sala+" esta vazia.");
				velhoTempo = entry.getValue();
				//Se velhoTempo for maior significa que a velha msg foi enviada
				//proximo de meia noite e a nova msg apos meia noite.
				if ( velhoTempo > novoTempo  )
				{	//entao adicione os segundos totais do proximo dia.
					novoTempo = novoTempo + 86400;
				}
				//Apos estar tudo nos conformes, buscaremos o que queremos.
				//Se a sala esta vazia por mais de 10min (600seg), entao remova.
				if ( (novoTempo-velhoTempo) > 5 )
				{
					System.out.println("salasListLocal antes: "+salasListLocal);
					salasListLocal.remove(sala);
					System.out.println("salasListLocal apos: "+salasListLocal);
				}
			}
			//Se entrou algum usuario na sala, entao tire essa sala da lista de salas vazias.
			else
			{	
				salasVazias.remove(sala);
			}
			c++;
		}
		return salasListLocal;
	}

	private void atualizarMensagensSalas(TreeMap<String, TreeMap<String, Queue<String>>> mensagensSalas)
	{
		try
		{
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{
					message.mensagensSalas = mensagensSalas;
				}
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void atualizarMensagensUsuarios(TreeMap<String, TreeMap<String, Queue<String>>> mensagensUsuarios)
	{
		try
		{
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{
					message.mensagensUsuarios = mensagensUsuarios;
				}
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void desconectar()
	{
		conectado = false;
		tpLog.setText(tpLog.getText() + "Servidor offline.\n");
		btnConectar.setText("Conectar");
		//Caso ainda haja alguma tupla no javaspace, remova.
		try
		{
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}
