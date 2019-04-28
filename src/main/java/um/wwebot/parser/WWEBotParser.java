package um.wwebot.parser;

import java.util.List;

import um.wwebot.model.Champion;
import um.wwebot.model.Event;

public interface WWEBotParser {
	List<Champion> getChampionsFromSection(String section, String sectionName);
	List<Event> getNextEventsFromSection(String section, int noOfEventsToFetch);
}
