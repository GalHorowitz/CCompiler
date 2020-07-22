package lasercompiler.parser.nodes;

import java.util.List;

import lasercompiler.parser.PrettyPrint;

public class StatementCompound extends Statement {
	
	private final List<BlockItem> items;
	
	public StatementCompound(List<BlockItem> items) {
		this.items = items;
	}
	
	public List<BlockItem> getItems() {
		return items;
	}
	
	@Override
	public String toString() {
		StringBuilder format = new StringBuilder("{\n");
		for(BlockItem item : items) {
			format.append(PrettyPrint.tabLines(item.toString()));
//			format.append("\n");
		}
		format.append("}");
		return format.toString();
	}

}
