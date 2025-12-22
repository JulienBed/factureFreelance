package com.facture.service;

import com.facture.entity.Invoice;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OpenSearchService {

    private static final Logger logger = Logger.getLogger(OpenSearchService.class);

    @ConfigProperty(name = "opensearch.host", defaultValue = "localhost")
    String opensearchHost;

    @ConfigProperty(name = "opensearch.port", defaultValue = "9200")
    int opensearchPort;

    @ConfigProperty(name = "opensearch.scheme", defaultValue = "http")
    String opensearchScheme;

    @ConfigProperty(name = "opensearch.index.invoices", defaultValue = "invoices")
    String invoicesIndex;

    private RestHighLevelClient client;

    @PostConstruct
    void initialize() {
        try {
            // Create OpenSearch client
            client = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(opensearchHost, opensearchPort, opensearchScheme)
                )
            );

            // Create index if it doesn't exist
            createIndexIfNotExists();

            logger.infof("OpenSearch client initialized successfully. Connected to %s://%s:%d",
                opensearchScheme, opensearchHost, opensearchPort);
        } catch (Exception e) {
            logger.errorf("Failed to initialize OpenSearch client: %s", e.getMessage(), e);
        }
    }

    @PreDestroy
    void cleanup() {
        if (client != null) {
            try {
                client.close();
                logger.info("OpenSearch client closed successfully");
            } catch (IOException e) {
                logger.errorf("Error closing OpenSearch client: %s", e.getMessage(), e);
            }
        }
    }

    private void createIndexIfNotExists() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(invoicesIndex);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(invoicesIndex);

            // Define mapping for invoice documents
            String mapping = """
                {
                  "properties": {
                    "invoiceId": { "type": "long" },
                    "invoiceNumber": { "type": "keyword" },
                    "clientName": { "type": "text" },
                    "userId": { "type": "long" },
                    "status": { "type": "keyword" },
                    "total": { "type": "double" },
                    "issueDate": { "type": "date" },
                    "dueDate": { "type": "date" },
                    "pdfContent": { "type": "text", "analyzer": "french" },
                    "indexedAt": { "type": "date" }
                  }
                }
                """;

            createIndexRequest.mapping(mapping, XContentType.JSON);
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            logger.infof("Created OpenSearch index: %s", invoicesIndex);
        } else {
            logger.infof("OpenSearch index already exists: %s", invoicesIndex);
        }
    }

    /**
     * Index a PDF invoice in OpenSearch
     * @param invoice The invoice entity
     * @param pdfBytes The PDF file as byte array
     */
    public void indexInvoice(Invoice invoice, byte[] pdfBytes) {
        try {
            // Extract text from PDF using Apache Tika
            String pdfContent = extractTextFromPdf(pdfBytes);

            // Create document to index
            Map<String, Object> document = new HashMap<>();
            document.put("invoiceId", invoice.id);
            document.put("invoiceNumber", invoice.invoiceNumber);
            document.put("clientName", invoice.client != null ? invoice.client.companyName : "");
            document.put("userId", invoice.user.id);
            document.put("status", invoice.status.toString());
            document.put("total", invoice.total);
            document.put("issueDate", invoice.issueDate.toString());
            document.put("dueDate", invoice.dueDate.toString());
            document.put("pdfContent", pdfContent);
            document.put("indexedAt", java.time.Instant.now().toString());

            // Index the document
            IndexRequest indexRequest = new IndexRequest(invoicesIndex)
                .id(invoice.id.toString())
                .source(document, XContentType.JSON);

            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);

            logger.infof("Indexed invoice %s with result: %s", invoice.invoiceNumber, response.getResult());
        } catch (Exception e) {
            logger.errorf("Error indexing invoice %s: %s", invoice.invoiceNumber, e.getMessage(), e);
        }
    }

    /**
     * Extract text content from PDF using Apache Tika
     */
    private String extractTextFromPdf(byte[] pdfBytes) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler(-1); // No limit
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            parser.parse(inputStream, handler, metadata, context);
        }

        return handler.toString();
    }

    /**
     * Search invoices by query string
     * @param userId The user ID to filter results
     * @param query The search query
     * @return List of invoice IDs matching the query
     */
    public List<Long> searchInvoices(Long userId, String query) {
        List<Long> invoiceIds = new ArrayList<>();

        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // Multi-match query across multiple fields
            sourceBuilder.query(
                QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("userId", userId))
                    .must(
                        QueryBuilders.multiMatchQuery(query,
                            "invoiceNumber", "clientName", "pdfContent")
                            .fuzziness("AUTO")
                    )
            );

            sourceBuilder.size(100); // Limit to 100 results
            sourceBuilder.fetchSource(new String[]{"invoiceId"}, null);

            SearchRequest searchRequest = new SearchRequest(invoicesIndex);
            searchRequest.source(sourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if (sourceAsMap.containsKey("invoiceId")) {
                    Object invoiceIdObj = sourceAsMap.get("invoiceId");
                    if (invoiceIdObj instanceof Number) {
                        invoiceIds.add(((Number) invoiceIdObj).longValue());
                    }
                }
            }

            logger.infof("Search for query '%s' returned %d results", query, invoiceIds.size());
        } catch (Exception e) {
            logger.errorf("Error searching invoices: %s", e.getMessage(), e);
        }

        return invoiceIds;
    }
}
