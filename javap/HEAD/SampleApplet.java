// last updated:<2016/10/01/Sat 22:59:20 from:yusuke-FMVS76G>
// SampleApplet.javaでappletviewerを実行出来るようにする
// <applet code="SampleApplet.class" width="700" height="650"></applet>

// javac -encoding utf-8 SampleApplet.java

import java.applet.Applet;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Random;

public class SampleApplet extends Applet implements Runnable, KeyListener {
    // error check ================================
    // SampleApplet.error_check+;
    public static int error_check = 0;

    // ゲームの設定 ================================
    public static final int bullet_max = 300; // 同時に発射できるユニットの弾数
    public static final int group_max = 10;	// 同時に表示できる敵のグループ数
    public static final int e_bullet_max = 1500;	// 同時に表示できる敵の弾
    public static final int invincible_time = 80; // ユニットが死んだ時の無敵時間
    public static int frame = 0;             // 全体の同期をとる
    public static final int enemy_max = 100; // 同時に表示できる敵の数

    // その他の設定 ===============================
    public static final int sidebar_width = 200; // sidebar の幅
    public static final int star_max = 35; // 画面に表示できる星の数
    public static Graphics gv;             // buffer用
    public static FontMetrics fm; // 文字の位置を調節する
    public static boolean game_flag = false;
    public static boolean end_flag = false;
    public static int field_width, field_height; // フィールドの幅と高さ
    public static Dimension applet_size; // 画面の縦と横の大きさ
    public static int score = 0, score_c = 0; // スコア関連
    private Image offImage;		// buffer用イメージ
    public int strWidth, strHeight;
    public static int bullet_wait = 0; // ユニットの弾の発射間隔を数える
    public String message;     // タイトル画面の表示する文字
    public int select = 1;     // タイトル画面のモードセレクト
    public int ripple = 0;     // タイトルに波紋を表示させる
    public static int dif = 0; // 難易度
    public static int gradually = 0, R = 200, G = 0, B = 0;	// タイトル画面の背景の色
    static boolean my_shot_flag = true;
    static Star star_f[] = new Star[star_max];
    static Star star_b[] = new Star[star_max];
    static Unit my_unit = new Unit();
    static Bullet my_bullet[] = new Bullet[bullet_max];
    static Boss boss = new Boss();
    static Group group[] = new Group[group_max];
    static Enemy enemy[] = new Enemy[enemy_max];
    static Ebullet e_bullet[] = new Ebullet[e_bullet_max];
    static Random rand = new Random();	// 乱数用
    Thread gameThread = null;	// スレッドを入れる変数

    // ===========================================

