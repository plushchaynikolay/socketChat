package client;

import client.Client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ClientFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2526930244929415001L;

	private static final Icon NO_CONNECTION = new ImageIcon(ClientFrame.class.getResource("NO_CONNECTION.png"));
	private static final Icon GOT_CONNECTION = new ImageIcon(ClientFrame.class.getResource("GOT_CONNECTION.png"));
	
	private static final String JOIN_ROOM = "<JOIN_ROOM>";
	private static final String LEAVE_ROOM = "<LEAVE_ROOM>";
//	private static final String EXIT = "<EXIT>";
	private static final String GET_LIST = "<GET_LIST>";
	
	
	private JLabel lbNickname 			= new JLabel("Your nickname:");
	private JTextField fldNickname 		= new JTextField();
	private JButton btnGetConnection 	= new JButton("Connect");
	private JTextArea textArea			= new JTextArea();
	private JScrollPane spTextArea		= new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private JTextField textField		= new JTextField();
	private JButton btnSend				= new JButton("Send");
	private JLabel lbStatusNC			= new JLabel(NO_CONNECTION);
	private JLabel lbStatusGC			= new JLabel(GOT_CONNECTION);
	private JButton btnJoinRoom			= new JButton("Join room");
	private JButton btnLeaveRoom		= new JButton("Leave room");
	private JButton btnGetList			= new JButton("Get list");
	
	private Client client;
	private InetAddress ip;
	private int port;
	
	ClientFrame(InetAddress ip, int port) {
		super("Client");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		this.ip = ip;
		this.port = port;
		
//		При закрытии окна закрыть соединение
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
				super.windowClosing(e);
			}
		});
		
//		Создание элементов
//			Область вывода текста
		textArea.setLineWrap(true);
		textArea.setEnabled(false);
		spTextArea.setPreferredSize(new Dimension(500, 200));
//			Поле воода ник-нейма
		fldNickname.setPreferredSize(new Dimension(200, 20));
		fldNickname.setMaximumSize(new Dimension(500, 20));
//			Поле ввода текста
		textField.setPreferredSize(new Dimension(300, 20));
		textField.setMaximumSize(new Dimension(1000, 20));

//			Кнопка подключения
		btnGetConnection.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fldNickname.getText().isEmpty())
					return;
				getConnection();
				print("Connected");
				setEnableActions(true);
			}
		});
		
//			Кнопка отправки сообщения
		btnSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				client.send(textField.getText());
				textField.setText("");
				
			}
		});
		
//			Кнопка присоединения к чат комнате
		btnJoinRoom.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog dialog = createDialog();
				dialog.setVisible(true);
			}
		});
		
//			Кнопка выхода из чат комнаты в комнату поумолчанию
		btnLeaveRoom.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				client.send(LEAVE_ROOM);
			}
		});
		
//			Кнопка вывода списка пользователей данной комнаты
		btnGetList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				client.send(GET_LIST);
				
			}
		});
//		Определение менеджера расположения
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
//		Горизонтальная группа
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(lbNickname)
						.addComponent(fldNickname)
						.addComponent(btnGetConnection)
						.addComponent(lbStatusNC)
						.addComponent(lbStatusGC)
						.addComponent(btnJoinRoom)
						.addComponent(btnLeaveRoom))
				.addComponent(spTextArea, Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
						.addComponent(btnGetList)
						.addComponent(textField)
						.addComponent(btnSend))
				);
//		Вертикальная группа
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(lbNickname)
						.addComponent(fldNickname)
						.addComponent(btnGetConnection)
						.addComponent(lbStatusNC)
						.addComponent(lbStatusGC)
						.addComponent(btnJoinRoom)
						.addComponent(btnLeaveRoom))
				.addComponent(spTextArea)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnGetList)
						.addComponent(textField)
						.addComponent(btnSend)));
		
		setLayout(layout);
		pack();
		setLocationRelativeTo(null);
		
//		Запрещаем любые действия до подключения к серверу
		setEnableActions(false);
	}
	
	/**
	 * Подключение клиента сокета.
	 * Клиенту передается информация о фрейме для обращения к методам ClientFrame.print(),
	 * никнейм для обращения на сервер, а также ip и номер порта для подключения.
	 */
	private void getConnection() {

		client = new Client(this, ip, port);
		client.setNickname(fldNickname.getText());
		client.start();
	}

	/**
	 * Выполняет закрытие сессии клиента перед закрытием окна
	 */
	private void exit() {
		if(!client.isAlive())
			return;
		try {
			client.setStoped();
			client.close();
		} catch (IOException e) {
			print(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Выполняет вывод информации в текстовое поле
	 * @param obj Применяется метод .toString() и вывод
	 */
	public void print (Object obj) {
		textArea.append(obj.toString() + "\n");
	}
	
	private JDialog createDialog() {
		JDialog dialog = new JDialog(this, "Joining the room", true);
		dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		dialog.setSize(new Dimension(180, 90));
		Container container = dialog.getContentPane();
		JTextField tfRoomNumer = new JTextField();
		tfRoomNumer.setPreferredSize(new Dimension(100, 20));
		
		JButton btnJoin = new JButton("Join");
		btnJoin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!tfRoomNumer.getText().isEmpty()) {
					client.send(JOIN_ROOM);
					client.send(tfRoomNumer.getText());
				}
				
			}
		});
		container.add(tfRoomNumer, BorderLayout.WEST);
		container.add(btnJoin, BorderLayout.EAST);
		dialog.pack();
		dialog.setLocationRelativeTo(btnJoinRoom);
		return dialog;
	}
	
	/**
	 * Включает/выключает кнопки входа/выхода комнаты, получения списка пользоателей
	 * в комнате, кнопку и поле отправки сообщения и переключает значки подключения
	 * @param set должен принять значение <code>true</code> если клиент подключен к серверу и <code>false</code> если не подключен
	 */
	public void setEnableActions(boolean set) {
		btnJoinRoom.setEnabled(set);
		btnLeaveRoom.setEnabled(set);
		btnSend.setEnabled(set);
		btnGetConnection.setEnabled(!set);
		btnGetList.setEnabled(set);
		fldNickname.setEnabled(!set);
		lbStatusNC.setVisible(!set);
		lbStatusGC.setVisible(set);
	}
}
