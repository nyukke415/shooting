/*
  Shooting.java - shooting game
  Last modified: Fri 28 Aug 2015 04:06:06 JST
*/

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/*
  <applet code="Shooting.class" width="400" height="400"></applet>
*/

public class Shooting extends Applet implements KeyListener, Runnable {

  // [CONFIG] ========================================================
  public static final Color bgcolor = new Color(20, 20, 20);
  public static final int enemy_group_max = 8;
  // =================================================================

  public static Unit1 unit;
  public static Dimension size;
  public static int bullets_max = 20;
  public static Graphics buffer;
  int frame_count = 0;
  Enemy_group enemy_group[] = new Enemy_group[enemy_group_max];
  Thread thread = null;
  boolean shot_flag = false;
  Image back;
  static Bullet bullets[] = new Bullet[bullets_max];
  int bullets_wait = 0, bullets_wait_min = 3;

  public void init() {// init called func : applet
    size = getSize();
    back = createImage(size.width, size.height);// for double buffering
    buffer = back.getGraphics();// for double buffering
    // enemy grpup
    for (int i=0; i < enemy_group_max; i++)
      { enemy_group[i] = new Enemy_group(); }
    // bullets
    for (int i=0; i < bullets_max; i++) { bullets[i] = new Bullet(); }
    // [unit] ----------------------------------------------
    unit = new Unit1(0);
    // initial position
    unit.x = size.width/2;
    unit.y = size.height - 40;
    // -----------------------------------------------------
    // thread
    thread = new Thread(this);
    thread.start();
    // other
    addKeyListener(this);
  }

  public void paint(Graphics g) {// auto(init) & manual(@thread)
    draw_background();
    draw_enemies();
    unit.update();
    shot();
    draw_bullets();
    g.drawImage(back, 0, 0, this);
    requestFocusInWindow();
  }

  public void draw_background() {
    buffer.setColor(bgcolor);
    buffer.fillRect(0, 0, size.width, size.height);
  }

  public void draw_enemies() {
    // generate enemy_group[i].set(nmax, wait, type, x, y, dx, dy, vfact, dv)
    switch (frame_count) {
    case 100:
      set_enemy_group(8, 10, 0, 200, 0, 20, 0, 3, 0); break;
    case 200:
      set_enemy_group(8, 10, 0, 200, 0, -20, 0, 3, 0); break;
    case 350:
      set_enemy_group(6, 10, 0, 20, 0, 20, 0, 2, 0);
      set_enemy_group(6, 10, 0, size.width - 20, 0, -20, 0, 2, 0); break;
    case 450:
      set_enemy_group(6, 0, 0, 100, 0, 40, 0, 1, 1); break;
    }
    for (int i=0; i < enemy_group_max; i++) { enemy_group[i].update(); }
  }

  public void set_enemy_group(int nmax, int wait, int type,
                              int x, int y, int dx, int dy,
                              int vfact, int dv) {
    for (int i=0; i < enemy_group_max; i++) {
      if (!enemy_group[i].moveflag) {
        enemy_group[i].set(nmax, wait, type, x, y, dx, dy, vfact, dv);
        break;
      }
    }
  }

  public void draw_bullets() {
    if (bullets_wait < bullets_wait_min) { bullets_wait += 1; }
    buffer.setColor(bullets[0].color);
    for (int i=0; i < bullets_max; i++) {
      if (bullets[i].active) {
        Bullet b = bullets[i];
        b.update();
        // if (unit2_alive
        //     && bullets[i].x >= unit2_x - unit2_width/2
        //     && bullets[i].x <= unit2_x + unit2_width/2
        //     && bullets[i].y >= unit2_y - unit2_height/2
        //     && bullets[i].y <= unit2_y + unit2_height/2) {
        //   bullets[i].active = false;
        //   unit2_hp -= 1;
        //   if (unit2_hp <= 0) { unit2_alive = false; }
        // }
        if (b.y < 0 || b.x1 > size.width || b.y > size.height)
          { b.active = false; }
      }
    }
  }

