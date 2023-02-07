/**
 * ImageOutput.java
 *
 * Created on 25. 12. 2021, 12:42:14 by burgetr
 */
package cz.vutbr.fit.scrapers.util;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import cz.vutbr.fit.layout.io.ImageOutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * Methods for producing annotated page images.
 * 
 * @author burgetr
 */
public class ImageOutput implements Closeable
{
    private OutputStream osi;
    private ImageOutputDisplay disp;

    public ImageOutput(String path, int width, int height) throws FileNotFoundException
    {
        osi = new FileOutputStream(path);
        disp = new ImageOutputDisplay(width, height);
    }
    
    public ImageOutputDisplay getDisplay()
    {
        return disp;
    }

    public void showPage(Page page)
    {
        disp.drawPage(page, false);
    }

    public void showPage(Page page, boolean useScreenshot)
    {
        disp.drawPage(page, useScreenshot);
    }

    public void showAreas(Area root, String nameSubstring)
    {
        if (nameSubstring == null || root.toString().contains(nameSubstring))
            disp.drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAreas(root.getChildAt(i), nameSubstring);
    }
    
    public void colorizeAreasByTags(Area root)
    {
        disp.colorizeByTags(root, root.getTags().keySet());
        for (int i = 0; i < root.getChildCount(); i++)
            colorizeAreasByTags(root.getChildAt(i));
    }
    
    public void colorizeChunksByTags(Collection<TextChunk> chunks)
    {
        for (TextChunk chunk : chunks)
        {
            disp.colorizeByTags(chunk, chunk.getTags().keySet());
        }
    }
    
    public void drawConnections(Collection<AreaConnection> conns, String filterName, float minWeight, java.awt.Color color)
    {
        for (AreaConnection con : conns)
        {
            if (con.getWeight() >= minWeight &&
                    (filterName == null || filterName.equals(con.getRelation().getName())))
            {
                disp.drawConnection(con.getA1(), con.getA2(), color);
            }
        }
    }
    
    @Override
    public void close() throws IOException
    {
        disp.saveTo(osi);
        osi.close();
    }

}