    public void init() {		// アプレット実行時に自動実行される
        applet_size = getSize();
        field_width = applet_size.width - sidebar_width;
        field_height = applet_size.height;
        for (int i = 0; i < bullet_max; i++) { my_bullet[i] = new Bullet();	}
        for (int i = 0; i < enemy_max; i++) { enemy[i] = new Enemy(); }
        for (int i = 0; i < group_max; i++) { group[i] = new Group(); }
        for (int i = 0; i < e_bullet_max; i++) { e_bullet[i] = new Ebullet(); }
        for (int i = 0; i < star_max; i++) {
            star_f[i] = new Star(); star_b[i] = new Star();
            Star sf = star_f[i], sb = star_b[i];
            sf.star_vf = 10; sb.star_vf = 7;
            sf.y = (i%7)*(field_height/7) + 10 + rand.nextInt(80);
            sb.y = (i%7)*(field_height/7) + 10 + rand.nextInt(80);
            sf.x = (i%5)*(field_width/5) + 10 + rand.nextInt(80);
            sb.x = (i%5)*(field_width/5) + 10 + rand.nextInt(80);
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

    public void paint(Graphics g) { // 描画全般を行う関数 (最後に書いたものが一番上に表示される)
        if (game_flag) {
            background();
            my_unit.update();
            draw_bullet();
            appear();
            for (int i = 0; i < group_max; i++) { group[i].update(); }
            for (int i = 0; i < enemy_max; i++) { enemy[i].update(); }
            for (int i = 0; i < e_bullet_max; i++) { e_bullet[i].update(); }
            if (Boss.active) { Boss.update(); }
            sidebar();
            if (end_flag) { display(); }
        } else {
            title();
        }
        g.drawImage(offImage, 0, 0, this); // bufferの内容を描画
        requestFocusInWindow();	// 自動でappletviewerにfocusさせる

    }

    // group(type, property, num, wait, dx, dy, x, y, behavior, b_property)
    // type 0:右  1:下  2:右下 [enemy]
    // behavior 0:真下  1:右下  2:左下 [bullet]
    public void appear() {	// 敵を出現させる関数
        switch (frame) {
        case 50:
            group[available("g")].group(1,3, 6,0, 80,0, 60,0, 0,6);
            group[available("g")].group(2,4, 5,60, 0,20, 0,0, 2,3);
            break;
        case 150:
            group[available("g")].group(0,3, 5,70, 0,0, 0,250, 1,3);
            group[available("g")].group(1,3, 5,0, 80,0, 100,0, 0,6);
            break;
        case 300:
            group[available("g")].group(2,5, 6,50, 0,20, 0,0, 0,6);
            group[available("g")].group(0,-3, 5,50, 0,0, 0,250, 2,3);
            break;
        case 350:
            group[available("g")].group(0,-3, 5,50, 0,0, 0,250, 1,3);
            group[available("g")].group(2,3, 5,40, 0,20, 0,0, 0,6);
            break;
        }
        if (frame == 700 && !end_flag) { Boss.active = true; } // Bossを出現させる
        frame++;
    }

    public void draw_bullet() {	// ユニットの弾を表示する
        for (int i = 0; i < bullet_max; i++) {
            if (my_bullet[i].active) { my_bullet[i].update(); }
        }
        if (my_shot_flag && !end_flag) {
            if (bullet_wait >= Bullet.bullet_wait_min && !my_unit.lost_flag) {
                for (int i = 1; i <= Unit.power*2-1; i++) {
                    my_bullet[available("b")].shot_bullet(i);
                }
            }
        }
        bullet_wait++;
    }

    public int available(String s) {
        switch (s) {
        case "g":
            for (int i = 0; i < group_max; i++) {
                if (!group[i].active) { return i; }
            }
        case "b":
            for (int i = 0; i < bullet_max; i++) {
                if (!my_bullet[i].active) { return i; }
            }
        }
        return -1;
    }

    public void title () {
        if (gradually == 0) { G++; if (G == 200) { gradually++; } }
        else if (gradually == 1) { R--; if (R == 0) { gradually++; } }
        else if (gradually == 2) { B++; if (B == 200) { gradually++; } }
        else if (gradually == 3) { G--; if (G == 0) { gradually++; } }
        else if (gradually == 4) { R++; if (R == 200) { gradually++; } }
        else { B--; if (B == 0) { gradually = 0;} }
        gv.setColor (new Color(20, 20, 20));
        gv.fillRect (0, 0, applet_size.width, applet_size.height); // 背景を描画
        if (1000 <= ripple ) { ripple = 0; }
        gv.setColor (new Color(R, G, B ));
        int x = applet_size.width/2 - ripple, y = applet_size.height/2 - ripple;
        gv.drawOval(x + 80, y + 80, 2*ripple - 160, 2*ripple - 160);
        gv.drawOval(x + 160, y + 160, 2*ripple - 320, 2*ripple - 320);
        gv.drawOval(x, y, 2*ripple, 2*ripple);
        ripple += 7;
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 70));
        gv.setColor (new Color(200, 200, 200));
        message = "タイトル募集中";
        fm = gv.getFontMetrics(); strWidth = fm.stringWidth(message);
        gv.drawString(message,(applet_size.width - strWidth)/2, 130);
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 50));
        gv.setColor (new Color(160, 160, 160));
        if (select == 1) {	message = "EASY"; }
        else if (select == 2) {	message = "NORMAL"; }
        else if (select == 3) { message = "HARD"; }
        fm = gv.getFontMetrics(); strWidth = fm.stringWidth(message);
        gv.drawString(message, (applet_size.width - strWidth)/2, 330);
        message = "△";
        fm = gv.getFontMetrics(); strWidth = fm.stringWidth(message);
        gv.drawString(message, (applet_size.width - strWidth)/2, 270);
        gv.drawString("▽", (applet_size.width - strWidth)/2, 390);
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 35));
        message = "PRESS ENTER";
        fm = gv.getFontMetrics(); strWidth = fm.stringWidth(message);
        gv.drawString(message, (applet_size.width - strWidth)/2, 450);
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 20));
        message = "##   操作説明   ##";
        fm = gv.getFontMetrics(); strWidth = fm.stringWidth(message);
        gv.drawString(message, (applet_size.width - strWidth)/2, applet_size.height - 95);
        gv.drawString("Cursor          移動", 100, applet_size.height - 70);
        gv.drawString("Shift           押している間、低速移動", 100, applet_size.height - 45);
        gv.drawString("Z               ボム", 100, applet_size.height - 20);
    }

    public void display () {
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 60));
        gv.setColor (new Color(255, 0, 0));
        fm = gv.getFontMetrics();
        if (Unit.alive) { message = "Game Clear!!"; }
        else { message = "Game Over"; }
        fm = gv.getFontMetrics();
        strWidth = fm.stringWidth(message);
        gv.drawString(message, (field_width - strWidth)/2, 200);
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 40));
        fm = gv.getFontMetrics();
        message = "Score";
        strWidth = fm.stringWidth(message);
        gv.drawString(message, (field_width - strWidth)/2, 350);
        message = Integer.toString(score);
        strWidth = fm.stringWidth(message);
        gv.drawString(message, (field_width - strWidth)/2, 400);
    }

    public void background() {		   // 背景を描画する関数
        gv.setColor(new Color(16, 16, 16)); // RGB
        gv.fillRect(0, 0, field_width, field_height);
        if (Unit.bomb_r <  2 * field_height + 100) { // ユニットがボムを使った時
            int r = Unit.bomb_r/2;
            gv.setColor(new Color(0, 255, 255));
            gv.drawOval(Unit.bomb_x - r, Unit.bomb_y - r, 2*r, 2*r);
            gv.drawOval(Unit.bomb_x - r - 20, Unit.bomb_y - r - 20, 2*r + 40, 2*r + 40);
            gv.drawOval(Unit.bomb_x - r - 40, Unit.bomb_y - r - 40, 2*r + 80, 2*r + 80);
            Unit.bomb_r += 120;
        }
        SampleApplet.gv.setColor(new Color(255, 255, 0, 130));
        SampleApplet.gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 9));
        for (int i = 0; i < star_max; i++) { star_f[i].update(); }
        SampleApplet.gv.setColor(new Color(255, 255, 0, 120));
        SampleApplet.gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 8));
        for (int i = 0; i < star_max; i++) { star_b[i].update(); }
        if ( 0 < Unit.effect ) {	   // ユニットが死んだ時のエフェクト
            gv.setColor(new Color(230, 230, 230, Unit.effect));
            gv.fillRect(0, 0, applet_size.width, applet_size.height);
            Unit.effect -= 20;
        }
    }

    public void sidebar () { // サイドバーを描画する関数
        gv.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 18));
        gv.setColor(new Color(100, 100, 100));
        gv.fillRect(field_width, 0,	applet_size.width, applet_size.height);
        gv.setColor(new Color(200, 200, 200));
        gv.fillRect(field_width, 0,	applet_size.width, (int)(applet_size.height*((boss.life_m - boss.life)/boss.life_m)));
        gv.setColor(new Color(0, 0, 0));
        gv.drawString("Score :"+score+"",field_width+5, 40);
        gv.drawString("Player:"+my_unit.life+"",field_width+5 ,80);
        gv.drawString("Power :"+Unit.power+"",field_width+5 ,120);
        gv.drawString("Bomb  :"+Unit.bomb+"",field_width+5 ,160);
        // 以下エラー確認用
        // gv.drawString("FRAME :"+frame+"",field_width+10 ,field_height - 20);
    }

    public void run() {			// スレッドに実行される関数
        while (true) {
            repaint();
            if (game_flag && !end_flag && Unit.alive) { score++; }
            try { Thread.sleep(30); }
            catch (InterruptedException e) {}
        }
    }

    public void update(Graphics g) { // 軌跡を表示するために再定義
        paint(g);
    }

    public void reset() {
        frame = 0; score = 0; score_c = 0;
        end_flag = false;
        my_shot_flag = true;
        Unit.reset();
        Bullet.reset();
        Group.reset();
        Enemy.reset();
        Ebullet.reset();
        Boss.reset();
    }

    public void keyPressed(KeyEvent e) { // キーを押した時用の関数
        switch (e.getKeyCode()) {
        case KeyEvent.VK_SHIFT:	my_unit.slow = true; break;
        case KeyEvent.VK_ESCAPE: game_flag = false; break;
        case KeyEvent.VK_SPACE:
            if (my_shot_flag) { my_shot_flag = false; }
            else { if (my_unit.alive) { my_shot_flag = true; } }
            break;
        case KeyEvent.VK_Z: if (game_flag && 0 < Unit.bomb && !Unit.invincible) { Unit.bomb(); } break;
        case KeyEvent.VK_RIGHT:	my_unit.vx++; if(my_unit.vx > 1) { my_unit.vx = 1; } break;
        case KeyEvent.VK_LEFT:	my_unit.vx--; if(my_unit.vx < -1) { my_unit.vx = -1; } break;
        case KeyEvent.VK_UP:
            if (game_flag) { my_unit.vy--; if (my_unit.vy < -1) { my_unit.vy = -1; } }
            else { select++; if (select == 4) { select = 1; } }
            break;
        case KeyEvent.VK_DOWN:
            if (game_flag) { my_unit.vy++; if (my_unit.vy > 1) { my_unit.vy = 1; } }
            else { select--; if (select == 0) { select = 3; } }
            break;
        case KeyEvent.VK_ENTER:
            if (1 <= select && select <= 3 && !game_flag) {	dif = select; reset(); game_flag = true; }
            break;
        }
    }

    public void keyReleased(KeyEvent e) { // キーを離した時用の関数
        switch (e.getKeyCode()) {
        case KeyEvent.VK_RIGHT: my_unit.vx -= 1; break;
        case KeyEvent.VK_LEFT:  my_unit.vx += 1; break;
        case KeyEvent.VK_UP:    my_unit.vy += 1; break;
        case KeyEvent.VK_DOWN:  my_unit.vy -= 1; break;
        case KeyEvent.VK_SHIFT: my_unit.slow = false; break;
        }
    }

    public void keyTyped(KeyEvent e) {
        // キーを入力した時用の関数（中身は空）
    }
}
// class ================================
class Unit {
    public static int life_m = 3;			// 初期の残機数
    public static int power_m = 5; // ユニットのパワーの最大値
    public static int vx_m = 12, vy_m = 12; // ユニットの速度
    public static int vx_s = 5, vy_s = 5;	 // 低速移動時の速度
    public static int vx = 0, vy = 0;		 // ユニットの進む向き(-1, 0, 1)
    public static int width = 25, height = 25, life;
    public static int x, y;
    public static int p_r = 4;								 // ユニットの中心の点の大きさ
    public static boolean alive, invincible, slow, lost_flag;
    public static int bomb = 0;			// 残りボム
    public static int bomb_power = 15; // ボムの攻撃力
    public static int effect;			// ボムのエフェクトの時間を数える
    public static int bomb_x, bomb_y, bomb_r;			// ボムのエフェクト用
    public static int power = 1, power_cnt = 0; // パワーと敵を倒した数を数える
    public static int invincible_cnt = 0;	   // 無敵時間をカウント(ms)
    public Color color = new Color(70, 190, 80); // ユニットの色
    public Color i_color = new Color(70, 190, 80, 99); // 無敵時の色
    public Color p_color = new Color(255, 255, 255); // 当たり判定の点の色
    Unit () { // はじめに一回だけ実行される
        reset();
    }

