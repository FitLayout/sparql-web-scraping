Scraping data from web pages using SPARQL queries
=================================================

(c) 2023 Radek Burget (burgetr@fit.vutbr.cz)

This code demonstrates the usage of SPARQL for extracting arbitrary data from web pages.

# Building

The demo application is built using Maven. Use `mvn package` for compiling all components. This will create the runnable `SparqlScraper.jar` archive in the `target` folder.

# Usage

```
java -jar SparqlScraper.jar SparqlScraper <sparql_file> <page_url>
```

See the [sparql](https://github.com/FitLayout/sparql-web-scraping/tree/main/sparql) folder for examples of SPARQL query definitions.

# Results

Sample results are available in the [results](https://github.com/FitLayout/sparql-web-scraping/tree/main/results) folder.
