import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.*;

public class Hashcode {
    final int numBooks;
    final int numLibraries;
    final int days;

    final int[] bookScores;
    final Library[] libraries;

    final HashMap<Integer, HashSet<Library>> bookToLibraries;

    long score;
    String filename;

    Hashcode(String filename) {
        this.filename = filename;
        int[][] lines = parseFile(filename);

        numBooks = lines[0][0];
        numLibraries = lines[0][1];
        days = lines[0][2];

        bookScores = lines[1];
        libraries = new Library[numLibraries];

        bookToLibraries = new HashMap<>();

        score = 0;
        for (int i = 2; i < lines.length; i++) {
            // library case
            int libraryIndex = (i - 2) / 2;
            if (i % 2 == 0) {
                int[] attr = lines[i];
                libraries[libraryIndex] = new Library(attr[0], attr[1], attr[2], libraryIndex);
            } else {
                libraries[libraryIndex].setupBooks(lines[i]);
            }
        }
    }

    // parses the input file as a 2d int array
    static int[][] parseFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    lines.add(line);
                }
            }
            reader.close();

            int[][] nums = new int[lines.size()][];

            for (int i = 0; i < lines.size(); i++) {
                nums[i] = Arrays.stream(lines.get(i).split(" ")).mapToInt(Integer::parseInt).toArray();
            }
            return nums;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // mark a book as read and update all heuristics
    void removeBook(int bookId) {
        // see associated libraries
        HashSet<Library> librariesWithBook = bookToLibraries.get(bookId);

        // sanity check
        if (librariesWithBook == null) {
            System.out.println(bookId + " already removed");
        }
        bookScores[bookId] = 0;
        for (Library l : librariesWithBook) {
            l.updateLibraryHeuristics();
        }
        bookToLibraries.remove(bookId);
    }

    void greedySolver() {
        ArrayList<Integer> runningLibraries = new ArrayList<>();
        int[] startDayForLibrary = new int[libraries.length];
        for (int i = 0; i < numLibraries; i++) {
            startDayForLibrary[i] = 0;
        }


        Library[] pqLib = new Library[libraries.length];
        System.arraycopy(libraries, 0, pqLib, 0, libraries.length);
        Arrays.sort(pqLib);

        int libIndex = 0;
        int day = 0;
        while (day < days && libIndex < libraries.length) {
            // find new library to start
            int chosen = pqLib[libIndex++].libraryId;

            startDayForLibrary[chosen] = day;
            day += libraries[chosen].setupTime;
            // add chosen to running list
            runningLibraries.add(chosen);
        }
        validate(runningLibraries, startDayForLibrary);
        write(runningLibraries);
    }

    void write(ArrayList<Integer> rLs) {
        StringBuilder sb = new StringBuilder();

        int libs = 0;
        for (int l : rLs) {
            if (libraries[l].scanned != 0) {
                libs++;
                sb.append(l + " " + libraries[l].scanned + "\n");
                for (int b : libraries[l].scannedIds) {
                    sb.append(b + " ");
                }
                sb.append("\n");
            }
        }
        sb.insert(0, libs + "\n");

        // write the solution
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename.replaceAll(".txt", ".result")));
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    long validate(ArrayList<Integer> running, int[] startDaysForLibrary) {
        long sum = 0;
        for (int libId : running) {
            int maxDays = days - startDaysForLibrary[libId];
            sum += libraries[libId].count(maxDays);
        }
        score = sum;
        return sum;
    }

    public static void main(String[] args) {
        String[] files = {"a_example", "b_read_on", "c_incunabula", "d_tough_choices", "e_so_many_books", "f_libraries_of_the_world"};
        String dir = System.getProperty("user.dir").replaceAll("hashcode.*", "hashcode/");

        long s = 0;
        for (String file : files) {
            String filename = dir + file + ".txt";
            Hashcode hc = new Hashcode(filename);
            hc.greedySolver();
            s += hc.score;
        }
    }

    // libraries can be signed up, one at a time, in any order
    class Library implements Comparable<Library> {
        // given params
        final Book[] books;
        final int setupTime;
        final int scanRate;

        // extra info
        final int numBooks;
        final int libraryId;

        // heuristics
        int maxRunningTime;
        final int[] scoreAfterNDays;
        final double[] percentOfLibraryMax;

        int scanned = 0;
        ArrayList<Integer> scannedIds = new ArrayList<>();

        Library(int numBooks, int setupTime, int scanRate, int libraryId) {
            this.numBooks = numBooks;
            this.setupTime = setupTime;
            this.scanRate = scanRate;
            this.libraryId = libraryId;

            books = new Book[numBooks];
            maxRunningTime = numBooks + setupTime;
            scoreAfterNDays = new int[maxRunningTime];
            percentOfLibraryMax = new double[maxRunningTime];
        }

        // setup books and sort
        void setupBooks(int[] bookIds) {
            for (int i = 0; i < bookIds.length; i++) {
                int bookId = bookIds[i];
                books[i] = new Book(bookId);
                HashSet<Library> librariesWithBook = bookToLibraries.getOrDefault(bookId, new HashSet<>());
                librariesWithBook.add(this);
                bookToLibraries.put(bookId, librariesWithBook);
            }
            Arrays.sort(books);
            for (int i = setupTime; i < maxRunningTime; i++) {
                scoreAfterNDays[i] = scoreAfterNDays[i - 1] + books[i - setupTime].getScore();
            }
            double max = scoreAfterNDays[maxRunningTime - 1];
            for (int i = setupTime; i < maxRunningTime; i++) {
                percentOfLibraryMax[i] = scoreAfterNDays[i] / max;
            }
        }

        void updateLibraryHeuristics() {
            Arrays.sort(books);
            for (int i = setupTime; i < maxRunningTime; i++) {
                scoreAfterNDays[i] = scoreAfterNDays[i - 1] + books[i - setupTime].getScore();
            }
            double max = scoreAfterNDays[maxRunningTime - 1];
            for (int i = setupTime; i < maxRunningTime; i++) {
                percentOfLibraryMax[i] = scoreAfterNDays[i] / max;
            }
        }

        long count(int maxDays) {
            int bound = (maxDays - setupTime) * scanRate;

            long sum = 0;
            for (int i = 0; i < books.length && scanned < bound; i++) {
                int bid = books[i].bookId;
                if (bookScores[bid] != 0) {
                    sum += bookScores[bid];
                    bookScores[bid] = 0;
                    scanned++;
                    scannedIds.add(bid);
                }
            }
            return sum;
        }

        public int compareTo(Library l2) {
            // find and average of setupTime interval
            int n = 2;
            int i1 = Math.min(scoreAfterNDays.length - 1, setupTime * n * scanRate);
            int i2 = Math.min(l2.scoreAfterNDays.length - 1, l2.setupTime * n * l2.scanRate);
            double score1 = 1. * scoreAfterNDays[i1] / setupTime;
            double score2 = 1. * l2.scoreAfterNDays[i2] / l2.setupTime;
            return -Double.compare(score1, score2);
        }
    }

    // can be scanned in parallel
    class Book implements Comparable<Book> {
        final int bookId;

        public Book(int bookId) {
            this.bookId = bookId;
        }

        public int getScore() {
            return bookScores[bookId];
        }

        public int compareTo(Book b2) {
            return bookScores[b2.bookId] - bookScores[bookId];
        }
    }

}