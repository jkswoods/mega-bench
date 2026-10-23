package dev.megabench;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.*;
import java.util.*;
import java.util.function.Function;

/** Native container copies and native insertion simulate all outputs against shared physical slots. */
final class OutputSpace {
    private OutputSpace() {}
    static boolean fits(List<ItemStack> outputs,Function<ItemStack,ItemContainer> route){
        Map<ItemContainer,ItemContainer> copies=new IdentityHashMap<>();
        try{
            for(var output:outputs){
                if(ItemStack.isEmpty(output))continue;
                var tx=copy(route.apply(output),copies).addItemStack(output,true,true,true);
                if(!tx.succeeded()||!ItemStack.isEmpty(tx.getRemainder()))return false;
            }
            return true;
        }catch(UnsupportedOperationException ex){return false;}
    }
    private static ItemContainer copy(ItemContainer original,Map<ItemContainer,ItemContainer> copies){
        var found=copies.get(original);if(found!=null)return found;
        ItemContainer clone;
        if(original instanceof CombinedItemContainer combined){
            ItemContainer[] parts=new ItemContainer[combined.getContainersSize()];
            for(int i=0;i<parts.length;i++)parts[i]=copy(combined.getContainer(i),copies);
            clone=new CombinedItemContainer(parts);
        }else if(original.getClass()==SimpleItemContainer.class){
            var isolated=new SimpleItemContainer((SimpleItemContainer)original);
            for(short i=0;i<original.getCapacity();i++){
                var item=original.getItemStack(i);
                if(!ItemStack.isEmpty(item))isolated.setItemStackForSlot(i,ItemStack.CODEC.decode(ItemStack.CODEC.encode(item),com.hypixel.hytale.codec.EmptyExtraInfo.EMPTY));
            }
            clone=isolated;
        }else if(original==EmptyItemContainer.INSTANCE)clone=EmptyItemContainer.INSTANCE;
        else throw new UnsupportedOperationException("Unverified native inventory copy: "+original.getClass().getName());
        copies.put(original,clone);return clone;
    }
}
