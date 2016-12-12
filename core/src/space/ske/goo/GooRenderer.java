package space.ske.goo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public class GooRenderer implements Disposable {
    private SpriteBatch batch;
    private FrameBuffer buffer1;
    private FrameBuffer buffer2;

    private ShaderProgram gooShader;
    private ShaderProgram blurShader;

    private int width;
    private int height;

    private float time;
    private float h;
    private float s;
    private float v;

    public GooRenderer(int w, int h) {
        this.width = w;
        this.height = h;

        batch = new SpriteBatch();
        gooShader = new ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/goo.frag"));
        blurShader = new ShaderProgram(Gdx.files.internal("shaders/default.vert"), Gdx.files.internal("shaders/blur.frag"));
        Gdx.app.error("shader", gooShader.getLog());
        Gdx.app.error("shader", blurShader.getLog());

        buffer1 = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        buffer2 = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
    }

    public void update(float delta) {
        time += delta;
    }

    public void begin(float h, float s, float v) {
        this.h = h / 360f;
        this.s = s / 100f;
        this.v = v / 100f;

        buffer1.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void doBlur(FrameBuffer in, FrameBuffer out, float dirX, float dirY, float res, float rad) {
        out.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.getProjectionMatrix().setToOrtho2D(0, height, width, -height);
        batch.begin();

        batch.setShader(blurShader);
        blurShader.setUniformf("u_dir", dirX, dirY);
        blurShader.setUniformf("u_resolution", res);
        blurShader.setUniformf("u_radius", rad);

        batch.draw(in.getColorBufferTexture(), 0, 0, width, height);
        batch.end();
        out.end();
    }

    public void end(float timeOffset, float radius) {
        buffer1.end();

        doBlur(buffer1, buffer2, 1, 0, width, radius);
        doBlur(buffer2, buffer1, 0, 1, height, radius);
        doBlur(buffer1, buffer2, 1, 0, width, radius * 2 / 3f);
        doBlur(buffer2, buffer1, 0, 1, height, radius * 2 / 3f);

        batch.getProjectionMatrix().setToOrtho2D(0, height, width, -height);

        batch.begin();
        batch.setShader(gooShader);
        gooShader.setUniformf("u_time", time + timeOffset);
        gooShader.setUniformf("u_hsv", h, s, v);
        batch.draw(buffer1.getColorBufferTexture(), 0, 0, width, height);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        buffer1.dispose();
        buffer2.dispose();

        gooShader.dispose();
        blurShader.dispose();
    }
}
