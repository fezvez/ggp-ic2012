package player.request.grammar;

import java.util.List;

import player.gamer.Gamer;
import util.gdl.grammar.GdlSentence;
import util.logging.GamerLogger;

public final class PlayRequest extends Request
{
	private final Gamer gamer;
	private final String matchId;
	private final List<GdlSentence> moves;

	public PlayRequest(Gamer gamer, String matchId, List<GdlSentence> moves)
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
	    try {
			GdlSentence move = gamer.play(matchId, moves, receptionTime);
			if (move == null) return "nil";
			return move.toString();
		} catch (Exception e) {
		    GamerLogger.logStackTrace("GamePlayer", e);
			return "nil";
		}
	}

	@Override
	public String toString()
	{
		return "play";
	}
}