package um.wwebot.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
import um.wwebot.model.Event;
import um.wwebot.model.Match;

@Slf4j
@Component
public class XPathParser implements WWEBotParser{
	
	private final List<String> xpathChampionChecks;
	
	public XPathParser() {
		xpathChampionChecks = new LinkedList<>();
		xpathChampionChecks.add("/div/table/tbody/tr[count(preceding-sibling::*) = <rowNumber>]/td[count(preceding-sibling::*) = 2]/a/text()");
		xpathChampionChecks.add("/div/table/tbody/tr[count(preceding-sibling::*) = <rowNumber>]/td[count(preceding-sibling::*) = 1]/a/text()");
		xpathChampionChecks.add("/div/table/tbody/tr[count(preceding-sibling::*) = <rowNumber>]/td[count(preceding-sibling::*) = 2]/text()");
	}

	@Override
	@SneakyThrows
	public List<Champion> getChampionsFromSection(String section, String sectionName) {
		log.info("Parsing section {}", sectionName);
		log.debug("Parsing section: \n{}", section);
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(section.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);
        is.close();
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        /* Titles */
        /* XPath: /div/table/tbody/tr/td[count(preceding-sibling::*) = 0]/a/text() */
        XPathExpression titlesExpr = xpath.compile("/div/table/tbody/tr/td[count(preceding-sibling::*) = 0]/a/text()");
        Object titlesResult = titlesExpr.evaluate(doc, XPathConstants.NODESET);
        NodeList titleNodes = (NodeList) titlesResult;
     
        /* Champions */
        List<Champion> champions = new LinkedList<>();

        /*The page has different structure for each section.*/
        /* Case 1: Table structure: photo-name. XPath: "/div/table/tbody/tr/td[count(preceding-sibling::*) = 2]/a/text()" */
        /* Case 2: Table structure: name-photo. XPath: "/div/table/tbody/tr/td[count(preceding-sibling::*) = 1]/a/text()" */
        /* Case 3: Champion name without his own page (no <a>, just plain text). XPath: "/div/table/tbody/tr/td[count(preceding-sibling::*) = 2]/text()" */
        
        for (int i=1; i<=titleNodes.getLength(); i++){ //avoids header row
        	String titleName = titleNodes.item(i-1).getTextContent();
        	
        	String championName = null;
        	
        	for(String check: xpathChampionChecks) {
        		String caseExpr = check.replace("<rowNumber>", ""+i);
        		String checkResult = checkCase(caseExpr, xpath, doc, titleNodes);
        		
        		if(checkResult != null && !checkResult.isEmpty()) {
        			championName = checkResult;
        			break;
        		}
        	}
        	
        	if(championName == null) {
        		log.error("Cannot extract champion for title {} ", titleName);
        		throw new IllegalStateException("Cannot extract champion for title "+titleName);
        	}
        	
        	Champion foundChampion = new Champion(championName, titleName);
        	champions.add(foundChampion);
        	log.debug("Found Champion -> {}", foundChampion);
        }
        
        log.info("Returning champions for section {}", sectionName);
        return champions;
	}

	
	@SneakyThrows
	private String checkCase(String xpathExpr, XPath xpath, Document doc, NodeList titleNodes) {
		XPathExpression championsExpr = xpath.compile(xpathExpr);
        Object championsResult = championsExpr.evaluate(doc, XPathConstants.NODESET);
        NodeList championsNodes = (NodeList) championsResult;
        
        List<String> foundNames = new ArrayList<>();
        
        for(int i=0; i<championsNodes.getLength(); i++) {
    		String nodeText = championsNodes.item(i).getTextContent();
    		nodeText = nodeText.trim().replace("\n", "");
        	
    		if(!nodeText.isEmpty())
    			foundNames.add(championsNodes.item(i).getTextContent());
        }
        
        if(foundNames.size() == 0) {
        	return null;
        }
        
        if(foundNames.size() > 1) {
        	return foundNames.stream().collect(Collectors.joining(" and "));
//        	log.error("Too many elements found for expr:'{}', expected=1, found={}", xpathExpr, foundNames.size());
//        	throw new IllegalStateException("Too many elements found for expr:"+xpathExpr+". Found "+foundNames.size()+" elements");
        }
        	
    	return foundNames.get(0);
	}