  static void change_bullets(int r, int vx, int vy, int dx, int dy,
                             Color color) {
    for (int i=0; i < bullets_max; i++) {
      Bullet b = bullets[i];
      b.stat_color = color;
      b.stat_dx = dx;
      b.stat_dy = dy;
      b.stat_r = r;
      b.stat_vx = vx;
      b.stat_vy = vy;
    }
  }

  public void shot() {
    if (shot_flag) {
      if (bullets_wait == bullets_wait_min) {
        for (int i=0; i < bullets_max; i++) {
          if (!bullets[i].active) {
            Bullet b = bullets[i];
            b.active = true;
            b.x1 = unit.x; b.x2 = unit.x; b.y = unit.y - b.dy;
            b.vx = b.stat_vx; b.vy = b.stat_vy;
            b.color = b.stat_color;
            b.dx = b.stat_dx; b.dy = b.stat_dy;
            b.r = b.stat_r;
            bullets_wait = 0;
            break;
          }
        }
      }
    }
  }

  public void update(Graphics g) { paint(g); }

  // THREAD
  public void run() {
    while (true) {
      repaint();
      frame_count++;
      try { Thread.sleep(40); }
      catch (InterruptedException e) { }
    }
  }

  public void initialize() {
    // unit2_hp = unit2_hp_max;
    // unit2_alive = true;
  }

  // KEY CONTROL -----------------------------------------------------
  public void keyPressed(KeyEvent e){
    int keycode = e.getKeyCode();
    switch (keycode) {
    case KeyEvent.VK_SPACE: shot_flag = true; break;
    case KeyEvent.VK_LEFT: unit.vx -= 1; break;
    case KeyEvent.VK_RIGHT: unit.vx += 1; break;
    case KeyEvent.VK_UP: unit.vy -= 1; break;
    case KeyEvent.VK_DOWN: unit.vy += 1; break;
    case KeyEvent.VK_A: unit.type_change(0); break;
    case KeyEvent.VK_S: unit.type_change(1); break;
    case KeyEvent.VK_D: unit.type_change(2); break;
    case KeyEvent.VK_F: unit.type_change(3); break;
    case KeyEvent.VK_G: unit.type_change(4); break;
    case KeyEvent.VK_Z: shot_flag = !shot_flag; break;
      //case KeyEvent.VK_R: initialize(); break;
    }
  }

  public void keyReleased(KeyEvent e){
    int keycode = e.getKeyCode();
    switch (keycode) {
    case KeyEvent.VK_SPACE: shot_flag = false; break;
    case KeyEvent.VK_LEFT: unit.vx += 1; break;
    case KeyEvent.VK_RIGHT: unit.vx -= 1; break;
    case KeyEvent.VK_UP: unit.vy += 1; break;
    case KeyEvent.VK_DOWN: unit.vy -= 1; break;
      //case KeyEvent.VK_R: initialize(); break;
    }
  }
  public void keyTyped(KeyEvent e){
  }
  // -----------------------------------------------------------------

}

class Bullet {
  public int x1, x2, y, vx, vy, r, dx, dy;
  public boolean active;
  public Color color;
  static int stat_vx, stat_vy, stat_r, stat_dx, stat_dy;
  static Color stat_color;
  Bullet() {
    x1 = 0; x2 = 0; y = 0; active = false;
    vx = 0; vy = 10; r = 6; dx = 2; dy = Shooting.unit.height/2;
    color = new Color(140, 140, 250);
    stat_color = color;
    stat_vx = vx; stat_vy = vy; stat_r = r;
    stat_dx = dx; stat_dy = dy;
  }
  public void update() {
    x1 += vx; x2 -= vx; y -= vy;
    Shooting.buffer.fillOval(x1 - r/2 + dx, y - r/2, r, r);
    Shooting.buffer.fillOval(x2 - r/2 - dx, y - r/2, r, r);
  }
}

