package me.pajic.toolpouch.recipe;

import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {

	public static RecipeSerializer<AttachToolPouchRecipe> ATTACH_TOOL_POUCH = new CustomRecipe.Serializer<>(AttachToolPouchRecipe::new);

	public static RecipeSerializer<DetachToolPouchRecipe> DETACH_TOOL_POUCH = new CustomRecipe.Serializer<>(DetachToolPouchRecipe::new);
}
