import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TicTacToeClient {
	private static Scanner keyboard = new Scanner(System.in);

	public static void main(String[] args) throws UnknownHostException,
			IOException, ClassNotFoundException {
		try (Socket socket = new Socket("45.50.5.238", 38007)) {
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			Thread error = new Thread(new err());
			error.start();
			BoardMessage board;

			System.out.print("Name: ");
			String name = keyboard.nextLine();
			out.writeObject(new ConnectMessage(name));
			while (true) {
				while (true) {
					menuMain();
					int choice = keyboard.nextInt();
					if (choice == 1) {
						CommandMessage cmd = new CommandMessage(
								CommandMessage.Command.NEW_GAME);
						out.writeObject(cmd);
						break;
					}

					else if (choice == 2) {
						out.writeObject(new CommandMessage(
								CommandMessage.Command.EXIT));
						System.out.println("Thank you for playing!");
						System.exit(0);
					}

					else {
						System.out.println("Select a valid option");
					}

				}

				while (true) {
					board = (BoardMessage) in.readObject();
					if (board.getStatus() == BoardMessage.Status.IN_PROGRESS) {
						while (true) {
							menuPlay();
							int choice = keyboard.nextInt();
							if (choice == 1) {
								printBoard(board);
								System.out.print("Select Row (1,2,3): ");
								byte row = (byte) (keyboard.nextInt() - 1);
								System.out.print("Select Column (1,2,3): ");
								byte col = (byte) (keyboard.nextInt() - 1);
								out.writeObject(new MoveMessage(row, col));
								break;
							}

							else if (choice == 2) {
								out.writeObject(new CommandMessage(
										CommandMessage.Command.SURRENDER));
								break;
							}

							else {
								System.out.println("Select a valid option");
							}
						}
					} else if (board.getStatus() == BoardMessage.Status.PLAYER1_VICTORY) {
						System.out.println("You Win!");
						break;
					} else if (board.getStatus() == BoardMessage.Status.PLAYER2_VICTORY) {
						System.out.println("Your Opponent Wins!");
						break;
					} else if (board.getStatus() == BoardMessage.Status.PLAYER1_SURRENDER) {
						System.out.println("You have Surrendered.");
						break;
					} else if (board.getStatus() == BoardMessage.Status.PLAYER2_SURRENDER) {
						System.out.println("Your Oppenent has Surrendered.");
						break;
					} else if (board.getStatus() == BoardMessage.Status.STALEMATE) {
						printBoard(board);
						System.out.println("Stalemate.");
						break;
					} else {
						System.out.println("Error");
						break;
					}
				}
			}
		}

	}

	private static void printBoard(BoardMessage board) {
		byte[][] b = board.getBoard();
		int r= 0;
		for (int i = 0; i < 5; i++) {
			if (i % 2 == 0) {
				int c = 0;
				for (int j = 0; j < 5; j++) {
					if (j % 2 == 0) {
						if (b[r][c] == 1) {
							System.out.print("X");
						} else if (b[r][c] == 2) {
							System.out.print("O");
						} else {
							System.out.print(" ");
						}
						c++;
					} else {
						System.out.print("|");
					} if (j==4){
						System.out.println();
					}
				}
				r++;
			} else {
				System.out.println("-----");
			}
		}

	}

	private static void menuMain() {
		System.out.println("1. New Game");
		System.out.println("2. Exit");
		System.out.print("Choice: ");

	}

	public static void menuPlay() {
		System.out.println("1. Make a Move");
		System.out.println("2. Surrender");
		System.out.print("Choice: ");
	}

}

class err extends Thread {

	@Override
	public void run() {
		try (Socket socket = new Socket("45.50.5.238", 38007)) {
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			while (true) {
				ErrorMessage error = (ErrorMessage) in.readObject();
				if (error != null) {
					System.out.println(error.getError());
					break;
				}
			}
			System.exit(0);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
