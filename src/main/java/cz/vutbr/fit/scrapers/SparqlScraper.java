/**
 * SparqlScraper.java
 *
 * Created on 6. 2. 2023, 19:05:38 by burgetr
 */
package cz.vutbr.fit.scrapers;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.RESOURCE;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;


/**
 * A web scraper that identifies the web page elements based on SPARQL queries.
 * 
 * @author burgetr
 */
public class SparqlScraper extends BaseExtractor
{
    private static Logger log = LoggerFactory.getLogger(SparqlScraper.class);

    /**
     * Creates the scrapper, initializes the RDF repository.
     */
    public SparqlScraper()
    {
        super(null); // in-memory repository
        //super("/tmp/repository"); // native repository
        
        // ensure the tags are defined for the repository
        checkTagDefs(getRepository());
    }
    
    /**
     * Renders the source page.
     * 
     * @param url source page URL
     * @return the rendered Page model
     * @throws IOException
     * @throws InterruptedException
     */
    public Page render(URL url) throws IOException, InterruptedException
    {
        // create and configure the service manager
        final ServiceManager manager = FLConfig.createServiceManager(getRepository());
        
        // call several services to create artifacts
        Artifact page = manager.applyArtifactService(
                "FitLayout.Playwright", 
                Map.of("url", url.toString(),
                        "width", 1200,
                        "height", 800,
                        "persist", 3,
                        "acquireImages", false,
                        "includeScreenshot", true),
                null);
        // the page must be saved before tagging in order to make the metadata
        // appear in the RDF repository
        manager.getArtifactRepository().addArtifact(page);
        
        log.info("Page: " + page.getIri());
        return (Page) page;
    }

    /**
     * Performs the preprocessing step - find visual areas, tag them and discover spatial relationships.
     * 
     * @param page the page to be preprocessed
     */
    public void preprocess(Page page)
    {
        // create and configure the service manager
        final ServiceManager manager = FLConfig.createServiceManager(getRepository());
        
        // Create basc areas
        Artifact atree1 = manager.applyArtifactService(
                "FitLayout.BasicAreas", 
                Map.of("preserveAuxAreas", true),
                page);
        
        // Tag areas by regular expressions
        var tagOp = manager.findAreaTreeOperators().get("FitLayout.Tag.Entities");
        tagOp.apply((AreaTree) atree1);
        
        // Store the area tree
        manager.getArtifactRepository().addArtifact(atree1);
        log.info("areaTree: " + atree1.getIri());
        
        // Create a chunk set
        Artifact cset = manager.applyArtifactService(
                "FitLayout.TextChunks", 
                Map.of("useWholeAreaText", true),
                atree1);
        
        // Store the chunk set
        manager.getArtifactRepository().addArtifact(cset);
        log.info("chunkSet: " + cset.getIri());
    }
    
    /**
     * Executes a sparql query and prints the resulting tuples to a print stream.
     * 
     * @param sparqlQuery the query to execute
     * @param out the stream to print the results to
     * @return a list of IRIs found in the results (so they may be processed in other way)
     */
    public List<IRI> execQuery(String sparqlQuery, PrintStream out)
    {
        List<IRI> ret = new ArrayList<>();
        var bindings = getRepository().getStorage().executeSparqlTupleQuery(sparqlQuery, true, 10, 0);
        Set<String> bindingNames = null;
        for (BindingSet b : bindings)
        {
            if (bindingNames == null)
                bindingNames = b.getBindingNames();
            
            for (String bname : bindingNames)
            {
                Value val = b.getValue(bname);
                if (val != null)
                {
                    if (val.isLiteral()) // literals are printed to output stream
                    {
                        out.print(bname + "=");
                        out.print(val.stringValue());
                        out.print("\t");
                    }
                    else if (val.isIRI()) // IRIs are added to the resulting list
                    {
                        ret.add((IRI) val);
                    }
                }
            }
            out.println();
        }
        return ret;
    }
    
    // ==================================================================================
    
    /**
     * Checks whether the tag definitions are loaded. If not, tries to load them
     * from tag_products.ttl.
     * 
     * @param repository the repository to check
     */
    protected void checkTagDefs(RDFArtifactRepository repository)
    {
        final ValueFactory vf = repository.getStorage().getValueFactory(); 
        final IRI tIri = vf.createIRI(RESOURCE.NAMESPACE, "tag-generic--title");
        final Value val = repository.getStorage().getPropertyValue(tIri, RDF.TYPE);
        if (val == null)
        {
            log.info("Initializing tag definitions");
            final IRI context = vf.createIRI("file://resources/rdf/tags_products.ttl");
            String rdfFile = BaseExtractor.loadResource("/tags_products.ttl");
            try
            {
                repository.getStorage().importTurtle(rdfFile, context);
            } catch (StorageException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
