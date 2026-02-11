package me.pajic.toolpouch.util;

import me.fzzyhmstrs.fzzy_config.annotations.Translation;
import me.fzzyhmstrs.fzzy_config.util.AllowableStrings;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class AllowedItem implements Walkable {

	@Translation(prefix = "toolpouch.config.allowedItem")
	public ValidatedString id;
	@Translation(prefix = "toolpouch.config.allowedItem")
	public ValidatedInt maxStackSize;
	@Translation(prefix = "toolpouch.config.allowedItem")
	public ValidatedInt maxStackCount;

	public AllowedItem(String id, int maxStackSize, int maxStackCount) {
		this.id = new ValidatedString(id);
		this.maxStackSize = new ValidatedInt(maxStackSize);
		this.maxStackCount = new ValidatedInt(maxStackCount);
	}

	public AllowedItem() {
		this.id = new ValidatedString("", new AllowableStrings(
				s -> true,
				() -> ToolPouchUtil.ITEM_SUGGESTIONS
		));
		this.maxStackSize = new ValidatedInt(1, 64, 0);
		this.maxStackCount = new ValidatedInt(1, 64, 0);
	}
}
