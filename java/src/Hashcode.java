import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Hashcode {
    final int numBooks;
    final int numLibraries;
    final int days;
    int day;

    final int[] bookScores;
    final Library[] libraries;

    final HashMap<Integer, HashSet<Library>> bookToLibraries;

    int score;
    double scale;
    String filename;

    Hashcode(String filename, double interval) {
        this.filename = filename;
        this.scale = interval;

        int[][] lines = parseFile(filename);

        numBooks = lines[0][0];
        numLibraries = lines[0][1];
        days = lines[0][2];
        day = 0;

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
    void removeBooks(ArrayList<Integer> bookIds) {
        // see associated libraries
        HashSet<Library> libsToUpdate = new HashSet<>();

        for (int bookId : bookIds) {
            HashSet<Library> librariesWithBook = bookToLibraries.get(bookId);

            // sanity check
            if (librariesWithBook == null) {
                System.out.println(bookId + " already removed");
            }
            libsToUpdate.addAll(librariesWithBook);
            bookScores[bookId] = 0;
            bookToLibraries.remove(bookId);
        }

        for (Library l : libsToUpdate) {
            l.updateLibraryHeuristics();
        }
    }

    void greedySolver() {
        // list of running libraries (in order), and remaining ones
        ArrayList<Integer> runningLibraries = new ArrayList<>();
        HashSet<Integer> remainingLibraries = new HashSet<>();

        // day the library at index i was started
        int[] startDayForLibrary = new int[libraries.length];
        for (int i = 0; i < numLibraries; i++) {
            startDayForLibrary[i] = 0;
            remainingLibraries.add(i);
        }

        while (day < days && remainingLibraries.size() > 0) {
            // sort remaining libraries
            NPriorityQueue<Library> libs = new NPriorityQueue<>(1);
            for (int li : remainingLibraries) {
                libs.add(libraries[li]);
            }

            // find new library to start
            int chosen = libs.poll().libraryId;

            startDayForLibrary[chosen] = day;
            runningLibraries.add(chosen);
            remainingLibraries.remove(chosen);
            day += libraries[chosen].setupTime;

            score += libraries[chosen].selectLibrary(startDayForLibrary[chosen]);
        }
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

    public static void main(String[] args) {
        String[] files = {"a_example", "b_read_on", "c_incunabula", "d_tough_choices", "e_so_many_books", "f_libraries_of_the_world"};
        String dir = System.getProperty("user.dir").replaceAll("hashcode.*", "hashcode/");

        for (double i = .5; i < 1.5; i += .1) {
            int score = 0;
            for (String file : files) {
                String filename = dir + file + ".txt";
                Hashcode hc = new Hashcode(filename, i);
                hc.greedySolver();
                score += hc.score;
            }
            System.out.println(i + " score: " + score);
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

        int selectLibrary(int startDay) {
            int bound = (days - startDay - setupTime) * scanRate;

            int sum = 0;
            for (int i = 0; i < books.length && scanned < bound; i++) {
                int bid = books[i].bookId;
                if (bookScores[bid] != 0) {
                    sum += bookScores[bid];
                    bookScores[bid] = 0;
                    scanned++;
                    scannedIds.add(bid);
                }
            }
            removeBooks(scannedIds);
            return sum;
        }

        public int compareTo(Library l2) {
            // larger is better
            double score1 = 0;
            double score2 = 0;

            int len1 = scoreAfterNDays.length - 1;
            int len2 = l2.scoreAfterNDays.length - 1;
            double remaining = days - day;

            // find and average of setupTime interval
            int i1 = (int) (Math.min(len1, remaining * scale * scanRate));
            int i2 = (int) (Math.min(len2, remaining * scale * l2.scanRate));

            score1 = 1. * scoreAfterNDays[i1] / setupTime;
            score2 = 1. * l2.scoreAfterNDays[i2] / l2.setupTime;

            // max priority queue
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