    public void update() {
        if (life == 0) {
            alive = false;
            SampleApplet.my_shot_flag = false;
            SampleApplet.end_flag = true;
        }
        if (alive) {
            if (invincible) { invincible(); }
            if ( power*power*50 <= power_cnt ) { // ユニットのパワーをあげる
                if (power <= power_m - 1) {
                    power++;
                    power_cnt = 0;
                }
            }
            paint();
            move();
        }
    }

    public void paint() {
        if (invincible) { SampleApplet.gv.setColor(i_color); }
        else { SampleApplet.gv.setColor(color); }
        SampleApplet.gv.fillRect(x - width/2, y - height/2, width, height);
        SampleApplet.gv.setColor(p_color);
        SampleApplet.gv.fillOval(x - p_r/2, y - p_r/2 , p_r, p_r);
    }

    public void move() {
        // ユニットに低速移動をさせるかどうか
        if (!slow) { x += vx_m * vx; y += vy_m * vy; }
        else { x += vx_s * vx; y += vy_s * vy; }
        // ユニットが画面の端ギリギリまで行けるように調整する
        if (x < 0) {x = 0;}
        if (SampleApplet.field_width < x) {
            x = SampleApplet.field_width;
        }
        if (y < 0) {y = 0;}
        if (SampleApplet.field_height < y) {
            y = SampleApplet.field_height;
        }
    }

