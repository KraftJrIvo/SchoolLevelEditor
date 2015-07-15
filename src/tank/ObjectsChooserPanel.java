/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tank;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author IVO
 */

//SEREGA
public class ObjectsChooserPanel extends JPanel implements MouseListener, MouseMotionListener {
    private int imgColumnsCount = 0;
    private int imgLinesCount = 0;
    private String[] imageNames = new String[10];
    //private Image[] images = new Image[10];
    ArrayList<Image> images, tilesets;
    ArrayList<Rect> imagesRect;
    //private Rect[] imagesRect = new Rect[10];
    private int selectedImage = 2;
    private Grid grid = new Grid(false);
    private ChooserListener listener = null;
    FileHandler fileHandler;
    String currentDir = "default";
    boolean loaded = false;
    int imagesCount =0, tilesetsCount=0;

    public interface ChooserListener {
        void onChoose(int i);
    }

    public void setOnChooser(ChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        calculateImagesRect();
    }



    public void mouseClicked(MouseEvent e) {
    }

    public int getSelectedObject() {
        return selectedImage;
    }

    public Image getImage(int index) {
        if (index < 0) return null;
        if (index >= imagesCount) return tilesets.get(index-imagesCount);
        if (index >= imagesCount) return tilesets.get(index-imagesCount);
        return images.get(index);
    }

    public Image getCurrentImage() {
        return getImage(selectedImage);
    }

