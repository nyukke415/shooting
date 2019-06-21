//last updated:<2015/08/28/Fri 15:22:48 from:YUSUKE-PC>

// SampleApplet.javaでappletviewerを実行出来るようにする700 650
// <applet code="SampleApplet.class" width="500" height="400"></applet>

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class SampleApplet extends Applet implements Runnable, KeyListener {
	// ゲームの設定 ================================
	public static final int bullets_max = 10; // 同時に発射できる弾の数
  public static final int bullet_wait_min = 2;
	// その他の設定 ===============================
	public static final int sidebar_width = 200; // sidebar の幅
	public static Graphics gv;	// buffer用
	public static Dimension applet_size;
	private Image offImage;		// buffer用イメージ
	static boolean my_shot_flag = false;
	static Unit my_unit;
	static Bullet my_bullets[] = new Bullet[bullets_max];
	static int my_bullets_cnt = 0; // 画面上に発射されている弾数

	Thread gameThread = null;	// スレッドを入れる変数
  public int bullet_wait = 0;

	// ===========================================

	public void init() {		// アプレット実行時に勝手に実行される
		applet_size = getSize(); // アプレットのサイズを取得してapplet_sizeに代入
		my_unit = new Unit();
		for (int i = 0; i < bullets_max; i++) {
			my_bullets[i] = new Bullet();
		}
		// ダブルバッファリング
		offImage = createImage(applet_size.width, applet_size.height);
		gv = offImage.getGraphics();
		// スレッド関連
		gameThread = new Thread(this);
		gameThread.start();
		// キー入力を待つ
		addKeyListener(this);
	}

	public void paint(Graphics g) { // 描画全般を行う関数
		draw_background();
		my_unit.update();
		shot_bullet();
		draw_bullet();
		g.drawImage(offImage, 0, 0, this); // bufferの内容を描画
		requestFocusInWindow();	// 自動でappletviewerにfocusさせる
	}

	public void draw_background() {		   // 背景を描画する関数
		gv.setColor(new Color(0, 250, 0)); // RGB
		gv.fillRect(0, 0, applet_size.width, applet_size.height);
		gv.setColor(new Color(100, 250, 100));
		gv.fillRect(applet_size.width - sidebar_width, 0,
					applet_size.height, applet_size.height);
	}
	public void shot_bullet() {	// 弾を発射する関数
		if (my_shot_flag == true) {
      if (bullet_wait == bullet_wait_min) {
        for (int i = 0; i < bullets_max; i++) {
          if (my_bullets[i].active == false) {
            my_bullets[i].active = true;
            my_bullets[i].x = my_unit.x;
            my_bullets[i].y = my_unit.y;
            my_bullets_cnt += 1;
            break;
          }
        }
        bullet_wait = 0;
      }
      bullet_wait++;
		}
	}
	public void draw_bullet() {
		// 画面上に弾が一つでもあれば
		if (SampleApplet.my_bullets_cnt != 0) {
			for (int i = 0; i < bullets_max; i++) {
				if (SampleApplet.my_bullets[i].active == true) {
					my_bullets[i].update();
				}
			}
		}
	}

	public void run() {
		while (true) {
			repaint();
			try { Thread.sleep(30); }
			catch (InterruptedException e) {}
		}
	}

	public void update(Graphics g) { // 軌跡を表示するために再定義
		paint(g);
	}

	public void keyPressed(KeyEvent e) { // キーを押した時用の関数
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE: my_shot_flag = true; break;
		case KeyEvent.VK_RIGHT: my_unit.vx = 1; break;
		case KeyEvent.VK_LEFT:  my_unit.vx = -1; break;
		case KeyEvent.VK_UP:    my_unit.vy = -1; break;
		case KeyEvent.VK_DOWN:  my_unit.vy = 1; break;
		}
	}

	public void keyReleased(KeyEvent e) { // キーを離した時用の関数
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE: my_shot_flag = false; break;
		case KeyEvent.VK_RIGHT: my_unit.vx = 0; break;
		case KeyEvent.VK_LEFT:  my_unit.vx = 0; break;
		case KeyEvent.VK_UP:    my_unit.vy = 0; break;
		case KeyEvent.VK_DOWN:  my_unit.vy = 0; break;
		}
	}

	public void keyTyped(KeyEvent e) {
		// キーを入力した時用の関数（中身は空）
	}
}

// class ================================
class Unit {
	public int vx_m = 6, vy_m = 6;
	public int x, y, vx, vy, life;
	public boolean alive;
	public Color color = new Color(100, 100, 100);
	static int width = 20, height = 20, life_max = 5;
	Unit () {					// はじめに一回だけ実行される
		color = new Color(100, 100, 200);
		alive = true;
		x = SampleApplet.applet_size.width/2;
		y = SampleApplet.applet_size.height/2;
	}
	public void paint() {
		SampleApplet.gv.setColor(color);
		SampleApplet.gv.fillRect(x - width/2, y - height/2,
								 width, height);
	}
	public void move() {
		if ((width/2) <= (x + vx_m * vx)
			&& (x + vx_m * vx)
			<= (SampleApplet.applet_size.width - width/2
				- SampleApplet.sidebar_width )) {
			x += vx_m * vx;
		}
		if ((height/2) <= (y + vy_m * vy)
			&& (y + vy_m * vy)
			<= (SampleApplet.applet_size.height - height/2)) {
			y += vy_m * vy;
		}
	}
	public void update() {
		if (alive == true) {
			paint();
			move();
		}
	}
}

class Bullet {
	public int vxf = 0, vyf = 24, r = 8;
	public int x = 0, y = 0;
	public boolean active = false;
	public Color color = new Color(250, 0, 0);
	public void update() {
		SampleApplet.gv.setColor(color);
    if (active == true) {
      y -= vyf;
      // 発射されている弾を描画する
      SampleApplet.gv.fillOval(x - r/2, y - r/2, r, r);
			// 画面から出ている弾を回収する
			if (((-1) * r/2 > y)) {
        active = false;
				SampleApplet.my_bullets_cnt--;
			}
    }
  }
}
