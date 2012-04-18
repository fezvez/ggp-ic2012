package util.gdl.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class GdlDistinct extends GdlLiteral
{

	private final GdlTerm arg1;
	private final GdlTerm arg2;
	private transient Boolean ground;

	GdlDistinct(GdlTerm arg1, GdlTerm arg2)
	{
		this.arg1 = arg1;
		this.arg2 = arg2;
		ground = null;
	}

	public GdlTerm getArg1()
	{
		return arg1;
	}

	public GdlTerm getArg2()
	{
		return arg2;
	}

	@Override
	public boolean isGround()
	{
		if (ground == null)
		{
			ground = arg1.isGround() && arg2.isGround();
		}

		return ground;
	}

	@Override
	public String toString()
	{
		return "( distinct " + arg1 + " " + arg2 + " )";
	}

	@Override
	public GdlDistinct replace(Map<Gdl, Gdl> replacementMap) {
		Gdl newArg1 = Gdl.applyReplacement(this.arg1, replacementMap);
		Gdl newArg2 = Gdl.applyReplacement(this.arg2, replacementMap);
		
		if (newArg1 != this.arg1 || newArg2 != this.arg2) {		
			return GdlPool.getDistinct((GdlTerm)newArg1, (GdlTerm)newArg2);
		}
		
		return this;
	}

	@Override
	public List<Gdl> getChildren() {
		List<Gdl> result = new ArrayList<Gdl>();
		result.add(this.arg1);
		result.add(this.arg2);
		return result;
	}

}
