package um.wwebot.model;

public enum WWEBotCommand {
	CHAMPIONS("/champions"),
	NEXT_PPV("/nextppv"),
	TITLE_HOLDERS("/titles");
	
	private String cmdPath;

	private WWEBotCommand(String cmdPath) {
        this.cmdPath = cmdPath;
    }
	
	public String getCmdPath() {
		return cmdPath;
	}
	
	public static WWEBotCommand lookup(String cmdPath) {
		for(WWEBotCommand cmd : WWEBotCommand.values()) {
			if(cmd.cmdPath.equals(cmdPath))
				return cmd;
		}
		return null;
	}
}