    private void mouseEvent(MouseEvent e) {
        if (imagesRect == null) return;
        int oldSelectedImage = selectedImage;
        selectedImage = -1;
        for (int i = 0; i<imagesRect.size(); ++i) {
            if (e.getX() >= imagesRect.get(i).left && e.getX() < imagesRect.get(i).right
                    && e.getY() >= imagesRect.get(i).top && e.getY() < imagesRect.get(i).bottom) {
                selectedImage = i;
                if (listener != null) listener.onChoose(selectedImage);
                if (oldSelectedImage != selectedImage) repaint();
                return;
            }
        }
        if (oldSelectedImage != selectedImage) {
            repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
        mouseEvent(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        mouseEvent(e);
    }

    public void mouseMoved(MouseEvent e) {
    }

    public static class Rect {
        public int left = 0;
        public int right = 0;
        public int top = 0;
        public int bottom = 0;
        public int width() {
            return right - left;
        }
        public int height() {
            return bottom - top;
        }
    }

    public void reload(String newDir) {
        loaded = false;
        currentDir = newDir;
        imgColumnsCount = 4;
        imgLinesCount = 3;
        images = new ArrayList<Image>();
        tilesets = new ArrayList<Image>();
        imagesRect = new ArrayList<Rect>();
        URLClassLoader urlLoader =
                (URLClassLoader)getClass().getClassLoader();
        int urlID = 0;
        for (int i=0; i<urlLoader.getURLs().length; ++i) {
            if (urlLoader.getURLs()[i].toString().contains("images\\"+currentDir)) {
                urlID = i;
                break;
            }
        }
        File dir = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+currentDir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    images.add(image);
                }
            }
            dir = new File(urlLoader.getURLs()[urlID].getPath()+"\\images\\"+currentDir+"\\tiles");
            directoryListing = dir.listFiles();;
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    tilesets.add(image);
                }
            }
        }
        imagesCount = images.size();
        tilesetsCount = tilesets.size();
        //calculateImagesRect();
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        addMouseListener((MouseListener)this);
        addMouseMotionListener((MouseMotionListener) this);
        loaded = true;
        calculateImagesRect();
    }

    public ObjectsChooserPanel(String str) {
        imgColumnsCount = 4;
        imgLinesCount = 10;
        images = new ArrayList<Image>();
        tilesets = new ArrayList<Image>();
        imagesRect = new ArrayList<Rect>();
        //chooser.setCurrentDirectory();
        File dir = new File(str);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    images.add(image);
                }
            }
            dir = new File(str+"\\tiles");
            directoryListing = dir.listFiles();
            for (File child : directoryListing) {
                Image image = getImage(this, child.getPath());
                if (image != null) {
                    tilesets.add(image);
                }
            }
        }
        imagesCount = images.size();
        tilesetsCount = tilesets.size();
        calculateImagesRect();
        grid.setSize(imgColumnsCount, imgLinesCount, getWidth(), getHeight());
        addMouseListener((MouseListener)this);
        addMouseMotionListener((MouseMotionListener)this);
        loaded = true;
    }

    private Rect getImageRect(int index) {
       Rect rect = new Rect();
       if (index < 0 || index >= imagesRect.size()) return rect;
       return imagesRect.get(index);
    }

    private void calculateImagesRect() {
        if (!loaded) return;
       if (imagesRect==null || grid.isNull()) return;
       int line, column, imageID;
       for (line = 0; line < imgLinesCount; ++line) {
           for (column = 0; column < imgColumnsCount; ++column) {
                imageID = line * imgColumnsCount + column;
                //if (imageID >= imagesRect.size()) return;
                imagesRect.add(new Rect());
                imagesRect.get(imageID).left = grid.getLineX(column);
                imagesRect.get(imageID).top = grid.getLineY(line);
                imagesRect.get(imageID).right = grid.getLineX(column + 1);
                imagesRect.get(imageID).bottom = grid.getLineY(line+1);
           }
       }
    }

    @Override
    public void paint(Graphics g) {
       if (imagesRect == null || imagesRect.size()==0 || images.get(0) == null) return;
       if (grid.isNull()) return;
       g.setColor(Color.white);
       g.fillRect(0, 0, getWidth(), getHeight());
       for (int imageID = 0; imageID< imagesCount +tilesetsCount; ++imageID) {
            if (imageID == selectedImage)
               g.setColor(Color.white);
            else
               g.setColor(Color.GRAY);
            g.fillRect(imagesRect.get(imageID).left, imagesRect.get(imageID).top, imagesRect.get(imageID).width(), imagesRect.get(imageID).height());
            if (imageID < imagesCount) {
                g.drawImage(images.get(imageID), imagesRect.get(imageID).left, imagesRect.get(imageID).top, imagesRect.get(imageID).width(), imagesRect.get(imageID).height(), this);
            } else {
                //hhh//images.get(imageID)
                //g.drawImage()
                int width = tilesets.get(imageID- imagesCount).getWidth(this)/3;
                int height = tilesets.get(imageID- imagesCount).getHeight(this)/4;
                //boolean left =
                g.drawImage(tilesets.get(imageID- imagesCount), imagesRect.get(imageID).left, imagesRect.get(imageID).top, imagesRect.get(imageID).right, imagesRect.get(imageID).bottom,
                        0, 0, width, height, this);
            }
            if (imageID == selectedImage)
                g.setColor(Color.red);
            else
                g.setColor(Color.black);
            g.drawRect(imagesRect.get(imageID).left, imagesRect.get(imageID).top, imagesRect.get(imageID).width(), imagesRect.get(imageID).height());
       }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 100);
    }

    public static Image getImage(Component component, String name) {
        if (!name.contains(".png"))return null;
        Image img = null;
        /*URLClassLoader urlLoader =
            (URLClassLoader)component.getClass().getClassLoader();
        URL fileLoc = urlLoader.findResource(name);*/
        img = component.getToolkit().createImage(name);
        //img = component.getToolkit().

        MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error loading image " + name);
            }
        } catch (Exception ex) { ex.toString(); }
        return img;
    }

    public static Image getImageAtPath(JPanel panel, String path) {
        Image img = null;
        URLClassLoader urlLoader =
            (URLClassLoader)panel.getClass().getClassLoader();
        //URL fileLoc = urlLoader.
        URL fileLoc;
        try {
            File file = new File(path);
            fileLoc = file.toURI().toURL();
            img = panel.getToolkit().createImage(fileLoc);

            MediaTracker tracker = new MediaTracker(panel);
            tracker.addImage(img, 0);
            try {
                tracker.waitForID(0);
                if (tracker.isErrorAny()) {
                    System.out.println("Error loading image " + path);
                }
            } catch (Exception ex) { ex.toString(); }
            return img;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Image getImageFromZipFile(ZipFile zip, String entryName, boolean startWith) {
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            if (!ze.getName().startsWith(entryName)) return null;
            fis = zip.getInputStream(ze);
            BufferedImage bImage = ImageIO.read(fis);
            fis.close();
            return bImage;
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Image getImageFromZip(String zipPath, String entryName, boolean startWith) {
        File file = new File(zipPath);
        if (!file.exists()) return null;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
            return getImageFromZipFile(zip, entryName, startWith);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void writeImageFromZipToZipOutputStream(ZipOutputStream zos, String zipPath,
            String entryName, boolean startWith) {
        File file = new File(zipPath);
        if (!file.exists()) return;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            entryName = ze.getName();
            fis = zip.getInputStream(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fis == null) {
            try {
                zip.close();
            } catch (IOException ex) {
                Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        ze = new ZipEntry(entryName);
        int bufferSize = 1024;
        byte[] buff = new byte[bufferSize];
        int buffRead;
        try {
            zos.putNextEntry(ze);
            while ((buffRead = fis.read(buff)) > 0) {
                zos.write(buff, 0, buffRead);
            }
            zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fis.close();
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static class ImageBufferAndName {
        byte[] buffer = null;
        String name;
    }

    public static ImageBufferAndName getImageFileFromZip(String zipPath, String entryName, boolean startWith) {
        ImageBufferAndName image = new ImageBufferAndName();
        File file = new File(zipPath);
        if (!file.exists()) return image;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
            return image;
        }
        InputStream fis = null;
        ZipEntry ze = null;
        try {
            Enumeration<? extends ZipEntry> enm = zip.entries();
            do {
                ze = enm.nextElement();
            } while (((startWith) ? !ze.getName().startsWith(entryName)
                    : ze.getName().compareTo(entryName) != 0) && enm.hasMoreElements());
            fis = zip.getInputStream(ze);
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fis == null) {
            try {
                zip.close();
            } catch (IOException ex) {
                Logger.getLogger(ObjectsChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return image;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buff = new byte[bufferSize];
        int buffRead;
        try {
            while ((buffRead = fis.read(buff)) > 0) {
                baos.write(buff, 0, buffRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        image.buffer = baos.toByteArray();
        image.name = ze.getName();
        try {
            fis.close();
            zip.close();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public static void writeImageBufferToZipOutputStream(ZipOutputStream zos, ImageBufferAndName image, int id) {
        ZipEntry ze = null;
        if (image.name.startsWith("platform_decoration/image")) {
            String ext = GameLevel.getExtension(image.name);
            image.name = "platform_decoration/image" + String.format("%03d", id) + ext;
        }
        ze = new ZipEntry(image.name);
        try {
            zos.putNextEntry(ze);
            zos.write(image.buffer, 0, image.buffer.length);
            zos.closeEntry();
        } catch (IOException ex) {
            Logger.getLogger(GameLevel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
