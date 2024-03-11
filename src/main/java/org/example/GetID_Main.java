package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class GetID_Main {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String DATA = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\urls.json";


  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Hello world!");

    JsonNode input = MAPPER.readTree(new File(DATA));

    ObjectNode output = MAPPER.createObjectNode();

    input.fieldNames().forEachRemaining(type->{
      ArrayNode v = (ArrayNode) input.get(type);
      var out = output.putArray(type);

      v.forEach(u->{
        String url = u.asText();
        var slug = url.substring(url.lastIndexOf('/')+1);
        var o = MAPPER.createObjectNode();
        o.put("url", url);
        o.put("slug", slug);

        getId(type, slug, o);

        out.add(o);
      });
    });
    System.out.println(output.toPrettyString());
  }


  private static void getId(String type, String slug, ObjectNode o) {
    // Load the query
    // sub in the query

    // get the results

    // look for the slug

    var id = slug.substring(slug.lastIndexOf('-')+1);
    o.put("id", id);
  }
}