    public void invincible () {
        invincible_cnt--;
        if (invincible_cnt == 0) {
            if (!SampleApplet.end_flag) { invincible = false; } // ゲームクリア後にダメージを受けるのを回避
            lost_flag = false;
        }
    }

    static void bomb () {		// ボム
        bomb--;
        invincible_cnt = SampleApplet.invincible_time;
        invincible = true;
        bomb_x = x; bomb_y = y;
        bomb_r = 0;
        for (int i = 0; i < SampleApplet.e_bullet_max; i++) { // 画面上の敵の弾を全て消す
            if (SampleApplet.e_bullet[i].active) {
                SampleApplet.e_bullet[i].active = false;
                SampleApplet.score += 100;
            }
        }
        for (int i = 0; i < SampleApplet.enemy_max; i++) { // アクティブな敵を攻撃する
            if (SampleApplet.enemy[i].active) {
                SampleApplet.enemy[i].life -= bomb_power ;
                power_cnt += 10;
            }
        }
        if (Boss.active) {		// ボスがアクティブだったら攻撃する
            Boss.life -= bomb_power;
            power_cnt += 100;
        }
    }

    static void unit_lost (int _invincible_time) { // ユニットがやられた時の関数
        life--;
        invincible_cnt = _invincible_time;
        lost_flag = true;
        invincible = true;
        effect = 255;
        x = SampleApplet.field_width/2;
        y = SampleApplet.field_height - height;
        Bullet.reset();
        Group.reset();
        Enemy.reset();
        Ebullet.reset();
        power_cnt = 0;
        SampleApplet.score_c = 0;
        SampleApplet.score -= 10000;
        bomb = 4 - SampleApplet.dif;
        if (1 < power) { power--; }
    }