	@Override
	@SneakyThrows
	public List<Event> getNextEventsFromSection(String section, int noOfEventsToFetch) {
		log.debug("Parsing next event section: \n{}", section);
		
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(section.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);
        is.close();
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        String nextEventLineBaseXPathExpr;
        List<Event> nextEventsList = new LinkedList<>();
        
        for(int i=1; i<=noOfEventsToFetch; i++) {
        	nextEventLineBaseXPathExpr = "/div/table[count(preceding-sibling::*) = 3]/tbody/tr[count(preceding-sibling::*) = "+i+"]";
        	
            XPathExpression eventDateExpr = xpath.compile(nextEventLineBaseXPathExpr+"/td[count(preceding-sibling::*) = 0]/span/text()");
            String eventDate = (String) eventDateExpr.evaluate(doc, XPathConstants.STRING);
            
            XPathExpression eventNameExpr = xpath.compile(nextEventLineBaseXPathExpr+"/td[count(preceding-sibling::*) = 1]/a/text()");
            String eventName = (String) eventNameExpr.evaluate(doc, XPathConstants.STRING);
            
            XPathExpression eventVenueExpr = xpath.compile(nextEventLineBaseXPathExpr+"/td[count(preceding-sibling::*) = 2]/a/text()");
            String eventVenue = (String) eventVenueExpr.evaluate(doc, XPathConstants.STRING);
  
            XPathExpression eventLocationExpr = xpath.compile(nextEventLineBaseXPathExpr+"/td[count(preceding-sibling::*) = 3]/a/text()");
            String eventLocation = (String) eventLocationExpr.evaluate(doc, XPathConstants.STRING);
            
            XPathExpression eventPageUrlExpr = xpath.compile(nextEventLineBaseXPathExpr+"/td[count(preceding-sibling::*) = 1]/a/@href");
            String eventPageUrl = (String) eventPageUrlExpr.evaluate(doc, XPathConstants.STRING);
            eventPageUrl = eventPageUrl.substring(6); //Remove '/wiki/'
            
            Event e = new Event(eventName, eventDate, eventVenue, eventLocation, eventPageUrl);
            log.debug("Found event{}", e);
            nextEventsList.add(e);
        }
        
        return nextEventsList;
	}

	@Override
	@SneakyThrows
	public List<Match> getMatchesFromEventSection(String section) {
		log.debug("Parsing next event matches section: \n{}", section);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(section.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(is);
        is.close();
 
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        
        /* Matches */
        /* XPath: /div/table/tbody/tr*/
        XPathExpression matchesExpr = xpath.compile("/div/table/tbody/tr");
        Object matchesResult = matchesExpr.evaluate(doc, XPathConstants.NODESET);
        NodeList matchesNodes = (NodeList) matchesResult; //matches rows
        
        List<Match> matches = new ArrayList<>();
        
        for (int i=1; i<matchesNodes.getLength()-1; i++){ //avoids header row and last row
        	String participants = matchesNodes.item(i).getChildNodes().item(3).getTextContent(); //tr/td[3]
        	String stipulation = matchesNodes.item(i).getChildNodes().item(5).getTextContent();  //tr/td[5]
        	
        	int supStartIndex = stipulation.indexOf("[");
        	if(supStartIndex != -1)
        		stipulation = stipulation.substring(0, stipulation.indexOf("[")); //Removing the <sup>
        	
        	log.debug("Match found: {} {}", stipulation ,participants);
        	matches.add(new Match(i, participants, stipulation));
        }
        
		return matches;
	}
}
