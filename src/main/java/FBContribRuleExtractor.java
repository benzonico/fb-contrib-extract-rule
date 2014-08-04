import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class FBContribRuleExtractor {
  static Map<String, String> categories = new HashMap<>();
  public static final String FINDBUGS_XML = "/home/benzonico/Development/SonarSource/fb-contrib/etc/findbugs.xml";
  public static final String MESSAGES_XML = "/home/benzonico/Development/SonarSource/fb-contrib/etc/messages.xml";

  public static void main(String args[]) {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    try {
      extractCategories(factory);
      XMLStreamReader reader;
      PrintStream ps = System.out;
      ps.println("<rules>");
      ps.println("<!-- fb contrib version 6.0.0 -->");
      reader = factory.createXMLStreamReader(new FileInputStream(MESSAGES_XML));
      Rule currentRule = null;
      while (reader.hasNext()) {
        int event = reader.next();
        switch (event) {
          case XMLStreamConstants.START_ELEMENT:
            if("BugPattern".equals(reader.getLocalName())){
              currentRule = new Rule();
              currentRule.key = reader.getAttributeValue("", "type");
            }
            if("ShortDescription".equals(reader.getLocalName()) && currentRule != null){
              currentRule.title = categories.get(currentRule.key)+" - " +reader.getElementText();
            }
            if("Details".equals(reader.getLocalName()) && currentRule != null){
              currentRule.description = reader.getElementText();
            }
            break;
          case XMLStreamConstants.END_ELEMENT:
            if("BugPattern".equals(reader.getLocalName())){
              ps.println(currentRule);
              currentRule = null;
            }
            break;
        }
      }
      System.out.println("</rules>");
    } catch (XMLStreamException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  private static class Rule {
    String key;
    String title;
    String description;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("<rule key=\"").append(key).append("\" priority=\"INFO\">\n");
      sb.append("<name><![CDATA[").append(title).append("]]></name>\n");
      sb.append("<configKey><![CDATA[").append(key).append("]]></configKey>\n");
      sb.append("<description><![CDATA[").append(description).append("]]></description>\n");
      sb.append("</rule>\n");
      return sb.toString();
    }
  }

  private static void extractCategories(XMLInputFactory factory) throws XMLStreamException, FileNotFoundException {
    XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(FINDBUGS_XML));
    while (reader.hasNext()) {
      int event = reader.next();
      if (event == XMLStreamConstants.START_ELEMENT && "BugPattern".equals(reader.getLocalName())) {
        //<BugPattern abbrev="ISB" type="ISB_INEFFICIENT_STRING_BUFFERING" category="PERFORMANCE" />
        String category = reader.getAttributeValue("", "category").toLowerCase();
        category = category.substring(0, 1).toUpperCase() + category.substring(1);
        categories.put(reader.getAttributeValue("", "type"), category);
      }
    }
  }
}
