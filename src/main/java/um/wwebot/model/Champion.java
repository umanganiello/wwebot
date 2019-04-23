package um.wwebot.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Champion {
	private String name;
	private String title;
	
	public String toString() {
		return title + ": " + name;
	}
}
