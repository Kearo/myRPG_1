package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import database.Cryption;
import database.ServerDBaccess;
import mechanics.ErrorLog;

public class LogInServer {
	private Selector selector;
	private SocketAddress serverAddress;
	private ServerSocketChannel ssc;
	private boolean initialized = false;
	private Thread recieve;
	private Map<SocketAddress, SocketChannel> clients;
	private ByteBuffer buffer;
	private GameServer gs;

	public LogInServer(String hostName, int port, GameServer gs) {
		this.gs = gs;
		initialized = init(hostName, port);
		if (initialized) {
			recieve();
		}
	}

	private boolean init(String hostName, int port) {
		serverAddress = new InetSocketAddress(hostName, port);

		clients = new HashMap<SocketAddress, SocketChannel>();
		buffer = ByteBuffer.allocate(1024);
		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
		
			ssc.socket().bind(serverAddress);
			//System.out.println(ssc.socket().getInetAddress());
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("logIn_init", e);
			return false;
		}
		return true;
	}

	private void recieve() {
		recieve = new Thread() {
			@Override
			public void run() {
				try {
					selector = Selector.open();
					ssc.register(selector, SelectionKey.OP_ACCEPT);
				} catch (IOException e) {
					e.printStackTrace();
					ErrorLog.writeError("logIn_selector", e);
				}
				while (initialized) {
					try {
						selector.select();
					} catch (IOException e) {
						e.printStackTrace();
						ErrorLog.writeError("logIn_select", e);
					}
					Set<SelectionKey> selKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selKeys.iterator();

					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						if (key.isValid()) {
							if (key.isAcceptable()) {
								try {
									SocketChannel sc = ssc.accept();
									sc.configureBlocking(false);
									sc.register(selector, SelectionKey.OP_READ);
									clients.put(sc.getRemoteAddress(), sc);
								} catch (IOException e) {
									e.printStackTrace();
									ErrorLog.writeError("logIn_accept", e);
								}
							} else if (key.isReadable()) {
								SocketChannel sc = (SocketChannel) key.channel();
								buffer.clear();
								try {
									sc.read(buffer);
									String message = new String(buffer.array()).trim();
									manageInput(message, sc);
								} catch (IOException e) {
									e.printStackTrace();
									ErrorLog.writeError("logIn_read", e);
									try {
										sc.close();
									} catch (IOException e1) {
										e1.printStackTrace();
										ErrorLog.writeError("logIn_close", e);
									}
								}
							}
						}
						iter.remove();
					}
				}
			}
		};
		recieve.start();
	}

	private void manageInput(String message, SocketChannel sc) {
		String pw, id;
		SocketAddress sa = null;
		try {
			sa = sc.getRemoteAddress();
		} catch (IOException e2) {
			e2.printStackTrace();
			ErrorLog.writeError("logIn_input_getRemote", e2);
		}
		if (message.startsWith("login")) {
			id = message.substring(message.indexOf("/") + 1, message.lastIndexOf("/"));
			pw = message.substring(message.lastIndexOf("/") + 1, message.length());

			String hash = null;
			String hashserver = null;
			try {
				hash = Cryption.generatePWHash(pw, ServerDBaccess.getLastlogin(id));
				hashserver = ServerDBaccess.getPWhash(id);
			} catch (NoSuchAlgorithmException | ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				ErrorLog.writeError("logIn_hash", e);
			}
			if (hashserver.equals(hash)) {
				if (gs.addPlayer(id, sa)) {
					System.out.println("success");
					sendToSpecific("login_success", sa);
				} else {
					sendToSpecific("access denied, already logged in", sa);
					gs.removePlayer(id, sa);
					disconnectClient(sc);

					System.out.println("access denied");
				}
				try {
					ServerDBaccess.setLastlogin(id);
					ServerDBaccess.setPWhash(id, pw);
				} catch (ClassNotFoundException | SQLException | NoSuchAlgorithmException e) {
					e.printStackTrace();
					ErrorLog.writeError("logIn_db", e);
				}
			} else {
				sendToSpecific("access denied", sa);
				disconnectClient(sc);
				System.out.println("access denied");
			}
		}
		if (message.equals("connect_world/")) {
			String s = "worldLogIn/" + gs.getAddress() + "/" + gs.getWorlds();
			sendToSpecific(s, sa);
		}
		if (message.equals("/disconnect")) {
			try {
				clients.remove(sc.getRemoteAddress(), sc);
				sc.close();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("logIn_dc", e);
			}
		}
	}

	public void sendToAll(String message) {
		buffer.clear();
		for (Map.Entry<SocketAddress, SocketChannel> entry : clients.entrySet()) {
			SocketChannel sc = entry.getValue();
			buffer.put(message.getBytes());
			buffer.flip();
			try {
				sc.write(buffer);
				buffer.clear();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("logIn_write", e);
			}
		}
	}

	public void sendToSpecific(String message, SocketAddress sa) {
		buffer.clear();
		if (sa != null) {
			buffer.put(message.getBytes());
			buffer.flip();
			SocketChannel sc = clients.get(sa);
			try {
				sc.write(buffer);
				buffer.clear();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorLog.writeError("logIn_write2", e);
			}
		}
	}

	public void disconnectClient(SocketChannel sc) {
		String s = "/disconnect";
		ByteBuffer.wrap(s.getBytes());
		try {
			sc.write(buffer);
			sc.close();
			buffer.clear();
			clients.remove(sc.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("logIn_dc", e);
		}
	}

	public void disconnectClient(SocketAddress sa) {
		String s = "/disconnect";
		ByteBuffer.wrap(s.getBytes());
		SocketChannel sc = clients.get(sa);
		try {
			sc.write(buffer);
			sc.close();
			buffer.clear();
			clients.remove(sa);
		} catch (IOException e) {
			e.printStackTrace();
			ErrorLog.writeError("logIn_dc", e);
		}
	}
	public void closeServer() {
		for (Map.Entry<SocketAddress, SocketChannel> entry : clients.entrySet()) {
			SocketChannel sc = entry.getValue();
			disconnectClient(sc);
			clients.remove(entry.getKey());
		}
	}
}
