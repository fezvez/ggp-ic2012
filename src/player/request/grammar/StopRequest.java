package player.request.grammar;

import java.util.List;

import player.gamer.Gamer;
import util.gdl.grammar.GdlSentence;

public final class StopRequest extends Request
{
	private final Gamer gamer;
	private final String matchId;
	private final List<GdlSentence> moves;

	public StopRequest(Gamer gamer, String matchId, List<GdlSentence> moves)
	{
		this.gamer = gamer;
		this.matchId = matchId;
		this.moves = moves;
	}
	
	@Override
	public String getMatchId() {
		return matchId;
	}

	@Override
	public String process(long receptionTime)
	{
		if(gamer.stop(matchId, moves)) {
			return "done";
		}
		return "busy";
	}

	@Override
	public String toString()
	{
		return "stop";
	}
}