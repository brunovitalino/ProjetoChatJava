
import net.jini.space.JavaSpace;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.AbstractListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Cliente extends JFrame {

	private boolean conectado = false;
	//private static TreeMap<String, ArrayList<String>> salasList = new TreeMap<String, ArrayList<String>>(String.CASE_INSENSITIVE_ORDER); //Nao importa o tamanho das letras das keys
	private Lookup finder;
	private JavaSpace space;
	private TelaAtiva telaAtiva = null;
	private String telaInicial = "";
	
	private JPanel contentPane;
	private JList lstUsuarios;
	private JList lstSalas;
	private JScrollPane spLog;
	private JTextPane tpLog;
	private JTextField txtUsuario;
	private JTextField txtMensagem;
	private JButton btnConectar;
	private JButton btnEnviar;
	private JLabel lblSalasConectadas;
	private JLabel lblUsuariosConectados;
	private JLabel lblTelaAtiva;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Cliente frame = new Cliente();
					frame.setVisible(true);
					frame.txtUsuario.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Cliente() {
		setTitle("Cliente");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 540, 415);
		contentPane = new JPanel();
		contentPane.setForeground(new Color(255, 255, 255));
		contentPane.setBackground(new Color(0, 0, 128));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblUsuario = new JLabel("Usu\u00E1rio:");
		lblUsuario.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsuario.setForeground(Color.CYAN);
		lblUsuario.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUsuario.setBounds(132, 11, 51, 19);
		contentPane.add(lblUsuario);
		
		JLabel lblSalas = new JLabel("Salas:");
		lblSalas.setForeground(Color.CYAN);
		lblSalas.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblSalas.setBounds(32, 47, 30, 14);
		contentPane.add(lblSalas);
		
		lblSalasConectadas = new JLabel("0");
		lblSalasConectadas.setHorizontalAlignment(SwingConstants.LEFT);
		lblSalasConectadas.setForeground(Color.CYAN);
		lblSalasConectadas.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblSalasConectadas.setBounds(62, 47, 20, 14);
		contentPane.add(lblSalasConectadas);
		
		JLabel lblUsuarios = new JLabel("Usu\u00E1rios:");
		lblUsuarios.setForeground(Color.CYAN);
		lblUsuarios.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblUsuarios.setBounds(418, 47, 42, 14);
		contentPane.add(lblUsuarios);
		
		lblTelaAtiva = new JLabel("TELA: INICIO");
		lblTelaAtiva.setHorizontalAlignment(SwingConstants.LEFT);
		lblTelaAtiva.setForeground(Color.CYAN);
		lblTelaAtiva.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTelaAtiva.setBounds(132, 43, 257, 19);
		contentPane.add(lblTelaAtiva);
		
		lblUsuariosConectados = new JLabel("0");
		lblUsuariosConectados.setHorizontalAlignment(SwingConstants.LEFT);
		lblUsuariosConectados.setForeground(Color.CYAN);
		lblUsuariosConectados.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblUsuariosConectados.setBounds(465, 47, 20, 14);
		contentPane.add(lblUsuariosConectados);
		
		JScrollPane spSalas = new JScrollPane();
		spSalas.setBounds(10, 72, 112, 261);
		contentPane.add(spSalas);
		
		lstSalas = new JList();
		lstSalas.setModel(new AbstractListModel() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		lstSalas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JList auxJList = (JList)arg0.getSource();
		        if (arg0.getClickCount() == 2)
		        {
		            // Duplo-click detectado
		            //int index = lstSalas.locationToIndex(arg0.getPoint()); //Solucao usada para quando mais de um item eh selecionado.
		        	int index = auxJList.getSelectedIndex();
		        	String sala = lstSalas.getModel().getElementAt(index).toString();
		        	entrarSala(txtUsuario.getText(), sala);
		        }
			}
		});
		spSalas.setViewportView(lstSalas);
		
		spLog = new JScrollPane();
		spLog.setBounds(132, 73, 257, 261);
		contentPane.add(spLog);
		
		tpLog = new JTextPane();
		//O caret faz com que ocorra o autoscroll.
		DefaultCaret caret = (DefaultCaret)tpLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		spLog.setViewportView(tpLog);
		
		JScrollPane spUsuariosConectados = new JScrollPane();
		spUsuariosConectados.setBounds(399, 73, 112, 261);
		contentPane.add(spUsuariosConectados);
		
		lstUsuarios = new JList();
		lstUsuarios.setModel(new AbstractListModel() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		lstUsuarios.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JList auxJList = (JList)arg0.getSource();
		        if (arg0.getClickCount() == 2)
		        {
		            // Duplo-click detectado
		            //int index = lstSalas.locationToIndex(arg0.getPoint()); //Solucao usada para quando mais de um item eh selecionado.
		        	int index = auxJList.getSelectedIndex();
		        	String usuarioDestino = lstUsuarios.getModel().getElementAt(index).toString();
		        	if ( !usuarioDestino.equals( txtUsuario.getText() ) )
		        	{
		        		wisparUsuario(txtUsuario.getText(), usuarioDestino);
		        	}
		        }
			}
		});
		spUsuariosConectados.setViewportView(lstUsuarios);
		lblUsuariosConectados.setText( Integer.toString(lstUsuarios.getModel().getSize()) );
		
		txtUsuario = new JTextField();
		txtUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Sem a thread a GUI ficava travada.
				Thread t = new Thread()
				{
					public void run()
					{
						if (conectado)
						{
							conectado = false;
							tpLog.setText(tpLog.getText() + "Desconectado.\n");
							txtUsuario.setEnabled(true);
							txtUsuario.setBackground(Color.WHITE);
							txtUsuario.setForeground(Color.BLACK);
							btnConectar.setText("Conectar");
						}
						else
						{
							conectar();
						}
				    }
				};
				t.start();
			}
		});
		txtUsuario.setColumns(10);
		txtUsuario.setBounds(193, 12, 164, 20);
		contentPane.add(txtUsuario);
		
		txtMensagem = new JTextField();
		txtMensagem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarComando();
			}
		});
		txtMensagem.setBounds(132, 345, 257, 20);
		contentPane.add(txtMensagem);
		txtMensagem.setColumns(10);
		
		btnConectar = new JButton("Conectar");
		btnConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Sem a thread a GUI ficava travada.
				Thread t = new Thread()
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
				t.start();
			}
		});
		btnConectar.setBounds(398, 11, 113, 23);
		contentPane.add(btnConectar);
		
		btnEnviar = new JButton("Enviar");
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarComando();
			}
		});
		btnEnviar.setBounds(399, 344, 112, 23);
		contentPane.add(btnEnviar);
		
		//INICIALIZACAO
		lblSalasConectadas.setText( Integer.toString(lstSalas.getModel().getSize()) );
		lblUsuariosConectados.setText( Integer.toString(lstUsuarios.getModel().getSize()) );
	}
	
	//METODOS	
	public void conectar()
	{
		if (txtUsuario.getText().isEmpty())
		{
			JOptionPane.showMessageDialog(null, "Digite um nome de usuário.");
		}
		else
		{
			txtUsuario.setEnabled(false);
			txtUsuario.setBackground(Color.GRAY);
			txtUsuario.setForeground(Color.WHITE);
			btnConectar.setText("Conectando...");
			try
			{
				TreeMap<String, Queue<String>> salas;
				TreeMap<String, Queue<String>> mensagensDeSalas = new TreeMap<String, Queue<String>>(String.CASE_INSENSITIVE_ORDER);
				ArrayList<String> usuarios = new ArrayList<String>();
				//Mensagens recebidas de outros usuarios, composto por sua origem e mengagens.
				TreeMap<String, Queue<String>> mensagensDeUsuarios = new TreeMap<String, Queue<String>>(String.CASE_INSENSITIVE_ORDER);
				
				tpLog.setText(tpLog.getText() + "Procurando pelo servico JavaSpace...\n");
				System.out.print("Procurando pelo servico JavaSpace...\n");
				finder = new Lookup(JavaSpace.class);
				space = (JavaSpace) finder.getService();

				if (space == null)
				{
					tpLog.setText(tpLog.getText() + "O servico JavaSpace nao foi encontrado. Encerrando...\n");
					System.out.print("O servico JavaSpace nao foi encontrado. Encerrando...\n");
					//System.exit(-1);
					btnConectar.setText("Conectar");
					return;
				}
				else
				{
					tpLog.setText(tpLog.getText() + "O servico JavaSpace foi encontrado.\nConectando...\n");
					System.out.print("O servico JavaSpace foi encontrado.\nConectando...\n");

					Message template = new Message();
					Message message = (Message) space.take(template, null, 30 * 1000);
					
					if ( message==null )
					{
						JOptionPane.showMessageDialog(null, "Não foi possível conectar. Não há tupla.");
						tpLog.setText(tpLog.getText() + "Desconectado.\n");
						txtUsuario.setEnabled(true);
						txtUsuario.setBackground(Color.WHITE);
						txtUsuario.setForeground(Color.BLACK);
						btnConectar.setText("Conectar");
						System.out.println("Nao foi possível conectar. Nao ha tupla.");
					}
					else
					{
						if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
						{
							if (message.mensagensUsuarios.containsKey(txtUsuario.getText()))
							{
								JOptionPane.showMessageDialog(null, "Usuário já existe.");
								tpLog.setText(tpLog.getText() + "Desconectado.\n");
								txtUsuario.setEnabled(true);
								txtUsuario.setBackground(Color.WHITE);
								txtUsuario.setForeground(Color.BLACK);
								btnConectar.setText("Conectar");
								System.out.println("Usuario ja existe.");
							}
							else
							{
								mensagensDeUsuarios = new TreeMap<String, Queue<String>>();
								message.mensagensUsuarios.put(txtUsuario.getText(), mensagensDeUsuarios);
								message.tipoOperacao = TipoOperacao.CONECTAR_USUARIO;
								message.nomeRemetente = txtUsuario.getText();
								conectado = true;
								StringBuilder sb = new StringBuilder();
								sb.append( tpLog.getText() ).append("Conectado!\n\n").append("COMANDOS:\n");
								sb.append("Entrar na sala:\n/j <sala>\n").append("Sair da sala:\n/l <sala>\n");
								sb.append("Enviar msg privada ao usuario:\n/w <usuario>\n");
								//sb.append("Listar usuarios de uma sala:\n/lu <sala>\n").append("Listar todos usuarios:\n/lau\n\n");
								tpLog.setText( sb.toString() );
								btnConectar.setText("Conectado");
								txtMensagem.setEnabled(true);
								btnEnviar.setEnabled(true);
								System.out.println("Conectado!\n");
								
								System.out.println("Usuarios: " + message.mensagensUsuarios.keySet());
								telaInicial = tpLog.getText();
							}
						}
						
						space.write(message, null, 60 * 1000);
					}
					
					while (conectado)
					{	//Carrega as informacoes nos componentes.
						try
						{
							template = new Message();
							message = (Message) space.read(template, null, 10 * 1000);
							try { Thread.sleep(500); } catch (Exception ex) { } //So pra dar uma desacelerada no programa.
							//Condicao "conectado" necessarioa, pois como pode haver uma demora para o space receber a msg,
							//entao pode ser que apos recebida, o usuario ja tenha clicado no botao desconectar. 
							if ( conectado && message!=null && message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA)
							{
								//Carrega a lista de salas no componente lstSalas.
								if ( !lstSalas.getModel().toString().equals( message.mensagensSalas.keySet().toString() ) )
								{
									DefaultListModel DLM = new DefaultListModel();
									TreeMap<String, ArrayList<String>> listaDeSalas = message.salasList;
									
									for( Map.Entry<String, ArrayList<String>> entry : listaDeSalas.entrySet() )
									{
										DLM.addElement( entry.getKey() ); //Entao adicione sala ao componente lista de salas.
									}
									lstSalas.setModel(DLM);
									lblSalasConectadas.setText( Integer.toString( lstSalas.getModel().getSize() ) );
								}
								
								//Carrega as mensagens no componente tpLog e renomeia o titulo da tela no lblTelaAtiva.
								if ( telaAtiva!=null )
								{	
									if ( telaAtiva.getTipoTela()==null ) //telaAtiva.getNomeTela().equals("INICIO")
									{	
										//Atualizamos o nome da nossa sala.
										lblTelaAtiva.setText("TELA: " + telaAtiva.getNomeTela());
										//Deixamos o componente lstUsuarios vazio, ja que e uma tela inicial.
										DefaultListModel DLM = new DefaultListModel();
										if ( !lstUsuarios.getModel().toString().equals( DLM ) )
										{	//Tela inicial nao possui "lista de usuarios", entao deixamos tudo em branco.
											lstUsuarios.setModel( DLM );
											lblUsuariosConectados.setText( "0" );
										}
										//Restauramos as informacoes de nossa tela de inicio.
										tpLog.setText(telaInicial);
									}
									
									//Se for uma sala, entao TAMBEM carregara a lista de usuarios do componente lstUsuarios.
									if ( telaAtiva.getTipoTela()==TipoDestino.SALA && message.mensagensSalas.containsKey(telaAtiva.getNomeTela())
											&& message.mensagensSalas.get(telaAtiva.getNomeTela()).containsKey(txtUsuario.getText()) )
									{	
										String sala = telaAtiva.getNomeTela();
										//Atualizamos o nome da nossa tela caso necessario.
										if ( !lblTelaAtiva.getText().equals("TELA: sala " + sala) )
										{
											lblTelaAtiva.setText("TELA: sala " + sala);
										}
										//Carrega os usuarios no componente lstUsuarios.
										//A tree salasList possui a relacao de salas e seus usuarios. telaAtiva e nossa key sala.
										if ( !lstUsuarios.getModel().toString().equals( message.salasList.get(sala).toString() ) )
										{
											DefaultListModel DLM = new DefaultListModel();
											ArrayList<String> listaDeUsuarios = message.salasList.get(sala);
											
											Iterator<String> iterador = listaDeUsuarios.iterator();
											while( iterador.hasNext() )
											{
												DLM.addElement( iterador.next() ); //Entao adicione sala ao componente lista de salas.
											}
											lstUsuarios.setModel(DLM);
											lblUsuariosConectados.setText( Integer.toString( listaDeUsuarios.size() ) );
										}
										
										mensagensDeSalas = (TreeMap<String, Queue<String>>) message.mensagensSalas.get( sala ).clone();
										
										//Carrega na tela o texto da sala que aquele usuario pode ter acesso.
										String texto = "";
										texto = toTexto( new LinkedList<String>( mensagensDeSalas.get(txtUsuario.getText()) ) );
										if ( !tpLog.getText().equals(texto) )
										{
											tpLog.setText( "Agora falando em " + sala + "...\n" + texto );
										}
										
					/*JMS AQUI!!!*/		//Atualizacao de novas mensagens pelo JMS que possa vir a ter. !!!!!!!!!!! JMS
										ReceberFilaJMS rf = new ReceberFilaJMS( sala, txtUsuario.getText() );
										Queue<String> novasMensagens = rf.getFila();

										System.out.println("---1 message.mensagensSalas.get("+sala+").get("+txtUsuario.getText()+"): \n" + message.mensagensSalas.get(sala).get(txtUsuario.getText()));
										if ( !novasMensagens.isEmpty() )
										{
											receberMensagensDaFilaJMS(sala, txtUsuario.getText(), novasMensagens);
											/*
											System.out.println("---2 novasMensagens: " + novasMensagens);
											Iterator<String> iterador = novasMensagens.iterator();
											while ( iterador.hasNext() )
											{
												mensagensDeSalas.get( txtUsuario.getText() ).add( iterador.next() );
											}
											message.mensagensSalas.put(sala, mensagensDeSalas);
											System.out.println("---3 message.mensagensSalas.get("+sala+").get("+txtUsuario.getText()+"): " + message.mensagensSalas.get(sala).get(txtUsuario.getText()));
											*/
										}
										try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
									}
									
									if ( telaAtiva.getTipoTela()==TipoDestino.USUARIO && message.mensagensUsuarios.containsKey(telaAtiva.getNomeTela())
											&& message.mensagensUsuarios.get(telaAtiva.getNomeTela()).containsKey(txtUsuario.getText()) )
									{
										//Atualizamos o nome da nossa tela caso necessario.
										if ( !lblTelaAtiva.getText().equals("TELA: usuário " + telaAtiva.getNomeTela()) )
										{
											lblTelaAtiva.setText("TELA: usuário " + telaAtiva.getNomeTela());
										}										
										//Deixa o componente lstUsuarios em branco. Gambiarra para dar a impressão pro usuario de estar em uma sala privada. rsrs
										/*
  Como solicitado, agora a lista		DefaultListModel DLM = new DefaultListModel();
  de usuarios aparece durante as			if ( !lstUsuarios.getModel().toString().equals( DLM.toString() ) )
  msgs privadas.						{	//Se nao for vazio, entao:
											lstUsuarios.setModel( DLM );
											lblUsuariosConectados.setText( "0" );
										}
										*/								
										//Carrega o texto recebido do usuario destino na tela.										
										//Primeiro pegamos a tree do nosso usuario destino, que contem a relacao de todos usuarios remetentes com suas respectivas mensagens:
										mensagensDeUsuarios = message.mensagensUsuarios.get( telaAtiva.getNomeTela() );
										//Depois selecionamos as mensagens recebidas pelo nosso destino apenas da origem que nos interessa, ou seja, o usuario atual deste cliente.
										String texto = "";
										texto = toTexto( mensagensDeUsuarios.get( txtUsuario.getText() ) );
										if ( !tpLog.getText().equals(texto) )
										{
											tpLog.setText( "Agora falando com " + telaAtiva.getNomeTela() + "...\n" + texto );
										}
									}
								}
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}		
	}
	
	public void enviarComando()
	{
		try
		{
			if (conectado)
			{
				String msg = txtMensagem.getText();
				
				
				if (msg!=null || !msg.equals("") )
				{	//Entrar sala.
					if ( msg.length()>3 && msg.substring(0, 3).equals("/j ") )
					{	//Separando a proxima condicao, garante que toda msg comecando com "/j " fique reservada, mesmo que nao tenha nada apos escrito.
						if (msg.substring(3).equals( msg.substring(3).replaceAll(" ","") )) //trim() nao funcionou
						{	//O usuario tenta criar uma nova sala ou entrar numa existente.
							entrarSala(txtUsuario.getText(), msg.substring(3));
						}
					}	//Deixar sala.
					else if ( msg.length()>3 && msg.substring(0, 3).equals("/l ") )
					{	//Separando a proxima condicao, garante que toda msg comecando com "/l " fique reservada, mesmo que nao tenha nada apos escrito.
						if (msg.substring(3).equals( msg.substring(3).replaceAll(" ","") )) //trim() nao funcionou
						{	//O usuario tenta criar uma nova sala ou entrar numa existente.
							sairSala(txtUsuario.getText(), msg.substring(3));
						}
					}	//Wispar usuario. (falar em privado)
					else if ( msg.length()>3 && msg.substring(0, 3).equals("/w ") )
					{	//Separando a proxima condicao, garante que toda msg comecando com "/l " fique reservada, mesmo que nao tenha nada apos escrito.
						if (msg.substring(3).equals( msg.substring(3).replaceAll(" ","") )) //trim() nao funcionou
						{	//O usuario tenta criar uma nova sala ou entrar numa existente.
							wisparUsuario(txtUsuario.getText(), msg.substring(3));
						}
					}	//Wispar usuario. (falar em privado)
					else if ( msg.length()>4 && msg.substring(0, 4).equals("/lu ") )
					{	//Separando a proxima condicao, garante que toda msg comecando com "/l " fique reservada, mesmo que nao tenha nada apos escrito.
						if (msg.substring(4).equals( msg.substring(4).replaceAll(" ","") )) //trim() nao funcionou
						{	//O usuario tenta criar uma nova sala ou entrar numa existente.
							//listarUsuariosSala(msg.substring(4), txtUsuario.getText());
						}
					}	//Wispar usuario. (falar em privado)
					else if ( msg.length()>4 && msg.substring(0, 4).equals("/lau") )
					{	//Separando a proxima condicao, garante que toda msg comecando com "/l " fique reservada, mesmo que nao tenha nada apos escrito.
						//listarTodosUsuarios(txtUsuario.getText());
					}
					else
					{
						if ( !msg.substring(0, 1).equals("/") )
						{
							enviarMensagem(txtUsuario.getText(), msg);
						}
					}
					txtMensagem.setText("");
				}				
				
				
				/**/
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//Abre a tela de sala para poder conversar.
	public void entrarSala(String usuario, String sala)
	{
		try
		{	//Preenchera o array de mensagens da tree mensagensSalas => (usuario, mensagens), (usuario, mensagens), (usuario, mensagens)...
			TreeMap<String, Queue<String>> mensagensDeSala = new TreeMap<String, Queue<String>>(String.CASE_INSENSITIVE_ORDER);
			//Preenchera o array de usuarios da tree salasList
			ArrayList<String> usuarios = new ArrayList<String>();
			String salaAntiga = "";
			
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{
					//A sala destino existe.
					if ( message.salasList.containsKey(sala) )
					{	
						//Como solicitado, agora um usuario que ja se encontra na sala pode tentar
						//entrar na mesma sala. Mas para evitar repeticao do mesmo nick, foi colocado
						//uma verificacao que checa se o nick ja existe na lista ou nao.
						if ( !isElementoNaLista(usuario, message.salasList.get(sala)) )
						{
							message.tipoOperacao = TipoOperacao.ENTRAR_SALA;
							message.tipoDestino = TipoDestino.SALA;
							message.nomeRemetente = usuario;
							message.nomeDestino = sala;
							//Usuario ficara apenas na nova sala, e saira da antiga (caso haja) atraves do laco abaixo.
							for (Map.Entry<String, ArrayList<String>> entry : message.salasList.entrySet())
							{
								if ( isElementoNaLista(usuario, entry.getValue()) && !entry.getKey().equals(sala) )
								{
									salaAntiga = entry.getKey(); //Guarda a sala antiga.
									usuarios = entry.getValue(); //Guarda os usuarios dessa sala.
									usuarios = removeElementoDaListaIgnorandoCase(usuario, usuarios); //Remove o usuario desse cliente da sala.
									//Guarda a nova lista com user da sala antiga removido.
									message.salasList.put(salaAntiga, usuarios);
									//Usaremos o message.conteudo para guardar a antiga sala, que sera usada
									//no servidor durante o algoritmo de remocao de salas vazias.
									message.conteudo = salaAntiga;
									//Usuario nao se repete, entao nao ha necessidade de continuar.
									break;
								}
							}
							usuarios = message.salasList.get(sala);
							usuarios.add(usuario);
							Collections.sort(usuarios);
							message.salasList.put(sala, usuarios);
							//Se o usuario nunca entrou nessa sala, entao receba a fila de mensagens enviadas/recebidas por outros usuarios desta sala,
							//adicione uma fila de mensagens enviadas/recebidas por ele, para que assim possa receber mensagens relacionadas a essa sala.
							if ( !message.mensagensSalas.get(sala).containsKey(usuario) )
							{
								//Recebe a tree de todos usuarios dessa salas e suas filas de mensagens relacionadas a esta sala.
								mensagensDeSala = message.mensagensSalas.get(sala);
								//Adiciona o nosso novo usuario que esta entrando na sala.
								mensagensDeSala.put(usuario, new LinkedList<String>());
								message.mensagensSalas.put(sala, mensagensDeSala);
							}
							//Muda para tela que o usuario ficara vendo.
							telaAtiva = new TelaAtiva(TipoDestino.SALA, sala);
						}
					}
					//A sala destino nao existe.
					else
					{	//Usuario nao esta na sala. (logico! ela n existe! rsrs)
						message.tipoOperacao = TipoOperacao.ENTRAR_SALA;
						message.tipoDestino = TipoDestino.SALA;
						message.nomeRemetente = usuario;
						message.nomeDestino = sala;
						//Usuario ficara apenas nessa sala e saira da antiga, caso esteja.
						for (Map.Entry<String, ArrayList<String>> entry : message.salasList.entrySet())
						{
							if ( isElementoNaLista(usuario, entry.getValue()) && !entry.getKey().equals(sala) )
							{
								salaAntiga = entry.getKey(); //Guarda a sala antiga.
								usuarios = (ArrayList<String>) entry.getValue().clone();
								usuarios = removeElementoDaListaIgnorandoCase(usuario, usuarios);
								//Guardando a nova lista com user da sala antiga removido.
								message.salasList.put(salaAntiga, usuarios);
								//Usaremos o message.conteudo para guardar a antiga sala, que sera usada
								//no servidor durante o algoritmo de remocao de salas vazias.
								message.conteudo = salaAntiga;
								//Usuario nao se repete, nao ha necessidade de continuar.
								break;
							}
						}
						usuarios = new ArrayList<String>();
						usuarios.add(usuario);
						message.salasList.put(sala, usuarios);
						//Adicione uma fila de mensagens enviadas/recebidas por ele nesta sala,
						//para que assim possa receber mensagens relacionadas a essa sala.
						mensagensDeSala.put(usuario, new LinkedList<String>());
						message.mensagensSalas.put(sala, mensagensDeSala);
						//Muda para tela que o usuario ficara vendo.
						telaAtiva = new TelaAtiva(TipoDestino.SALA, sala);
					}
				}
				space.write(message, null, 60 * 1000);
			}
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	//Deixa a sala e retorna para a tela de INICIO.
	public void sairSala(String usuario, String sala)
	{
		try
		{	//Preenchera o array de usuarios da tree salasList
			ArrayList<String> usuarios = new ArrayList<String>();
			
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null)
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{
					if ( !message.salasList.isEmpty() && message.salasList.containsKey(sala) )
					{
						if ( isElementoNaLista(usuario, message.salasList.get(sala)) )
						{
							message.tipoOperacao = TipoOperacao.SAIR_SALA;
							message.tipoDestino = TipoDestino.SALA;
							message.nomeRemetente = usuario;
							message.nomeDestino = sala;
							
							usuarios = message.salasList.get(sala);
							usuarios = removeElementoDaListaIgnorandoCase(usuario, usuarios);
							message.salasList.put(sala, usuarios);
							//Se a tela em que o usuario se encontra for o da propria sala a ser deixada, entao volte pra tela de inicio.
							if ( telaAtiva.getNomeTela().equals(sala) )
							{
								telaAtiva = new TelaAtiva(null, "INICIO");
							}
						}
					}
				}				
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Abre a tela de usuario para poder conversar.
	public void wisparUsuario(String origem, String destino)
	{	
		try
		{	//origem=usuarioOrigem (usuario desse cliente). destino=usuarioDestino.
			
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA)
				{
					//Usaremos o proprio historico de msg como referencia
					//para saber se o usuario destino existe ou nao.
					//Se usuario destino existe:
					if ( message.mensagensUsuarios.containsKey(destino) )
					{
						message.tipoOperacao = TipoOperacao.WISPAR_USUARIO;
						message.tipoDestino = TipoDestino.USUARIO;
						message.nomeRemetente = origem;
						message.nomeDestino = destino;
						//Ambiguidade.
						if ( !message.mensagensUsuarios.get(destino).containsKey(origem) )
						{
							//So bastaria a primeira linha, ja que a comunicacao entre eles eh
							//a mesmo da segunda linha. Mas como o mensagensUsuarios esta sendo
							//necessario para checar os usuarios conectados, entao manteremos.
							message.mensagensUsuarios.get(destino).put(origem, new LinkedList<String>());
							message.mensagensUsuarios.get(origem).put(destino, new LinkedList<String>());
						}
						//A segunda condicao inversa "!message.mensagensUsuarios.get(origem).containsKey(destino)"
						//nao e necessaria, pois a primeira condicao garante isso, ja que sempre sera feito dois puts ambiguos.

						//Muda para tela que o usuario ficara vendo.
						telaAtiva = new TelaAtiva(TipoDestino.USUARIO, destino);
					}
				}
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	//Envia msg para a sala ou usuario. Destino nao e necessario, pois a tela ja esta aberta.
	public void enviarMensagem(String origem, String msg)
	{
		try
		{	Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{
					if (msg != null && !msg.equals(""))
					{
						message.tipoOperacao = TipoOperacao.NOVA_MENSAGEM;
						message.tipoDestino = telaAtiva.getTipoTela(); //tipo de destino: sala ou usuario.
						message.nomeRemetente = origem;					//E o usuario desse cliente.
						message.nomeDestino = telaAtiva.getNomeTela(); //nome de destino: NomeSala ou NomeUsuario.
						message.conteudo = msg;
						
						/*if ( telaAtiva.getTipoTela()==null )
						{
							tpLog.setText(tpLog.getText() + msg + "\n");
						}
						else if ( message.tipoDestino==TipoDestino.SALA )
						{
							message.conteudo = msg;
						}
						else if ( message.tipoDestino==TipoDestino.USUARIO )
						{
							message.conteudo = msg;							
						}*/
					}
				}
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void receberMensagensDaFilaJMS(String sala, String usuario, Queue<String> novasMensagens)
	{
		try
		{
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				if ( message.tipoOperacao==TipoOperacao.AGUARDANDO_TAREFA )
				{					
					System.out.println("---2 novasMensagens: " + novasMensagens);
					Iterator<String> iterador = novasMensagens.iterator();
					while ( iterador.hasNext() )
					{
						message.mensagensSalas.get( sala ).get( usuario ).add( iterador.next() );
					}
					message.mensagensSalas.put(sala, message.mensagensSalas.get( sala ));
					System.out.println("---3 message.mensagensSalas.get("+sala+").get("+usuario+"): " + message.mensagensSalas.get(sala).get(usuario));					
				}
				space.write(message, null, 60 * 1000);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String toTexto(Queue<String> fila)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iterador = fila.iterator();
		
		//sb.append("");
		while (!fila.isEmpty())
		{
			sb.append(fila.poll()).append("\n");
		}
		return sb.toString();
	}
	
	//Metodo para fazer comparacao na lista de forma caseInSensitive.
	public boolean isElementoNaLista(String elemento, ArrayList<String> lista)
	{
		Iterator<String> iterador = lista.iterator();
		while (iterador.hasNext())
		{
			if (elemento.equalsIgnoreCase( iterador.next() ))
			{
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> removeElementoDaListaIgnorandoCase(String elemento, ArrayList<String> lista)
	{
		Iterator<String> iterador = lista.iterator();
		String aux = "";
		
		while (iterador.hasNext())
		{
			aux = iterador.next();
			if ( elemento.equalsIgnoreCase(aux) )
			{
				lista.remove(aux);
				//Como o elemento nao se repete, e tambem daria erro caso
				//o elemento fosse o ultimo, entao colocamos um break.
				//Ou poderiamos substituir o Iterator com outro tipo de laco, mas nao sera necessario.
				break;
			}
		}
		return lista;
	}
	
	private void desconectar()
	{
		conectado = false;
		tpLog.setText(tpLog.getText() + "Desconectado.\n");
		txtUsuario.setEnabled(true);
		txtUsuario.setBackground(Color.WHITE);
		txtUsuario.setForeground(Color.BLACK);
		btnConectar.setText("Conectar");		
		try
		{	
			Message template = new Message();			
			Message message = (Message) space.take(template, null, 60 * 1000);
			
			if ( message!=null )
			{
				//Todo usuario que se conecta esta obrigatoriamente contido em message.mensagensUsuarios.
				//Se a primeira condicao abaixo nao satisfaz, entao nao ha necessidade de computar o codigo restante contigo nela.
				if ( message.mensagensUsuarios.containsKey( txtUsuario.getText() ) )
				{
					//Remove o usuario da lista de mensagens privadas.
					message.mensagensUsuarios.remove( txtUsuario.getText() );
					//Remove o usuario de todas as salas que ele possa vir a estar, se houver alguma sala existente.
					if ( !message.mensagensSalas.isEmpty() )
					{
						for (Map.Entry<String, TreeMap<String, Queue<String>>> entrySalas : message.mensagensSalas.entrySet())
						{
							if ( message.mensagensSalas.get(entrySalas.getKey()).containsKey(txtUsuario.getText()) )
							{
								message.mensagensSalas.get(entrySalas.getKey()).remove( txtUsuario.getText() );
							}
							if ( message.salasList.get(entrySalas.getKey()).contains(txtUsuario.getText()) )
							{
								message.salasList.get(entrySalas.getKey()).remove( txtUsuario.getText() );
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}