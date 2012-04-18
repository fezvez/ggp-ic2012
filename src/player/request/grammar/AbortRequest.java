package player.request.grammar;

import player.gamer.Gamer;

public final class AbortRequest extends Request
{
	private final Gamer gamer;
	private final String matchId;

	public AbortRequest(Gamer gamer, String matchId)
	{
		this.gamer = gamer;
		this.matchId = matchId;
	}
	
	@Override
	public String getMatchId() {
		return matchId;
	}

	@Override
	public String process(long receptionTime)
	{
		if (gamer.abort(matchId)) return "aborted";
		return "busy";
	}

	@Override
	public String toString()
	{
		return "abort";
	}
}