package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Properties;

public class Verbalize_Main {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String DATA = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\fetched";
  private static final String OUTPUT = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\output";
  private static final String TEMPLATE_BASE = "C:\\dev\\Lions\\Extractor\\src\\main\\resources\\templates\\";


  public static void main(String[] args) throws IOException {
    System.out.println("VERBALIZE: " + DATA);
    System.out.println("Output   : " + OUTPUT);
    Properties vp = new Properties();
    vp.put("file.resource.loader.path", "");
    Velocity.init(vp);

    Files.walkFileTree(Paths.get(DATA), new FileVisitor());
  }


  static class FileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
      Path relative = Paths.get(DATA).relativize(file);
      String type = relative.getParent().getFileName().toString();
      System.out.format("Processing %s: %s (%d bytes)%n", type, relative, attr.size());

      // Load the file
      JsonNode json = MAPPER.readTree(file.toFile());

      // Open the output file
      File outFile = new File(OUTPUT, relative.toString().replace(".json", ".txt"));
      String verbalized = verbalizeVelocity(json, TEMPLATE_BASE + type + ".vm");
      write(outFile, verbalized);
      return FileVisitResult.CONTINUE;
    }


    private String verbalizeVelocity(JsonNode json, String template) throws IOException {
      // Convert the Jackon Node to a simple map
      Map<String, Object> map = MAPPER.convertValue(json, Map.class);

      // Get the template
      Template t = Velocity.getTemplate(template);

      VelocityContext context = new VelocityContext();
      context.put("_", Utils.class);
      if (map != null)
        map.entrySet().forEach(e -> context.put(e.getKey(), e.getValue()));

      Writer sw = new StringWriter();
      t.merge(context, sw);

      sw.flush();
      return sw.toString();
    }


    private void write(File file, String text) throws IOException {
      // Ensure the directories exist
      file.getParentFile().mkdirs();

      // And write the file
      Files.writeString(file.toPath(), text);
    }
  }
}