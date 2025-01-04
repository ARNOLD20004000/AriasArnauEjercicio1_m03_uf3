import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        Juego juego = new Juego();
        Ranking ranking = new Ranking();

        String archivoPeliculas = "peliculas.txt";
        List<String> peliculas = juego.cargarPeliculas(archivoPeliculas);

        if (peliculas.isEmpty()) {
            System.out.println("El archivo de películas está vacío o no existe. Verifica su contenido.");
            return;
        }

        String titulo = juego.seleccionarPelicula(peliculas);
        juego.iniciarJuego(titulo, scanner);

        System.out.println("\nEl título era: " + titulo);
        System.out.println("Tu puntuación final: " + juego.getPuntuacion());

        if (juego.getPuntuacion() > 0) {
            System.out.println("\nIngresa tu nickname para el ranking:");
            String nickname;
            do {
                nickname = scanner.nextLine();
            } while (ranking.nicknameExiste(nickname));
            ranking.agregarPuntuacion(nickname, juego.getPuntuacion());
            ranking.mostrarRanking();
        } else {
            System.out.println("\nNo entraste en el ranking.");
        }
    }
}

class Juego {
    private int puntuacion = 0;
    private int intentos = 10;
    private final Set<Character> letrasAdivinadas = new HashSet<>();
    private final Set<Character> letrasErroneas = new HashSet<>();

    public List<String> cargarPeliculas(String archivo) {
        try {
            Path path = Paths.get(archivo);
            if (!Files.exists(path)) {
                System.out.println("El archivo " + archivo + " no existe.");
                return Collections.emptyList();
            }
            return Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String seleccionarPelicula(List<String> peliculas) {
        Random random = new Random();
        return peliculas.get(random.nextInt(peliculas.size())).toLowerCase();
    }

    public void iniciarJuego(String titulo, Scanner scanner) {
        char[] progreso = titulo.replaceAll("[a-zA-Z]", "*").toCharArray();

        while (intentos > 0 && !String.valueOf(progreso).equals(titulo)) {
            System.out.println("\nProgreso: " + String.valueOf(progreso));
            System.out.println("Intentos restantes: " + intentos);
            System.out.println("Letras incorrectas: " + letrasErroneas);
            System.out.println("[1] Adivinar una letra");
            System.out.println("[2] Adivinar el título");
            System.out.println("[3] Salir");

            String opcion = scanner.nextLine();
            switch (opcion) {
                case "1":
                    adivinarLetra(titulo, progreso, scanner);
                    break;
                case "2":
                    adivinarTitulo(titulo, scanner);
                    return;
                case "3":
                    System.out.println("Has salido del juego.");
                    intentos = 0;
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }

        if (String.valueOf(progreso).equals(titulo)) {
            System.out.println("\n¡Felicidades! Has adivinado el título.");
        } else {
            System.out.println("\nTe has quedado sin intentos.");
            puntuacion -= 10;
        }
    }

    private void adivinarLetra(String titulo, char[] progreso, Scanner scanner) {
        System.out.println("\nIntroduce una letra:");
        String input = scanner.nextLine().toLowerCase();

        if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
            System.out.println("Por favor, introduce una letra válida.");
            return;
        }

        char letra = input.charAt(0);
        if (letrasAdivinadas.contains(letra) || letrasErroneas.contains(letra)) {
            System.out.println("Ya has intentado esta letra.");
            return;
        }

        if (titulo.contains(String.valueOf(letra))) {
            boolean letraNueva = false;
            for (int i = 0; i < titulo.length(); i++) {
                if (titulo.charAt(i) == letra && progreso[i] != letra) {
                    progreso[i] = letra;
                    letraNueva = true;
                }
            }
            if (letraNueva) {
                letrasAdivinadas.add(letra);
                puntuacion += 10;
                System.out.println("¡Correcto!");
            } else {
                System.out.println("Esta letra ya se había revelado.");
            }
        } else {
            letrasErroneas.add(letra);
            intentos--;
            puntuacion -= 10;
            System.out.println("Incorrecto.");
        }
    }

    private void adivinarTitulo(String titulo, Scanner scanner) {
        System.out.println("\nIntroduce el título completo:");
        String input = scanner.nextLine().toLowerCase();

        if (input.equals(titulo)) {
            System.out.println("\n¡Correcto! Has adivinado el título.");
            puntuacion += 20;
        } else {
            System.out.println("\nIncorrecto. Has perdido.");
            puntuacion -= 20;
        }
        intentos = 0;
    }

    public int getPuntuacion() {
        return puntuacion;
    }
}

class Ranking {
    private static final String ARCHIVO_RANKING = "ranking.bin";
    private final List<Jugador> ranking = new ArrayList<>();

    public Ranking() throws IOException, ClassNotFoundException {
        cargarRanking();
    }

    public void cargarRanking() throws IOException, ClassNotFoundException {
        if (Files.exists(Paths.get(ARCHIVO_RANKING))) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO_RANKING))) {
                ranking.addAll((List<Jugador>) ois.readObject());
            }
        }
    }

    public void guardarRanking() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_RANKING))) {
            oos.writeObject(ranking);
        }
    }

    public boolean nicknameExiste(String nickname) {
        return ranking.stream().anyMatch(jugador -> jugador.nickname.equalsIgnoreCase(nickname));
    }

    public void agregarPuntuacion(String nickname, int puntuacion) throws IOException {
        ranking.add(new Jugador(nickname, puntuacion));
        ranking.sort((a, b) -> Integer.compare(b.puntuacion, a.puntuacion));
        if (ranking.size() > 5) {
            ranking.remove(ranking.size() - 1);
        }
        guardarRanking();
    }

    public void mostrarRanking() {
        System.out.println("\nRanking:");
        for (Jugador jugador : ranking) {
            System.out.println(jugador.nickname + ": " + jugador.puntuacion);
        }
    }
}

class Jugador implements Serializable {
    String nickname;
    int puntuacion;

    public Jugador(String nickname, int puntuacion) {
        this.nickname = nickname;
        this.puntuacion = puntuacion;
    }
}
