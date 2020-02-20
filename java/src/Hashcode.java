import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class Hashcode {

    // parses the input file as a 2d int array
    static int[][] parseFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String[] lines = reader.lines().toArray(String[]::new);
            reader.close();

            int[][] nums = new int[lines.length][];
            for (int i = 0; i < lines.length; i++) {
                nums[i] = Arrays.stream(lines[i].split(" ")).mapToInt(Integer::parseInt).toArray();
            }
            return nums;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String[] files = {"a_example.txt"};
        String dir = System.getProperty("user.dir").replaceAll("hashcode.*", "hashcode/");

        for (String file : files) {
            String filename = dir + file;
            int[][] lines = parseFile(filename);
            for (int[] line : lines) {
                System.out.println(Arrays.toString(line));
            }
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
        Book[] books;
        int setupTime;
        int scanRate;

        // extra info
        int libraryId;

        // heuristics
        int[] scoreFromDay[];
        int timeToCompletion;
    }

    // can be scanned in parallel
    class Book {
        int bookId;
        int bookScore;
    }

}