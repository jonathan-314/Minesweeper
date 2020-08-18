package Minesweeper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Minesweeper extends JPanel implements MouseListener {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * minesweeper instance
	 */
	public static Minesweeper game;

	/**
	 * width of the game
	 */
	int width = 48;

	/**
	 * height of the game
	 */
	int height = 26;

	/**
	 * number of mines
	 */
	int mines = 210;

	/**
	 * x coordinate of the upper left corner
	 */
	int startx = 100;

	/**
	 * y coordinate of the upper left corner
	 */
	int starty = 40;

	/**
	 * size of each square in pixels
	 */
	int size = 35;

	/**
	 * adjacency array
	 */
	int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };

	/**
	 * adjacency array
	 */
	int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };

	/**
	 * game array
	 */
	int[][] array = new int[width][height];

	/**
	 * is the square revealed
	 */
	boolean[][] reveal = new boolean[width][height];

	/**
	 * did the user flag the square
	 */
	boolean[][] flag = new boolean[width][height];

	/**
	 * number of flags the user has put down
	 */
	int flags = 0;

	/**
	 * is this the start of the game
	 */
	boolean start = true;

	/**
	 * computer solve
	 */
	boolean auto = false;

	/**
	 * is the game over
	 */
	boolean revealed = false;

	/**
	 * is the game over
	 */
	boolean gameOver = false;

	/**
	 * JFrame
	 */
	JFrame jf;

	/**
	 * starting time, used for timing
	 */
	long startTime = 0;

	/**
	 * minesweeper constructor
	 */
	public Minesweeper() {
		jf = new JFrame("minesweeper");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(2000, 1000);
		jf.setVisible(true);
		jf.add(this);
		addMouseListener(this);
		setFocusable(true);
		if (auto)
			auto();
		while (true) {
			try {
				if (startTime == 0 || revealed || gameOver) {
					Thread.sleep(100);
					continue;
				}
				long time = System.currentTimeMillis();
				time -= startTime;
				time %= 1000;
				Thread.sleep(1000 - time); // 1 frame per second
				jf.repaint();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * initializes the playing field
	 * 
	 * @param x x coordinate of user click
	 * @param y y coordinate of user click
	 */
	public void init(int x, int y) {
		Random random = new Random();
		int numberOfMines = 0;
		while (numberOfMines < mines) {
			int minex = random.nextInt(width);
			int miney = random.nextInt(height);
			if ((Math.abs(minex - x) + Math.abs(miney - y)) <= 2)
				continue;
			if (array[minex][miney] != -1) {
				array[minex][miney] = -1;
				for (int i = 0; i < 8; i++) {
					int adjx = minex + dx[i];
					int adjy = miney + dy[i];
					if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height) // out of bounds
						continue;
					if (array[adjx][adjy] != -1)
						array[adjx][adjy]++;
				}
				numberOfMines++;
			}
		}
		startTime = System.currentTimeMillis();
	}

	/**
	 * paint function, called each frame
	 */
	public void paint(Graphics g) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (reveal[i][j] || flag[i][j]) {
					g.setColor(Color.CYAN);
					String text = "" + array[i][j];
					if (array[i][j] == -1) {
						text = "X";
						g.setColor(Color.ORANGE);
					} else if (array[i][j] == 0)
						text = "";

					if (flag[i][j] && !revealed) {
						text = "O";
						g.setColor(Color.GREEN);
					}
					if (flag[i][j] && revealed && array[i][j] != -1)
						g.setColor(Color.RED);

					g.fillRect(i * size + startx, j * size + starty, size, size);
					g.setColor(Color.BLACK);
					g.drawString(text, i * size + startx + size / 2 - 3, j * size + starty + size / 2 + 5);
				}
				g.drawRect(i * size + startx, j * size + starty, size, size);
			}
		}
		g.drawOval(590, 8, 15, 15);
		g.drawString("C", 593, 20);
		g.drawString("Jonathan Guo", 610, 20);
		g.drawString("" + (mines - flags), 50, 100);
		Font f = new Font("Helvetica", 20, 20);
		g.setFont(f);
		long time = System.currentTimeMillis();
		time -= startTime;
		long seconds = time / 1000;
		long minutes = seconds / 60;
		seconds %= 60;
		String timeDisplay = Long.toString(seconds);
		if (timeDisplay.length() == 1)
			timeDisplay = "0" + timeDisplay;
		if (startTime == 0) {
			minutes = 0;
			timeDisplay = "00";
		}
		g.drawString(minutes + ":" + timeDisplay, 50, 130);
	}

	/**
	 * left click
	 * 
	 * @param mousex x coordinate of click
	 * @param mousey y coordinate of click
	 */
	public void leftClick(int mousex, int mousey) {
		if (revealed)
			return;
		if (start) {
			init(mousex, mousey);
			start = false;
		}
		if (array[mousex][mousey] == -1 && !flag[mousex][mousey]) {
			revealAll();
		} else if (array[mousex][mousey] == 0) {
			LinkedList<Integer> qx = new LinkedList<Integer>();
			LinkedList<Integer> qy = new LinkedList<Integer>();
			qx.add(mousex);
			qy.add(mousey);
			while (!qx.isEmpty()) {
				int currx = qx.remove();
				int curry = qy.remove();
				if (reveal[currx][curry])
					continue;
				reveal[currx][curry] = true;
				for (int i = 0; i < 8; i++) {
					int adjx = currx + dx[i];
					int adjy = curry + dy[i];
					if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height)
						continue;
					if (reveal[adjx][adjy])
						continue;
					if (array[adjx][adjy] == 0) {
						qx.add(adjx);
						qy.add(adjy);
					} else if (array[adjx][adjy] > 0) {
						reveal[adjx][adjy] = true;
					}
				}
			}
		} else {
			reveal[mousex][mousey] = true;
		}
		if (!revealed) {
			boolean won = true;
			o: for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (array[i][j] >= 0 && !reveal[i][j]) {
						won = false;
						break o;
					}
				}
			}
			if (won) {
				jf.repaint();
				gameOver = true;
				JOptionPane.showMessageDialog(this, "Game Over! You win!");
				System.exit(ABORT);
			}
		}
	}

	/**
	 * right click
	 * 
	 * @param mousex x coordinate of click
	 * @param mousey y coordinate of click
	 */
	public void rightClick(int mousex, int mousey) {
		if (revealed)
			return;
		flag[mousex][mousey] = !flag[mousex][mousey];
		if (flag[mousex][mousey])
			flags++;
		else
			flags--;
	}

	/**
	 * the computer solve the game
	 */
	public void auto() {
		LinkedList<Integer> qx = new LinkedList<Integer>();
		LinkedList<Integer> qy = new LinkedList<Integer>();
		boolean[][] visited = new boolean[width][height];
		if (start) {
			Random random = new Random();
			int initx = random.nextInt(width);
			int inity = random.nextInt(height);
			leftClick(initx, inity);
			qx.add(initx);
			qy.add(inity);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while (!qx.isEmpty()) {
			int currx = qx.remove();
			int curry = qy.remove();
			if (!reveal[currx][curry]) // if it is not revealed
				continue;
			if (flag[currx][curry]) // if it is flagged
				continue;
			if (visited[currx][curry])
				continue; // if we have already visited it
			int mines = array[currx][curry];
			if (mines == -1)
				revealAll(); // oops
			else if (mines == 0) {
				visited[currx][curry] = true;
				for (int i = 0; i < 8; i++) {
					int adjx = currx + dx[i];
					int adjy = curry + dy[i];
					if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height)
						continue;
					if (visited[adjx][adjy])
						continue;
					if (!reveal[adjx][adjy])
						leftClick(adjx, adjy);
					qx.add(adjx);
					qy.add(adjy);
				}
				jf.repaint();
			} else {
				int empty = 0;
				int numMines = 0;
				for (int i = 0; i < 8; i++) {
					int adjx = currx + dx[i];
					int adjy = curry + dy[i];
					if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height)
						continue;
					// not revealed, not flag = empty
					if (reveal[adjx][adjy])
						continue;
					if (!flag[adjx][adjy])
						empty++;
					else
						numMines++;
				}
				if (numMines == mines) {
					visited[currx][curry] = true;
					for (int i = 0; i < 8; i++) {
						int adjx = currx + dx[i];
						int adjy = curry + dy[i];
						if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height)
							continue;
						if (flag[adjx][adjy])
							continue;
						if (visited[adjx][adjy])
							continue;
						if (!reveal[adjx][adjy])
							leftClick(adjx, adjy);
						qx.add(adjx);
						qy.add(adjy);
					}
					jf.repaint();
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (empty == mines - numMines) {
					visited[currx][curry] = true;
					for (int i = 0; i < 8; i++) {
						int adjx = currx + dx[i];
						int adjy = curry + dy[i];
						if (adjx < 0 || adjy < 0 || adjx >= width || adjy >= height)
							continue;
						if (!reveal[adjx][adjy] && !flag[adjx][adjy]) {
							rightClick(adjx, adjy);
							for (int j = 0; j < 8; j++) {
								int nextx = adjx + dx[j];
								int nexty = adjy + dy[j];
								if (nextx < 0 || nexty < 0 || nextx >= width || nexty >= height)
									continue;
								if (flag[nextx][nexty])
									continue;
								if (visited[nextx][nexty])
									continue;
								qx.add(nextx);
								qy.add(nexty);
							}
						}
					}
					jf.repaint();
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Computer Done");
	}

	/**
	 * main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		game = new Minesweeper();
	}

	/**
	 * reveals all, when user loses
	 */
	public void revealAll() {
		revealed = true;
		for (int i = 0; i < width; i++)
			Arrays.fill(reveal[i], true);
		jf.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (revealed)
			return;
		boolean left = SwingUtilities.isLeftMouseButton(e);
		int mousex = (e.getX() - startx) / size;
		int mousey = (e.getY() - starty) / size;
		if (mousex < 0 || mousey < 0 || mousex >= width || mousey >= height)
			return;
		if (reveal[mousex][mousey])
			return;
		if (left) {
			leftClick(mousex, mousey);
		} else {
			rightClick(mousex, mousey);
		}
		jf.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
