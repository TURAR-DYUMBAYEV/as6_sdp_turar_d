public class Main {
    public static void main(String[] args) {
        try { Application.run(args); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); System.err.println("Application interrupted"); }
        catch (Exception ex) { System.err.println("Application failed: " + ex.getMessage()); }
    }
}
