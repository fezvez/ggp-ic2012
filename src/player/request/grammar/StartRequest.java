package player.request.grammar;

import player.gamer.Gamer;
import util.game.Game;
import util.gdl.grammar.GdlProposition;
import util.logging.GamerLogger;

public final class StartRequest extends Request
{
	private final Game game;
	private final Gamer gamer;
	private final String matchId;
	private final int playClock;
	private final GdlProposition roleName;
	private final int startClock;

	public StartRequest(Gamer gamer, String matchId, GdlProposition roleName, Game theGame, int startClock, int playClock)
	{
		this.gamer = gamer;
		this.matchId = matchId;
		this.roleName = roleName;
		this.game = theGame;
		this.startClock = startClock;
		this.playClock = playClock;
	}
	
	@Override
	public String getMatchId() {
		return matchId;
	}
	
	public GdlProposition getRoleName () {
		return this.roleName;
	}

	@Override
	public String process(long receptionTime)
	{
		String result = "busy";
		// Finally, have the gamer begin metagaming.
		try {
			if (gamer.start(matchId, roleName, game, startClock, playClock, receptionTime)) {
				result = "ready";
			}
		} catch (Exception e) {		    
		    GamerLogger.logStackTrace("GamePlayer", e);

		    // Upon encountering an uncaught exception during metagaming,
		    // assume that indicates that we aren't actually able to play
		    // right now, and tell the server that we're busy.
			gamer.abort(matchId);
		}

		return result;
	}

	@Override
	public String toString()
	{
		return "start";
	}
}