    public static void reset () {
        life = life_m;
        alive = true;
        invincible = false;
        slow = false;
        lost_flag = false;
        power = 1;
        bomb = 4 - SampleApplet.dif;
        x = SampleApplet.field_width/2;
        y = SampleApplet.field_height;
        vx = 0; vy = 0;
        invincible_cnt = 0;
        width = 25; height = 25;
        power_cnt = 0;
        SampleApplet.bullet_wait = 0;
        effect = 0;
        bomb_x = 0; bomb_y = 0;
        bomb_r = 2 * SampleApplet.field_height + 100;
    }

}

class Bullet {
    public static final int bullet_wait_min = 2; // ユニットの弾の発射間隔 2
    public final int vxf = 0, vyf = 24, r = 8; // 0, 24, 8
    public int dvx = 1;
    public int vx = 0, vy = 0;
    public int x = 0, y = 0;
    public boolean active = false;
    public boolean bullet_check; // ユニットか敵の弾かを判定する (ユニット:true,  敵:false)
    public Color color = new Color(75, 255, 70, 100);
    public void shot_bullet(int power) {	// ユニットの弾を発射する関数(SampleApplet から呼ばれる)
        active = true;
        if (power % 2 == 1) { vx = vxf + (power/2)*dvx; }
        if (power % 2 == 0) { vx = (-1) * (vxf + (power/2)*dvx); }
        vy = vyf;
        x = SampleApplet.my_unit.x;
        y = SampleApplet.my_unit.y - SampleApplet.my_unit.height/2;
        SampleApplet.bullet_wait = 0;
    }

    public void update() {
        SampleApplet.gv.setColor(color);
        // ユニットの発射されている弾を動かして描画する
        if (active) {
            x += vx;
            y -= vy;
            SampleApplet.gv.fillOval(x - r/2, y - r/2, r, r);
            active = Utils.check_f(x, y, r, r);
            if (!Utils.check_b(x, y)) { // 弾が敵にあたっていたら
                active = false;
                SampleApplet.score += (SampleApplet.score_c);
                SampleApplet.score_c += SampleApplet.dif;
                Unit.power_cnt++;
            }
        }
    }

    public static void reset () {
        for (int i = 0; i < SampleApplet.bullet_max; i++) {
            SampleApplet.my_bullet[i].active = false;
        }
    }

}

class Boss {
    public static double life, life_m = 3000; // ボスの体力
    public static int width,  width_m = 100;
    public static int height, height_m = 100;
    public static int shot_wait_m = 40; // 敵の弾を発射する間隔(easy)
    public static int m_time = 30;		// めり込み許容時間
    public static int m_time_cnt;		// めり込み時間をカウント
    public static double x, y;
    public static double vx, vy;
    public static boolean oko_flag, geki_oko_flag;
    public static boolean active;        // ボスが生きているかどうか
    public static int shot_wait;		 // 弾の発射間隔を数える
    public static int frame;			 // ボスの同期をとる
    public static int check;			 // ボスがどこの壁にあたったかを判定
    public static int big = 0, small = 1;		 // ボスの大きさを変える変数
    public static int size_max = 150, size_min = 25; // ボスの最大、最小サイズ

    Boss () {
        reset ();
    }

    public static void update() {
        pattern();
        move();
        check();
        draw();
        if (shot_wait >= shot_wait_m / SampleApplet.dif) {attack(); shot_wait = 0;}
        shot_wait++;
        if (0 < m_time_cnt) { m_time_cnt--; }
    }

    static void pattern () {
        if (frame == 0) { vx = 0; vy = 7; }
        if (frame == 20) { vx = 5; vy = 5; }
        if (geki_oko_flag) { // ボスの大きさを変える
            width += (big - small);
            height += (big - small);
            if (width <= size_min) { big += 2; }
            if (size_max <= width) { small += 2; }
        }
    }

    static void move () {
        x += vx; y += vy;
    }

