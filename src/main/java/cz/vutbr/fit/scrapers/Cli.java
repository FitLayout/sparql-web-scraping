/**
 * Cli.java
 *
 * Created on 6. 2. 2023, 19:07:46 by burgetr
 */
package cz.vutbr.fit.scrapers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.rdf.model.RDFTextChunk;
import cz.vutbr.fit.scrapers.util.ImageOutput;

/**
 * 
 * @author burgetr
 */
public class Cli
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: SparqlScraper <sparql_file> <url>");
            System.exit(1);
        }
        
        String sparqlQuery = null;
        String urlString = args[1];
        
        // load the SPARQL query from file
        try {
            sparqlQuery = loadFile(args[0]);
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't open " + args[0]);
            System.exit(2);
        }
        
        // perform the extraction
        try {
            SparqlScraper scraper = new SparqlScraper();
            
            var page = scraper.render(new URL(urlString));
            scraper.preprocess(page);
            
            var iris = scraper.execQuery(sparqlQuery, System.out);
            
            drawImageOutput(scraper, iris, "screenshot.png");
            System.err.println("Image saved to screenshot.png");
            
            scraper.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String loadFile(String filePath) throws FileNotFoundException
    {
        try (Scanner scanner = new Scanner(new FileInputStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }

    private static void drawImageOutput(SparqlScraper scraper, List<IRI> chunkIRIs, String filename) throws IOException
    {
        Page page = (Page) scraper.load("http://fitlayout.github.io/resource/art1");
        ChunkSet cset = (ChunkSet) scraper.load("http://fitlayout.github.io/resource/art3");
        ImageOutput imgOut = new ImageOutput(filename, page.getWidth(), page.getHeight());
        imgOut.showPage(page);

        for (IRI iri : chunkIRIs)
        {
            var chunk = findTextChunk(cset, iri);
            if (chunk != null)
            {
                imgOut.getDisplay().colorizeByClass(chunk, "value");
            }
        }
        
        imgOut.close();
    }
    
    private static TextChunk findTextChunk(ChunkSet cset, IRI chunkIri)
    {
        for (TextChunk chunk : cset.getTextChunks())
        {
            if (chunk instanceof RDFTextChunk && ((RDFTextChunk) chunk).getIri().equals(chunkIri))
            {
                return chunk;
            }
        }
        return null;
    }

}
