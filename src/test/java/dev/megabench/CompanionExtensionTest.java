package dev.megabench;

import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.container.*;
import com.hypixel.hytale.server.core.ui.builder.*;

/** Optional registration and actual native source composition, without a running world. */
public final class CompanionExtensionTest {
    static int checks;
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
    static void refuses(Runnable action){try{action.run();throw new AssertionError("Expected registration refusal");}catch(IllegalStateException expected){checks++;}}
    public static void main(String[] args){
        check(CompanionExtension.API_VERSION==1,"Public API identity is 1");
        check(CompanionExtension.open(null)==null,"Absent companion needs no registration or dependency");
        var own=new SimpleItemContainer((short)2);var extra=new SimpleItemContainer((short)3);int[] calls={0};
        var session=new CompanionExtension.Session(){
            public ItemContainer inputs(CraftingRecipe recipe,int quantity){calls[0]++;check(quantity==4,"Requested batch quantity reaches the provider");return extra;}
            public void render(String view,long version,UICommandBuilder c,UIEventBuilder e){}
            public void action(String action,String search){}
        };
        CompanionExtension.Provider provider=context->session,other=context->null;
        refuses(()->CompanionExtension.register(2,provider));
        refuses(()->CompanionExtension.register(1,null));
        CompanionExtension.register(1,provider);
        check(CompanionExtension.open(null)==session,"Registered provider opens its session");
        refuses(()->CompanionExtension.register(1,other));
        CompanionExtension.unregister(other);
        check(CompanionExtension.open(null)==session,"Unrelated provider cannot unregister the owner");
        check(NativeBridge.inputs(own,null,false,null,4)==own,"No companion preserves exact native inventory");
        check(NativeBridge.inputs(own,session,true,null,4)==own&&calls[0]==0,"Creative never requests or combines companion ingredients");
        var combined=(CombinedItemContainer)NativeBridge.inputs(own,session,false,null,4);
        check(calls[0]==1&&combined.getContainersSize()==2&&combined.getCapacity()==5,"Survival composes actual containers once");
        check(combined.getContainer(0)==own&&combined.getContainer(1)==extra,"Player sources precede additive companion sources");
        CompanionExtension.unregister(provider);
        check(CompanionExtension.open(null)==null,"Matching unregister restores ordinary operation");
        CompanionExtension.register(1,other);
        check(CompanionExtension.open(null)==null,"Provider may decline to open a session");
        CompanionExtension.unregister(other);
        System.out.println("PASS "+checks+" companion API checks; native craft and client lifecycle remain separate gates");
    }
}
