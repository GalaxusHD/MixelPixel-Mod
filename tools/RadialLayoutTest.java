import net.mixelpixel.mod.client.screen.RadialLayout;
import java.util.*;

public class RadialLayoutTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        int assertions = 0;
        // Every toggle combination retains priority order, even when earlier actions are absent.
        for (int mask = 0; mask < 32; mask++) {
            List<Integer> enabled = new ArrayList<>();
            for (int action = 0; action < 5; action++) if ((mask & (1 << action)) != 0) enabled.add(action);
            int count = enabled.size();
            check(RadialLayout.segmentAt(0, 0, 58, 112, count) == -1, "Center"); assertions++;
            check(RadialLayout.segmentAt(0, -113, 58, 112, count) == -1, "Exterior"); assertions++;
            int[][] slots = {{}, {0}, {0, 1}, {0, 2, 1}, {0, 3, 1, 2}, {0, 4, 1, 3, 2}};
            for (int rank = 0; rank < count; rank++) {
                double angle = slots[count][rank] * Math.PI * 2 / count;
                int selected = RadialLayout.segmentAt(Math.sin(angle) * 85, -Math.cos(angle) * 85, 58, 112, count);
                check(selected == rank, "Priority position: " + mask + "/" + rank); assertions++;
                check(enabled.get(selected).equals(enabled.get(rank)), "Action mapping"); assertions++;
            }
            for (int degree = 0; degree < 360; degree++) {
                double angle = Math.toRadians(degree + .25);
                int hit = RadialLayout.segmentAt(Math.sin(angle) * 85, -Math.cos(angle) * 85, 58, 112, count);
                if (count == 0) check(hit == -1, "No actions");
                else if (count == 1) check((hit == 0) == (degree < 45 || degree >= 315), "V hit area");
                else check(hit >= 0 && hit < count, "No gaps between sectors");
                assertions++;
            }
        }
        check(RadialLayout.segmentAt(-80, 80, 58, 114, 4) == 1, "Kick on left boundary");
        System.out.println("Passed " + assertions + " checks across all 32 toggle combinations.");
    }
}
