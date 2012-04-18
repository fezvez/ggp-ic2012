package util.gdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import util.gdl.grammar.Gdl;

public class GdlPreorderIterator {

	private List<Gdl> description;
	private Stack<List<Gdl>> toVisit;
	private List<Gdl> curList;
	
	public GdlPreorderIterator(Gdl gdl) {
		description = new ArrayList<Gdl>();
		description.add(gdl);
		this.rewind();
	}
	
	public GdlPreorderIterator(List<Gdl> description) {
		this.description = description;
		this.rewind();
	}

	public void rewind() {
		this.toVisit = new Stack<List<Gdl>>();
		
		for (int i = this.description.size() - 1; i >= 0; i--) {
			Gdl gdl = this.description.get(i);
			List<Gdl> list = new ArrayList<Gdl>();
			list.add(gdl);
			this.toVisit.push(list);
		}
	}
	
	public Gdl next() {
		Gdl result = null;
		
		while (result == null) {
			if (this.curList == null || this.curList.isEmpty()) {
				if (this.toVisit.isEmpty()) return null;
				this.curList = toVisit.pop();
			}
			
			while (result == null && this.curList != null && this.curList.size() > 0) {
				result = this.curList.remove(0);
			}
		}
		
		// Add the children of this Gdl to the list of GDL to visit if necessary
		List<Gdl> children = result.getChildren();
		this.toVisit.push(children);
		
		return result;
	}
	
	/**
	 * This function avoids expanding the current level of traversal.
	 */
	public void truncateLevel() {
		this.curList = null;
		if (this.toVisit != null && !this.toVisit.isEmpty()) {
			this.curList = toVisit.pop();
		}
	}
	
	public boolean hasNext() {
		return (this.curList != null && !this.curList.isEmpty()) || 
				(this.toVisit != null && this.toVisit.size() > 0);
	}
	
		
}
