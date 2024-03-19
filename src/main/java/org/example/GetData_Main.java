package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class GetData_Main {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String DATA = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\fetch.json";
  private static final String QUERY_BASE = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\getData";


  public static void main(String[] args) throws IOException {
    JsonNode input = MAPPER.readTree(new File(DATA));
    Path outDir = Paths.get(Paths.get(DATA).getParent().toFile().toString(), "fetched");
    outDir.toFile().mkdirs();
    System.out.println("OUTPUT: " + outDir);

    input.fieldNames().forEachRemaining(type -> {
      // TODO - skip reports for now
      if (type.equals("report"))
        return;

      ArrayNode v = (ArrayNode) input.get(type);

      // Load the query
      try {
        String query = Files.readString(Paths.get(QUERY_BASE, type + ".gql"), StandardCharsets.UTF_8);

        v.forEach(d -> {
          var id = d.get("id").asText();
          var slug = d.get("slug").asText();
          var url = d.get("url").asText();
          File out = new File(new File(outDir.toFile(), type), slug + ".json");
          out.getParentFile().mkdir();
          System.out.printf("Get %s: %s (%s)%n", type, id, out);
          String q = query.replace("${ID}", id);
          try {
            var ret = fetch(type, out, q);
            if (ret.isMissingNode()) {
              System.out.printf("NO DATA: %s (%s)%n", slug, url);
            } else {
              var s = ret.get("slug");
              if (s == null) {
                System.out.printf("SLUG MISSING: %s (%s)%n", slug, url);
              } else if (!s.asText().equals(slug)) {
                System.out.printf("SLUG MISMATCH: %s=>%s (%s)%n", slug, s, url);
              }
            }
          } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }


  private static JsonNode fetch(String type, File out, String query) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(120)).build();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://graphql-demo.lovethework.com"))
        .POST(HttpRequest.BodyPublishers.ofString(query))
        .header("x-api-key", "5836312f-fddd-4d5a-96b9-7ebb38f0ebcc")
        .build();
    var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    if (response.statusCode() != 200) {
      System.out.printf("FAILED - %d: %s%n", response.statusCode(), query);
      return MAPPER.missingNode();
    }
    var json = MAPPER.readTree(response.body());

    String jp = switch (type) {
//      case "report": jp = "/data/getTalksByIds/0";
//        break;
      case "talk" -> "/data/getTalksByIds/0";
      case "brand" -> "/data/getBrandsByIds/0";
      case "agency" -> "/data/getAgenciesByIds/0";
      case "entry" -> "/data/getEntriesByIds/0";
      case "campaign" -> "/data/getCampaignsByIds/0";
      case "individual" -> "/data/getPortfoliosByIds/0";
      case "inspiration" -> "/data/inspirationForId";
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
    var item = json.at(jp);
    MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, item);
    return item;
  }
}