package stockexchange.app;

import java.util.Scanner;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        try {
            int producers = 3;
            boolean interactive = false;
            for (String arg : args) {
                if (arg.equals("--interactive")) interactive = true;
                else producers = Integer.parseInt(arg);
            }
            if (producers < 3) throw new IllegalArgumentException("Use at least three producer threads");
            var app = Application.create();
            app.demo(producers, System.out);
            if (interactive) app.interactive(producers, new Scanner(System.in), System.out);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted.");
        } catch (Exception ex) {
            System.err.println("Cannot run simulation: " + ex.getMessage());
            System.exit(1);
        }
    }
}
