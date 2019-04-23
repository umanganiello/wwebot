package um.wwebot.parser;

import java.util.List;

import um.wwebot.model.Champion;

public interface WWEBotParser {
	List<Champion> getChampionsFromSection(String section, String sectionName);
}
