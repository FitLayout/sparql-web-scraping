/**
 * BaseExtractor.java
 *
 * Created on 14. 9. 2022, 9:12:01 by burgetr
 */
package cz.vutbr.fit.scrapers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;

/**
 * Common utility functions for experimental extractors.
 * 
 * @author burgetr
 */
public class BaseExtractor
{
    private RDFArtifactRepository repository;
    
    /**
     * Creates the extractor and initializes the RDF repositoy.
     * 
     * @param repoPath RDF repository path for native repositories or {@code null} for
     * an in-memory repository.
     */
    public BaseExtractor(String repoPath)
    {
        if (repoPath == null)
            repository = RDFArtifactRepository.createMemory(null);
        else
            repository = RDFArtifactRepository.createNative(repoPath);
    }

    public RDFArtifactRepository getRepository()
    {
        return repository;
    }

    public void setRepository(RDFArtifactRepository repository)
    {
        this.repository = repository;
    }
    
    public void close()
    {
        repository.disconnect();
    }
    
    public String getPrefixes()
    {
        return repository.getIriDecoder().declarePrefixes();
    }
    
    public Artifact load(IRI iri)
    {
        Artifact ret = repository.getArtifact(iri);
        return ret;
    }
    
    public Artifact load(String iriString)
    {
        return load(repository.getStorage().getValueFactory().createIRI(iriString));
    }
    
    public void queryToCSV(String queryStr, OutputStream out)
    {
        repository.getStorage().queryExportCSV(queryStr, out);
    }
    
    public List<IRI> getIRIsByQuery(String queryString)
    {
        var bindings = repository.getStorage().executeSafeTupleQuery(queryString);
        List<IRI> ret = new ArrayList<>(bindings.size());
        for (var binding : bindings)
        {
            var iri = binding.getValue("iri");
            if (iri.isIRI())
                ret.add((IRI) iri);
        }
        return ret;
    }
    
    public List<IRI> getIRIsByType(IRI typeIri)
    {
        final String q = getPrefixes() + " SELECT ?iri WHERE { ?iri rdf:type <" + String.valueOf(typeIri) +"> }";
        return getIRIsByQuery(q);
    }
    
    public static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(RDFArtifactRepository.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }
    
}
