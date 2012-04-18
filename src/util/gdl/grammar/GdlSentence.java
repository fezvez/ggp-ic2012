package util.gdl.grammar;

import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class GdlSentence extends GdlLiteral
{

	public abstract int arity();

	public abstract GdlTerm get(int index);

	public abstract GdlConstant getName();

	@Override
	public abstract boolean isGround();

	@Override
	public abstract String toString();

	public abstract GdlTerm toTerm();
	
	@Override
	public abstract GdlSentence replace(Map<Gdl, Gdl> replacementMap);
	
	//TODO: remove if not needed
	public abstract List<GdlTerm> getBody();

}
