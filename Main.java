import java.util.*;

public class Main {

    // --- Classes ---
    static class CampusMap {
        private Map<String, Building> buildings = new HashMap<>();

        private String normalize(String name) { return name.trim().toLowerCase(); }

        public void addBuilding(String name, int x, int y) {
            String key = normalize(name);
            if (buildings.containsKey(key)) { System.out.println("Building already exists."); return; }
            buildings.put(key, new Building(name, x, y));
            System.out.println("Building '" + name + "' added.");
        }

        public void removeBuilding(String name) {
            String key = normalize(name);
            Building toRemove = buildings.get(key);
            if (toRemove == null) { System.out.println("Building '" + name + "' not found."); return; }
            for (Building b : buildings.values()) b.getNeighbors().remove(toRemove);
            buildings.remove(key);
            System.out.println("Building '" + name + "' removed.");
        }

        public void connectBuildings(String from, String to, int distance) {
            String keyFrom = normalize(from), keyTo = normalize(to);
            if (buildings.containsKey(keyFrom) && buildings.containsKey(keyTo)) {
                buildings.get(keyFrom).addNeighbor(buildings.get(keyTo), distance);
                buildings.get(keyTo).addNeighbor(buildings.get(keyFrom), distance);
                System.out.println("Connection added between " + from + " and " + to + ".");
            } else System.out.println("Invalid building names.");
        }

        public void disconnectBuildings(String from, String to) {
            String keyFrom = normalize(from), keyTo = normalize(to);
            Building b1 = buildings.get(keyFrom), b2 = buildings.get(keyTo);
            if (b1 == null || b2 == null) { System.out.println("Invalid building names."); return; }
            b1.getNeighbors().remove(b2);
            b2.getNeighbors().remove(b1);
            System.out.println("Connection removed between " + from + " and " + to + ".");
        }

        public Building getBuilding(String name) { return buildings.get(normalize(name)); }

        public Set<String> getAllBuildings() {
            Set<String> names = new HashSet<>();
            for (Building b : buildings.values()) names.add(b.getName());
            return names;
        }

        public Collection<Building> getAllBuildingObjects() { return buildings.values(); }

        public void showAllConnections() {
            System.out.println("\nAll Campus Connections:");
            Set<String> printed = new HashSet<>();

            // ANSI color codes array
            String[] colors = {
                    "\u001B[31m", // RED
                    "\u001B[32m", // GREEN
                    "\u001B[33m", // YELLOW
                    "\u001B[34m", // BLUE
                    "\u001B[35m", // MAGENTA
                    "\u001B[36m"  // CYAN
            };
            String RESET = "\u001B[0m";
            Random rand = new Random();

            for (Building b : buildings.values()) {
                for (Map.Entry<Building, Integer> neighbor : b.getNeighbors().entrySet()) {
                    String key = b.getName() + "-" + neighbor.getKey().getName();
                    String reverseKey = neighbor.getKey().getName() + "-" + b.getName();

                    if (!printed.contains(key) && !printed.contains(reverseKey)) {
                        // Pick random colors for building and connection
                        String buildingColor = colors[rand.nextInt(colors.length)];
                        String connectionColor = colors[rand.nextInt(colors.length)];

                        System.out.println(buildingColor + b.getName() + RESET
                                + " " + connectionColor + "----" + neighbor.getValue() + "---->" + RESET + " "
                                + buildingColor + neighbor.getKey().getName() + RESET);

                        printed.add(key);
                    }
                }
            }
        }

    }

    static class Building {
        private String name; private int x, y; private Map<Building, Integer> neighbors = new HashMap<>();
        public Building(String name, int x, int y) { this.name = name; this.x = x; this.y = y; }
        public void addNeighbor(Building neighbor, int distance) { neighbors.put(neighbor, distance); }
        public String getName() { return name; }
        public Map<Building, Integer> getNeighbors() { return neighbors; }
        public int getX() { return x; } public int getY() { return y; }
    }

