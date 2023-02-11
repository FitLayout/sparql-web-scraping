# Scraping data from web pages using SPARQL queries: Sample results

This repository contains the sample results of using SPARQL for scraping data from the web in two application scenarios:

## Extraction of multiple records from a single website

We demonstrate the extraction of multiple repeating records from the [Cast tables on IMDb](https://www.imdb.com/title/tt6468322/fullcredits/#cast).

- Tagger definitions: [tags_movies.ttl](https://github.com/FitLayout/sparql-web-scraping/blob/main/src/main/resources/tags_movies.ttl)
- SPARQL query: [cast.sparql](https://github.com/FitLayout/sparql-web-scraping/blob/main/sparql/cast.sparql)
- Results folder (extracted data with URLs and screenshots): [movies](https://github.com/FitLayout/sparql-web-scraping/tree/main/results/movies)

## Extraction from Different Web Sites

We extract product title and price from different e-commerce websites (the URLs are provided in the extraction results).

- Tagger definitions: [tags_products.ttl](https://github.com/FitLayout/sparql-web-scraping/blob/main/src/main/resources/tags_products.ttl)
- SPARQL query: [products.sparql](https://github.com/FitLayout/sparql-web-scraping/blob/main/sparql/products.sparql)
- Results folder (extracted data with URLs and screenshots): [products](https://github.com/FitLayout/sparql-web-scraping/tree/main/results/products)
