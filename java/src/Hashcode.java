import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Hashcode {
    final int numBooks;
    final int numLibraries;
    final int days;

    final int[] bookScores;
    final Library[] libraries;

    Hashcode(String filename) {
        int[][] lines = parseFile(filename);

        numBooks = lines[0][0];
        numLibraries = lines[0][1];
        days = lines[0][2];

        bookScores = lines[1];
        libraries = new Library[numLibraries];

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
                books[i] = new Book(bookId, bookScores[bookId]);
            }
            Arrays.sort(books);
            for (int i = setupTime; i < maxRunningTime; i++) {
                scoreAfterNDays[i] = scoreAfterNDays[i - 1] + books[i - setupTime].bookScore;
            }
            double max = scoreAfterNDays[maxRunningTime - 1];
            for (int i = setupTime; i < maxRunningTime; i++) {
                percentOfLibraryMax[i] = scoreAfterNDays[i] / max;
            }
//            System.out.println(Arrays.toString(scoreAfterNDays));
//            System.out.println(Arrays.toString(percentOfLibraryMax));
        }
    }

    // can be scanned in parallel
    class Book implements Comparable<Book> {
        final int bookId;
        final int bookScore;

        public Book(int bookId, int bookScore) {
            this.bookId = bookId;
            this.bookScore = bookScore;
        }

        public int compareTo(Book b2) {
            return b2.bookScore - bookScore;
        }
    }

}