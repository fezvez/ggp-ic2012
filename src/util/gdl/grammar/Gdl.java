package util.gdl.grammar;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class Gdl implements Serializable
{

	public abstract boolean isGround();

	@Override
	public abstract String toString();
	
	public abstract Gdl replace(Map<Gdl, Gdl> replacementMap);
	
	public abstract List<Gdl> getChildren();
	
	protected static Gdl applyReplacement(Gdl gdl, Map<Gdl, Gdl> replacementMap) {
		if (replacementMap.containsKey(gdl)) {
			return replacementMap.get(gdl);
		} else {
			Gdl cloneCandidate = gdl.replace(replacementMap);
			if (cloneCandidate == gdl) return gdl;
			else return cloneCandidate;
		}
	}
	
}
