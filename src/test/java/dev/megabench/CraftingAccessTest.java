package dev.megabench;

import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import java.util.UUID;

public final class CraftingAccessTest {
    static int checks;
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
    static CraftingRecipe recipe(String source,int tier,String... augments){
        var requirement=new BenchRequirement();requirement.type=BenchType.Crafting;requirement.id=source;
        requirement.requiredTierLevel=tier;requirement.requiredAugmentTags=augments;
        return new CraftingRecipe(null,null,null,1,new BenchRequirement[]{requirement},2,false,0);
    }
    public static void main(String[] args){
        check(CraftingAccess.creative(GameMode.Creative),"Creative is recognized");
        check(!CraftingAccess.creative(GameMode.Adventure)&&!CraftingAccess.creative(null),"Missing or Survival mode cannot gain Creative access");
        var player=UUID.randomUUID();var other=UUID.randomUUID();var progress=new Progress();
        progress.paid(player,"Workbench","paid",1,0);String saved=progress.save();
        var upper=recipe("Workbench",3);
        check(!CraftingAccess.permits(false,true,upper,"Workbench",0),"Locked Survival bench refused");
        check(!CraftingAccess.permits(false,true,upper,"Workbench",1),"Unpaid Survival tier refused");
        check(CraftingAccess.permits(false,true,upper,"Workbench",3),"Paid Survival tier accepted");
        check(CraftingAccess.permits(true,true,upper,"Workbench",0),"Creative can use an entirely locked bench and tier");
        check(!CraftingAccess.permits(true,false,upper,"Workbench",0),"Creative respects server-disabled and special benches");
        check(!CraftingAccess.permits(true,true,upper,"OtherWorkshop",0),"Creative cannot submit another source's recipe");
        check(!CraftingAccess.permits(true,true,recipe("Workbench",1,"SpecialAttachment"),"Workbench",0),"Unsupported augments stay unsupported");
        check(!CraftingAccess.permits(true,true,recipe("Workbench",65),"Workbench",0),"Native capacity bound retained");
        check(saved.equals(progress.save())&&progress.tier(player,"Workbench")==1&&progress.tier(other,"Workbench")==0,"Creative access never writes paid progress or unlocks another player");
        check(!CraftingAccess.permits(CraftingAccess.creative(GameMode.Adventure),true,upper,"Workbench",progress.tier(player,"Workbench")),"Returning to Survival restores the paid tier immediately");
        System.out.println("PASS "+checks+" crafting access checks; native inventory behavior checked separately");
    }
}