class Unit1 {
  public int x, y, vx, vy, life, type, vfact;
  public boolean alive;
  public Color color;
  static int width = 20, height = 20, life_max = 5;
  Unit1(int _type) {
    type = _type;
    x = 0; y = 0; vx = 0; vy = 0; vfact = 0;
    life = life_max;
    alive = true;
    type_change(type);
  }
  public void type_change(int newtype) {
    type = newtype;
    switch (type) {
    case 0:
      color = new Color(130,130,250);
      Shooting.change_bullets(8, 0, 40, 5, Shooting.unit.height/2,
                              new Color(130,130,250));
      vfact = 5;
      break;
    case 1:
      color = new Color(250,130,130);
      Shooting.change_bullets(8, 0, 40, 5, Shooting.unit.height/2,
                              new Color(250,130,130));
      vfact = 10;
      break;
    case 2:
      color = new Color(130,250,130);
      Shooting.change_bullets(12, 12, 40, 5, Shooting.unit.height/2,
                              new Color(130,250,130));
      vfact = 5;
      break;
    case 3:
      color = new Color(250,80,250);
      Shooting.change_bullets(8, 0, -40, 5, -Shooting.unit.height/2,
                              new Color(250,80,250));
      vfact = 5;
      break;
    case 4:
      color = new Color(250,250,80);
      Shooting.change_bullets(30, 40, 0, 15, 0, new Color(250,250,80));
      vfact = 5;
      break;
    }
  }
  public void move() {
    int vxf = vx*vfact, vyf = vy*vfact;
    if ((x + vxf > width/2) && (x + vxf < Shooting.size.width - width/2))
      { x += vxf; }
    if ((y + vyf > height/2) && (y + vyf < Shooting.size.height - height/2))
      { y += vyf; }
  }
  public void draw() {
    Shooting.buffer.setColor(color);
    Shooting.buffer.fillRect(x - width/2, y - height/2, width, height);
  }
  public void update() { if (alive) { move(); draw(); } }
}

class Enemy {
  public int x, y, vx, vy, width, height, type, parity, vfact;
  public boolean alive;
  public Color color;
  Enemy() {
    x = 0; y = 0; type = 0; parity = 0; width = 0; height = 0;
    alive = false;
  }
  public void on(int _type, int _x, int _y, int _vfact) {
    alive = true;
    x = _x; y = _y;
    type = _type;
    vfact = _vfact;
    parity = _type >= 0 ? 1 : -1;
    switch (type) {
    case 0: vx = 0; vy = 1*vfact; width = 10; height = 10;
      color = new Color(50,50,200); break;
    }
  }
  public boolean update() {
    boolean res = true;
    if (alive) {
      x += vx*parity; y += vy;
      if (!alive || x < 0 || x > Shooting.size.width ||
          y > Shooting.size.height || y < 0) { res = false; } else {
        Shooting.buffer.setColor(color);
        Shooting.buffer.fillRect(x - width/2, y - height/2, width, height);
        Shooting.buffer.fillRect(x - width/2, y - height/2, width, height);
      }
    } else { res = false; }
    alive = res;
    return alive;
  }
}

class Enemy_group {
  int x, y, dx, dy, type, nmax, wait, t, idx, dv, vfact;
  public boolean flag, moveflag;
  Enemy[] enemies;
  Enemy_group() {
    flag = false; moveflag = false;
  }
  public void set(int _nmax, int _wait,
                  int _type, int _x, int _y,
                  int _dx, int _dy, int _vfact, int _dv) {
    x = _x; y = _y; type = _type; nmax = _nmax; vfact = _vfact;
    dx = _dx; dy = _dy; dv = _dv;
    wait = _wait; idx = 0;
    enemies = new Enemy[nmax];
    for (int i=0; i < nmax; i++) {
      enemies[i] = new Enemy();
    }
    flag = true; moveflag = true;
  }
  public void update() {
    boolean c = false;
    if (flag) {
      if (t == wait) {
        t = 0;
        if (wait == 0) {
          for (int i=idx; i < nmax; i++) {
            enemies[i].on(type, x+dx*i, y+dy*i, vfact + dv*i);
          }
          idx = nmax - 1;
        } else {
          enemies[idx].on(type, x+dx*idx, y+dy*idx, vfact + dv*idx);
        }
        if (idx == nmax - 1) { flag = false; }
        idx++;
      }
      t++;
    }
    if (moveflag) {
      for (int i=0; i < nmax; i++) {
        c = enemies[i].update() || c;
      }
      if (idx == nmax) { moveflag = c; }
      if (!moveflag) { idx = 0; t = 0; }
    }
  }
}
