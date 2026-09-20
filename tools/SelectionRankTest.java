import net.mixelpixel.mod.client.target.TargetLease;
import java.util.UUID;
public class SelectionRankTest {
 static int checks;
 static void ok(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  UUID a=UUID.randomUUID(),b=UUID.randomUUID();
  for(int seconds:new int[]{3,30,120}){
   TargetLease t=new TargetLease();t.lookAt(a,0);
   ok(a.equals(t.current(seconds*1000000000L-1,seconds)),"before deadline");
   t.lookAt(a,seconds*1000000000L-1);
   ok(t.current(seconds*1000000000L,seconds)==null,"exact expiry");
   t.lookAt(a,seconds*1000000000L+1);ok(t.current(seconds*1000000000L+1,seconds)==null,"no automatic reselection");
   t.lookAt(null,seconds*1000000000L+2);t.lookAt(a,seconds*1000000000L+3);
   ok(a.equals(t.current(seconds*1000000000L+3,seconds)),"reselect after looking away");
   t.lookAt(b,seconds*1000000000L+4);ok(b.equals(t.current(seconds*1000000000L+4,seconds)),"switch target");
   t.clear();ok(t.current(seconds*1000000000L+5,seconds)==null,"clear on disconnect");
  }
  TargetLease t=new TargetLease();t.lookAt(a,0);ok(t.current(4000000000L,3)==null,"slider shortening immediately expires");
  ok(TargetLease.clampSeconds(-10)==3,"lower clamp");ok(TargetLease.clampSeconds(500)==120,"upper clamp");
  System.out.println(checks+" selection checks passed.");
 }
}
