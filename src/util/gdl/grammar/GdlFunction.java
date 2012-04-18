package util.gdl.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class GdlFunction extends GdlTerm
{

	private final List<GdlTerm> body;
	private transient Boolean ground;
	private final GdlConstant name;

	GdlFunction(GdlConstant name, List<GdlTerm> body)
	{
		this.name = name;
		this.body = body;
		ground = null;
	}

	public int arity()
	{
		return body.size();
	}

	private boolean computeGround()
	{
		for (GdlTerm term : body)
		{
			if (!term.isGround())
			{
				return false;
			}
		}

		return true;
	}

	public GdlTerm get(int index)
	{
		return body.get(index);
	}

	public GdlConstant getName()
	{
		return name;
	}
	
	public List<GdlTerm> getBody()
	{
		return body;
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = computeGround();
		}

		return ground;
	}

	@Override
	public GdlSentence toSentence()
	{
		return GdlPool.getRelation(name, body);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("( " + name + " ");
		for (GdlTerm term : body)
		{
			sb.append(term + " ");
		}
		sb.append(")");

		return sb.toString();
	}

	@Override
	public GdlFunction replace(Map<Gdl, Gdl> replacementMap) {
		Gdl newName = Gdl.applyReplacement(this.name, replacementMap);
		
		List<GdlTerm> newBody = new ArrayList<GdlTerm>();
		boolean hasChanged = newName != this.name;
		for (GdlTerm literal : this.body) {
			Gdl newLiteral = Gdl.applyReplacement(literal, replacementMap);
			if (newLiteral != literal) hasChanged = true;
			newBody.add((GdlTerm) newLiteral);
		}
		
		if (hasChanged) {
			return GdlPool.getFunction((GdlConstant) newName, newBody);
		}
		
		return this;
	}

	@Override
	public List<Gdl> getChildren() {
		List<Gdl> result = new ArrayList<Gdl>();
		
		result.add(this.name);
		result.addAll(this.body);
		
		return result;
	}

}