    static void check () {
        if (!Utils.check_u("r", (int)x, (int)y, width, height)) { // ユニットとボスの当たり判定
            Unit.unit_lost(SampleApplet.invincible_time);
        }
        if (life <= life_m * 0.2 * SampleApplet.dif) { // ボスの体力が減ったらボスが怒る
            oko_flag = true;
        }
        if (life <= life_m * 0.1 * SampleApplet.dif) { // ボスの体力がかなり減ったら
            geki_oko_flag = true;
        }
        if (life <= 0) { 		// ボスを倒したら
            SampleApplet.score += 7500000 * (SampleApplet.dif + Unit.life);
            SampleApplet.score *= SampleApplet.dif * SampleApplet.dif * SampleApplet.dif;
            Unit.invincible = true;
            SampleApplet.end_flag = true;
            active = false;
        }
        if (!Utils.check_f((int)x, (int)y, width, height)) { // 画面にめり込んだら
            frame = -1;
            m_time_cnt = 0;
            x = SampleApplet.field_width/2 - width/2;
            y = (-1)*height;
        }
        frame++;				// ボスの同期をとる
        if (20 < frame) {		// ボスが壁にあたったら向きを変える
            if (x <= 0 || SampleApplet.field_width <= x + width) {
                attack();
                if (!oko_flag) { vx *= -1; }
                m_time_cnt += 2;
                if (m_time <= m_time_cnt) { return; }
                if (oko_flag && vx < 0) { vx = 5 + SampleApplet.rand.nextInt(5); }
                else if (oko_flag && vx >= 0) {vx = (-1) * (5 + SampleApplet.rand.nextInt(5)); }
                if (oko_flag) { vy = SampleApplet.rand.nextInt(15) * (frame % 2 == 0 ? 1: -1); }
            }
            if (y <= 0 || SampleApplet.field_height <= y + height) {
                attack();
                if (!oko_flag) { vy *= -1; }
                m_time_cnt += 2;
                if (m_time <= m_time_cnt) { return; }
                if (oko_flag && vy < 0) { vy = 5 + SampleApplet.rand.nextInt(5); }
                else if (oko_flag && vy >= 0){ vy = (-1) * ( 5 + SampleApplet.rand.nextInt(5)); }
                if (oko_flag) { vx = SampleApplet.rand.nextInt(15) * (frame % 2 == 0 ? 1: -1); }
            }
        }

    }

