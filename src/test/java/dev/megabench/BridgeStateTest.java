package dev.megabench;
import java.util.*;
import java.util.concurrent.*;
public final class BridgeStateTest {
 static int checks;
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static void refuses(Runnable r,String message){try{r.run();throw new AssertionError(message);}catch(IllegalArgumentException|IllegalStateException expected){checks++;}}
 public static void main(String[] args)throws Exception{
  var a=UUID.randomUUID();var b=UUID.randomUUID();
  var progress=new Progress();check(progress.tier(a,"Workbench")==0,"New player locked");
  progress.paid(a,"Workbench","native-source",1,2);check(progress.tier(a,"Workbench")==0,"Paid unlock waits source time");
  refuses(()->progress.paid(a,"Workbench","repeated",1,2),"Repeated unlock refused");
  progress.tick(1);var reloaded=progress.clone();check(reloaded.tier(a,"Workbench")==0,"Reload retains pending payment");reloaded.tick(1);
  check(reloaded.tier(a,"Workbench")==1,"Reload completes exactly paid unlock");check(reloaded.tier(b,"Workbench")==0,"Other player remains locked");
  reloaded.paid(a,"Workbench","tier1-upgrade",2,5);reloaded.tick(2);var third=new Progress();third.load(reloaded.save());third.tick(3);
  check(third.tier(a,"Workbench")==2,"Paid tier persists remaining time");check(third.tier(a,"EmberworksWorkbench")==0,"Other bench remains locked");
  refuses(()->third.paid(a,"Workbench","skip",4,0),"Tier skip refused");refuses(()->third.load("{\"not-a-uuid\":{\"tiers\":{}}}"),"Invalid player save rejected");
  check(new Progress().tier(a,"Workbench")==0,"World progress is separate");
  System.out.println("PASS "+checks+" progress checks; not native world/client evidence");
 }
}
