package space.ske.goo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import space.ske.goo.entity.*;
import space.ske.goo.util.SpringingContext1D;
import space.ske.goo.util.SpringingContext2D;

public class GooGame extends ApplicationAdapter {
    public static GooGame i;

    public static boolean DEBUG = true;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch   batch;
    private World         world;
    private GooRenderer   gooRenderer;
    private OrthographicCamera camera         = new OrthographicCamera();
    private SpringingContext2D cameraPosition = new SpringingContext2D(0.9f, 4f);

    private Array<Entity> entities = new Array<Entity>();
    private CollisionTracker collisionTracker;
    private Level            level;
    private Player           player;
    private int              levelNum;
    private UI               ui;
    private Vector2          spawnPoint;

    private float transitionProgress;
    private int transitioningTo = -1;
    private boolean hasLoaded;

    private float screenshake;

    private SpringingContext2D titleSize = new SpringingContext2D(0.2f, 1.5f);

    private SpringingContext1D mainMusic = new SpringingContext1D(1, 0.3f);
    private SpringingContext1D bossMusic = new SpringingContext1D(1, 0.6f);

    private static void createColliders(World world, Level level) {
        EdgeShape es = new EdgeShape();
        Body b = world.createBody(new BodyDef());
        for (int x = 0; x < level.getWidth(); x++){
            for (int y = 0; y < level.getHeight(); y++){
                char tt = level.getTile(x, y);
                if (tt == '#') {
                    if (level.getTile(x - 1, y) != '#') {
                        es.set(x, y, x, y + 1);
                        b.createFixture(es, 0).setFriction(0.1f);
                    }

                    if (level.getTile(x + 1, y) != '#') {
                        es.set(x + 1, y, x + 1, y + 1);
                        b.createFixture(es, 0).setFriction(0.1f);
                    }

                    if (level.getTile(x, y - 1) != '#') {
                        es.set(x, y, x + 1, y);
                        b.createFixture(es, 0).setFriction(0.1f);
                    }

                    if (level.getTile(x, y + 1) != '#') {
                        es.set(x, y + 1, x + 1, y + 1);
                        b.createFixture(es, 0).setFriction(0.1f);
                    }
                }
            }
        }
        es.dispose();
    }

    @Override
    public void create() {
        i = this;
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        collisionTracker = new CollisionTracker();
        gooRenderer = new GooRenderer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui = new UI(gooRenderer);

        titleSize.target.set(titleSize.value.set(1, 1));
        mainMusic.target = 0.6f;

        loadLevel(-1);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        gooRenderer.update(deltaTime);


        mainMusic.target = 0.6f;
        if (levelNum == -1 || levelNum == 7) mainMusic.target = 0;
        mainMusic.update(deltaTime);
        Assets.liquidlust.setVolume(mainMusic.value);

        bossMusic.update(deltaTime);
        Assets.bossMusic.setVolume(bossMusic.value);

        // oh god why
        if (levelNum == 8) {
            renderCredits();
            handleTransition(deltaTime);
            return;
        } else if (levelNum == -1) {
            titleSize.update(deltaTime);
            updateCamera(deltaTime);
            renderMenu();
            handleTransition(deltaTime);
            return;
        }
        Assets.font.getData().setScale(1);

        world.step(deltaTime, 5, 5);

        Array.ArrayIterator<Entity> iter = new Array.ArrayIterator<Entity>(this.entities);
        for (Entity entity : iter){
            if (entity.shouldDestroy) {
                iter.remove();
                world.destroyBody(entity.getBody());
                continue;
            }

            entity.update(deltaTime);
        }

        updateCamera(deltaTime);

        drawEntities(iter);

        if (DEBUG && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            switchLevel(levelNum);
        } /*else if (DEBUG && Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            switchLevel(levelNum + 1);
        }*/

        System.out.println(1 / Gdx.graphics.getDeltaTime());

        ui.setGooLevel(player.getGooLevel());
        ui.update(deltaTime);
        ui.draw();

        handleTransition(deltaTime);

        //new Box2DDebugRenderer().render(world, camera.combined);
    }

