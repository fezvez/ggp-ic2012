package util.gdl.grammar;

import java.util.Map;

@SuppressWarnings("serial")
public abstract class GdlTerm extends Gdl
{

	@Override
	public abstract boolean isGround();

	public abstract GdlSentence toSentence();

	@Override
	public abstract String toString();

	@Override
	public abstract GdlTerm replace(Map<Gdl, Gdl> replacementMap);
}
