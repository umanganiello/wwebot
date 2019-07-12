package um.wwebot.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class Event {
	private String name;
	private String date;
	private String venue;
	private String location;
	private String pageUrl;
	
	public String toBotString() {
		return new 	StringBuilder()
					.append("*")
					.append(name)
					.append("*")
					.append("\n")
					.append(date)
					.append(" @ ")
					.append(venue)
					.append(" - ")
					.append(location)
					.toString();
	}
}
