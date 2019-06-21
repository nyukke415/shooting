//last updated:<2015/08/28/Fri 15:22:48 from:YUSUKE-PC>

// SampleApplet.java��appletviewer�����s�o����悤�ɂ���700 650
// <applet code="SampleApplet.class" width="500" height="400"></applet>

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class SampleApplet extends Applet implements Runnable, KeyListener {
	// �Q�[���̐ݒ� ================================
	public static final int bullets_max = 10; // �����ɔ��˂ł���e�̐�
  public static final int bullet_wait_min = 2;
	// ���̑��̐ݒ� ===============================
	public static final int sidebar_width = 200; // sidebar �̕�
	public static Graphics gv;	// buffer�p
	public static Dimension applet_size;
	private Image offImage;		// buffer�p�C���[�W
	static boolean my_shot_flag = false;
	static Unit my_unit;
	static Bullet my_bullets[] = new Bullet[bullets_max];
	static int my_bullets_cnt = 0; // ��ʏ�ɔ��˂���Ă���e��

	Thread gameThread = null;	// �X���b�h������ϐ�
  public int bullet_wait = 0;

	// ===========================================

	public void init() {		// �A�v���b�g���s���ɏ���Ɏ��s�����
		applet_size = getSize(); // �A�v���b�g�̃T�C�Y���擾����applet_size�ɑ��
		my_unit = new Unit();
		for (int i = 0; i < bullets_max; i++) {
			my_bullets[i] = new Bullet();
		}
		// �_�u���o�b�t�@�����O
		offImage = createImage(applet_size.width, applet_size.height);
		gv = offImage.getGraphics();
		// �X���b�h�֘A
		gameThread = new Thread(this);
		gameThread.start();
		// �L�[���͂�҂�
		addKeyListener(this);
	}

	public void paint(Graphics g) { // �`��S�ʂ��s���֐�
		draw_background();
		my_unit.update();
		shot_bullet();
		draw_bullet();
		g.drawImage(offImage, 0, 0, this); // buffer�̓��e��`��
		requestFocusInWindow();	// ������appletviewer��focus������
	}

	public void draw_background() {		   // �w�i��`�悷��֐�
		gv.setColor(new Color(0, 250, 0)); // RGB
		gv.fillRect(0, 0, applet_size.width, applet_size.height);
		gv.setColor(new Color(100, 250, 100));
		gv.fillRect(applet_size.width - sidebar_width, 0,
					applet_size.height, applet_size.height);
	}
	public void shot_bullet() {	// �e�𔭎˂���֐�
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
		// ��ʏ�ɒe����ł������
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

	public void update(Graphics g) { // �O�Ղ�\�����邽�߂ɍĒ�`
		paint(g);
	}

	public void keyPressed(KeyEvent e) { // �L�[�����������p�̊֐�
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE: my_shot_flag = true; break;
		case KeyEvent.VK_RIGHT: my_unit.vx = 1; break;
		case KeyEvent.VK_LEFT:  my_unit.vx = -1; break;
		case KeyEvent.VK_UP:    my_unit.vy = -1; break;
		case KeyEvent.VK_DOWN:  my_unit.vy = 1; break;
		}
	}

	public void keyReleased(KeyEvent e) { // �L�[�𗣂������p�̊֐�
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE: my_shot_flag = false; break;
		case KeyEvent.VK_RIGHT: my_unit.vx = 0; break;
		case KeyEvent.VK_LEFT:  my_unit.vx = 0; break;
		case KeyEvent.VK_UP:    my_unit.vy = 0; break;
		case KeyEvent.VK_DOWN:  my_unit.vy = 0; break;
		}
	}

	public void keyTyped(KeyEvent e) {
		// �L�[����͂������p�̊֐��i���g�͋�j
	}
}

// class ================================
class Unit {
	public int vx_m = 6, vy_m = 6;
	public int x, y, vx, vy, life;
	public boolean alive;
	public Color color = new Color(100, 100, 100);
	static int width = 20, height = 20, life_max = 5;
	Unit () {					// �͂��߂Ɉ�񂾂����s�����
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
      // ���˂���Ă���e��`�悷��
      SampleApplet.gv.fillOval(x - r/2, y - r/2, r, r);
			// ��ʂ���o�Ă���e���������
			if (((-1) * r/2 > y)) {
        active = false;
				SampleApplet.my_bullets_cnt--;
			}
    }
  }
}