    private void renderMenu() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        float hw = Gdx.graphics.getWidth() / 2;

        gooRenderer.begin(210, 100, 15);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
        gooRenderer.end(-0.4f, 3);

        gooRenderer.begin(210, 90, 90);
        batch.begin();

        Assets.font.getData().setScale(titleSize.value.x);
        Assets.font.draw(batch, "space to start", hw, 150, 1, Align.center, false);

        Assets.font.getData().setScale(1);
        Assets.font.draw(batch, "OOBLECK", hw, 550, 1, Align.center, false);

        batch.end();
        gooRenderer.end(0, 1.1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            switchLevel(0);
            titleSize.velocity.set(5, 5);
        }
    }

    private void renderCredits() {
        float hw = Gdx.graphics.getWidth() / 2;

        gooRenderer.begin(210, 100, 15);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
        gooRenderer.end(-0.4f, 3);

        gooRenderer.begin(210, 90, 90);
        batch.begin();
        Assets.font.draw(batch, "Game by Ske", hw, 550, 1, Align.center, false);
        Assets.font.draw(batch, "Music by Elijah Lucian", hw, 450, 1, Align.center, false);
        Assets.font.draw(batch, "THANKS FOR PLAYING", hw, 150, 1, Align.center, false);
        batch.end();
        gooRenderer.end(0, 1.1f);
    }

    private void handleTransition(float deltaTime) {
        if (transitioningTo > -1) {
            if (transitionProgress > 0.5f && !hasLoaded) {
                loadLevel(transitioningTo);
                hasLoaded = true;
            }

            transitionProgress += deltaTime;

            if (transitionProgress > 1) {
                transitioningTo = -1;
                hasLoaded = false;
            }

            gooRenderer.begin(210, 90, 90);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            float cw = camera.viewportWidth;
            float ch = camera.viewportHeight;
            float cx = camera.position.x - cw / 2;
            float cy = camera.position.y - ch / 2;

            if (transitionProgress < 0.5f) {
                shapeRenderer.triangle(
                        cx, cy,
                        cx + MathUtils.lerp(0, cw * 2, transitionProgress * 2), cy,
                        cx, cy + MathUtils.lerp(0, ch * 2, transitionProgress * 2)
                );
            } else {
                shapeRenderer.triangle(
                        cx + cw, cy + ch,
                        cx + MathUtils.lerp(-cw, cw, transitionProgress * 2 - 1), cy + ch,
                        cx + cw, cy + MathUtils.lerp(-ch, ch, transitionProgress * 2 - 1)
                );
            }
            shapeRenderer.end();
            gooRenderer.end(0, 3);
        }
    }

    public void switchLevel(int target) {
        if (transitioningTo >= 0) return;

        transitioningTo = target;
        transitionProgress = 0;

        Assets.transition.play();
    }

    private void updateCamera(float deltaTime) {
        if (player == null) return;

        Vector2 t = cameraPosition.target;
        findCameraTarget(t);

        cameraPosition.update(deltaTime);

        camera.position.set(cameraPosition.value, 0);

        screenshake = Math.max(0, screenshake - deltaTime);

        Vector2 shake = new Vector2(screenshake, 0).rotate(MathUtils.random(0f, 360f));
        camera.position.add(shake.x, shake.y, 0);

        camera.update();
    }

    private void findCameraTarget(Vector2 out) {
        float hw = camera.viewportWidth / 2f;
        float hh = camera.viewportHeight / 2f;
        out.set(player.getBody().getPosition());
        if (level.getWidth() > 30) {
            out.x = MathUtils.clamp(out.x, hw, level.getWidth() - hw);
        } else {
            out.x = 15;
        }

        if (level.getHeight() > 20) {
            out.y = MathUtils.clamp(out.y, hh, level.getHeight() - hh);
        } else {
            out.y = 10;
        }
    }

    private void drawEntities(Array.ArrayIterator<Entity> iter) {
        shapeRenderer.setProjectionMatrix(camera.combined);

        gooRenderer.begin(210, 100, 15);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
        gooRenderer.end(-0.4f, 3);

        gooRenderer.begin(210, 90, 90);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int x = 0; x < level.getWidth(); x++){
            for (int y = 0; y < level.getHeight(); y++){
                char tt = level.getTile(x, y);
                if (tt == '#') {
                    shapeRenderer.rect(x, y, 1, 1);
                }
            }
        }

        iter.reset();
        for (Entity entity : iter){
            if (entity.z == 0) entity.draw(shapeRenderer);
        }
        shapeRenderer.end();
        gooRenderer.end(0, 3);

        gooRenderer.begin(0, 90, 90);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        iter.reset();
        for (Entity entity : iter){
            if (entity.z == 1) entity.draw(shapeRenderer);
        }
        shapeRenderer.end();
        gooRenderer.end(0, 3);
    }

    public void shake(float amount) {
        screenshake = Math.max(amount, screenshake);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / 32f, height / 32f);
        cameraPosition.target.set(cameraPosition.value.set(camera.position.x, camera.position.y));
    }

    private void loadLevel(int idx) {
        if (world != null) world.dispose();
        entities.clear();
        player = null;

        levelNum = idx;
        world = new World(new Vector2(0f, -50f), true);
        world.setContactListener(collisionTracker);

        level = Level.load(Gdx.files.internal("levels/level" + idx + ".txt"));

        createColliders(world, level);
        for (int x = 0; x < level.getWidth(); x++){
            for (int y = 0; y < level.getHeight(); y++){
                char t = level.getTile(x, y);
                if (t == '@') {
                    player = new Player();
                    player.getBody().setTransform(x + 0.75f, y + 0.5f, 0);
                    spawnPoint = new Vector2(x + 0.75f, y + 0.5f);
                    entities.add(player);
                }

                if (t == 'E') {
                    Entity e = new BasicEnemy();
                    e.getBody().setTransform(x + 0.5f, y + 0.5f, 0);
                    entities.add(e);
                }


                if (t == 'S') {
                    Entity e = new Shooter();
                    e.getBody().setTransform(x + 0.5f, y + 0.5f, 0);
                    entities.add(e);
                }

                if (t == 'D') {
                    Door d = new Door();
                    d.getBody().setTransform(x + 0.5f, y + 0.5f, 0);
                    entities.add(d);
                }

                if (t == 'b') {
                    Boss1 b = new Boss1();
                    b.getBody().setTransform(x + 0.5f, y + 0.5f, 0);
                    entities.add(b);
                }

                if (t == 'L') {
                    for (int i = 0; i < 13; i++){
                        Goo g = new Goo(Goo.GooType.LAVA, 0.15f);
                        g.z = 1;
                        g.getBody().setTransform(x + MathUtils.random(0.25f, 0.75f), y + MathUtils.random(0.25f, 0.75f), 0);
                        entities.add(g);
                    }
                }
            }
        }

        EdgeShape es = new EdgeShape();
        es.set(-1000, 0, 1000, 0);
        world.createBody(new BodyDef()).createFixture(es, 0);
        es.dispose();

        entities.add(new NextLevelSensor());

        findCameraTarget(cameraPosition.value);
        cameraPosition.target.set(cameraPosition.value);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    public World getWorld() {
        return world;
    }

    public CollisionTracker getCollisionTracker() {
        return collisionTracker;
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    public Level getLevel() {
        return level;
    }

    public int getLevelNum() {
        return levelNum;
    }

    public Player getPlayer() {
        return player;
    }

    public Vector2 getSpawnPoint() {
        return spawnPoint;
    }


    public SpringingContext1D getBossMusic() {
        return bossMusic;
    }
}
