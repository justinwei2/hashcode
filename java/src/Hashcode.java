import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Hashcode {
    final int numBooks;
    final int numLibraries;
    final int days;

    final int[] bookScores;
    final Library[] libraries;

    final HashMap<Integer, HashSet<Library>> bookToLibraries;

    Hashcode(String filename) {
        int[][] lines = parseFile(filename);

        numBooks = lines[0][0];
        numLibraries = lines[0][1];
        days = lines[0][2];

        bookScores = lines[1];
        libraries = new Library[numLibraries];

        bookToLibraries = new HashMap<>();

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
//                System.out.println(lines[i]);
//                System.out.println(Arrays.toString(lines[i].split(" ")));
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
        assert librariesWithBook != null && bookScores[bookId] > 0;
        bookScores[bookId] = 0;
        for (Library l : librariesWithBook) {
            l.updateLibraryHeuristics();
        }
        bookToLibraries.remove(bookId);
    }

    @Override
    public String toString() {
        return "Hashcode{" +
                "numBooks=" + numBooks +
                ", numLibraries=" + numLibraries +
                ", days=" + days +
                ", bookScores=" + Arrays.toString(bookScores) +
                ", libraries=" + Arrays.toString(libraries) +
                '}';
    }

    void solve() {

    }

    public static void main(String[] args) {
//        String[] files = {"a_example"};
        String[] files = {"a_example", "b_read_on", "c_incunabula", "d_tough_choices", "e_so_many_books", "f_libraries_of_the_world"};
        String dir = System.getProperty("user.dir").replaceAll("hashcode.*", "hashcode/");

        for (String file : files) {
            String filename = dir + file + ".txt";
            Hashcode hc = new Hashcode(filename);
            hc.solve();
//            System.out.println(hc.toString());
            System.out.println(file);
        }
    }

    /**
     * output in the format:
     * library id:
     */

    // copies of book can exist in multiple libs
    // copies might exists in a library


    // libraries can be signed up, one at a time, in any order
    class Library {
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
//            System.out.println(Arrays.toString(scoreAfterNDays));
//            System.out.println(Arrays.toString(percentOfLibraryMax));
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