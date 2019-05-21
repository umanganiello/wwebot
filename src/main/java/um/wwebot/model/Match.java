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
public class Match {
	private int matchPositionInCard;
	private String participants;
	private String stipulation;
	
	public String toBotString() {
		return new StringBuilder()
				.append(matchPositionInCard)
				.append(". *")
				.append(stipulation)
				.append("*")
				.append("\n")
				.append(participants)
				.toString();
	}
}
