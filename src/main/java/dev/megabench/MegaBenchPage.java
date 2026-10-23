package dev.megabench;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.packets.interface_.*;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** Left: source progression. Centre: recipes. Right: selected recipe and native craft. */
final class MegaBenchPage extends InteractiveCustomUIPage<MegaBenchPage.Action> {
    public static final class Action {
        String action="",version="",search="";
        static final BuilderCodec<Action> CODEC=BuilderCodec.builder(Action.class,Action::new)
            .append(new KeyedCodec<>("Action",Codec.STRING),(a,v)->a.action=v,a->a.action).add()
            .append(new KeyedCodec<>("Version",Codec.STRING),(a,v)->a.version=v,a->a.version).add()
            .append(new KeyedCodec<>("@Search",Codec.STRING),(a,v)->a.search=v,a->a.search).add().build();
    }
    record Entry(Catalogue.Source source,CraftingRecipe recipe){}
    static final int COLUMNS=8,PAGE_SIZE=32;
    static final String LIMIT_NOTICE="Private test build";
    final NativeBridge.Position at;final Catalogue catalogue;
    final Map<String,String> sourceRows=new HashMap<>();
    NativeBridge bridge;boolean dismissed;long version=1,nextLive;int offset,benchChoice,quantity=1;
    String source="*",category="*",filter="All",search="",benchMode="Unlock",lastLive="";
    String notice=LIMIT_NOTICE;
    List<Entry> visible=List.of();Entry selected;
    MegaBenchPage(PlayerRef p,NativeBridge.Position at,Catalogue c){super(p,CustomPageLifetime.CanDismiss,Action.CODEC);this.at=at;catalogue=c;}
    boolean current(Store<EntityStore>s,Ref<EntityStore>r){var p=s.getComponent(r,Player.getComponentType());return !dismissed&&p!=null&&p.getPageManager().getCustomPage()==this;}
    @Override public void build(Ref<EntityStore>r,UICommandBuilder c,UIEventBuilder e,Store<EntityStore>s){
        c.append("Pages/MegaBench/Main.ui");
        if(bridge==null){bridge=new NativeBridge(s,r,playerRef,at,()->current(s,r));bridge.uiRefresh=this::refreshLive;}
        render(r,s,c,e);
    }
    void bind(UIEventBuilder e,String selector,String action){e.addEventBinding(CustomUIEventBindingType.Activating,selector,EventData.of("Action",action).append("Version",Long.toString(version)),false);}
    void render(Ref<EntityStore>r,Store<EntityStore>s,UICommandBuilder c,UIEventBuilder e){
        var progress=MegaBenchPlugin.progress(s);int row=0;sourceRows.clear();
        c.clear("#Sources");c.clear("#Categories");c.clear("#Grid");
        c.append("#Sources","Pages/MegaBench/Source.ui");c.set("#Sources[0] #Name.Text","All workbenches");c.set("#Sources[0] #State.Text","Browse your unlocked recipes");c.set("#Sources[0] #Icon.ItemId","MB_MegaBench");c.set("#Sources[0] #Selected.Visible",source.equals("*"));bind(e,"#Sources[0]","source:*");row++;
        String lastGroup="";
        var sorted=catalogue.sources.values().stream().sorted(Comparator.comparing((Catalogue.Source x)->!x.supported()).thenComparingInt(x->x.group().equals("Hytale")?0:x.group().equals("Emberworks")?1:x.group().equals("Foundry Floor")?2:3).thenComparing(Catalogue.Source::id)).toList();
        for(var entry:sorted){
            String group=entry.supported()?entry.group():"Requires an adapter";
            if(!group.equals(lastGroup)){String heading="#Sources["+(row++)+"]";c.append("#Sources","Pages/MegaBench/Group.ui");c.set(heading+".Text",group);lastGroup=group;}
            String sel="#Sources["+(row++)+"]";c.append("#Sources","Pages/MegaBench/Source.ui");sourceRows.put(entry.id(),sel);
            c.set(sel+" #Name.Text",NativePresentation.text(entry.item(),playerRef.getLanguage()));c.set(sel+" #Icon.ItemId",entry.item());c.set(sel+" #Selected.Visible",entry.id().equals(source));bind(e,sel,"source:"+entry.id());
        }
        for(String f:List.of("Craftable","Unlocked","All")){bind(e,"#"+f,"filter:"+f);c.set("#"+f+".Style",Value.ref("Common.ui",f.equals(filter)?"SmallTertiaryTextButtonStyle":"SmallSecondaryTextButtonStyle"));}
        e.addEventBinding(CustomUIEventBindingType.Activating,"#SearchGo",EventData.of("Action","search").append("Version",Long.toString(version)).append("@Search","#Search.Value"),false);
        for(String action:List.of("Refresh","Close","Previous","Next","Act","CancelCraft","BenchPrevious","BenchNext","BenchPay","QuantityDown","QuantityUp"))bind(e,"#"+action,action);
        bind(e,"#BenchUnlock","benchMode:Unlock");bind(e,"#BenchUpgrade","benchMode:Upgrade");
        var src=source.equals("*")?null:catalogue.source(source);
        List<String> categories=new ArrayList<>();categories.add("*");
        if(src!=null&&src.bench()!=null&&src.bench().getCategories()!=null)for(var cat:src.bench().getCategories())categories.add(cat.getId());
        for(int i=0;i<categories.size();i++){
            String sel="#Categories["+i+"]",id=categories.get(i);c.append("#Categories","Pages/MegaBench/Category.ui");
            var anchor=new Anchor();anchor.setWidth(Value.of(Math.min(82,715/categories.size()-5)));anchor.setHeight(Value.of(62));anchor.setRight(Value.of(5));c.setObject(sel+".Anchor",anchor);
            String icon="Icons/ItemsGenerated/Bench_WorkBench.png";
            if(src!=null&&src.bench()!=null&&src.bench().getCategories()!=null)for(var cat:src.bench().getCategories())if(cat.getId().equals(id)&&cat.getIcon()!=null)icon=cat.getIcon();
            c.set(sel+" #CategoryIcon.Background",Value.ref("Pages/MegaBench/CategoryIcons.ui","Icon"+icon.replaceAll("[^A-Za-z0-9]","")));
            c.set(sel+".TooltipText",id.equals("*")?"All categories":NativePresentation.label(id).replaceFirst("^(EW |Foundry |Workbench |Furniture |Weapon )",""));
            c.set(sel+" #Selected.Visible",category.equals(id));bind(e,sel,"category:"+id);
        }
        c.set("#Context.Text","Recipes  ›  "+(src==null?"All workbenches":NativePresentation.text(src.item(),playerRef.getLanguage()))+(category.equals("*")?"":"  ›  "+NativePresentation.label(category).replaceFirst("^(EW |Foundry |Workbench |Furniture |Weapon )","")));
        List<Entry> entries=new ArrayList<>();
        for(var b:catalogue.sources.values()){
            if(!b.supported()||!source.equals("*")&&!source.equals(b.id()))continue;
            int tier=progress.tier(playerRef.getUuid(),b.id());if(source.equals("*")&&tier==0)continue;
            for(var recipe:b.recipes()){
                if(!search.isBlank()&&!(recipe.getId()+" "+Catalogue.output(recipe)+" "+NativePresentation.text(Catalogue.output(recipe),playerRef.getLanguage())+" "+b.id()).toLowerCase(Locale.ROOT).contains(search))continue;
                if(!category.equals("*")&&Arrays.stream(recipe.getBenchRequirement()).noneMatch(req->b.id().equals(req.id)&&req.categories!=null&&Arrays.asList(req.categories).contains(category)))continue;
                if(filter.equals("Unlocked")&&(tier==0||Catalogue.requirement(recipe,b.id(),tier)==null))continue;
                if(filter.equals("Craftable")){try{if(tier==0||Catalogue.requirement(recipe,b.id(),tier)==null)continue;NativeBridge.knowledge(s,r,recipe);if(!NativeBridge.inventory(s,r).canRemoveMaterials(CraftingManager.getInputMaterials(recipe,quantity)))continue;}catch(IllegalArgumentException ex){continue;}}
                entries.add(new Entry(b,recipe));
            }
        }
        entries.sort(Comparator.comparing((Entry x)->x.source().group()).thenComparing(x->x.source().id()).thenComparing(x->x.recipe().getId()));
        offset=Math.min(Math.max(0,offset),Math.max(0,(entries.size()-1)/PAGE_SIZE*PAGE_SIZE));visible=List.copyOf(entries.subList(offset,Math.min(offset+PAGE_SIZE,entries.size())));
        if(selected==null&&!visible.isEmpty())selected=visible.getFirst();
        for(int i=0;i<visible.size();i++){
            if(i%COLUMNS==0)c.append("#Grid","Pages/MegaBench/Row.ui");String parent="#Grid["+(i/COLUMNS)+"]",sel=parent+"["+(i%COLUMNS)+"]";var entry=visible.get(i);c.append(parent,"Pages/MegaBench/Tile.ui");var out=entry.recipe().getPrimaryOutput();
            if(out!=null&&out.getItemId()!=null)c.set(sel+" #Icon.ItemId",out.getItemId());
            c.set(sel+".TooltipText",NativePresentation.text(Catalogue.output(entry.recipe()),playerRef.getLanguage())+" · "+NativePresentation.label(entry.source().id())+"\n"+entry.recipe().getId());c.set(sel+" #Selected.Visible",selected!=null&&selected.equals(entry));c.set(sel+" #Amount.Text",out!=null&&out.getQuantity()>1?Integer.toString(out.getQuantity()):"");bind(e,sel,"entry:"+i);
        }
        c.set("#Count.Text",entries.isEmpty()?"No recipes":entries.size()+" recipes · "+(offset/PAGE_SIZE+1)+" / "+((entries.size()+PAGE_SIZE-1)/PAGE_SIZE));live(r,s,c);
    }
    void refreshLive(){
        if(bridge==null||!bridge.valid()||System.nanoTime()<nextLive)return;nextLive=System.nanoTime()+250_000_000L;var data=MegaBenchPlugin.progress(bridge.store).player(playerRef.getUuid());
        String signature=NativePresentation.inventorySignature(bridge.store,bridge.ref)+bridge.status+data.tiers+(data.pending==null?"":data.pending.bench+Math.ceil(data.pending.remaining));
        if(signature.equals(lastLive))return;lastLive=signature;var c=new UICommandBuilder();live(bridge.ref,bridge.store,c);sendUpdate(c,false);
    }
    void benchControls(Ref<EntityStore>r,Store<EntityStore>s,UICommandBuilder c){
        var progress=MegaBenchPlugin.progress(s);var inv=NativeBridge.inventory(s,r);var src=source.equals("*")?null:catalogue.source(source);c.set("#BenchControls.Visible",src!=null);if(src==null)return;
        int tier=progress.tier(playerRef.getUuid(),src.id());var pending=progress.player(playerRef.getUuid()).pending;
        c.set("#BenchTitle.Text",NativePresentation.text(src.item(),playerRef.getLanguage()));c.set("#BenchIcon.ItemId",src.item());c.set("#BenchState.Text",!src.supported()?"Adapter required":pending!=null&&pending.bench.equals(src.id())?(pending.tier>1?"Upgrading…":"Unlocking…"):tier==0?"Locked":"Tier "+tier+" · Unlocked");
        c.set("#BenchUnlock.Style",Value.ref("Common.ui",benchMode.equals("Unlock")?"SmallTertiaryTextButtonStyle":"SmallSecondaryTextButtonStyle"));c.set("#BenchUpgrade.Style",Value.ref("Common.ui",benchMode.equals("Upgrade")?"SmallTertiaryTextButtonStyle":"SmallSecondaryTextButtonStyle"));
        List<MaterialQuantity> costs=List.of();String availability="",action="Unlock workbench",time="";boolean enabled=false;var options=src.supported()?src.unlocks():List.<CraftingRecipe>of();benchChoice=Math.max(0,Math.min(benchChoice,options.size()-1));
        c.set("#BenchOptions.Visible",benchMode.equals("Unlock")&&tier==0&&options.size()>1);c.set("#BenchOption.Text","Source recipe "+(benchChoice+1)+" / "+options.size());
        if(!src.supported())availability="Use the source station";
        else if(benchMode.equals("Upgrade")){
            var upgrade=tier<1||tier>=PrivateBenchContext.CAPACITY?null:src.block().getBench().getUpgradeRequirement(tier);
            if(tier==0){availability="Unlock this workbench first";action="Unlock required";}
            else if(upgrade==null){availability="No higher native tier";action="Fully upgraded";}
            else{
                costs=Arrays.asList(upgrade.getInput());time=upgrade.getTimeSeconds()+"s upgrade";action="Upgrade to tier "+(tier+1);
                try{if(pending!=null)throw new IllegalArgumentException("Wait for the paid unlock or upgrade");if(!bridge.idle())throw new IllegalArgumentException("Finish the current craft first");if(!inv.canRemoveMaterials(costs))throw new IllegalArgumentException("Missing ingredients");enabled=true;availability="Pay ingredients to upgrade";}catch(IllegalArgumentException ex){availability=ex.getMessage();}
            }
        }else if(tier>0){availability="Ready to craft";action="Unlocked";}
        else if(options.isEmpty())availability="No supported unlock recipe";
        else{
            var recipe=options.get(benchChoice);costs=CraftingManager.getInputMaterials(recipe);time=recipe.getTimeSeconds()==0?"Instant unlock":recipe.getTimeSeconds()+"s unlock";
            try{if(pending!=null)throw new IllegalArgumentException("Unlocking…");if(!bridge.idle())throw new IllegalArgumentException("Finish the current craft first");unlockPrerequisites(r,s,recipe);if(!inv.canRemoveMaterials(costs))throw new IllegalArgumentException("Missing ingredients");enabled=true;availability="Pay ingredients to unlock";}catch(IllegalArgumentException ex){availability=ex.getMessage();}
        }
        NativePresentation.benchIngredients(c,inv,costs,playerRef.getLanguage());int rows=Math.min(2,(costs.size()+1)/2);var a=new Anchor();a.setHeight(Value.of(rows*68));c.setObject("#BenchIngredients.Anchor",a);
        var panel=new Anchor();panel.setHeight(Value.of(202+rows*68+(benchMode.equals("Unlock")&&tier==0&&options.size()>1?32:0)));panel.setTop(Value.of(10));c.setObject("#BenchControls.Anchor",panel);
        c.set("#BenchTime.Text",time);c.set("#BenchAvailability.Text",availability);c.set("#BenchPay.Text",action);c.set("#BenchPay.Disabled",!enabled);
    }
    void live(Ref<EntityStore>r,Store<EntityStore>s,UICommandBuilder c){
        NativePresentation.inventory(c,s,r);var progress=MegaBenchPlugin.progress(s);var inv=NativeBridge.inventory(s,r);
        for(var row:sourceRows.entrySet()){var bench=catalogue.source(row.getKey());var pending=progress.player(playerRef.getUuid()).pending;int paid=progress.tier(playerRef.getUuid(),bench.id());c.set(row.getValue()+" #State.Text",!bench.supported()?"Adapter required":pending!=null&&pending.bench.equals(bench.id())?(pending.tier>1?"Upgrading…":"Unlocking…"):paid==0?"Locked":"Unlocked · Tier "+paid);}
        benchControls(r,s,c);c.set("#Selection.Text",selected==null?"Recipe":NativePresentation.text(Catalogue.output(selected.recipe()),playerRef.getLanguage()));c.set("#OutputIcon.Visible",selected!=null);c.set("#OutputCount.Text","");c.set("#Time.Text","");
        List<MaterialQuantity> costs=List.of();String detail="Choose a workbench on the left, then select a recipe.",availability="";boolean enabled=false;
        if(selected!=null){
            var recipe=selected.recipe();int paid=progress.tier(playerRef.getUuid(),selected.source().id());float seconds=PrivateBenchContext.seconds(selected.source().block(),recipe,Math.max(1,paid));
            costs=CraftingManager.getInputMaterials(recipe,quantity);c.set("#OutputIcon.ItemId",Catalogue.output(recipe));int total=CraftingManager.getOutputItemStacks(recipe,quantity).stream().filter(x->x.getItemId().equals(Catalogue.output(recipe))).mapToInt(x->x.getQuantity()).sum();c.set("#OutputCount.Text","× "+total);c.set("#Time.Text",seconds+"s each · "+(seconds*quantity)+"s total");
            detail="Source: "+NativePresentation.text(selected.source().item(),playerRef.getLanguage())+"\n\nOutputs\n"+outputs(recipe)+"\n\nRequires\n"+requirements(recipe)+"\n\n"+(recipe.isKnowledgeRequired()?"Recipe must be learned.":"No recipe knowledge required.");
            try{if(paid<1||Catalogue.requirement(recipe,selected.source().id(),paid)==null)throw new IllegalArgumentException("Workbench or tier locked");NativeBridge.knowledge(s,r,recipe);var pending=progress.player(playerRef.getUuid()).pending;if(pending!=null&&pending.bench.equals(selected.source().id()))throw new IllegalArgumentException("Workbench upgrade in progress");if(!bridge.idle())throw new IllegalArgumentException("Crafting…");if(!inv.canRemoveMaterials(costs))throw new IllegalArgumentException("Missing ingredients");enabled=true;availability="Ready";}catch(IllegalArgumentException ex){availability=ex.getMessage();}
        }
        NativePresentation.ingredients(c,inv,costs,playerRef.getLanguage());c.set("#Detail.Text",detail);c.set("#CraftQuantity.Text","Quantity × "+quantity);c.set("#QuantityDown.Disabled",selected==null||!bridge.idle()||quantity<=1);c.set("#QuantityUp.Disabled",selected==null||!bridge.idle()||quantity>=NativeBridge.MAX_QUANTITY);c.set("#Act.Text","Craft ×"+quantity);c.set("#Act.Disabled",!enabled);c.set("#Availability.Text",availability);c.set("#CancelCraft.Visible",!bridge.idle());c.set("#Notice.Text",notice+"\n"+bridge.status);
    }
    String outputs(CraftingRecipe recipe){var text=new StringJoiner("\n");for(var item:CraftingManager.getOutputItemStacks(recipe,quantity))text.add(item.getQuantity()+" × "+NativePresentation.text(item.getItemId(),playerRef.getLanguage()));return text.toString();}
    static String requirements(CraftingRecipe recipe){var text=new StringJoiner("\nOR ");if(recipe.getBenchRequirement()!=null)for(var req:recipe.getBenchRequirement())text.add(NativePresentation.label(req.id)+" · tier "+Math.max(1,req.requiredTierLevel)+(req.requiredAugmentTags!=null&&req.requiredAugmentTags.length>0?" · augments "+String.join(", ",req.requiredAugmentTags):""));return text.toString();}
    void unlockPrerequisites(Ref<EntityStore>r,Store<EntityStore>s,CraftingRecipe recipe){
        NativeBridge.knowledge(s,r,recipe);boolean valid=false;
        if(recipe.getBenchRequirement()!=null)for(var req:recipe.getBenchRequirement()){
            if(req.type!=com.hypixel.hytale.protocol.BenchType.Crafting||(req.requiredAugmentTags!=null&&req.requiredAugmentTags.length>0))continue;
            if(req.id.equals("Fieldcraft")&&req.requiredTierLevel<=1)valid=true;
            var origin=catalogue.sources.get(req.id);if(origin!=null&&origin.supported()&&MegaBenchPlugin.progress(s).tier(playerRef.getUuid(),req.id)>=Math.max(1,req.requiredTierLevel))valid=true;
        }
        if(!valid)throw new IllegalArgumentException("Unlock the original prerequisite first");
    }
    void pay(Ref<EntityStore>r,Store<EntityStore>s){
        if(source.equals("*"))throw new IllegalArgumentException("Select a workbench on the left");
        var src=catalogue.source(source);var progress=MegaBenchPlugin.progress(s);int tier=progress.tier(playerRef.getUuid(),src.id());
        if(!src.supported())throw new IllegalArgumentException("Use the source workbench for this adapter");
        if(!bridge.idle())throw new IllegalArgumentException("Finish or cancel the current craft first");
        if(progress.player(playerRef.getUuid()).pending!=null)throw new IllegalArgumentException("Wait for the paid unlock or upgrade to finish");
        List<MaterialQuantity> inputs;float seconds;int target;String receipt;boolean upgrading=benchMode.equals("Upgrade");
        if(upgrading){
            if(tier<1)throw new IllegalArgumentException("Unlock this workbench first");
            var upgrade=tier>=PrivateBenchContext.CAPACITY?null:src.block().getBench().getUpgradeRequirement(tier);
            if(upgrade==null)throw new IllegalArgumentException("No higher native tier; no payment taken");
            inputs=Arrays.asList(upgrade.getInput());seconds=upgrade.getTimeSeconds();target=tier+1;receipt="NativeUpgrade:"+src.id()+":"+target;
        }else{
            if(!benchMode.equals("Unlock")||tier!=0)throw new IllegalArgumentException("Workbench already unlocked");
            var options=src.unlocks();if(benchChoice<0||benchChoice>=options.size())throw new IllegalArgumentException("Choose an unlock recipe");
            var recipe=options.get(benchChoice);unlockPrerequisites(r,s,recipe);inputs=CraftingManager.getInputMaterials(recipe);seconds=recipe.getTimeSeconds();target=1;receipt=recipe.getId();
        }
        var inv=NativeBridge.inventory(s,r);if(!inv.canRemoveMaterials(inputs))throw new IllegalArgumentException("Missing ingredients; nothing changed");
        if(!inv.removeMaterials(inputs,true,true,true).succeeded())throw new IllegalArgumentException("Native material transaction refused; nothing changed");
        progress.paid(playerRef.getUuid(),src.id(),receipt,target,seconds);MegaBenchPlugin.cue(s,r,"Unlock");
        String name=NativePresentation.text(src.item(),playerRef.getLanguage());notice=seconds==0?(upgrading?"Upgraded ":"Unlocked ")+name+(upgrading?" · Tier "+target:""):(upgrading?"Upgrade":"Unlock")+" payment accepted · "+seconds+"s";
        MegaBenchPlugin.instance.getLogger().atInfo().log("MB_PAID player=%s source=%s tier=%s recipe=%s time=%s kind=%s",playerRef.getUuid(),src.id(),target,receipt,seconds,upgrading?"Upgrade":"Unlock");
    }
    @Override public void handleDataEvent(Ref<EntityStore>r,Store<EntityStore>s,Action a){s.getExternalData().getWorld().execute(()->{
        if(!r.isValid()||!current(s,r)||bridge==null||!a.version.equals(Long.toString(version)))return;if(!bridge.valid()){close();return;}
        try{
            String value=a.action.contains(":")?a.action.substring(a.action.indexOf(':')+1):"";
            if(a.action.equals("Close")){close();return;}
            if(a.action.startsWith("source:")){source=value;category="*";selected=null;offset=0;benchChoice=0;benchMode=source.equals("*")||MegaBenchPlugin.progress(s).tier(playerRef.getUuid(),source)==0?"Unlock":"Upgrade";}
            else if(a.action.startsWith("filter:")){if(!List.of("Craftable","Unlocked","All").contains(value))return;filter=value;offset=0;selected=null;}
            else if(a.action.startsWith("category:")){category=value;offset=0;selected=null;}
            else if(a.action.startsWith("benchMode:")){if(!List.of("Unlock","Upgrade").contains(value))return;benchMode=value;}
            else if(a.action.startsWith("entry:")){int i=Integer.parseInt(value);if(i<0||i>=visible.size())return;selected=visible.get(i);}
            else if(a.action.equals("search")){search=a.search.strip().toLowerCase(Locale.ROOT);if(search.length()>100)search=search.substring(0,100);offset=0;selected=null;}
            else if(a.action.equals("Next")){offset+=PAGE_SIZE;selected=null;}
            else if(a.action.equals("Previous")){offset-=PAGE_SIZE;selected=null;}
            else if(a.action.equals("BenchNext"))benchChoice++;
            else if(a.action.equals("BenchPrevious"))benchChoice=Math.max(0,benchChoice-1);
            else if(a.action.equals("BenchPay"))pay(r,s);
            else if(a.action.equals("QuantityDown")||a.action.equals("QuantityUp")){
                if(!bridge.idle())throw new IllegalArgumentException("Finish or cancel the current craft first");
                quantity=Math.clamp(quantity+(a.action.equals("QuantityUp")?1:-1),1,NativeBridge.MAX_QUANTITY);
            }
            else if(a.action.equals("CancelCraft"))notice=bridge.cancelCraft()?LIMIT_NOTICE:"No active craft to cancel";
            else if(a.action.equals("Act")){if(selected==null)throw new IllegalArgumentException("Choose a recipe");bridge.craft(selected.source(),catalogue.recipe(selected.source(),selected.recipe().getId()),MegaBenchPlugin.progress(s).tier(playerRef.getUuid(),selected.source().id()),quantity);notice=LIMIT_NOTICE;}
        }catch(IllegalArgumentException|IllegalStateException ex){notice=ex.getMessage();}
        version++;var c=new UICommandBuilder();var e=new UIEventBuilder();render(r,s,c,e);sendUpdate(c,e,false);
    });}
    @Override public void onDismiss(Ref<EntityStore>r,Store<EntityStore>s){dismissed=true;if(bridge!=null)bridge.close();}
}
