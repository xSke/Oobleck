package space.ske.goo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Assets {
    public static Sound goo3        = Gdx.audio.newSound(Gdx.files.internal("audio/goo3.wav"));
    public static Sound playerJump  = Gdx.audio.newSound(Gdx.files.internal("audio/goo4.wav"));
    public static Sound goo5        = Gdx.audio.newSound(Gdx.files.internal("audio/goo5.wav"));
    public static Sound playerLand  = Gdx.audio.newSound(Gdx.files.internal("audio/goo6.wav"));
    public static Music gooSplatter = Gdx.audio.newMusic(Gdx.files.internal("audio/goo8.wav"));
    public static Sound enemyDeath  = Gdx.audio.newSound(Gdx.files.internal("audio/goo9.wav"));
    public static Sound playerHurt  = Gdx.audio.newSound(Gdx.files.internal("audio/goo11.wav"));
    public static Sound playerDeath = Gdx.audio.newSound(Gdx.files.internal("audio/goo12.wav"));
    public static Sound transition  = Gdx.audio.newSound(Gdx.files.internal("audio/goo13.wav"));
    public static Sound yay         = Gdx.audio.newSound(Gdx.files.internal("audio/yay.wav"));
    public static Music liquidlust  = Gdx.audio.newMusic(Gdx.files.internal("audio/liquidlust.ogg"));
    public static Music bossMusic  = Gdx.audio.newMusic(Gdx.files.internal("audio/bossmusic.ogg"));

    public static BitmapFont font       = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
    public static BitmapFont font_small = new BitmapFont(Gdx.files.internal("fonts/font_small.fnt"));

    static {
        gooSplatter.setLooping(true);
        gooSplatter.setVolume(0);
        gooSplatter.play();

        liquidlust.setLooping(true);
        liquidlust.setVolume(0f);
        liquidlust.play();

        bossMusic.setLooping(true);
        bossMusic.setVolume(0f);
        bossMusic.play();
    }
}
