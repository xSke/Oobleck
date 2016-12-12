package space.ske.goo;

import com.badlogic.gdx.files.FileHandle;

public class Level {
    private char[][] map;
    private int      width;
    private int      height;

    private Level(char[][] map, int width, int height) {
        this.map = map;
        this.width = width;
        this.height = height;
    }

    public static Level load(FileHandle f) {
        String[] lines = f.readString().replaceAll("\r", "").split("\n");

        int width = 0;
        for (String line : lines) if (line.length() > width) width = line.length();

        int height = lines.length;
        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) map[x][y] = ' ';

        for (int y = 0; y < lines.length; y++){
            String line = lines[lines.length - y - 1];
            for (int x = 0; x < line.length(); x++){
                map[x][y] = line.charAt(x);
            }
        }

        return new Level(map, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char getTile(int x, int y) {
        if (x < 0 || x >= width) return ' ';
        if (y < 0 || y >= height) return ' ';
        return map[x][y];
    }

    public enum TileType {
        AIR,
        GOO,
        PLAYER,
        ENEMY,
        DOOR
    }
}
