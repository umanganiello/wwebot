package um.wwebot.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Event {
	private String name;
	private String date;
	private String venue;
	private String location;
	
	public String toString() {
		return name + ": " + date + " @ " + venue + " - " + location;
	}
}
