/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.Component;
import java.awt.Image;
import java.io.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import tank.Grid.Point;

/**
 *
 * @author IVO
 */
public class GameLevel {
    private Component parent;
    private ObjectsChooserPanel objectsChooserPanel = null;
    private ObjectsChooserPanel attributesChooserPanel = null;
    private String name = "Untitled1";
    //private int width = 32;
    //private int height = 32;
    private int width = 16;
    private int height = 16;
    public int tileWidth = 32, tileHeight = 16;
    boolean platformMode = false;
    private boolean changed = false;
    public int[][] content0 = null;
    public int[][] content1 = null;
    public int[][] content2 = null;
    //public int[][] content3 = null;

    public int[][][] values = null;
    int curType=0;
    int curAngle=0;
    int curXOffset=0;
    int curYOffset=0;

    private String backgroundPath = null;
    private Image backgroundImage = null;
    private int tempPlatformDecorationPaletteSize = -1;
    private String versionString = "";
    private ArrayDeque<Platform> platforms = new ArrayDeque<Platform>();
    private ArrayDeque<ImageForPalette> platformPaletteImages = new ArrayDeque<ImageForPalette>();
    ImageForPalette[] platformPaletteImageArray = null;
    public int worldWidth = 5, worldHeight = 3, coordX = 0, coordY = 0, coordZ = 0;
    public int nextLevelX = 0, nextLevelY = 0;

    public static class ImageForPalette {
        public Image image = null;
        public String path = "";
    }

