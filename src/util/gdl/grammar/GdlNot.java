package util.gdl.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class GdlNot extends GdlLiteral
{

	private final GdlLiteral body;
	private transient Boolean ground;

	GdlNot(GdlLiteral body)
	{
		this.body = body;
		ground = null;
	}

	public GdlLiteral getBody()
	{
		return body;
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = body.isGround();
		}

		return ground;
	}

	@Override
	public String toString()
	{
		return "( not " + body + " )";
	}

	@Override
	public GdlNot replace(Map<Gdl, Gdl> replacementMap) {
		Gdl newBody = Gdl.applyReplacement(this.body, replacementMap);
		
		if (newBody != this.body) {
			return GdlPool.getNot((GdlLiteral) newBody);
		}
		
		return this;
	}

	@Override
	public List<Gdl> getChildren() {
		List<Gdl> result = new ArrayList<Gdl>();
		
		result.add(this.body);
		
		return result;
	}
}