    static class RouteFinder {
        public static List<String> findShortestRoute(CampusMap map, String start, String end) {
            Map<Building, Integer> distances = new HashMap<>();
            Map<Building, Building> previous = new HashMap<>();
            PriorityQueue<Building> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

            Building startBuilding = map.getBuilding(start), endBuilding = map.getBuilding(end);
            if (startBuilding == null || endBuilding == null) throw new IllegalArgumentException("Invalid building names provided.");

            for (Building b : map.getAllBuildingObjects()) distances.put(b, Integer.MAX_VALUE);
            distances.put(startBuilding, 0); pq.add(startBuilding);

            while (!pq.isEmpty()) {
                Building current = pq.poll();
                for (Map.Entry<Building, Integer> neighbor : current.getNeighbors().entrySet()) {
                    int newDist = distances.get(current) + neighbor.getValue();
                    if (newDist < distances.get(neighbor.getKey())) {
                        distances.put(neighbor.getKey(), newDist);
                        previous.put(neighbor.getKey(), current);
                        pq.add(neighbor.getKey());
                    }
                }
            }

            List<String> path = new LinkedList<>();
            Building step = endBuilding;
            if (!previous.containsKey(step) && !step.equals(startBuilding)) return Collections.singletonList("No path found.");
            while (step != null) { path.add(0, step.getName()); step = previous.get(step); }
            return path;
        }
    }

    // --- Main ---
    private static Scanner scanner = new Scanner(System.in);
    private static final String RESET = "\u001B[0m", RED = "\u001B[31m", GREEN = "\u001B[32m", CYAN = "\u001B[36m";

    public static void main(String[] args) {
        CampusMap map = initializeSampleMap();
        System.out.println("=== Campus Navigation Assistant ===");
        System.out.println("Supporting SDG 9: Industry, Innovation & Infrastructure");

        while (true) {
            try {
                System.out.println("\nMenu:\n1. Find route\n2. Add building\n3. Remove building\n4. Connect buildings\n5. Disconnect buildings\n6. Show all buildings\n7. Show all connections\n8. Exit");
                System.out.print("Choose an option: ");
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1: findRoute(map); break;
                    case 2: addBuilding(map); break;
                    case 3: removeBuilding(map); break;
                    case 4: connectBuildings(map); break;
                    case 5: disconnectBuildings(map); break;
                    case 6: System.out.println("Buildings: " + map.getAllBuildings()); break;
                    case 7: map.showAllConnections(); break;
                    case 8: System.out.println("Goodbye!"); return;
                    default: System.out.println("Invalid option.");
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private static CampusMap initializeSampleMap() {
        CampusMap map = new CampusMap();
        map.addBuilding("Library", 0, 0);
        map.addBuilding("Cafeteria", 0, 1);
        map.addBuilding("Lab", 1, 0);
        map.addBuilding("Auditorium", 1, 1);
        map.addBuilding("Admin", 2, 0);
        map.connectBuildings("Library", "Cafeteria", 5);
        map.connectBuildings("Library", "Lab", 10);
        map.connectBuildings("Cafeteria", "Auditorium", 15);
        map.connectBuildings("Lab", "Auditorium", 20);
        map.connectBuildings("Admin", "Library", 25);
        return map;
    }

    private static void findRoute(CampusMap map) {
        System.out.println("Available buildings: " + map.getAllBuildings());
        System.out.print("Enter start building: "); String start = scanner.nextLine();
        System.out.print("Enter destination building: "); String end = scanner.nextLine();
        List<String> path = RouteFinder.findShortestRoute(map, start, end);
        if (path.size() == 1 && path.get(0).equals("No path found.")) System.out.println("No path found.");
        else displayMiniMap(map, path);
    }

    private static void addBuilding(CampusMap map) {
        System.out.print("Enter new building name: "); String name = scanner.nextLine();
        System.out.print("Enter X coordinate: "); int x = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Y coordinate: "); int y = Integer.parseInt(scanner.nextLine());
        map.addBuilding(name, x, y);
    }

    private static void removeBuilding(CampusMap map) {
        System.out.print("Enter building name to remove: "); map.removeBuilding(scanner.nextLine());
    }

    private static void connectBuildings(CampusMap map) {
        System.out.print("From building: "); String from = scanner.nextLine();
        System.out.print("To building: "); String to = scanner.nextLine();
        System.out.print("Distance: "); int dist = Integer.parseInt(scanner.nextLine());
        map.connectBuildings(from, to, dist);
    }

    private static void disconnectBuildings(CampusMap map) {
        System.out.print("From building: "); String from = scanner.nextLine();
        System.out.print("To building: "); String to = scanner.nextLine();
        map.disconnectBuildings(from, to);
    }

    private static void displayMiniMap(CampusMap map, List<String> path) {
        for (int i = 0; i < path.size(); i++) {
            String building = path.get(i);
            String color = (i == 0) ? GREEN : (i == path.size() - 1) ? RED : CYAN;
            System.out.println(color + building + RESET);
            if (i < path.size() - 1) {
                Building from = map.getBuilding(building);
                Building to = map.getBuilding(path.get(i + 1));
                int distance = from.getNeighbors().get(to);
                System.out.println("  │\n  " + distance + "\n  │");
            }
        }
    }
}