    static void attack () {
        int m = 3 / SampleApplet.dif, height_m = height/2; // ボスの弾の速度と発射位置を調節
        SampleApplet.e_bullet[Enemy.available()].behavior(20, m, (int)x,(int)y, width, height_m);
        SampleApplet.e_bullet[Enemy.available()].behavior(20, -1 * m, (int)x, (int)y, width, height_m);
        SampleApplet.e_bullet[Enemy.available()].behavior(21, m , (int)x, (int)y, width, height_m);
        SampleApplet.e_bullet[Enemy.available()].behavior(21, -1 * m, (int)x, (int)y, width, height_m);
        if (1 < SampleApplet.dif && oko_flag) { // Normal 以上の難易度で更に攻撃
            SampleApplet.e_bullet[Enemy.available()].behavior(22, m, (int)x, (int)y, width, height_m);
            SampleApplet.e_bullet[Enemy.available()].behavior(22, -1 * m, (int)x, (int)y, width, height_m);
            SampleApplet.e_bullet[Enemy.available()].behavior(23, m, (int)x, (int)y, width, height_m);
            SampleApplet.e_bullet[Enemy.available()].behavior(23, -1 * m, (int)x, (int)y, width, height_m);
            if (2 < SampleApplet.dif && (m_time/2 <= m_time_cnt || geki_oko_flag)) { // めり込む又は激おこで更に追加攻撃(Hardのみ)
                m *= 2;
                SampleApplet.e_bullet[Enemy.available()].behavior(30, m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(30, -1 * m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(31, m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(31, -1 * m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(32, m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(32, -1 * m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(33, m, (int)x, (int)y, width, height_m);
                SampleApplet.e_bullet[Enemy.available()].behavior(33, -1 * m, (int)x, (int)y, width, height_m);
            }
        }
    }

    static void draw () {
        if (!oko_flag) {
            SampleApplet.gv.setColor(new Color(30, 100, 200));
        } else {
            if (!geki_oko_flag) {
                SampleApplet.gv.setColor(new Color(240, 10, 220));
            } else {
                SampleApplet.gv.setColor(new Color(255, 10, 10));
            }
        }
        SampleApplet.gv.fillRect((int)x, (int)y, width, height);
    }

    static void reset () {
        active = false;
        oko_flag = false;
        geki_oko_flag = false;
        big = 0; small = 1;
        width = width_m; height = height_m;
        x = SampleApplet.field_width/2 - width/2;
        y = (-1)*height;
        vx = 0; vy = 0;
        life = life_m;
        frame = 0;
        check = 0;
        shot_wait = 0;
        m_time_cnt = 0;
    }

}

class Group {					// 敵のグループを管理する
    public int x, dx, y, dy;
    public int type, property, num, wait;
    public boolean active = false;
    public boolean group_check = true; // wait が 0 の時すべての敵を同時に表示
    public int wait_cnt = 0;
    public int b_property = 1;
    public int behavior = 0;

    public void group(int _type, int _property, int _num, int _wait,
                      int _dx, int _dy, int _x, int _y, int _behavior, int _b_property) {
        active = true;
        group_check = true;
        if (_wait == 0) { group_check = false; }
        type = _type; property = _property; num = _num; wait = _wait;
        wait_cnt = _wait;
        dx = _dx; dy = _dy;
        x = _x; y = _y;
        behavior = _behavior;
        b_property = _b_property;
    }

    public void update () {
        if (active) {
            if (group_check) {
                if (wait == wait_cnt) {
                    SampleApplet.enemy[available()]
                        .type(type, property, x, y, behavior, b_property);
                    num--;
                    if (num == 0) { active = false; }
                    x += dx; y += dy;
                    wait_cnt = 0;
                }
                wait_cnt++;
            } else { // wait が 0 の時用
                for (int i = 0; i < num; i++) {
                    SampleApplet.enemy[available()]
                        .type(type, property, x + i*dx, y + i*dy, behavior, b_property);
                }
                group_check = true;
                active = false;
            }
        }

    }

    public int available() {
        for (int i = 0; i < SampleApplet.enemy_max; i++) {
            if (!SampleApplet.enemy[i].active){ return i; }
        }
        return -1;
    }

    public static void reset () {
        for (int i = 0; i < SampleApplet.group_max; i++) {
            SampleApplet.group[i].active = false;
        }
    }

}

class Enemy {
    public int width, height, life, property;
    public int x, y, vx, vy;
    public int num, dx, dy, wait;
    public int behavior = 0;
    public String shape = "r";
    public Color color;
    public boolean active = false;
    public int  shot_wait_min = 0;		// 敵の発射する間隔を決める
    public int shot_wait = 0;			// 敵の発射する間隔をカウントする
    public int b_property = 1;			// 弾のプロパティ

    // 敵の種類を決める
    public void type(int _type, int _pro, int _x, int _y, int _beh, int _b_pro) {
        active = true;
        switch (_type) {
        case 0:					// 右に動く
            set_e(_pro,_x,_y,20,20, 1,1,0,new Color(140, 50, 255), 70,_beh,_b_pro);
            break;
        case 1:					// 下に動く
            set_e(_pro,_x,_y,20,20, 5,0,1,new Color(80, 120, 255), 100,_beh,_b_pro);
            break;
        case 2:					// 右下に動く
            set_e(_pro,_x,_y,20,20, 1,1,1,new Color(130, 140, 230), 80,_beh,_b_pro);
            break;
        }
    }

    public void set_e (int _property, int _x, int _y, int _width, int _height,
                       int _life, int _vx, int _vy, Color _color,
                       int _shot_wait_min, int _behavior, int _b_property) {
        property = _property;
        if (0 < property) {x = _x - _width;}
        else { x = SampleApplet.field_width + _x; }
        y = _y - _height;
        width = _width; height = _height;
        life = _life;
        vx = _property * _vx;
        vy = _vy * _property;
        if (vy < 0) { vy *= -1; }
        color = _color;
        shot_wait_min = _shot_wait_min / SampleApplet.dif;
        shot_wait = shot_wait_min;
        behavior = _behavior;
        b_property = _b_property;
    }

    public void update() {		// 出現している敵の動作
        if (active) {
            move();
            check();
            draw();
            attack();
        }
    }

    public void move() {
        x += vx;
        y += vy;
    }

    public void check() {		// 敵のダメージ判定と生死
        active = Utils.check_f(x, y, width, height); // 敵が画面内にいるかどうか
        if ( life <= 0){ active = false;  Unit.power_cnt += 5; return; } // 体力が無くなっている敵がいたら
        if (!Utils.check_u("r" , x, y, width, height)) { // ユニットと敵の接触判定
            Unit.unit_lost(SampleApplet.invincible_time);
        }
    }

    public void draw() {
        SampleApplet.gv.setColor(color);
        SampleApplet.gv.fillRect(x, y, width, height);
    }

    public void attack() {
        if (shot_wait_min <= shot_wait) {
            SampleApplet.e_bullet[available()]
                .behavior(behavior, b_property , x, y, width, height);
            shot_wait = 0;
        }
        shot_wait++;
    }

    public static int available() {
        for (int i = 0; i < SampleApplet.e_bullet_max; i++) {
            if (!SampleApplet.e_bullet[i].active) { return i; }
        }
        return -1;
    }

    public static void reset () {
        for (int i = 0; i < SampleApplet.enemy_max; i++) {
            SampleApplet.enemy[i].active = false;
        }
    }

}

// アクティブなエネミーがタイプを指定してそのタイプの弾を発射する
class Ebullet {
    public int x, y;		// 弾のx,y座標
    public int r_x, r_y;
    public int vx, vy;
    public Color color;
    public String shape;
    public boolean active = false;
    public int b_property = 1;

    // 敵の攻撃の種類を決める
    public void behavior(int behavior, int _b_pro, int _x, int _y, int width, int height) {
        x = _x + width/2 - r_x/2;
        y = _y + height;
        switch (behavior) {
        case 0:					// 真下に動く弾
            set_b(_b_pro, 0, 1, 10, 10, new Color(180, 180, 180), "c"); break;
        case 1:					// 右下に動く弾
            set_b(_b_pro, 1, 3, 10, 10, new Color(180, 180, 180), "c"); break;
        case 2:					// 左下に動く弾
            set_b(_b_pro, -1, 3, 10, 10, new Color(180, 180, 180), "c"); break;
        case 20:					// 真下に動く弾 (20番台はボス用)
            set_b(_b_pro, 0, 1, 10, 10, new Color(160, 50, 180), "c"); break;
        case 21:					// 真右に動く弾
            set_b(_b_pro, 1, 0, 10, 10, new Color(0, 180, 180), "c"); break;
        case 22:					// 右斜下に動く弾
            set_b(_b_pro, 1, 1, 10, 10, new Color(180, 180, 0), "c"); break;
        case 23:					// 左斜下に動く弾
            set_b(_b_pro, -1, 1, 10, 10, new Color(50, 190, 30), "c"); break;
        case 30:					// 真下に動く弾 (30番台はめり込んだ時用)
            set_b(_b_pro, 0, 1, 10, 20, new Color(10, 160, 160), "c"); break;
        case 31:					// 真右に動く弾
            set_b(_b_pro, 1, 0, 20, 10, new Color(10, 180, 30), "c"); break;
        case 32:					// 右斜下に動く弾
            set_b(_b_pro, 1, 1, 20, 20, new Color(200, 30, 20), "c"); break;
        case 33:					// 左斜下に動く弾
            set_b(_b_pro, -1, 1, 20, 20, new Color(60, 60, 180), "c"); break;
        }
    }

    public void set_b (int b_property, int _vx, int _vy, int _r_x, int _r_y,
                       Color _color, String _shape) {
        vx = _vx * b_property;
        vy =_vy * b_property;
        r_x = _r_x; r_y = _r_y;
        color = _color;
        shape = _shape;
        active = true;
    }

    public void update() {
        if (active) {
            x += vx;
            y += vy;
            active = Utils.check_f(x, y, r_x, r_y);
            if (!Utils.check_u(shape, x, y, r_x, r_y)) { // ユニットと敵の弾が当たったら
                active = false;
                Unit.unit_lost(SampleApplet.invincible_time);
            }
            SampleApplet.gv.setColor(color);
            SampleApplet.gv.fillOval(x, y, r_x, r_y);
        }
    }

    public static void reset () {
        for (int i = 0; i < SampleApplet.e_bullet_max; i++) {
            SampleApplet.e_bullet[i].active = false;
        }
    }

}

class Star {
    double x = 0, y = 0;
    public double star_vf = 5;		// 背景の星が流れる速度

    public void update() {
        SampleApplet.gv.drawString("*", (int)x, (int)y);
        if (y >= SampleApplet.field_height+star_vf) { y = -10; }
        y += star_vf;
    }

}

class Utils {
    // 画面から出たかを判定する
    static boolean check_f(int x, int y, int width, int height) {
        if (x  < (-1 * width) || SampleApplet.field_width < x){
            return false;
        }
        if (y  < (-1 * height) || SampleApplet.field_height < y) {
            return false;
        }
        return true;			// 画面内にいれば true を返す
    }

    // ユニットとの当たり判定を行う (shape で判定するものの形を渡す)
    static boolean check_u(String shape, int x, int y, int width, int height) {
        int r, c_x, c_y, a, b;
        if (!SampleApplet.my_unit.invincible) { // ユニットが無敵でない時だけ
            switch (shape) {
            case "r":
                if (x <= SampleApplet.my_unit.x
                    && SampleApplet.my_unit.x <= x + width
                    && y <= SampleApplet.my_unit.y
                    && SampleApplet.my_unit.y <= y + height) {
                    return false;
                }
                break;
            case "c":
                c_x = SampleApplet.my_unit.x - (x + width/2);
                c_y = SampleApplet.my_unit.y - (y + height/2);
                if (width == height) { // 円なら
                    r = (width/2);
                    if (c_x*c_x + c_y*c_y  <= r*r) { return false; }
                } else {			// 楕円なら
                    a = width/2;
                    b = height/2;
                    if ((c_x*c_x)*(b*b) + (c_y*c_y)*(a*a) <= a*a*b*b) { return false; }
                }
                break;
            }
        }
        return true; // 当たっていなければ true を返す
    }

    // ユニットの弾との当たり判定を行う
    static boolean check_b (int x, int y) { // 弾のx,y
        for (int i = 0; i < SampleApplet.enemy_max; i++) {
            if (SampleApplet.enemy[i].active
                && SampleApplet.enemy[i].x <= x
                && x <= SampleApplet.enemy[i].x + SampleApplet.enemy[i].width
                && SampleApplet.enemy[i].y <= y
                && y <= SampleApplet.enemy[i].y + SampleApplet.enemy[i].height) {
                SampleApplet.enemy[i].life--;
                return false;
            }
        }
        if (Boss.active
            && SampleApplet.boss.x <= x
            && x <= SampleApplet.boss.x + SampleApplet.boss.width
            && SampleApplet.boss.y <= y
            && y <= SampleApplet.boss.y + SampleApplet.boss.height) {
            SampleApplet.boss.life--;
            return false;
        }
        return true; // 当たっていなければ true を返す
    }
}
