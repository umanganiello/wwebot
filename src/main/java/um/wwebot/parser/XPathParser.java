package um.wwebot.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import um.wwebot.model.Champion;

@Slf4j
@Component
public class XPathParser implements WWEBotParser{

	@Override
	@SneakyThrows
	public List<Champion> getChampionsFromSection(String section, String sectionName) {
		log.info("Parsing section {}", sectionName);
		log.debug("Parsing section: \n{}", section);
		
		// Titles: /div/table/tbody/tr/td[count(preceding-sibling::*) = 0]/a/text()
		// Champions: /div/table/tbody/tr/td[count(preceding-sibling::*) = 2]/a/text()

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(section.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        /*Titles*/
        XPathExpression titlesExpr = xpath.compile("/div/table/tbody/tr/td[count(preceding-sibling::*) = 0]/a/text()");
        Object titlesResult = titlesExpr.evaluate(doc, XPathConstants.NODESET);
        NodeList titleNodes = (NodeList) titlesResult;
        for (int i=0; i<titleNodes.getLength(); i++)
        	log.debug("Title: {}", titleNodes.item(i));
        
        /*Champions*/
        XPathExpression championsExpr = xpath.compile("/div/table/tbody/tr/td[count(preceding-sibling::*) = 2]/a/text()");
        Object championsResult = championsExpr.evaluate(doc, XPathConstants.NODESET);
        NodeList championsNodes = (NodeList) championsResult;
        if(championsNodes.getLength() == 0) {
        	//try name-photo inverted
        	championsExpr = xpath.compile("/div/table/tbody/tr/td[count(preceding-sibling::*) = 1]/a/text()");
        	championsResult = championsExpr.evaluate(doc, XPathConstants.NODESET);
        	championsNodes = (NodeList) championsResult;
        }
        for (int i=0; i<championsNodes.getLength(); i++)
        	log.debug("Champion: {}", championsNodes.item(i));
        
        if(titleNodes.getLength() != championsNodes.getLength()) {
        	//TODO: try champion name without his own page (no <a>)
        	
        	log.error("#titles != #champions! for section {}: #titles={} #champions={}", sectionName, titleNodes.getLength(), championsNodes.getLength());
        	throw new IllegalStateException("#titles != #champions!");
        }
        else {
        	List<Champion> champions = new LinkedList<>();
        	
            for (int i = 0; i < titleNodes.getLength(); i++) {
                champions.add(new Champion(championsNodes.item(i).getTextContent(), titleNodes.item(i).getTextContent()));
            }
            return champions;
        }
	}
}
