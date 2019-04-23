package um.wwebot.rest.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor
public class WikipediaClient {
	
	private String allSectionAPI = "https://en.wikipedia.org/w/api.php?action=parse&prop=sections&page=<pageName>";
	private String specificSectionAPI = "https://en.wikipedia.org/w/api.php?action=parse&prop=text&section=<sectionNumber>&page=<pageName>&format=json";

	public String getSection(String pageName, String sectionNumber) {
		log.info("Retrieving section {} of page {} from Wikipedia API", sectionNumber, pageName);
		String fullURL = specificSectionAPI.replace("<sectionNumber>", sectionNumber).replace("<pageName>", pageName);
		
		log.info("Calling URL: {}", fullURL);
		RestTemplate restTemplate = new RestTemplate();
        String section = restTemplate.getForObject(fullURL, String.class);
        log.debug("Response from Wikipedia API: \n {}", section);
        log.info("Retrieved section {} of page {} from Wikipedia API", sectionNumber, pageName);
        
        DocumentContext jsonContext = JsonPath.parse(section);
		String textPath = "$.parse.text.['*']";
		String sectionHtml = jsonContext.read(textPath);
        log.info("Section HTML retrieved from Wikipedia API: \n {}", fullURL);
        
        return sectionHtml; 
	}
}
