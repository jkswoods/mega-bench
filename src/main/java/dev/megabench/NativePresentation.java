package dev.megabench;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.*;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** Read-only projections of live native inventory and original source recipe costs. */
final class NativePresentation {
    private NativePresentation(){}
    static String label(String id){return id.replaceFirst("^(EW_|FF_|Bench_|Ingredient_)","").replace("_Recipe_Generated_0","").replace('_',' ').replaceAll("([a-z])([A-Z])","$1 $2").replaceAll("([A-Z])([A-Z][a-z])","$1 $2").replace("Work Bench","Workbench").replace("Workbench Workbench","Workbench");}
    static Message itemName(String id){var item=Item.getAssetMap().getAsset(id);return item==null?Message.raw(label(id)):item.getTranslationMessage();}
    static String text(String id,String language){var item=Item.getAssetMap().getAsset(id);if(item==null)return label(id);var text=com.hypixel.hytale.server.core.modules.i18n.I18nModule.get().getMessage(language,item.getTranslationKey());return text==null?label(id):text;}
    static int owned(ItemContainer inv,MaterialQuantity material){int count=0;for(short i=0;i<inv.getCapacity();i++){var stack=inv.getItemStack(i);if(!ItemStack.isEmpty(stack)&&CraftingManager.matches(material,stack))count+=stack.getQuantity();}return count;}
    static void ingredients(UICommandBuilder c,ItemContainer inv,List<MaterialQuantity> costs,String language){
        c.clear("#Ingredients");for(int i=0;i<costs.size();i++){String sel="#Ingredients["+i+"]";c.append("#Ingredients","Pages/MegaBench/Ingredient.ui");ingredient(c,sel,"Ingredient",inv,costs.get(i),language);}
    }
    static void benchIngredients(UICommandBuilder c,ItemContainer inv,List<MaterialQuantity> costs,String language){
        c.clear("#BenchIngredients");for(int i=0;i<costs.size();i++){if(i%2==0)c.append("#BenchIngredients","Pages/MegaBench/BenchRow.ui");String parent="#BenchIngredients["+(i/2)+"]",sel=parent+"["+(i%2)+"]";c.append(parent,"Pages/MegaBench/BenchIngredient.ui");ingredient(c,sel,"BenchIngredient",inv,costs.get(i),language);}
    }
    static void ingredient(UICommandBuilder c,String sel,String template,ItemContainer inv,MaterialQuantity material,String language){
        String icon=material.getItemId();if(icon==null)for(var item:Item.getAssetMap().getAssetMap().values())if(CraftingManager.matches(material,new ItemStack(item.getId(),1))){icon=item.getId();break;}
        c.set(sel+" #Icon.Visible",icon!=null);if(icon!=null)c.set(sel+" #Icon.ItemId",icon);
        c.set(sel+" #Name.Text",material.getItemId()!=null?text(material.getItemId(),language):"Any "+label(Objects.toString(material.getResourceTypeId(),"matching item")));
        int have=owned(inv,material);c.set(sel+" #Quantity.Text",have+" / "+material.getQuantity());c.set(sel+" #Quantity.Style",com.hypixel.hytale.server.core.ui.Value.ref("Pages/MegaBench/"+template+".ui",have>=material.getQuantity()?"HaveStyle":"NeedStyle"));
    }
    static void inventory(UICommandBuilder c,Store<EntityStore>s,Ref<EntityStore>r){
        var storage=s.getComponent(r,InventoryComponent.Storage.getComponentType());var hotbar=s.getComponent(r,InventoryComponent.Hotbar.getComponentType());
        if(storage!=null)slots(c,"#PlayerStorage",storage.getInventory());if(hotbar!=null)slots(c,"#PlayerHotbar",hotbar.getInventory());
    }
    static void slots(UICommandBuilder c,String selector,ItemContainer inv){
        var slots=new ItemGridSlot[inv.getCapacity()];
        for(short i=0;i<slots.length;i++){
            var stack=inv.getItemStack(i);
            // Custom UI does not support arbitrary item metadata. The real inventory is never changed.
            slots[i]=new ItemGridSlot(ItemStack.isEmpty(stack)?null:new ItemStack(stack.getItemId(),stack.getQuantity()));
        }
        c.set(selector+".Slots",slots);
        // The installed client requires indices omitted by the server ItemGridSlot codec.
        var commands=c.getCommands();var command=commands[commands.length-1];
        var payload=com.google.gson.JsonParser.parseString(command.data).getAsJsonObject();var encoded=payload.getAsJsonArray("0");
        for(int i=0;i<encoded.size();i++)encoded.get(i).getAsJsonObject().addProperty("InventorySlotIndex",i);
        command.data=payload.toString();
    }
    static String inventorySignature(Store<EntityStore>s,Ref<EntityStore>r){
        var inv=NativeBridge.inventory(s,r);var signature=new StringBuilder();
        for(short i=0;i<inv.getCapacity();i++){var stack=inv.getItemStack(i);signature.append(i).append(':').append(stack).append(';');}
        return signature.toString();
    }
    static List<ItemStack> outputs(CraftingRecipe recipe){return CraftingManager.getOutputItemStacks(recipe);}
}