    public static class Platform {
        private byte[][] decorationArray = null;
        private int size = 0;
        public int movingSpeed;
        public int delay;
        public ArrayDeque<Point> posArray = null;
        public static final int PLATFORM_START = 1;
        public static final int PLATFORM_KEY = 2;
        public static final int PLATFORM_FINISH = 3;
        public byte getDecoration(int column, int row) {
            if (decorationArray == null) return -1;
            if (column < 0 || row < 0) return -1;
            if (column >= decorationArray.length) return -1;
            if (row >= decorationArray[0].length) return -1;
            return decorationArray[column][row];
        }
        public void setDecor(int column, int row, byte value) {
            if (decorationArray == null) return;
            if (column < 0 || row < 0) return;
            if (column >= decorationArray.length) return;
            if (row >= decorationArray[0].length) return;
            decorationArray[column][row] = value;
        }
        public void setSize(int newSize) {
            if (newSize < 1 || newSize > 10) return;
            size = newSize;
            decorationArray = new byte[size+2][3];
            int x, y;
            for (x = 0; x < getDecorationWidth(); ++x) {
                for (y = 0; y < 3; ++y) {
                    decorationArray[x][y] = -1;
                }
            }
        }
        public void saveDecoration(ZipOutputStream zos) {
            if (decorationArray == null) return;
            try {
                for (int x = 0; x < decorationArray.length; ++x) {
                    zos.write(decorationArray[x]);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public void loadDecoration(InputStream iStream) {
            decorationArray = new byte[size+2][3];
            try {
                for (int x = 0; x < decorationArray.length; ++x) {
                    iStream.read(decorationArray[x]);
                }
            } catch (IOException ex) {
                Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public int getDecorationWidth() {
            return size+2;
        }
        public int getSize() {
            return size;
        }
        public void copyDecoration(Platform from) {
            if (from == null) return;
            setSize(from.getSize());
            int x, y;
            for (x=0; x < getDecorationWidth(); ++x) {
                for (y=0; y < 3; ++y) {
                    decorationArray[x][y] = from.getDecoration(x, y);
                }
            }
        }
    }

    public void setWorldSize(int width, int height) {
        worldWidth = width;
        worldHeight = height;
    }

    public void setAreaCoords(int width, int height, int z) {
        coordX = width;
        coordY = height;
        coordZ = z;
    }

    public void fillFloor(int type) {
        for (int y=0; y<getHeight(); ++y){
            for (int x=0; x<getWidth(); ++x){
                content0[x][y] = type;
                if (values[x][y][0] == -1) values[x][y][0] = 1;
            }
        }
    }

    public Image getPlatformDecorationImage(Platform platform, int x, int y) {
        if (platform == null) return null;
        if (platformPaletteImageArray == null) return null;
        byte id = platform.getDecoration(x, y);
        if (id < 0 || id >= platformPaletteImageArray.length) return null;
        return platformPaletteImageArray[id].image;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void  setObjectsChooserPanel(ObjectsChooserPanel panel) {
        objectsChooserPanel = panel;
    }
    public void  setAttributesChooserPanel(ObjectsChooserPanel panel) {
        attributesChooserPanel = panel;
    }

    public ObjectsChooserPanel getObjectsChooserPanel() {
        return objectsChooserPanel;
    }

    public boolean isSaved() {
        return !changed;
    }

    public GameLevel(Component component) {
        parent = component;
        setSize(width, height);
    }

    public void setSize(int w, int h) {
        width = Math.max(1, w);
        height = Math.max(1, h);
        content0  = new int[getWidth()][getHeight()];
        content1  = new int[getWidth()][getHeight()];
        content2  = new int[getWidth()][getHeight()];
        values = new int[getWidth()][getHeight()][4];
        //content3  = new int[getWidth()][getHeight()];
        int x, y;
        for (y=0; y<getHeight(); ++y){
            for (x=0; x<getWidth(); ++x){
                content0[x][y] = -1;
                content1[x][y] = -1;
                content2[x][y] = -1;
                //content3[x][y] = -1;
                for (int i=0; i<4; ++i){
                    if (i == 0) values[x][y][i] = -1;
                    else values[x][y][i] = 0;
                }
            }
        }
        changed = false;
        platforms.clear();
    }

    public void setValues(int tw, int th, int pw, int ph, boolean pm) {
        boolean ok = true;
        if (tileWidth != tw || tileHeight != th) {
            ok = false;
        }
        tileWidth = Math.max(1, tw);
        tileHeight = Math.max(1, th);
        platformMode = pm;
        if (!ok) {
            content0  = new int[getWidth()][getHeight()];
            content1  = new int[getWidth()][getHeight()];
            content2  = new int[getWidth()][getHeight()];
            values = new int[getWidth()][getHeight()][4];
            int x, y;
            for (y=0; y<getHeight(); ++y){
                for (x=0; x<getWidth(); ++x){
                    content0[x][y] = -1;
                    content1[x][y] = -1;
                    content2[x][y] = -1;
                    for (int i=0; i<4; ++i){
                        if (i == 0) values[x][y][i] = -1;
                        else values[x][y][i] = 0;
                    }
                }
            }
            changed = false;
        }
    }

    public void ClearPlatform() {
        platforms.clear();
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public int getCell(int x, int y, int layer) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return -1;
        if (layer == 0) {
            return content0[x][y];
        } else if (layer == 1) {
            return content1[x][y];
        } else if (layer == 2) {
            return content2[x][y];
        } else {
            return values[x][y][0];
        }
    }

    public void setCell(int x, int y, int layer, int item) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return;


        if (layer == 0) {
            content0[x][y] = item;
            if (content1[x][y] == -1) {
                if (item == -1) {
                    values[x][y][0] = -1;
                }
                else values[x][y][0] = 1;
            }
        } else if (layer == 1) {
            content1[x][y] = item;
            if (item == -1) {
                if (content0[x][y] == -1) {
                    values[x][y][0] = -1;
                }
                else values[x][y][0] = 1;
            }
            else values[x][y][0] = 2;
        } else if (layer == 2) {
            content2[x][y] = item;
        } else {
            values[x][y][0] = item;
            if (item == -1) {
                values[x][y][1] = 0;
                values[x][y][2] = 0;
                values[x][y][3] = 0;
            } else {
                values[x][y][1] = curAngle;
                values[x][y][2] = curXOffset;
                values[x][y][3] = curYOffset;
            }
        }
        changed = true;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public void add(String fileName) {

        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            long read = 1;
            int namesCount = fos.read();
            //getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                read += nameSize + 1;
                //getObjectsChooserPanel().newNames.add(new String(buff));
            }
            //getObjectsChooserPanel().updateTiles();
            byte[] buff = new byte[(int)(fos.length() - read)];
            fos.read(buff);
            fos.seek(0);
            fos.write(getObjectsChooserPanel().names.size());
            for (int i =0; i < getObjectsChooserPanel().names.size(); ++i) {
                fos.write(getObjectsChooserPanel().names.get(i).length());
                fos.write(getObjectsChooserPanel().names.get(i).getBytes(), 0, getObjectsChooserPanel().names.get(i).length());
            }
            fos.write(buff);
            fos.seek(0);
            namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff2 = new byte[nameSize];
                fos.read(buff2);
                read += nameSize + 1;
                getObjectsChooserPanel().newNames.add(new String(buff2));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 5);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            read = size + 6;
            do {
                if (read >= fos.length()) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                if (x != coordX || y != coordY || z != coordZ) {
                    read += 5;
                    fos.skipBytes(w*h*7);
                    read += w*h*7;
                } else {
                    width = w;
                    height = h;
                    found = true;
                    break;
                }
            } while (read < fos.length());

            if (!found) {
                fos.write(width);
                fos.write(height);
                fos.write(coordX);
                fos.write(coordY);
                fos.write(coordZ);
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content0[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content1[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content2[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(values[x][y][0]);
                    fos.write(values[x][y][1]);
                    fos.write(values[x][y][2]);
                    fos.write(values[x][y][3]);
                }
            }
            //fos.write(254);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shift(String fileName) {

        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            int namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                getObjectsChooserPanel().newNames.add(new String(buff));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 5);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            long read = size + 6;
            do {
                if (w == -1) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                width = w;
                height = h;
                int c;
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer() - 1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer() - 1);
                        if (c < 250 && ((curAngle == 1 && c >= objectsChooserPanel.imagesCount) || (curAngle == 2 && c >= objectsChooserPanel.imagesCount + objectsChooserPanel.tilesetsCount) || curAngle == 0)) {
                            fos.write(c+curType);
                        } else {
                            fos.write(c);
                        }
                    }
                }
                for (y = 0; y < height; ++y) {
                    for (x = 0; x < width; ++x) {
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                        c = fos.read();
                        fos.seek(fos.getFilePointer()-1);
                        fos.write(c);
                    }
                }
            } while (read < fos.length());

            //fos.write(254);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String fileName) {

        try {
            RandomAccessFile fos = new RandomAccessFile(fileName, "rw");
            //fos.skipBytes((int)fos.length()-1);
            String nameTemp;
            int namesCount = fos.read();
            getObjectsChooserPanel().newNames.clear();
            int nameSize;
            for (int i =0; i < namesCount; ++i) {
                nameSize = fos.read();
                byte[] buff = new byte[nameSize];
                fos.read(buff);
                getObjectsChooserPanel().newNames.add(new String(buff));
            }
            getObjectsChooserPanel().updateTiles();
            int size = fos.read();
            fos.skipBytes(size + 5);
            int x=999, y=999, z=999, w=999, h=999;
            boolean found = false;
            long read = size + 6;
            do {
                if (read >= fos.length()) break;
                w = fos.read();
                h = fos.read();
                x = fos.read();
                y = fos.read();
                z = fos.read();
                if (x != coordX || y != coordY || z != coordZ) {
                    read += 5;
                    fos.skipBytes(w*h*7);
                    read += w*h*7;
                } else {
                    width = w;
                    height = h;
                    found = true;
                    break;
                }
            } while (read < fos.length());

            if (found) {
                fos.seek(fos.getFilePointer()-5);
                fos.write(w);
                fos.write(h);
                fos.write(-10);
                fos.write(-10);
                fos.write(-10);
            }

            //fos.write(254);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(String fileName) {

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        ZipEntry ze = null;
        try {
            fos = new FileOutputStream(fileName);
            //zos = new ZipOutputStream(fos);
            //ze = new ZipEntry("Content.tlv");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        //if (ze == null) return;
        /*try {
            zos.putNextEntry(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        String str = "Tank Game v1.4\n";
        try {
            fos.write(getObjectsChooserPanel().names.size());
            for (int i =0; i < getObjectsChooserPanel().names.size(); ++i) {
                fos.write(getObjectsChooserPanel().names.get(i).length());
                fos.write(getObjectsChooserPanel().names.get(i).getBytes(), 0, getObjectsChooserPanel().names.get(i).length());
            }
            //zos.write(str.getBytes(), 0, str.getBytes().length);
            fos.write(name.length());
            fos.write(name.getBytes(), 0, name.getBytes().length);
            int pm;
            if (!platformMode) pm = 0;
            else pm = 1;
            fos.write(pm);
            fos.write(worldWidth);
            fos.write(worldHeight);
            fos.write(tileWidth);
            fos.write(tileHeight);
            fos.write(width);
            fos.write(height);
            fos.write(coordX);
            fos.write(coordY);
            fos.write(coordZ);
            int x, y;
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content0[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content1[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(content2[x][y]);
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    fos.write(values[x][y][0]);
                    fos.write(values[x][y][1]);
                    fos.write(values[x][y][2]);
                    fos.write(values[x][y][3]);
                }
            }
            //fos.write(254);
            //zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        //saveImage(zos, fileName, true, backgroundImage, backgroundPath, 0, backgroundBufferAndName);
        //backgroundPath = fileName;
        //ImageForPalette ifp;
        //ObjectsChooserPanel.ImageBufferAndName ibn;
        try {
            //zos.close();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void saveImage(ZipOutputStream zos, String fileName, boolean isBackground, Image image, String imagePath, int id,
            ObjectsChooserPanel.ImageBufferAndName imageBufferAndName) {
        if ((imagePath == null || image == null) && imageBufferAndName == null) return;
        if (imageBufferAndName != null)
            ObjectsChooserPanel.writeImageBufferToZipOutputStream(zos, imageBufferAndName, id);
        else if(getExtension(imagePath).compareTo(".tlz") == 0) {
            if (isBackground) {
                ObjectsChooserPanel.writeImageFromZipToZipOutputStream(zos,
                        imagePath, "background", true);
            } else {
                ObjectsChooserPanel.writeImageFromZipToZipOutputStream(zos,
                        imagePath, "platform_decoration/image"+String.format("%03d", id), true);
            }
        } else {
            String ext = getExtension(imagePath);
            int buffSize = 1024;
            byte[] buff = new byte[buffSize];
            File imageFile = new File(imagePath);
            FileInputStream imageFIS = null;
            if (imageFile.exists()) {
                try {
                    imageFIS = new FileInputStream(imageFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
                    imageFIS = null;
                }
            }
            if (imageFIS != null) {
                ZipEntry ze;
                if (isBackground)
                    ze = new ZipEntry("background"+File.separator+"image"+ext);
                else {
                    ze = new ZipEntry("platform_decoration/image"+String.format("%03d"+ext, id));
                }
                int buffRead;
                try {
                    zos.putNextEntry(ze);
                    while ((buffRead = imageFIS.read(buff)) > 0) {
                        zos.write(buff, 0, buffRead);
                    }
                    zos.closeEntry();
                } catch (IOException ex) {
                    Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String getExtension(String filePath) {
        String ext;
        int dot = filePath.lastIndexOf(".");
        if (dot >= 0) {
            ext = filePath.substring(dot, filePath.length());
        } else {
            ext = "";
        }
        return ext;
    }

    public void load(String fileName) {
        if (changed) {
            int ret = JOptionPane.showConfirmDialog(parent, "Эта, как его, всё потрёте, дядя. Трём?", "Эта, предупреждениё", JOptionPane.YES_NO_OPTION );
            if (ret == JOptionPane.NO_OPTION) return;
        }
        /*if (!fileName.toLowerCase().endsWith(".tlv")) {
            fileName += ".tlv";
        }*/
        File file = new File(fileName);
        if (!file.exists()) {
            JOptionPane.showConfirmDialog(parent, "Эта, нету такова файла!", "Э...", JOptionPane.OK_CANCEL_OPTION );
            return;
        }
        if (!file.canRead()) {
            JOptionPane.showConfirmDialog(parent, "Нельзя читать энтот файл, дядя. Понял?", "Фига", JOptionPane.OK_CANCEL_OPTION );
            return;
        }
        loadTLW(file);
    }

    private boolean loadTLZ(File file) {
        InputStream fis = null;
        ZipFile zip = null;
        ZipEntry ze = null;
        try {
            zip = new ZipFile(file);
        } catch (ZipException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ze = zip.getEntry("Content.tlv");
            fis = zip.getInputStream(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fis == null) {
            try {
                zip.close();
            } catch (IOException ex) {
                Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        try {
            boolean ret = loadFromInputStream(fis, file.length());
            fis.close();
            if (ret) {
                backgroundImage = ObjectsChooserPanel.getImageFromZipFile(zip, "background", true);
                backgroundPath = file.getPath();
            }
            if (versionString.compareTo("1.4") == 0) {
                Image image;
                int i;
                platformPaletteImages.clear();
                if (platformPaletteImageArray != null) {
                    for (i = 0; i < platformPaletteImageArray.length; ++i)
                        platformPaletteImageArray[i] = null;
                    platformPaletteImageArray = null;
                }
                String imageName;
                for (i = 0; i < tempPlatformDecorationPaletteSize; ++i) {
                    imageName = "platform_decoration/image"+String.format("%03d", i);
                    image = ObjectsChooserPanel.getImageFromZipFile(zip, imageName, true);
                    addImageForPlatformPalette(image, imageName);
                }
            }
            zip.close();
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fis.close();
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return false;
    }


    private boolean loadTLW(File file) {
        FileInputStream fInput = null;
        try {
            fInput = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fInput == null) {
            return true;
        }
        try {
            if (loadFromInputStream(fInput, file.length())) {
                fInput.close();
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return false;
    }

    private boolean loadFromInputStream(InputStream fInput, long length) throws IOException {
        tempPlatformDecorationPaletteSize = -1;
        platforms.clear();

        String nameTemp;
        /*int namesCount = fInput.read();
        getObjectsChooserPanel().newNames.clear();
        int nameSize;
        for (int i =0; i < namesCount; ++i) {
            nameSize = fInput.read();
            byte[] buff = new byte[nameSize];
            fInput.read(buff);
            getObjectsChooserPanel().newNames.add(new String(buff));
        }
        getObjectsChooserPanel().updateTiles();*/
        int size;
        size = fInput.read();
        byte[] buff = new byte[size];
        fInput.read(buff);
        name = new String(buff);
        int tmp = fInput.read();
        if (tmp == 0) platformMode = false;
        else platformMode = true;
        worldWidth = fInput.read();
        worldHeight = fInput.read();
        tileWidth = fInput.read();
        tileHeight = fInput.read();

        int x=999, y=999, z=999, w=999, h=999;
        boolean found = false;
        long read = 6+name.length();
        do {
            if (read >= length) break;
            w = fInput.read();
            h = fInput.read();
            x = fInput.read();
            y = fInput.read();
            z = fInput.read();
            if (x != coordX || y != coordY || z != coordZ) {
                read += 5;
                fInput.skip(w*h*7);
                read += w*h*7;
            } else {
                found = true;
                break;
            }
        } while (read < length);

        if (found) {
            if (w == 999) {
                width = fInput.read();
            } else {
                width = w;
            }
            if (h == 999) {
                height = fInput.read();
            } else {
                height = h;
            }
            if (x == 999) {
                coordX = fInput.read();
            } else {
                coordX = x;
            }
            if (y == 999) {
                coordY = fInput.read();
            } else {
                coordY = y;
            }
            if (z == 999) {
                coordZ = fInput.read();
            } else {
                coordZ = z;
            }

            /*int endChecker = 0;
            while ((coordX != nextLevelX || coordY != nextLevelY)) {
                fInput.skip(width*height*4);
                endChecker = fInput.read();
                if (endChecker == 254) {
                    return false;
                } else {
                    coordX = endChecker;
                }
                //coordX = fInput.read();
                coordY = fInput.read();
            }*/
            content0 = new int[width][height];
            content1 = new int[width][height];
            content2 = new int[width][height];
            values = new int[width][height][4];
            changed = false;
            int sym;
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content0[x][y] = -1;
                    else content0[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content1[x][y] = -1;
                    else content1[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) content2[x][y] = -1;
                    else content2[x][y] = sym;
                }
            }
            for (y = 0; y < height; ++y) {
                for (x = 0; x < width; ++x) {
                    sym = fInput.read();
                    if (sym == 255) values[x][y][0] = -1;
                    else values[x][y][0] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][1] = 0;
                    else values[x][y][1] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][2] = 0;
                    else values[x][y][2] = sym;
                    sym = fInput.read();
                    if (sym == 255) values[x][y][3] = 0;
                    else values[x][y][3] = sym;
                }
            }
            return true;
        }
        return false;
    }

    public void setBackground(String name) {
        backgroundPath = name;
        backgroundImage = ObjectsChooserPanel.getImageAtPath((JPanel)parent, name);
    }

    /*public String getBackground() {
        return backgroundPath;
    }*/

    public void addPlatform(Platform platform) {
        changed = true;
        platforms.add(platform);
    }

    public int getPlatformCount() {
        return platforms.size();
    }

    public Iterator<Platform> getPlatformIterator() {
        return platforms.iterator();
    }



    public void addImageForPlatformPalette(Image newImage, String imagePath) {
        ImageForPalette image = new ImageForPalette();
        image.image = newImage;
        image.path = imagePath;
        platformPaletteImages.add(image);
        platformPaletteImageArray = platformPaletteImages.toArray(new ImageForPalette[0]);
    }

    public ImageForPalette getPlatformPaletteImage(int id) {
        if (id < 0 || id >= platformPaletteImageArray.length) return null;
        return platformPaletteImageArray[id];
    }

    public int getPlatformPaletteSize() {
        if (platformPaletteImageArray == null) return 0;
        return platformPaletteImageArray.length;
    }

    public void deleteImageFromPlatformPalette(int id){
        if (id == -1 || platformPaletteImages == null) return;
        platformPaletteImages.clear();
        int i;
        for (i = 0; i < platformPaletteImageArray.length; ++i){
            if (i != id)
                platformPaletteImages.add(platformPaletteImageArray[i]);
        }
        platformPaletteImageArray = platformPaletteImages.toArray(new ImageForPalette[0]);
        Iterator<Platform> platformIterator = platforms.iterator();
        Platform platform;
        int x, y, imgColumnsCount;
        byte item;
        while (platformIterator.hasNext()) {
            platform = platformIterator.next();
            imgColumnsCount = platform.getDecorationWidth();
            for (x = 0; x < imgColumnsCount; ++x) {
                for (y = 0; y < 3; ++y) {
                    item = platform.getDecoration(x, y);
                    if (item == id) {
                        platform.setDecor(x, y, (byte)-1);
                    } else if (item > id) {
                        platform.setDecor(x, y, (byte)(item-1));
                    }
                }
            }
        }
        if (parent instanceof EditorLevelPanel) {
            ((EditorLevelPanel)parent).repaint();
        }
    }
}
